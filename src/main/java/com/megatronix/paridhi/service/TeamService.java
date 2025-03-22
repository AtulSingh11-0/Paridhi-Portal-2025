package com.megatronix.paridhi.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.megatronix.paridhi.exception.EventNotFoundException;
import com.megatronix.paridhi.exception.TeamNotFoundException;
import com.megatronix.paridhi.exception.TeamRegistrationException;
import com.megatronix.paridhi.exception.UserNotFoundException;
import com.megatronix.paridhi.dto.request.TeamRequest;
import com.megatronix.paridhi.dto.response.TeamResponse;
import com.megatronix.paridhi.model.Team;
import com.megatronix.paridhi.repository.EventRepository;
import com.megatronix.paridhi.repository.MRDRepository;
import com.megatronix.paridhi.repository.TeamRepository;
import com.megatronix.paridhi.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamService {
  private final EmailService emailService;
  private final MRDRepository mrdRepository;
  private final TeamRepository teamRepository;
  private final UserRepository userRepository;
  private final EventRepository eventRepository;

  public TeamResponse registerTeam(TeamRequest request) {
    log.info("Team Registration request: {}", request);

    // find the event
    var event = eventRepository.findById(request.getEventId())
      .orElseThrow( () -> {
        log.error("Event not found with ID: {}", request.getEventId());
        return new EventNotFoundException("Event not found with ID: " + request.getEventId());
      });

    // check if registration for that event is open or not
    if ( !event.isRegistrationOpen() ) {
      log.error("Registration is closed for event: {}", event.getName());
      throw new TeamRegistrationException("Registration is closed for event: " + event.getName());
    }

    // find the team leader
    var teamLeader = userRepository.findUserByEmail(request.getTeamLeaderEmail())
      .orElseThrow( () -> {
        log.error("User not found with email: {}", request.getTeamLeaderEmail());
        return new UserNotFoundException("User not found with email: " + request.getTeamLeaderEmail());
      });

    // check if team name already exists for the event
    if ( teamRepository.existsByTeamNameAndEvent(request.getTeamName(), event) ) {
      log.error("Team name '{}' already exists for event: {}", request.getTeamName(), event.getName());
      throw new TeamRegistrationException(String.format("Team name '%s' already exists for event: %s", request.getTeamName(), event.getName()));
    }

    // validate team size based on the event
    int teamSize = request.getGidList().size();
    if ( teamSize < event.getMinPlayers() || teamSize > event.getMaxPlayers() ) {
      log.error("Invalid team size: {}. Required: min={}, max={} for event: {}", teamSize, event.getMinPlayers(), event.getMaxPlayers(), event.getName());
      throw new TeamRegistrationException(String.format("Team size '%d' must be between: %d and %d for event: %s", teamSize, event.getMinPlayers(), event.getMaxPlayers(), event.getName()));
    }

    // validate that all GIDs exist in MRD
    for ( String gid : request.getGidList() ) {
      if ( !mrdRepository.existsByGid(gid) ) {
        log.error("Invalid GID: {}", gid);
        throw new TeamRegistrationException("Invalid GID: " + gid);
      }
    }

    // check for duplicate GIDs in the request
    Set<String> uniqueGids = new HashSet<>(request.getGidList());
    if ( uniqueGids.size() != request.getGidList().size() ) {
      log.error("Duplicate GIDs found in the request");
      throw new TeamRegistrationException("Duplicate GIDs are not allowed");
    }

    // check if any of the GID is already registered for this event
    for ( String gid : request.getGidList() ) {
      // check if this GID i already in a team for this event
      boolean gidAlreadyRegistered = teamRepository.findByEvent(event)
        .stream()
        .flatMap(team -> team.getGidList().stream())
        .anyMatch(gid::equals);

        if (gidAlreadyRegistered) {
          log.error("GID '{}' is already registered for event: {}", gid, event.getName());
          throw new TeamRegistrationException(String.format("GID '%s' is already registered for event: %s", gid, event.getName())); 
        }
    }

    // create and save the team
    var team = Team.builder()
      .teamName(request.getTeamName())
      .event(event)
      .teamLeader(teamLeader)
      .gidList(request.getGidList())
      .contact(request.getContact())
      .isPaid(false)
      .hasPlayed(false)
      .build();

    var savedTeam = teamRepository.save(team);
    log.info("Team registered successfully: {}", savedTeam);

    // send email to the team leader
    emailService.sendEventRegistration(teamLeader.getEmail(), event.getName(), team.getTeamName(), team.getTid());

    // return the response
    return TeamResponse.fromTeam(savedTeam);
  }

  public List<TeamResponse> getTeamsByEvent(Long eventId) {
    log.info("Fetching teams for event with ID: {}", eventId);

    // find the event
    var event = eventRepository.findById(eventId)
      .orElseThrow( () -> {
        log.error("Event not found with ID: {}", eventId);
        return new EventNotFoundException("Event not found with ID: " + eventId);
      });

    // fetch all teams for the event
    var teams = teamRepository.findByEvent(event);

    // return a List of TeamResponse builder object
    return teams.stream()
      .map(TeamResponse::fromTeam)
      .toList();
  }

  public List<TeamResponse> getTeamsByUser(String email) {
    log.info("Fetching teams for user with email: {}", email);

    // find the user
    var user = userRepository.findUserByEmail(email)
      .orElseThrow( () -> {
        log.error("User not found with email: {}", email);
        return new UserNotFoundException("User not found with email: " + email);
      });

    // fetch all teams where the user is the team leader
    var teams = teamRepository.findByTeamLeader(user);
    
    // return a List of TeamResponse builder object
    return teams.stream()
      .map(TeamResponse::fromTeam)
      .toList();
  }

  public TeamResponse getTeamByTid(String tid) {
    log.info("Fetching team with TID: {}", tid);

    // find the team
    var team = teamRepository.findByTid(tid)
      .orElseThrow( () -> {
        log.error("Team not found with TID: {}", tid);
        return new TeamNotFoundException("Team not found with TID: " + tid);
      });

    // return a TeamResponse builder object
    return TeamResponse.fromTeam(team);
  }

  public TeamResponse updatePaymentStatus(String tid) {
    // find the team
    var team = teamRepository.findByTid(tid)
    .orElseThrow( () -> {
      log.error("Team not found with TID: {}", tid);  
      return new TeamNotFoundException("Team not found with TID: " + tid);
    });
    log.info("Updating payment status for team with TID {}: {}", tid, team.isPaid());
    
    // update and save the payment status
    team.setPaid(!team.isPaid());
    var updatedTeam = teamRepository.save(team);
    log.info("Updated payment status for team: {}", updatedTeam);
    
    // return the updated team
    return TeamResponse.fromTeam(updatedTeam);
  }

  public TeamResponse updatePlayedStatus(String tid) {
    // find the team
    var team = teamRepository.findByTid(tid)
    .orElseThrow( () -> {
      log.error("Team not found with TID: {}", tid);  
      return new TeamNotFoundException("Team not found with TID: " + tid);
    });
    log.info("Updating played status for team with TID {}: {}", tid, team.isHasPlayed());
    
    // update and save the played status
    team.setHasPlayed(!team.isHasPlayed());
    var updatedTeam = teamRepository.save(team);
    log.info("Updated played status for team: {}", updatedTeam);
    
    // return the updated team
    return TeamResponse.fromTeam(updatedTeam);
  }

  public List<TeamResponse> getTeamsByGid(String gid) {
    log.info("Fetching teams by GID: {}", gid);

    // first verify GID exists
    if ( !mrdRepository.existsByGid(gid) ) {
      log.error("GID not found: {}", gid);
      throw new TeamNotFoundException("GID not found: " + gid);
    }

    // fetch all teams where the GID is present
    var teams = teamRepository.findAll()
      .stream()
      .filter(team -> team.getGidList().contains(gid))
      .toList();

    // return a List of TeamResponse builder object
    return teams.stream()
      .map(TeamResponse::fromTeam)
      .toList();
  }
}
