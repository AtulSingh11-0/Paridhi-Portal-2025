package com.megatronix.paridhi.util;

import org.slf4j.Logger;
import org.slf4j.MDC;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;

import com.megatronix.paridhi.constant.AppConstant;
import com.megatronix.paridhi.model.User;

public class LoggingUtil {

	private LoggingUtil() {}

	/**
	 * Sets up request context in MDC for consistent logging
	 */
	public static void setupRequestContext(HttpServletRequest request) {
		String requestId = UUID.randomUUID().toString();
		MDC.put(AppConstant.REQUEST_ID, requestId);
		MDC.put(AppConstant.CLIENT_IP, getClientIp(request));
		MDC.put("userAgent", request.getHeader("User-Agent"));
		MDC.put("path", request.getRequestURI());
		MDC.put("method", request.getMethod());
	}
	
	/**
	 * Clear MDC context
	 */
	public static void clearRequestContext() {
		MDC.clear();
	}
	
	/**
	 * Log an operation with full context
	 */
	public static void logOperation(Logger logger, String operation, String resourceType, User user, String details) {
		String userId = user == null ? AppConstant.ANONYMOUS : user.getId().toString();
		String userEmail = user == null ? AppConstant.ANONYMOUS : user.getEmail();
		
		logger.info(
			"Operation: {} | Resource: {} | User: {} ({}) | Details: {} | RequestID: {}",
			operation,
			resourceType,
			userEmail,
			userId,
			details,
			MDC.get(AppConstant.REQUEST_ID) != null ? MDC.get(AppConstant.REQUEST_ID) : AppConstant.ANONYMOUS
		);
	}
	
	/**
	 * Log an error with full context
	 */
	public static void logError(Logger logger, String operation, String resourceType, User user, String details, Throwable error) {
		String userId = user == null ? AppConstant.ANONYMOUS : user.getId().toString();
		String userEmail = user == null ? AppConstant.ANONYMOUS : user.getEmail();
		
		logger.error(
			"ERROR | Operation: {} | Resource: {} | User: {} ({}) | Details: {} | ClientIP: {} | RequestID: {}",
			operation,
			resourceType,
			userEmail,
			userId,
			details,
			MDC.get(AppConstant.CLIENT_IP) != null ? MDC.get(AppConstant.CLIENT_IP) : AppConstant.ANONYMOUS,
			MDC.get(AppConstant.REQUEST_ID) != null ? MDC.get(AppConstant.REQUEST_ID) : AppConstant.ANONYMOUS,
			error
		);
	}
	
	/**
	 * Log security events with full context
	 */
	public static void logSecurity(Logger logger, String operation, User user, String status, String details) {
		String userId = user == null ? AppConstant.ANONYMOUS : user.getId().toString();
		String userEmail = user == null ? AppConstant.ANONYMOUS : user.getEmail();
		
		logger.info(
			"SECURITY | Operation: {} | User: {} ({}) | Status: {} | Details: {} | IP: {} | RequestID: {}",
			operation,
			userEmail,
			userId,
			status,
			details,
			MDC.get(AppConstant.CLIENT_IP) != null ? MDC.get(AppConstant.CLIENT_IP) : AppConstant.ANONYMOUS,
			MDC.get(AppConstant.REQUEST_ID) != null ? MDC.get(AppConstant.REQUEST_ID) : AppConstant.ANONYMOUS
		);
	}
	
	/**
	 * Extract client IP address with proxy handling
	 */
	public static String getClientIp(HttpServletRequest request) {
		String ip = request.getHeader("X-Forwarded-For");
		if (ip == null || ip.isEmpty() || AppConstant.UNKNOWN.equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.isEmpty() || AppConstant.UNKNOWN.equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.isEmpty() || AppConstant.UNKNOWN.equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_CLIENT_IP");
		}
		if (ip == null || ip.isEmpty() || AppConstant.UNKNOWN.equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (ip == null || ip.isEmpty() || AppConstant.UNKNOWN.equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}
}