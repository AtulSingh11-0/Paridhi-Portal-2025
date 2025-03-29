package com.megatronix.paridhi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.megatronix.paridhi.security.JwtAuthenticationEntryPoint;
import com.megatronix.paridhi.security.JwtAuthenticationFilter;
import com.megatronix.paridhi.service.UserDetailsServiceImpl;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
  private final JwtAuthenticationFilter jwtAuthFilter;
  private final UserDetailsServiceImpl userDetailsService;
  private final JwtAuthenticationEntryPoint jwtAuthEntryPoint;

	private static final String[] WHITELISTED_URLS = {
		"/", // Landing page
		"/api/auth/**", // Authentication endpoints
		"/api/mrd/register", // MRD registration endpoint
		"/api/events", // Event endpoints
		"/api/events/{id}", // Specific event endpoint
		"/api/events/status", // Event status endpoints
		"/api/events/type/**", // Event type endpoints
		"/api/events/domains/**", // Event domain endpoints
		"/api/combos", // Combo endpoints
		"/api/combos/{id}", // Specific combo endpoint
		"/api/combos/status", // Combo status endpoints
		"/api/combos/domains/**", // Combo domain endpoints
		"/api/galleries", // Gallery endpoints
		"/api/galleries/featured", // Featured gallery endpoints
		"/api/galleries/{id}", // Specific gallery endpoint
		"/api/megatronix-team/", // Megatronix team-member endpoints
	};

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
      .cors(Customizer.withDefaults())
      .csrf(AbstractHttpConfigurer::disable)
      .authorizeHttpRequests(auth -> auth
				.requestMatchers(WHITELISTED_URLS).permitAll()
        .anyRequest().authenticated()
      )
      .exceptionHandling(exception -> exception
        .authenticationEntryPoint(jwtAuthEntryPoint)
      )
      .sessionManagement(session -> session
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      )
      .authenticationProvider(authenticationProvider())
      .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
  }

  @Bean
  public BCryptPasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
