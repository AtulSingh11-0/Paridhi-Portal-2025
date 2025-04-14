package com.megatronix.paridhi.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.megatronix.paridhi.util.LoggingUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

	private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

	@Override
	protected void doFilterInternal(
		@NonNull HttpServletRequest request,
		@NonNull HttpServletResponse response,
		@NonNull FilterChain filterChain
	) throws ServletException, IOException {

		long startTime = System.currentTimeMillis();
		String clientIp = LoggingUtil.getClientIp(request);

		try {
			// Set up MDC context for this request
			LoggingUtil.setupRequestContext(request);

			// Log the incoming request
			log.info("REQUEST | {} {} | IP: {} | UserAgent: {}",
				request.getMethod(),
				request.getRequestURI(),
				clientIp,
				request.getHeader("User-Agent") != null ? request.getHeader("User-Agent") : "N/A"
			);

			// Continue with the filter chain
			filterChain.doFilter(request, response);
		} finally {
			// Log the completed request
			long duration = System.currentTimeMillis() - startTime;
			log.info("RESPONSE | {} {} | Status: {} | Duration: {}ms | IP: {}",
				request.getMethod(),
				request.getRequestURI(),
				response.getStatus(),
				duration,
				clientIp
			);

			// Clear MDC context
			LoggingUtil.clearRequestContext();
		}
	}
}