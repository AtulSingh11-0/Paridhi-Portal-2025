package com.megatronix.paridhi.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.megatronix.paridhi.constant.Role;
import com.megatronix.paridhi.dto.response.TeamResponse;
import com.megatronix.paridhi.exception.EventNotFoundException;
import com.megatronix.paridhi.exception.ForbiddenAccessException;
import com.megatronix.paridhi.model.User;
import com.megatronix.paridhi.repository.EventRepository;
import com.megatronix.paridhi.repository.TeamRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CRDService {
  private final TeamRepository teamRepository;
  private final EventRepository eventRepository;

	public List<TeamResponse> getTeamsByEventForPrelims(Long eventId, User user) {
		log.info("Fetching teams for Prelims of Event: {} by: {}", eventId, user);

		// check if the user has permission to fetch the data
		checkUserAccess(user, "fetch teams for Prelims of");

		// fetch the teams for the event
		var event = eventRepository.findById(eventId).orElseThrow(() -> {
			log.error("Event not found with ID: {}", eventId);
			return new EventNotFoundException("Event not found with ID: " + eventId);
		});

		var teams = teamRepository.findByEvent(event);

		// segregate the teams into who have their paid field as true and qualified field as false
		var prelimsTeams = teams.stream()
			.filter(team -> (team.isPaid() && !team.isQualified()))
			.toList();
		log.info("Fetched {} teams for Prelims of Event: {}", prelimsTeams.size(), event.getName());

		// convert the teams to response DTOs and return
		return prelimsTeams.stream()
			.map(TeamResponse::fromTeam)
			.toList();
	}

	public List<TeamResponse> getTeamsByEventForFinals(Long eventId, User user) {
		log.info("Fetching teams for Finals of Event: {} by: {}", eventId, user);

		// check if the user has permission to fetch the data
		checkUserAccess(user, "fetch teams for Finals of");

		// check if the event exists
		var event = eventRepository.findById(eventId).orElseThrow(() -> {
			log.error("Event not found with ID: {}", eventId);
			return new EventNotFoundException("Event not found with ID: " + eventId);
		});

		// fetch the teams for the event
		var teams = teamRepository.findByEvent(event);

		// segregate the teams into who have their paid field as true and played field as true and qualified field as true
		var finalsTeams = teams.stream()
			.filter(team -> (team.isPaid() && team.isHasPlayed() && team.isQualified()))
			.toList();
		log.info("Fetched {} teams for Finals of Event: {}", finalsTeams.size(), event.getName());

		// convert the teams to response DTOs and return
		return finalsTeams.stream()
			.map(TeamResponse::fromTeam)
			.toList();
	}

	private void checkUserAccess(User user, String action) {
		if (user == null) {
			log.error("User is not authenticated to {} the event", action);
			throw new ForbiddenAccessException("You do not have permission to " + action + " this event");
		}

		if (user.getRole() != null && user.getRole().equals(Role.ROLE_USER)) {
			log.error("User {} does not have permission to {} the event", user.getEmail(), action);
			throw new ForbiddenAccessException("You do not have permission to " + action + " this event");
		}
	}
}
