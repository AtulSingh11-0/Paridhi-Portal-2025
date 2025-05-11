package com.megatronix.paridhi.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.megatronix.paridhi.constant.MessageConstant;
import com.megatronix.paridhi.constant.AppConstant;
import com.megatronix.paridhi.constant.Role;
import com.megatronix.paridhi.dto.response.TeamResponse;
import com.megatronix.paridhi.exception.EventNotFoundException;
import com.megatronix.paridhi.exception.ForbiddenAccessException;
import com.megatronix.paridhi.model.User;
import com.megatronix.paridhi.repository.EventRepository;
import com.megatronix.paridhi.repository.MRDRepository;
import com.megatronix.paridhi.repository.TeamRepository;
import com.megatronix.paridhi.util.LoggingUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CRDService {
	private final TeamRepository teamRepository;
	private final EventRepository eventRepository;
	private final MRDRepository mrdRepository;
	private static final String EVENT = " event";
	private static final String CRD_SERVICE = "CRDService";
	private static final String EVENT_NOT_FOUND = "Event not found with ID: ";

	/*
	 * protected methods - requires authentication
	 */

	@Transactional(readOnly = true)
	public List<TeamResponse> getTeamsByEventForPrelims(Long eventId, User user) {
		// log the operation
		LoggingUtil.logOperation(
				log,
				MessageConstant.Operation.READ,
				CRD_SERVICE,
				user,
				"Fetching teams for prelims of event ID: " + eventId);

		// validate user access
		checkUserAccess(user, "fetch teams for Prelims of");

		// fetch the teams for the event
		var event = eventRepository.findById(eventId)
				.orElseThrow(() -> {
					LoggingUtil.logError(
							log,
							MessageConstant.Operation.READ,
							CRD_SERVICE,
							user,
							EVENT_NOT_FOUND + eventId,
							null);
					return new EventNotFoundException(EVENT_NOT_FOUND + eventId);
				});

		// fetch the teams for the event
		var teams = teamRepository.findByEventWithContacts(event);

		// segregate the teams into who have their paid field as true and qualified
		// field as false
		var prelimsTeams = teams.stream()
				.filter(team -> {
					// check if the team members have paid for their MRD
					var allMembersPaid = team.getGidList().stream()
						.allMatch(mrdRepository::existsByGidAndIsPaidTrue);
					
						// check if the team is paid and not qualified
						return ( allMembersPaid && team.isPaid() && !team.isQualified());
				})
				.toList();

		// log the successful retrieval of teams
		LoggingUtil.logOperation(
				log,
				MessageConstant.Operation.READ,
				CRD_SERVICE,
				user,
				"Fetched " + prelimsTeams.size() + " teams for Prelims of Event: " + event.getName());

		// convert the teams to response DTOs and return
		return prelimsTeams.stream()
				.map(TeamResponse::fromTeam)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<TeamResponse> getTeamsByEventForFinals(Long eventId, User user) {
		// log the operation
		LoggingUtil.logOperation(
				log,
				MessageConstant.Operation.READ,
				CRD_SERVICE,
				user,
				"Fetching teams for finals of event ID: " + eventId);

		// validate user access
		checkUserAccess(user, "fetch teams for Finals of");

		// check if the event exists
		var event = eventRepository.findById(eventId)
				.orElseThrow(() -> {
					LoggingUtil.logError(
							log,
							MessageConstant.Operation.READ,
							CRD_SERVICE,
							user,
							EVENT_NOT_FOUND + eventId,
							null);
					return new EventNotFoundException(EVENT_NOT_FOUND + eventId);
				});

		// fetch the teams for the event
		var teams = teamRepository.findByEventWithContacts(event);

		// segregate the teams into who have their paid field as true and played field
		// as true and qualified field as true
		var finalsTeams = teams.stream()
				.filter(team -> (team.isPaid() && team.isHasPlayed() && team.isQualified()))
				.toList();

		// log the successful retrieval of teams
		LoggingUtil.logOperation(
				log,
				MessageConstant.Operation.READ,
				CRD_SERVICE,
				user,
				"Fetched " + finalsTeams.size() + " teams for Finals of Event: " + event.getName());

		// convert the teams to response DTOs and return
		return finalsTeams.stream()
				.map(TeamResponse::fromTeam)
				.toList();
	}

	/*
	 * private methods - used internally
	 */

	private void checkUserAccess(User user, String action) {
		// check if user is null
		if (user == null) {
			LoggingUtil.logSecurity(
					log,
					AppConstant.ACCESS_DENIED,
					null,
					AppConstant.FAILED,
					"Authentication required to " + action + EVENT);
			throw new ForbiddenAccessException(MessageConstant.UserMessage.ACCESS_DENIED);
		}

		// check if the user has ROLE_USER
		if (user.getRole().equals(Role.ROLE_USER)) {
			LoggingUtil.logSecurity(
					log,
					AppConstant.ACCESS_DENIED,
					user,
					AppConstant.FAILED,
					"User lacks permission to " + action + EVENT);
			throw new ForbiddenAccessException(MessageConstant.UserMessage.ACCESS_DENIED);
		}

		// log the successful authorization
		LoggingUtil.logSecurity(
				log,
				AppConstant.ACCESS_GRANTED,
				user,
				AppConstant.SUCCESS,
				"User authorized to " + action + EVENT);
	}
}
