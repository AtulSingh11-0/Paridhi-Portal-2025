package com.megatronix.paridhi.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.megatronix.paridhi.dto.request.MRDRequest;
import com.megatronix.paridhi.dto.response.MRDResponse;
import com.megatronix.paridhi.model.User;
import com.megatronix.paridhi.service.MRDService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mrd")
public class MRDController {
  private final MRDService mrdService;

  // Public Endpoints

  @PostMapping("/register")
  public ResponseEntity<MRDResponse> registerMRD(
    @Valid @RequestBody MRDRequest request
  ) {
    return ResponseEntity.status(HttpStatus.CREATED).body(mrdService.registerMRD(request));
  }


  // Authorized endpoints

  @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPERADMIN')")
  @GetMapping("/user/{email}")
  public ResponseEntity<List<MRDResponse>> getUserMRDs(
    @PathVariable String email,
    @AuthenticationPrincipal User user
  ) {
    return ResponseEntity.ok(mrdService.getUserMRDs(email, user));
  }

  @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPERADMIN')")
  @GetMapping("/user/{email}/gids")
  public ResponseEntity<List<String>> getUserGids(
    @PathVariable String email,
    @AuthenticationPrincipal User user
  ) {
    return ResponseEntity.ok(mrdService.getUserGids(email, user));
  }
  
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
  @PatchMapping("{gid}/payment")
  public ResponseEntity<MRDResponse> updatePaymentStatus(
    @PathVariable String gid,
    @AuthenticationPrincipal User user
  ) {
    return ResponseEntity.ok(mrdService.updatePaymentStatus(gid, user));
  }
}
