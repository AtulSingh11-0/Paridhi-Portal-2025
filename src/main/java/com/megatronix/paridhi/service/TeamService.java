package com.megatronix.paridhi.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.megatronix.paridhi.constant.Position;
import com.megatronix.paridhi.dto.request.ComboTeamRequest;
import com.megatronix.paridhi.dto.request.TeamRequest;
import com.megatronix.paridhi.dto.response.TeamResponse;
import com.megatronix.paridhi.exception.ComboNotFoundException;
import com.megatronix.paridhi.exception.EventNotFoundException;
import com.megatronix.paridhi.exception.TeamNotFoundException;
import com.megatronix.paridhi.exception.TeamRegistrationException;
import com.megatronix.paridhi.exception.UserNotFoundException;
import com.megatronix.paridhi.model.Event;
import com.megatronix.paridhi.model.Team;
import com.megatronix.paridhi.repository.ComboRepository;
import com.megatronix.paridhi.repository.EventRepository;
import com.megatronix.paridhi.repository.MRDRepository;
import com.megatronix.paridhi.repository.TeamRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamService {
  private final EmailService emailService;
  private final MRDRepository mrdRepository;
  private final TeamRepository teamRepository;
  private final ComboRepository comboRepository;
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

    validateGids(request.getGidList());

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
			.contacts(
				request.getContacts().stream()
					.map(contactDTO -> new Team.Contact(contactDTO.getName(), contactDTO.getContact()))
					.toList()
			)
			.gidList(request.getGidList())
      .isPaid(false)
      .hasPlayed(false)
			.isQualified(false)
			.position(Position.NONE)
      .build();

    var savedTeam = teamRepository.save(team);
    log.info("Team registered successfully: {}", savedTeam);

		// fetch emails of all the team members registered with GID
		List<String> teamMemberEmails = mrdRepository.findUserEmailListByGidList(savedTeam.getGidList());

    // send email to all the team members
    emailService.sendEventRegistration(teamMemberEmails.toArray(new String[0]), event.getName(), team.getTeamName(), team.getTid());

    // return the response
    return TeamResponse.fromTeam(savedTeam);
  }

  @Transactional
  public List<TeamResponse> registerTeamForCombo(ComboTeamRequest request) {
    log.info("Team registration for combo request: {}", request);

    // find the combo
    var combo = comboRepository.findById(request.getComboId())
      .orElseThrow( () -> {
        log.error("Combo not found with ID: {}", request.getComboId());
        return new ComboNotFoundException("Combo not found with ID: " + request.getComboId());
      });
    
    // check if registration for the combo is open or not
    if (!combo.isRegistrationOpen()) {
      log.error("Registration is closed for combo: {}", combo.getName());
      throw new TeamRegistrationException("Registration is closed for combo: " + combo.getName());
    }

    // validate that we have GID mappings for all events in the combo
    Set<Long> comboEventIds = combo.getEvents().stream()
      .map(Event::getId)
      .collect(Collectors.toSet());
    
    if ( !request.getEventGidMap().keySet().containsAll(comboEventIds) ) {
      log.error("Missing GID mappings for some events in the combo: {}", combo.getName());
      throw new TeamRegistrationException("You must provide GID list for all events in the combo: " + combo.getName());
    }

		// convert ContactDTOs to Contact entities
		List<Team.Contact> contactEntities = request.getContacts().stream()
			.map(contactDTO -> new Team.Contact(contactDTO.getName(), contactDTO.getContact()))
			.toList();

    // create a list to hold all the team responses
    List<TeamResponse> teamResponses = new ArrayList<>();

    // for each event in the combo, create a team
    for (Event event : combo.getEvents()) {
      Long eventId = event.getId();
      List<String> eventGids = request.getEventGidMap().get(eventId);

      // check if registration is open for the event
      if (!event.isRegistrationOpen()) {
        log.error("Registration is closed for event: {}", event.getName());
        throw new TeamRegistrationException("Registration is closed for event: " + event.getName());
      }

      // validate team size based on the event
      int teamSize = eventGids.size();
      if (teamSize < event.getMinPlayers() || teamSize > event.getMaxPlayers()) {
        log.error("Invalid team size: {}. Required: min={}, max={} for event: {}", teamSize, event.getMinPlayers(), event.getMaxPlayers(), event.getName());
        throw new TeamRegistrationException(String.format("Team size '%d' must be between: %d and %d for event: %s", teamSize, event.getMinPlayers(), event.getMaxPlayers(), event.getName()));
      }

      // validate all GIDs for this event
      validateGids(eventGids);

      // check if any of the GID is already registered for this event
      for (String gid : eventGids) {
        // check if this GID is already in a team for this event
        boolean gidAlreadyRegistered = teamRepository.findByEvent(event)
          .stream()
          .flatMap(team -> team.getGidList().stream())
          .anyMatch(gid::equals);

        if (gidAlreadyRegistered) {
          log.error("GID '{}' is already registered for event: {}", gid, event.getName());
          throw new TeamRegistrationException(String.format("GID '%s' is already registered for event: %s", gid, event.getName()));
        }
      }
      // create and save the teams
      var team = Team.builder()
        .teamName(request.getTeamName())
        .event(event)
        .contacts(contactEntities)
        .gidList(eventGids)
        .isPaid(false)
        .hasPlayed(false)
				.isQualified(false)
				.position(Position.NONE)
        .build();
      
      var savedTeam = teamRepository.save(team);
      log.info("Team registered for event {} as part of combo: {}", event.getName(), savedTeam);
  
      // add to response list
      teamResponses.add(TeamResponse.fromTeam(savedTeam));

			// fetch emails of all the team members registered with GID
			List<String> teamMemberEmails = mrdRepository.findUserEmailListByGidList(savedTeam.getGidList());

			// send email to all the team members
			emailService.sendEventRegistration(teamMemberEmails.toArray(new String[0]), event.getName(), team.getTeamName(), team.getTid());
    }

    if (teamResponses.isEmpty()) {
      log.error("No teams registered for combo: {}", combo.getName());
      throw new TeamRegistrationException("No teams registered for combo: " + combo.getName());
    }

    return teamResponses;
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

    // get the user's GIDs from MRD
		var userGids = mrdRepository.findGidListByUserEmail(email);

		if (userGids.isEmpty()) {
			log.error("No GIDs found for user with email: {}", email);
			throw new UserNotFoundException("No GIDs found for user with email: " + email);
		}

		// fetch all teams where any of the user's GIDs are present
		var teams = teamRepository.findByGidListContaining(userGids);
		log.info("Found {} teams for user with email: {}", teams.size(), email);

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
    
		// check if team has paid
		if (!team.isPaid()) {
			log.error("Team must have paid to update played status");
			throw new IllegalArgumentException("Team must have paid to update played status");
		}

    // update and save the played status
    team.setHasPlayed(!team.isHasPlayed());
    var updatedTeam = teamRepository.save(team);
    log.info("Updated played status for team: {}", updatedTeam);
    
    // return the updated team
    return TeamResponse.fromTeam(updatedTeam);
  }

	public TeamResponse updateQualifiedStatus(String tid) {
		// find the team
		var team = teamRepository.findByTid(tid)
		.orElseThrow( () -> {
			log.error("Team not found with TID: {}", tid);  
			return new TeamNotFoundException("Team not found with TID: " + tid);
		});
		log.info("Updating qualified status for team with TID {}: {}", tid, team.isQualified());
		
		// check if team has played
		if (!team.isHasPlayed()) {
			log.error("Team must have played to update qualified status");
			throw new IllegalArgumentException("Team must have played to update qualified status");
		}

		// update and save the qualified status
		team.setQualified(!team.isQualified());
		var updatedTeam = teamRepository.save(team);
		log.info("Updated qualified status for team: {}", updatedTeam);
		
		// send email to the team who have qualified for finals
		if (updatedTeam.isQualified()) {
			// fetch emails of all team members
			var teamMemberEmails = mrdRepository.findUserEmailListByGidList(updatedTeam.getGidList());

			// send qualified email to all the team members
			emailService.sendQualificationCongratulations(
				teamMemberEmails.toArray(new String[0]), 
				updatedTeam.getEvent().getName(), 
				updatedTeam.getTeamName(), 
				updatedTeam.getTid()
			);
		}

		// return the updated team
		return TeamResponse.fromTeam(updatedTeam);
	}

	public TeamResponse updatePosition(String tid, Position position) {
		// find the team
		var team = teamRepository.findByTid(tid)
		.orElseThrow( () -> {
			log.error("Team not found with TID: {}", tid);  
			return new TeamNotFoundException("Team not found with TID: " + tid);
		});
		log.info("Updating position for team with TID {}: {}", tid, team.getPosition());
		
		// check if team is qualified
		if (!team.isQualified()) {
			log.error("Team must be qualified to update position");
			throw new IllegalArgumentException("Team must be qualified to update position");
		}

		// only send email if position is not 'NONE' and is not the same as the current position
		boolean isWinningPosition = position != null && position != Position.NONE;
		Position previousPosition = team.getPosition();
		
		// update and save the position
		team.setPosition(position != null ? position : Position.NONE);
		var updatedTeam = teamRepository.save(team);
		log.info("Updated position for team: {}", updatedTeam);
		
		// send email to the team who have secured a position in the finals
		if (isWinningPosition && position != previousPosition) {
			// fetch emails of all team members
			var teamMemberEmails = mrdRepository.findUserEmailListByGidList(updatedTeam.getGidList());

			// send position email to all the team members
			emailService.sendPositionCongratulations(
				teamMemberEmails.toArray(new String[0]), 
				updatedTeam.getEvent().getName(), 
				updatedTeam.getTeamName(), 
				updatedTeam.getTid(), 
				updatedTeam.getPosition()
			);
		}

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

  private void validateGids(List<String> gidList) {
    // validate that all GIDs exist in MRD
    for ( String gid : gidList ) {
      if ( !mrdRepository.existsByGid(gid) ) {
        log.error("Invalid GID: {}", gid);
        throw new TeamRegistrationException("Invalid GID: " + gid);
      }
    }

    // check for duplicate GIDs in the request
    Set<String> uniqueGids = new HashSet<>(gidList);
    if ( uniqueGids.size() != gidList.size() ) {
      log.error("Duplicate GIDs found in the request");
      throw new TeamRegistrationException("Duplicate GIDs are not allowed");
    }
  }
}
