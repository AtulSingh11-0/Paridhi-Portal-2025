package com.megatronix.paridhi.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.megatronix.paridhi.dto.request.ProfileRequest;
import com.megatronix.paridhi.dto.response.ProfileResponse;
import com.megatronix.paridhi.service.ProfileService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/profiles")
public class ProfileController {
	private final ProfileService profileService;

	@PostMapping
	public ResponseEntity< ProfileResponse > createProfile( 
		@Valid @RequestBody ProfileRequest request
	) {
		return ResponseEntity.status(HttpStatus.CREATED).body(profileService.createProfile(request));
	}

	@GetMapping("/{id}")
	public ResponseEntity< ProfileResponse > getProfileById(
		@PathVariable(name = "id") Long id
	) {
		return ResponseEntity.ok(profileService.getProfileById(id));
	}
}
