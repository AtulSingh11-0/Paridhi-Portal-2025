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

import com.megatronix.paridhi.dto.request.ContactQueryRequest;
import com.megatronix.paridhi.dto.request.ResolveQueryRequest;
import com.megatronix.paridhi.dto.response.ContactQueryResponse;
import com.megatronix.paridhi.model.User;
import com.megatronix.paridhi.service.ContactQueryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
@Slf4j
public class ContactQueryController {
	private final ContactQueryService contactQueryService;
    

	// Public Endpoints

	@PostMapping
	public ResponseEntity<ContactQueryResponse> createContactQuery(
		@Valid @RequestBody ContactQueryRequest request
	) {
		log.info("Received contact query request from: {}", request.getEmail());
		return ResponseEntity.status(HttpStatus.CREATED).body(contactQueryService.createContactQuery(request));
	}
    
	// Authorized Endpoints

	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
	@GetMapping
	public ResponseEntity<List<ContactQueryResponse>> getAllQueries(
		@RequestParam(required = false) Boolean isResolved,
		@AuthenticationPrincipal User user
	) {
		if (isResolved != null) {	
			return ResponseEntity.ok(contactQueryService.getQueriesByResolutionStatus(isResolved, user));
		}
		return ResponseEntity.ok(contactQueryService.getAllQueries(user));
	}
    
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
	@GetMapping("/{id}")
	public ResponseEntity<ContactQueryResponse> getQueryById(
		@PathVariable Long id,
		@AuthenticationPrincipal User user
	) {
		return ResponseEntity.ok(contactQueryService.getQueryById(id, user));
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
	@PutMapping("/{id}/resolve")
	public ResponseEntity<ContactQueryResponse> resolveQuery(
		@PathVariable Long id,
		@Valid @RequestBody ResolveQueryRequest request,
		@AuthenticationPrincipal User user
	) {
		return ResponseEntity.ok(contactQueryService.resolveQuery(id, request, user));
	}
}