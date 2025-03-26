package com.megatronix.paridhi.controller;

import java.util.Map;
import java.util.TreeMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.megatronix.paridhi.dto.request.LoginRequest;
import com.megatronix.paridhi.dto.request.OtpRequest;
import com.megatronix.paridhi.dto.request.OtpVerificationRequest;
import com.megatronix.paridhi.dto.request.PasswordResetConfirmationRequest;
import com.megatronix.paridhi.dto.request.PasswordResetRequest;
import com.megatronix.paridhi.dto.request.RegisterRequest;
import com.megatronix.paridhi.dto.response.AuthResponse;
import com.megatronix.paridhi.model.User;
import com.megatronix.paridhi.repository.UserRepository;
import com.megatronix.paridhi.security.JwtService;
import com.megatronix.paridhi.service.OtpService;
import com.megatronix.paridhi.service.PasswordResetService;
import com.megatronix.paridhi.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class UserController {
	private final OtpService otpService;
	private final JwtService jwtService;
	private final UserService userService;
	private final UserRepository userRepository;
	private final UserDetailsService userDetailsService;
	private final PasswordResetService passwordResetService;

	@PostMapping("/register")
	public ResponseEntity< AuthResponse > register(
		@Valid @RequestBody RegisterRequest request
	) {
		return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(request));
	}

	@PostMapping("/login")
	public ResponseEntity< AuthResponse > login(
		@Valid @RequestBody LoginRequest request
	) {
		return ResponseEntity.ok(userService.login(request));
	}

	@PostMapping("/logout")
	public ResponseEntity<Map<String, String>> logout(
		@RequestHeader( name = "Authorization" ) String authHeader
	) {
		userService.logout(authHeader);
		Map<String, String> response = new TreeMap<>();
		response.put("message", "Logout successful");
		return ResponseEntity.ok(response);
	}

	@PostMapping("/password-reset")
	public ResponseEntity<Map<String, String>> requestPasswordReset(
		@Valid @RequestBody PasswordResetRequest request
	) {
		passwordResetService.requestPasswordReset(request);

		Map<String, String> response = new TreeMap<>();
		response.put("message", "Password reset instructions sent successfully to your email");
		return ResponseEntity.ok(response);
	}

	@PostMapping("/reset-confirm")
	public ResponseEntity<Map<String, String>> confirmPasswordReset(
		@Valid @RequestBody PasswordResetConfirmationRequest request
	) {
		passwordResetService.confirmPasswordReset(request);

		Map<String, String> response = new TreeMap<>();
		response.put("message", "Password reset successful");
		return ResponseEntity.ok(response);
	}

	@PostMapping("/validate-token")
	public ResponseEntity<Map<String, String>> validateToken(
		@RequestParam String token
	) {
		boolean isValid = passwordResetService.validateToken(token);

		Map<String, String> response = new TreeMap<>();
		response.put("message", isValid ? "Token is valid" : "Token is invalid");
		response.put("valid", String.valueOf(isValid));
		return ResponseEntity.ok(response);
	}

	@PostMapping("/send-otp")
	public ResponseEntity<Map<String, String>> sendOtp(
		@Valid @RequestBody OtpRequest request
	) {
		otpService.generateAndSendOtp(request);

		Map<String, String> response = new TreeMap<>();
		response.put("message", "OTP sent successfully");
		return ResponseEntity.ok(response);
	}

	@PostMapping("/verify-otp")
	public ResponseEntity<Map<String, String>> verifyOtp(
		@Valid @RequestBody OtpVerificationRequest request
	) {
		boolean success = otpService.verifyOtp(request);

		Map<String, String> response = new TreeMap<>();
		response.put("message", success ? "OTP verified successfully" : "Invalid OTP");
		response.put("success", String.valueOf(success));
		return ResponseEntity.ok(response);
	}

	@PostMapping("/resend-otp")
	public ResponseEntity<Map<String, String>> resendOtp(
		@Valid @RequestBody OtpRequest request
	) {
		otpService.resendOtp(request);

		Map<String, String> response = new TreeMap<>();
		response.put("message", "OTP resent successfully");
		return ResponseEntity.ok(response);
	}

	@GetMapping("/check-token")
	public ResponseEntity<Map<String, Object>> checkTokenValidity(
		@RequestHeader(name = "Authorization") String authHeader
	) {
		log.info("Checking token validity");
		Map<String, Object> response = new TreeMap<>();

		try {
			// check if header is present
			if (authHeader == null || !authHeader.startsWith("Bearer ")) {
				response.put("valid", false);
				response.put("message", "Invalid authorization header");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
			}

			String token = authHeader.substring(7 );

			// check if token is blacklisted
			if (jwtService.isTokenBlacklisted(token)) {
				response.put("valid", false);
				response.put("message", "Token has been revoked");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
			}

			// extract username and check if expired
			String username = jwtService.extractUsername(token);
			if (username == null || jwtService.isTokenExpired(token)) {
				response.put("valid", false);
				response.put("message", "Token is invalid or expired");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
			}

			// Load user by email/username
			UserDetails userDetails = userDetailsService.loadUserByUsername(username);
			if (userDetails == null) {
				response.put("valid", false);
				response.put("message", "User not found");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
			}

			// full token validation
			boolean isValid = jwtService.isTokenValid(token, userDetails);
			if (isValid) {
				User user = userRepository.findUserByEmail(username)
					.orElseThrow(() -> {
						log.error("User not found");
						return new UsernameNotFoundException("User not found");
					});
				
				response.put("valid", true);
				response.put("message", "Token is valid");
				response.put("user", AuthResponse.UserDto.fromUser(user));
				return ResponseEntity.ok(response);
			} else {
				response.put("valid", false);
				response.put("message", "Token is invalid");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
			}
		} catch (Exception e) {
			log.error("Error validating token: {}", e.getMessage());
			response.put("valid", false);
			response.put("message", "Error validating token");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
		}
	}
}
