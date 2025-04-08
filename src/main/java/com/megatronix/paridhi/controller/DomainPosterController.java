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

import com.megatronix.paridhi.constant.Domain;
import com.megatronix.paridhi.dto.response.DomainPosterResponse;
import com.megatronix.paridhi.model.User;
import com.megatronix.paridhi.service.DomainPosterService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/domain-posters")
public class DomainPosterController {
	private final DomainPosterService domainPosterService;

	// Public Endpoints

	@GetMapping("/{domainName}")
	public ResponseEntity<DomainPosterResponse> getDomainPosterByName(
		@PathVariable Domain domainName
	) {
		return ResponseEntity.ok(domainPosterService.getDomainPoster(domainName));
	}

	@GetMapping
		public ResponseEntity<List<DomainPosterResponse>> getAllDomainPosters() {
		return ResponseEntity.ok(domainPosterService.getAllDomainPosters());
	}

	// Authorized Endpoints

	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
	@PostMapping(
		consumes = MediaType.MULTIPART_FORM_DATA_VALUE
	)
	public ResponseEntity<DomainPosterResponse> saveDomainPoster(
		@RequestParam("domainName") Domain domainName,
		@RequestParam("domainPoster") MultipartFile domainPoster,
		@AuthenticationPrincipal User user
	) {
		return ResponseEntity.status(HttpStatus.CREATED).body(domainPosterService.saveDomainPoster(domainName, domainPoster, user));
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
	@PutMapping(
		value = "/{id}",
		consumes = MediaType.MULTIPART_FORM_DATA_VALUE
	)
	public ResponseEntity<DomainPosterResponse> updateDomainPoster(
		@PathVariable Long id,
		@RequestParam("domainName") Domain domainName,
		@RequestParam("domainPoster") MultipartFile domainPoster,
		@AuthenticationPrincipal User user
	) {
		return ResponseEntity.ok(domainPosterService.updateDomainPoster(id, domainName, domainPoster, user));
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteDomainPoster(
		@PathVariable Long id,
		@AuthenticationPrincipal User user
	) {
		domainPosterService.deleteDomainPoster(id, user);
		return ResponseEntity.noContent().build();
	}

}
