package com.megatronix.paridhi.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.megatronix.paridhi.constant.Position;
import com.megatronix.paridhi.dto.request.TeamRequest;
import com.megatronix.paridhi.dto.response.TeamResponse;
import com.megatronix.paridhi.service.TeamService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/teams")
public class TeamController {
  private final TeamService teamService;

  @PreAuthorize("isAuthenticated()")
  @PostMapping("/register")
  public ResponseEntity<TeamResponse> registerTeam(
    @Valid @RequestBody TeamRequest request
  ) {
    return ResponseEntity.status(HttpStatus.CREATED).body(teamService.registerTeam(request));
  }

  @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
  @GetMapping("/events/{eventId}")
  public ResponseEntity<List<TeamResponse>> getTeamsByEventId(
    @PathVariable Long eventId
  ) {
    return ResponseEntity.ok(teamService.getTeamsByEvent(eventId));
  }

  @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or #email == authentication.name")
  @GetMapping("/users/{email}")
  public ResponseEntity<List<TeamResponse>> getTeamsByUserEmail(
    @PathVariable String email
  ) {
    return ResponseEntity.ok(teamService.getTeamsByUser(email));
  }

  @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or @teamSecurity.isTeamMember(#tid, authentication.name)")
  @GetMapping("/tid/{tid}")
  public ResponseEntity<TeamResponse> getTeamByTid(
    @PathVariable String tid
  ) {
    return ResponseEntity.ok(teamService.getTeamByTid(tid));
  }

  @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
  @GetMapping("/gid/{gid}")
  public ResponseEntity<List<TeamResponse>> getTeamsByGid(
    @PathVariable String gid
  ) {
    return ResponseEntity.ok(teamService.getTeamsByGid(gid));
  }

  @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
  @PatchMapping("/{tid}/payment")
  public ResponseEntity<TeamResponse> updatePaymentStatus(
    @PathVariable String tid
  ) {
    return ResponseEntity.ok(teamService.updatePaymentStatus(tid));
  }

  @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
  @PatchMapping("/{tid}/played")
  public ResponseEntity<TeamResponse> updatePlayedStatus(
    @PathVariable String tid
  ) {
    return ResponseEntity.ok(teamService.updatePlayedStatus(tid));
  }

	@PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
	@PatchMapping("{tid}/qualified")
	public ResponseEntity<TeamResponse> updateQualifiedStatus(
		@PathVariable String tid
	) {
		return ResponseEntity.ok(teamService.updateQualifiedStatus(tid));
	}

	@PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
	@PatchMapping("{tid}") 
	public ResponseEntity<TeamResponse> updatePosition(
		@PathVariable String tid,
		@RequestParam(name = "position") Position position
	) {
		return ResponseEntity.ok(teamService.updatePosition(tid, position));
	}
}
