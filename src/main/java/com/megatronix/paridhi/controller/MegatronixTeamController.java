package com.megatronix.paridhi.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.megatronix.paridhi.dto.request.MegatronixTeamRequest;
import com.megatronix.paridhi.dto.response.CategorizedMembersResponse;
import com.megatronix.paridhi.dto.response.MegatronixTeamResponse;
import com.megatronix.paridhi.model.User;
import com.megatronix.paridhi.service.MegatronixTeamService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/megatronix-team")
public class MegatronixTeamController {
	private final MegatronixTeamService megatronixTeamService;

	// Public endpoints

	@GetMapping
	public ResponseEntity<List<CategorizedMembersResponse>> getAllMemberProfilesCategorized() {
		return ResponseEntity.ok(megatronixTeamService.getAllMemberProfilesCategorized());
	}

	// Authorized endpoints

	@PreAuthorize("hasRole('SUPERADMIN')")
	@PostMapping
	public ResponseEntity<MegatronixTeamResponse> createMemberProfile(
		@RequestBody MegatronixTeamRequest request,
		@AuthenticationPrincipal User user
	) {
		return ResponseEntity.status(HttpStatus.CREATED).body(megatronixTeamService.createMemberProfile(request, user));
	}

	@PreAuthorize("hasRole('SUPERADMIN')")
	@PutMapping("/{id}")
	public ResponseEntity<MegatronixTeamResponse> updateMemberProfile(
		@PathVariable Long id,
		@RequestBody MegatronixTeamRequest request,
		@AuthenticationPrincipal User user
	) {
		return ResponseEntity.ok(megatronixTeamService.updateMemberProfile(id, request, user));
	}

	@PreAuthorize("hasRole('SUPERADMIN')")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteMemberProfile(
		@PathVariable Long id,
		@AuthenticationPrincipal User user
	) {
		megatronixTeamService.deleteMemberProfile(id, user);
		return ResponseEntity.noContent().build();
	}
}
