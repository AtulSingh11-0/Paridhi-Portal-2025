package com.megatronix.paridhi.filter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.megatronix.paridhi.constant.MessageConstant;
import com.megatronix.paridhi.util.LoggingUtil;

import org.springframework.lang.NonNull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RateLimitingFilter.class);

	private final ConcurrentHashMap<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	
	private static final int RATE_LIMIT = 100; // 100 requests per minute per IP
	
	public RateLimitingFilter() {
		// Schedule cleanup of counts every minute
		scheduler.scheduleAtFixedRate(requestCounts::clear, 1, 1, TimeUnit.MINUTES);
	}

	@Override
	protected void doFilterInternal(
		@NonNull HttpServletRequest request, 
		@NonNull HttpServletResponse response, 
		@NonNull FilterChain filterChain
	) throws ServletException, IOException {      
		// Exempt health check endpoints
		if (request.getRequestURI().contains("/actuator/")) {
			filterChain.doFilter(request, response);
			return;
		}

		String clientIp = LoggingUtil.getClientIp(request);
		AtomicInteger count = requestCounts.computeIfAbsent(clientIp, k -> new AtomicInteger(0));
		int requestCount = count.incrementAndGet();
		
		if (requestCount > RATE_LIMIT) {
				log.warn("Rate limit exceeded for IP: {} - count: {}", clientIp, requestCount);
				response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value()); // Too Many Requests
				response.setContentType("application/json");
				response.getWriter().write("{\"status\":429,\"message\":\"" + 
						MessageConstant.UserMessage.RATE_LIMIT_EXCEEDED + 
						"\",\"timestamp\":\"" + System.currentTimeMillis() + "\"}");
				return;
		}
		
		filterChain.doFilter(request, response);
	}
}