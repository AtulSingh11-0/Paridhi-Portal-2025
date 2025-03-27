package com.megatronix.paridhi.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.megatronix.paridhi.constant.Domain;
import com.megatronix.paridhi.constant.EventType;
import com.megatronix.paridhi.dto.request.EventRequest;
import com.megatronix.paridhi.dto.response.EventResponse;
import com.megatronix.paridhi.model.User;
import com.megatronix.paridhi.service.EventService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {
  private final EventService eventService;

  // Public endpoints

  @GetMapping()
  public ResponseEntity<List<EventResponse>> getAllEvents() {
    return ResponseEntity.ok(eventService.getAllEvents());
  }
  
  @GetMapping("/domains/{domain}")
  public ResponseEntity<List<EventResponse>> getEventsByDomain(@PathVariable Domain domain) {
    return ResponseEntity.ok(eventService.getEventsByDomain(domain));
  }

  @GetMapping("/status")  
  public ResponseEntity<List<EventResponse>> getEventsByStatus(
    @RequestParam(name = "isOpen") boolean isOpen
    ) {
    return ResponseEntity.ok(eventService.getEventsByRegistration(isOpen));
  }

  @GetMapping("/type/{eventType}")
  public ResponseEntity<List<EventResponse>> getEventsByType(@PathVariable EventType eventType) {
    return ResponseEntity.ok(eventService.getEventsByType(eventType));
  }
  
  @GetMapping("/{id}")
  public ResponseEntity<EventResponse> getEventById(@PathVariable Long id) {
    return ResponseEntity.ok(eventService.getEventById(id));
  }

  // Authorized endpoints

  @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
  @PostMapping()
  public ResponseEntity<EventResponse> createEvent(
    @Valid @RequestBody EventRequest request,
    @AuthenticationPrincipal User user
  ) {
    return ResponseEntity.status(HttpStatus.CREATED).body(eventService.createEvent(request, user));
  }

  @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
  @PutMapping("/{id}")
  public ResponseEntity<EventResponse> updateEvent(
    @PathVariable Long id,
    @Valid @RequestBody EventRequest request,
    @AuthenticationPrincipal User user
  ) {
    return ResponseEntity.ok(eventService.updateEvent(id, request, user));
  }

	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
	@PutMapping(
		value = "/{id}/upload",
		consumes = MediaType.MULTIPART_FORM_DATA_VALUE
	)
	public ResponseEntity<EventResponse> updateEventImage(
		@PathVariable Long id,
		@RequestParam("file") MultipartFile file,
		@AuthenticationPrincipal User user
	) {
		return ResponseEntity.ok(eventService.updateEventImage(id, file, user));
	}

  @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteEvent(
    @PathVariable Long id,
    @AuthenticationPrincipal User user
  ) {
    eventService.deleteEvent(id, user);
    return ResponseEntity.noContent().build();
  }

  @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
  @PatchMapping("/{id}/status")
  public ResponseEntity<EventResponse> updateEventStatus(
    @PathVariable Long id,
    @AuthenticationPrincipal User user
  ) {
    return ResponseEntity.ok(eventService.toggleRegistrationStatus(id, user));
  }
}
