package com.megatronix.paridhi.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.megatronix.paridhi.dto.request.AdminCreationRequest;
import com.megatronix.paridhi.dto.response.AuthResponse;
import com.megatronix.paridhi.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {
  private final UserService userService;

  @PreAuthorize("hasRole('ROLE_SUPERADMIN')")
  @PostMapping
  public ResponseEntity<AuthResponse> createAdmin(
    @Valid @RequestBody AdminCreationRequest request
  ) {
    return ResponseEntity.status(HttpStatus.CREATED).body(userService.createAdmin(request));
  }

}
