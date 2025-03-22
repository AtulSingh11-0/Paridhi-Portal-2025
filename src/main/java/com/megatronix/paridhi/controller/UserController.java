package com.megatronix.paridhi.controller;

import java.util.Map;
import java.util.TreeMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import com.megatronix.paridhi.service.OtpService;
import com.megatronix.paridhi.service.PasswordResetService;
import com.megatronix.paridhi.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class UserController {
	private final OtpService otpService;
	private final UserService userService;
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
}
