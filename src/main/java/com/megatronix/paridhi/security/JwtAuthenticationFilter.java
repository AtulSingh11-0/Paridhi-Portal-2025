package com.megatronix.paridhi.security;

import java.io.IOException;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.megatronix.paridhi.service.UserDetailsServiceImpl;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final JwtService jwtService;
  private final UserDetailsServiceImpl userDetailsService;
  
  @Override
  protected void doFilterInternal(
    @NonNull HttpServletRequest request, 
    @NonNull HttpServletResponse response, 
    @NonNull FilterChain filterChain
  ) throws ServletException, IOException {
    final String authHeader = request.getHeader("Authorization");
    final String jwt;
    final String userEmail;

    // Check if the request has a valid authorization header, which starts with "Bearer "
    if ( authHeader == null || !authHeader.startsWith("Bearer ") ) {
      filterChain.doFilter(request, response);
      return;
    }

    // Extract the JWT token from the authorization header
    jwt = authHeader.substring(7);
    
    try {
      // Extract the user email from the JWT token
      userEmail = jwtService.extractUsername(jwt);

      // If the user email is not null and the user is not already authenticated, authenticate the user
      if ( userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null ) {
        // Load the user details from the user email
        var userDetails = userDetailsService.loadUserByUsername(userEmail);
        
        // If the token is valid, set the user authentication in the security context
        if (jwtService.isTokenValid(jwt, userDetails)) {
          // Create the authentication token, set the authentication details and set the authentication token in the security context
          UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.getAuthorities()
          );

          authToken.setDetails(
            new WebAuthenticationDetailsSource().buildDetails(request)
          );

          SecurityContextHolder.getContext().setAuthentication(authToken);
        }
      }
    } catch (Exception e) {
      log.error("Could not set user authentication in Seucrtiy context: " + e.getMessage());
    }
    // Continue with the filter chain
    filterChain.doFilter(request, response);
  }
}
