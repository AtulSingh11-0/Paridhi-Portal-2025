package com.megatronix.paridhi.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.megatronix.paridhi.constant.Category;
import com.megatronix.paridhi.dto.response.TeamPhotoResponse;
import com.megatronix.paridhi.model.User;
import com.megatronix.paridhi.service.TeamPhotoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/team-photo")
public class TeamPhotoController {
	private final TeamPhotoService teamPhotoService;

	// Public Endpoints

	@GetMapping
	public ResponseEntity<List<TeamPhotoResponse>> getTeamPhotosByCategory(
		@RequestParam(name = "category", defaultValue = "MEGATRONS") Category category
	) {
		return ResponseEntity.ok(teamPhotoService.getTeamPhotosByCategory(category));
	}

	// Authorized Endpoints

	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<TeamPhotoResponse> saveTeamPhoto(
		@RequestParam("category") Category category,
		@RequestParam("teamPhoto") MultipartFile teamPhotoImage,
		@AuthenticationPrincipal User user
	) {
		return ResponseEntity.status(HttpStatus.CREATED).body(teamPhotoService.saveTeamPhoto(category, teamPhotoImage, user));
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
	@PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<TeamPhotoResponse> updateTeamPhoto(
		@PathVariable Long id,
		@RequestParam(value = "category", required = false) Category category,
		@RequestParam("teamPhoto") MultipartFile teamPhotoImage,
		@AuthenticationPrincipal User user
	) {
		return ResponseEntity.ok(teamPhotoService.updateTeamPhoto(id, category, teamPhotoImage, user));
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
	@DeleteMapping(value = "/{id}")
	public ResponseEntity<Void> deleteTeamPhoto(
		@PathVariable Long id,
		@AuthenticationPrincipal User user
	) {
		teamPhotoService.deleteTeamPhoto(id, user);
		return ResponseEntity.noContent().build();
	}
}
