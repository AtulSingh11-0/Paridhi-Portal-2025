package com.megatronix.paridhi.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1) // Run right after logging filter
public class LoadTestingDetectionFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(LoadTestingDetectionFilter.class);
    
    private static final List<String> BLOCKED_USER_AGENTS = Arrays.asList(
        "jmeter", "loadrunner", "blazemeter", "gatling", "locust",
        "apachebench", "vegeta", "wrk", "siege", "apache-http",
        "python-loadtester", "loadtester", "stresstest", "stress-test",
        "load-test", "performance-test", "benchmark", "artillery"
    );
    
    // Regex to detect patterns like "X workers" where X is a large number
    private static final Pattern WORKER_PATTERN = Pattern.compile("\\(\\d{4,}\\s+workers\\)");
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String userAgent = httpRequest.getHeader("User-Agent");
        String clientIp = getClientIP(httpRequest);
        
        if (userAgent != null) {
            // Check for blocked user agents
            if (BLOCKED_USER_AGENTS.stream().anyMatch(agent -> 
                    userAgent.toLowerCase().contains(agent.toLowerCase()))) {
                log.warn("Blocked load testing tool: {} from IP: {}", userAgent, clientIp);
                blockRequest(httpResponse, "Load testing tools are not allowed");
                return;
            }
            
            // Check for suspicious worker count in user agent
            if (WORKER_PATTERN.matcher(userAgent).find()) {
                log.warn("Blocked high-worker load testing: {} from IP: {}", userAgent, clientIp);
                blockRequest(httpResponse, "High concurrency testing is not allowed");
                return;
            }
        }
        
        chain.doFilter(request, response);
    }
    
    private void blockRequest(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.getWriter().write(message);
    }
    
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // Get the first IP in X-Forwarded-For as it's the original client
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}