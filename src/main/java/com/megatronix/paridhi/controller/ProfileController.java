package com.megatronix.paridhi.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.megatronix.paridhi.dto.request.ProfileRequest;
import com.megatronix.paridhi.dto.response.ProfileResponse;
import com.megatronix.paridhi.model.User;
import com.megatronix.paridhi.service.ProfileService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/profiles")
public class ProfileController {
	private final ProfileService profileService;

	@PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPERADMIN')")
	@PostMapping
	public ResponseEntity< ProfileResponse > createProfile( 
		@Valid @RequestBody ProfileRequest request,
		@AuthenticationPrincipal User user
	) {
		return ResponseEntity.status(HttpStatus.CREATED).body(profileService.createProfile(request, user));
	}

	@PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPERADMIN')")
	@GetMapping("/{id}")
	public ResponseEntity< ProfileResponse > getProfileById(
		@PathVariable(name = "id") Long id,
		@AuthenticationPrincipal User user
	) {
		return ResponseEntity.ok(profileService.getProfileById(id, user));
	}

	@PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPERADMIN')")
	@PutMapping("/{id}")
	public ResponseEntity< ProfileResponse > updateProfile(
		@PathVariable Long id, 
		@Valid @RequestBody ProfileRequest request,
		@AuthenticationPrincipal User user
	) {
		return ResponseEntity.ok(profileService.updateProfile(id, request, user));
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
	@GetMapping
	public ResponseEntity< List< ProfileResponse > > getAllByIsProfileCreated(
		@RequestParam(name = "profileCreated", required = true) boolean isProfileCreated,
		@AuthenticationPrincipal User user
	) {
		return ResponseEntity.ok(profileService.getAllByIsProfileCreated(isProfileCreated, user));
	}
}
