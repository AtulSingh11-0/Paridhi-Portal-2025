package com.megatronix.paridhi.service;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.megatronix.paridhi.constant.Domain;
import com.megatronix.paridhi.constant.EventType;
import com.megatronix.paridhi.constant.Role;
import com.megatronix.paridhi.dto.request.EventRequest;
import com.megatronix.paridhi.dto.response.EventResponse;
import com.megatronix.paridhi.exception.EventNotFoundException;
import com.megatronix.paridhi.exception.FileUploadException;
import com.megatronix.paridhi.exception.ForbiddenAccessException;
import com.megatronix.paridhi.model.Event;
import com.megatronix.paridhi.model.EventCombo;
import com.megatronix.paridhi.model.Team;
import com.megatronix.paridhi.model.User;
import com.megatronix.paridhi.repository.ComboRepository;
import com.megatronix.paridhi.repository.EventRepository;
import com.megatronix.paridhi.repository.TeamRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {
	private final TeamRepository teamRepository;
  private final EventRepository eventRepository;
	private final ComboRepository comboRepository;
	private final CloudinaryService cloudinaryService;
  
  public EventResponse createEvent(EventRequest request, User user) {
    // create event from request
    log.info("Creating event: {} by: {}", request, user);

    // check if the user has permission to create event
    checkUserAccess(user, "create");

    // create Event Builder object
    var event = Event.builder()
      .domain(request.getDomain())
      .name(request.getName())
      .eventType(request.getEventType())
      .eventDate(request.getEventDate())
      .description(request.getDescription())
      .venue(request.getVenue())
      .coordinatorDetails(request.getCoordinatorDetails())
      .eventPictureSecureUrl("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSxrgoLK49zGt45fybNVJfpDUt4otbtAfmWbg&s")
      .ruleBook(request.getRuleBook())
      .minPlayers(request.getMinPlayers())
      .maxPlayers(request.getMaxPlayers())
      .registrationFee(request.getRegistrationFee())
      .prizePool(request.getPrizePool())
      .createdBy(user)
      .updatedBy(null)
      .build();

    // save event to database
    Event savedEvent = eventRepository.save(event);
    log.info("Event created: {}", savedEvent);

    // return an EventResponse object
    return EventResponse.fromEvent(savedEvent);
  }

  public List<EventResponse> getAllEvents() {
    // fetch all events from database
    log.info("Fetching all events");
    return eventRepository.findAll().stream()
      .map(EventResponse::fromEvent)
      .toList();
  }

  public List<EventResponse> getEventsByDomain(Domain domain) {
    // fetch events by domain from database
    log.info("Fetching events by domain: {}", domain);
    return eventRepository.findByDomain(domain).stream()
      .map(EventResponse::fromEvent)
      .toList();
  }

  public List<EventResponse> getEventsByRegistration(boolean isRegistrationOpen) {
    // fetch events by registration status from database
    log.info("Fetching events by registration status: {}", isRegistrationOpen);
    return eventRepository.findByIsRegistrationOpen(isRegistrationOpen).stream()
      .map(EventResponse::fromEvent)
      .toList();
  }

  public List<EventResponse> getEventsByType(EventType eventType) {
    // fetch events by type from database
    log.info("Fetching events by type: {}", eventType);
    return eventRepository.findByEventType(eventType).stream()
      .map(EventResponse::fromEvent)
      .toList();
  }

  public EventResponse getEventById(Long eventId) {
    // fetch event by ID from database
    log.info("Fetching event by ID: {}", eventId);
    Event event = eventRepository.findById(eventId)
      .orElseThrow( () -> {
        log.error("Event with ID {} not found", eventId);
        return new EventNotFoundException("Event not found with ID: " + eventId);
      });
    return EventResponse.fromEvent(event);
  }

  public EventResponse updateEvent(Long id, EventRequest request, User user) {
    // update event by ID from request
    log.info("Updating event with ID: {}", id);

    // check if the event exists
    var existingEvent = eventRepository.findById(id)
    .orElseThrow( () -> {
      log.error("Event with ID {} not found", id);
      return new EventNotFoundException("Event not found with ID: " + id);
    });

    // check if the user has permission to update event
    checkUserAccess(user, "update");
    
    // update Event fields
    existingEvent.setDomain(request.getDomain());
    existingEvent.setName(request.getName());
    existingEvent.setEventType(request.getEventType());
    existingEvent.setEventDate(request.getEventDate());
    existingEvent.setDescription(request.getDescription());
    existingEvent.setVenue(request.getVenue());
    existingEvent.setCoordinatorDetails(request.getCoordinatorDetails());
    existingEvent.setRuleBook(request.getRuleBook());
    existingEvent.setMinPlayers(request.getMinPlayers());
    existingEvent.setMaxPlayers(request.getMaxPlayers());
    existingEvent.setRegistrationFee(request.getRegistrationFee());
    existingEvent.setPrizePool(request.getPrizePool());
    existingEvent.setUpdatedBy(user);

    // save updated event to database
    var updatedEvent = eventRepository.save(existingEvent);
    log.info("Event updated: {}", updatedEvent);

    // return an EventResponse object
    return EventResponse.fromEvent(updatedEvent);
  }

  public void deleteEvent(Long eventId, User user) {
    // delete event by ID from database
    log.info("Deleting event with ID: {}", eventId);

    // get event by ID
		var existingEvent = eventRepository.findById(eventId)
			.orElseThrow( () -> {
				log.error("Event with ID {} not found", eventId);
				return new EventNotFoundException("Event not found with ID: " + eventId);
			});

    // check if the user has permission to delete event
    checkUserAccess(user, "delete");

		// handle teams that reference this event
		List<Team> teams = teamRepository.findByEvent(existingEvent);
		if (!teams.isEmpty()) {
			log.info("Found {} teams registered for event: {}. Deleting teams...", teams.size(), existingEvent.getName());
			teamRepository.deleteAll(teams);			
		}

		//handle many-to-many relationship with combo
		Set<EventCombo> combos = comboRepository.findByEventsContaining(existingEvent);
		for (EventCombo combo : combos) {
			combo.getEvents().remove(existingEvent);
			if (combo.getEvents().size() < 2) {
				comboRepository.delete(combo);
			} else {
				comboRepository.save(combo);
			}
		}

    // delete event from database
		if (existingEvent.getEventPicturePublicId() != null) {
			// delete image from cloudinary
			cloudinaryService.deleteFile(existingEvent.getEventPicturePublicId());
		}
    eventRepository.delete(existingEvent);
    log.info("Event deleted with ID: {}", eventId);
  }

  public EventResponse toggleRegistrationStatus(Long id, User user) {
    // fetch event by ID from database
    var existingEvent = eventRepository.findById(id)
    .orElseThrow( () -> {
      log.error("Event with ID {} not found", id);
      return new EventNotFoundException("Event not found with ID: " + id);
    });

    // check if the user has permission to toggle registration status
    checkUserAccess(user, "toggle registration status of");

    log.info("Toggling registration status: {} for event with ID: {}", existingEvent.isRegistrationOpen(), id);
    // update registration status and save to database
    existingEvent.setRegistrationOpen(!existingEvent.isRegistrationOpen());
    var updatedEvent = eventRepository.save(existingEvent);
    log.info("Event registration status updated: {}", updatedEvent);

    // return an EventResponse object
    return EventResponse.fromEvent(updatedEvent);
  }

	@Transactional
	public EventResponse updateEventImage(Long id, MultipartFile file, User user) {
		log.info("Updating event image for event with ID: {}, by: {}", id, user);

		// check if user has permission
		checkUserAccess(user, "update image of");

		// fetch event by ID
		var existingEvent = eventRepository.findById(id)
			.orElseThrow( () -> {
				log.error("Event with ID {} not found", id);
				return new EventNotFoundException("Event not found with ID: " + id);
			});

		// upload image to cloudinary
		try {
			var imageDetails = cloudinaryService.uploadFile(file);

			// update event image URL
			existingEvent.setEventPictureSecureUrl(imageDetails.get("secure_url"));
			existingEvent.setEventPicturePublicId(imageDetails.get("public_id"));
			existingEvent.setUpdatedBy(user);

			// save updated event to database
			var updatedEvent = eventRepository.save(existingEvent);
			log.info("Event image updated: {}", updatedEvent);

			// return an EventResponse object
			return EventResponse.fromEvent(updatedEvent);
		} catch (Exception e) {
			log.error("Error updating event image: {}", e.getMessage());
      throw new FileUploadException("Error updating event image: " + e.getMessage(), e.getCause());
		}
	}

  private void checkUserAccess(User user, String methodType) {
    if (user == null) {
			log.error("Authentication required to {} the event", methodType);
			throw new ForbiddenAccessException("Authentication required to " + methodType + " the event");
		}
		
		log.info("User: {}", user);
    if ( user.getRole().equals(Role.ROLE_USER) ) {
      log.error("User with ID {} not authorized to {} the event", user == null ? "System" : user.getId(), methodType);
      throw new ForbiddenAccessException("User not authorized to " + methodType + " the event");
    }
  }
}
