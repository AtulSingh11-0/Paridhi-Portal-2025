package com.megatronix.paridhi.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
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

import com.megatronix.paridhi.constant.Domain;
import com.megatronix.paridhi.dto.request.ComboRequest;
import com.megatronix.paridhi.dto.request.ComboTeamRequest;
import com.megatronix.paridhi.dto.response.ComboResponse;
import com.megatronix.paridhi.dto.response.TeamResponse;
import com.megatronix.paridhi.model.User;
import com.megatronix.paridhi.service.ComboService;
import com.megatronix.paridhi.service.TeamService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/combos")
public class ComboController {
  private final ComboService comboService;
  private final TeamService teamService;

  // Public endpoints

  @GetMapping
  public ResponseEntity<List<ComboResponse>> getCombos() {
    return ResponseEntity.ok(comboService.getAllCombos());
  }

  @GetMapping("/{id}")
  public ResponseEntity<ComboResponse> getComboById(
    @PathVariable Long id
  ) {
    return ResponseEntity.ok(comboService.getComboById(id));
  }

  @GetMapping("/domains/{domain}")
  public ResponseEntity<List<ComboResponse>> getCombosByDomain(
    @PathVariable Domain domain
  ) {
    return ResponseEntity.ok(comboService.getCombosByDomain(domain));
  }

  @GetMapping("/status")
  public ResponseEntity<List<ComboResponse>> getCombosByStatus(
    @RequestParam(name = "isRegistrationOpen") boolean isRegistrationOpen
  ) {
    return ResponseEntity.ok(comboService.getCombosByStatus(isRegistrationOpen));
  }

  // Authorized endpoints

  @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN')")
  @PostMapping
  public ResponseEntity<ComboResponse> createCombo(
    @Valid @RequestBody ComboRequest request,
    @AuthenticationPrincipal User user
  ) {
    return ResponseEntity.status(HttpStatus.CREATED)
      .body(comboService.createCombo(request, user));
  }

  @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN')")
  @PutMapping("/{id}")
  public ResponseEntity<ComboResponse> updateCombo(
    @PathVariable Long id,
    @Valid @RequestBody ComboRequest request,
    @AuthenticationPrincipal User user
  ) {
    return ResponseEntity.ok(comboService.updateCombo(id, request, user));
  }

  @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN')")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteCombo(
    @PathVariable Long id,
    @AuthenticationPrincipal User user
  ) {
    comboService.deleteCombo(id, user);
    return ResponseEntity.noContent().build();
  }

  @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN')")
  @PatchMapping("/{id}/status")
  public ResponseEntity<ComboResponse> toggleComboStatus(
    @PathVariable Long id,
    @AuthenticationPrincipal User user
  ) {
    return ResponseEntity.ok(comboService.toggleComboStatus(id, user));
  }

  // Registration endpoint for combos
  @PostMapping("/register")
  public ResponseEntity<List<TeamResponse>> registerForCombo(
    @Valid @RequestBody ComboTeamRequest request
  ) {
    return ResponseEntity.status(HttpStatus.CREATED)
      .body(teamService.registerTeamForCombo(request));
  }
}
