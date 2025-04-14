package com.megatronix.paridhi.service;

import java.util.List;
import java.util.Set;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.megatronix.paridhi.constant.AppConstant;
import com.megatronix.paridhi.constant.Domain;
import com.megatronix.paridhi.constant.EventType;
import com.megatronix.paridhi.constant.MessageConstant;
import com.megatronix.paridhi.constant.Role;
import com.megatronix.paridhi.dto.request.EventRequest;
import com.megatronix.paridhi.dto.response.EventResponse;
import com.megatronix.paridhi.exception.EventNotFoundException;
import com.megatronix.paridhi.exception.ForbiddenAccessException;
import com.megatronix.paridhi.model.Event;
import com.megatronix.paridhi.model.EventCombo;
import com.megatronix.paridhi.model.Team;
import com.megatronix.paridhi.model.User;
import com.megatronix.paridhi.repository.ComboRepository;
import com.megatronix.paridhi.repository.EventRepository;
import com.megatronix.paridhi.repository.TeamRepository;
import com.megatronix.paridhi.util.LoggingUtil;

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
	private static final String EVENT = " event";
	private static final String EVENT_NOT_FOUND = "Event not found with ID: ";
  
	/*
	 * public methods - doesn't require authentication
	 */

	@Cacheable(value = "events")
  public List<EventResponse> getAllEvents() {
    // log the operation
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.READ,
			AppConstant.EVENT,
			null,
			"Fetching all events"
    );
    
		// fetch all events from the database
    List<Event> events = eventRepository.findAll();
    
		// log the successful retrieval of events
    LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.READ,
			AppConstant.EVENT,
			null,
			AppConstant.SUCCESSFULLY_RETRIEVED + events.size() + " events"
    );
    
		// map events to EventResponse DTOs and return
    return events.stream()
			.map(EventResponse::fromEvent)
			.toList();
  }

	@Cacheable(value = "eventsByDomain", key = "#domain.name()")
  public List<EventResponse> getEventsByDomain(Domain domain) {
    // log the operation
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.READ,
			AppConstant.EVENT,
			null,
			"Fetching events by domain: " + domain
    );
    
		// fetch events by domain from the database
    List<Event> events = eventRepository.findByDomain(domain);
    
		// log the successful retrieval of events
    LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.READ,
			AppConstant.EVENT,
			null,
			AppConstant.SUCCESSFULLY_RETRIEVED + events.size() + " events for domain: " + domain
    );
    
		// map events to EventResponse DTOs and return
    return events.stream()
			.map(EventResponse::fromEvent)
			.toList();
  }

	@Cacheable(value = "eventsByRegistration", key = "#isRegistrationOpen")
  public List<EventResponse> getEventsByRegistration(boolean isRegistrationOpen) {
    // log the operation
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.READ,
			AppConstant.EVENT,
			null,
			"Fetching events by registration status: " + (isRegistrationOpen ? AppConstant.OPEN : AppConstant.CLOSED)
    );
    
		// fetch events by registration status from the database
    List<Event> events = eventRepository.findByIsRegistrationOpen(isRegistrationOpen);
    
		// log the successful retrieval of events
    LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.READ,
			AppConstant.EVENT,
			null,
			AppConstant.SUCCESSFULLY_RETRIEVED + events.size() + " events with registration status: " + 
			(isRegistrationOpen ? AppConstant.OPEN : AppConstant.CLOSED)
    );
    
		// map events to EventResponse DTOs and return
    return events.stream()
			.map(EventResponse::fromEvent)
			.toList();
  }

	@Cacheable(value = "eventsByType", key = "#eventType.name()")
  public List<EventResponse> getEventsByType(EventType eventType) {
    // log the operation
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.READ,
			AppConstant.EVENT,
			null,
			"Fetching events by type: " + eventType
    );
    
		// fetch events by type from the database
    List<Event> events = eventRepository.findByEventType(eventType);
    
		// log the successful retrieval of events
    LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.READ,
			AppConstant.EVENT,
			null,
			AppConstant.SUCCESSFULLY_RETRIEVED + events.size() + " events of type: " + eventType
    );
    
		// map events to EventResponse DTOs and return
    return events.stream()
			.map(EventResponse::fromEvent)
			.toList();
  }

	@Cacheable(value = "eventById", key = "#eventId")
  public EventResponse getEventById(Long eventId) {
    // log the operation
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.READ,
			AppConstant.EVENT,
			null,
			"Fetching event with ID: " + eventId
    );
    
		// fetch event by ID from the database
    Event event = eventRepository.findById(eventId)
			.orElseThrow(() -> {
				LoggingUtil.logError(
					log,
					MessageConstant.Operation.READ,
					AppConstant.EVENT,
					null,
					EVENT_NOT_FOUND + eventId,
					null
				);
				return new EventNotFoundException(eventId);
			});
    
		// log the successful retrieval of the event
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.READ,
			AppConstant.EVENT,
			null,
			"Successfully retrieved event ID: " + eventId + " - " + event.getName()
		);

		// map event to EventResponse DTO and return
    return EventResponse.fromEvent(event);
  }


	/*
	 * protected methods - requires authentication
	 */

	@CacheEvict(
		value = {
			"events", 
			"eventsByDomain", 
			"eventsByRegistration", 
			"eventsByType", 
			"eventById"
		}, 
		allEntries = true
	)
	@Transactional
  public EventResponse createEvent(EventRequest request, User user) {
		// log the operation
    LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.CREATE,
			AppConstant.EVENT,
			user,
			"Creating new event: " + request.getName() + " for domain: " + request.getDomain()
    );
    
    // validate user access
    checkUserAccess(user, MessageConstant.Operation.CREATE);
    
    // create event builder object
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
      .updatedBy(user)
      .build();

    // save event to database
    Event savedEvent = eventRepository.save(event);
    
		// log the successful creation of the event
    LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.CREATE,
			AppConstant.EVENT,
			user,
			"Successfully created event ID: " + savedEvent.getId() + " - " + savedEvent.getName()
    );

    // return an EventResponse object
    return EventResponse.fromEvent(savedEvent);
  }

	@CacheEvict(
		value = {
			"events", 
			"eventsByDomain", 
			"eventsByRegistration", 
			"eventsByType",
			"eventById"
		}, 
		allEntries = true
	)
	@Transactional
  public EventResponse updateEvent(Long eventId, EventRequest request, User user) {
    // log the operation
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.UPDATE,
			AppConstant.EVENT,
			user,
			"Updating event with ID: " + eventId
    );
    
    // validate user access
    checkUserAccess(user, MessageConstant.Operation.UPDATE);
    
    // check if the event exists
    var existingEvent = eventRepository.findById(eventId)
			.orElseThrow(() -> {
				LoggingUtil.logError(
					log,
					MessageConstant.Operation.UPDATE,
					AppConstant.EVENT,
					user,
					EVENT_NOT_FOUND + eventId,
					null
				);
				return new EventNotFoundException(eventId);
			});
    
    // update event fields
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
    
		// log the successful update of the event
    LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.UPDATE,
			AppConstant.EVENT,
			user,
			"Successfully updated event ID: " + eventId + " - " + updatedEvent.getName()
    );
    
    // return an EventResponse object
    return EventResponse.fromEvent(updatedEvent);
  }

	@CacheEvict(
		value = {
			"events", 
			"eventsByDomain", 
			"eventsByRegistration", 
			"eventsByType",
			"eventById"
		}, 
		allEntries = true
	)
	@Transactional
  public void deleteEvent(Long eventId, User user) {
    // log the operation
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.DELETE,
			AppConstant.EVENT,
			user,
			"Deleting event with ID: " + eventId
    );
    
    // validate user access
    checkUserAccess(user, MessageConstant.Operation.DELETE);
    
    // get event by ID
    var existingEvent = eventRepository.findById(eventId)
			.orElseThrow(() -> {
				LoggingUtil.logError(
					log,
					MessageConstant.Operation.DELETE,
					AppConstant.EVENT,
					user,
					EVENT_NOT_FOUND + eventId,
					null
				);
				return new EventNotFoundException(eventId);
			});
    
    // handle teams that reference this event
    List<Team> teams = teamRepository.findByEvent(existingEvent);
    if (!teams.isEmpty()) {
			LoggingUtil.logOperation(
				log,
				MessageConstant.Operation.DELETE,
				AppConstant.EVENT,
				user,
				"Deleting " + teams.size() + " teams associated with event: " + existingEvent.getName()
			);
			teamRepository.deleteAll(teams);			
    }
    
    // handle many-to-many relationship with combo
    Set<EventCombo> combos = comboRepository.findByEventsContaining(existingEvent);
    if (!combos.isEmpty()) {
			LoggingUtil.logOperation(
				log,
				MessageConstant.Operation.UPDATE,
				AppConstant.EVENT_COMBO,
				user,
				"Updating " + combos.size() + " combos that contain event ID: " + eventId
			);
			
			// remove the event from each combo
			for (EventCombo combo : combos) {
				combo.getEvents().remove(existingEvent);
				// if the combo has fewer than 2 events, delete it
				if (combo.getEvents().size() < 2) {
					comboRepository.delete(combo);
					LoggingUtil.logOperation(
						log,
						MessageConstant.Operation.DELETE,
						AppConstant.EVENT_COMBO,
						user,
						"Deleting combo ID: " + combo.getId() + " as it has fewer than 2 events"
					);
				} else {
					// save the updated combo
					comboRepository.save(combo);
				}
			}
    }
    
    // delete image from cloudinary if exists
    if (existingEvent.getEventPicturePublicId() != null) {
			try {
				LoggingUtil.logOperation(
					log,
					MessageConstant.Operation.DELETE,
					AppConstant.CLOUDINARY_IMAGE,
					user,
					"Deleting image with public ID: " + existingEvent.getEventPicturePublicId()
				);
				cloudinaryService.deleteFile(existingEvent.getEventPicturePublicId());
			} catch (Exception e) {
				LoggingUtil.logError(
					log,
					MessageConstant.Operation.DELETE,
					AppConstant.CLOUDINARY_IMAGE,
					user,
					"Failed to delete image for event ID: " + eventId + " - Error: " + e.getMessage(),
					e
				);
				// continue with event deletion even if image deletion fails
			}
    }
    
    // delete event from database
    eventRepository.delete(existingEvent);
    
		// log the successful deletion of the event
    LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.DELETE,
			AppConstant.EVENT,
			user,
			"Successfully deleted event ID: " + eventId + " - " + existingEvent.getName()
    );
  }

	@CacheEvict(
		value = {
			"events", 
			"eventsByDomain", 
			"eventsByRegistration", 
			"eventsByType",
			"eventById"
		}, 
		allEntries = true
	)
	@Transactional
  public EventResponse toggleRegistrationStatus(Long eventId, User user) {
    // log the operation
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.UPDATE,
			AppConstant.EVENT,
			user,
			"Toggling registration status for event with ID: " + eventId
    );
    
    // validate user access
    checkUserAccess(user, "toggle registration status of");
    
    // fetch event by ID
    var existingEvent = eventRepository.findById(eventId)
			.orElseThrow(() -> {
				LoggingUtil.logError(
					log,
					MessageConstant.Operation.UPDATE,
					AppConstant.EVENT,
					user,
					EVENT_NOT_FOUND + eventId,
					null
				);
				return new EventNotFoundException(eventId);
			});
    
    // get current status for logging
    boolean currentStatus = existingEvent.isRegistrationOpen();
    boolean newStatus = !currentStatus;
    
    // update registration status
    existingEvent.setRegistrationOpen(newStatus);
    var updatedEvent = eventRepository.save(existingEvent);
    
		// log the successful update of registration status
    LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.UPDATE,
			AppConstant.EVENT,
			user,
			"Successfully updated registration status for event ID: " + eventId + 
			" - Name: " + updatedEvent.getName() + 
			" - Status: " + (currentStatus ? "OPEN -> CLOSED" : "CLOSED -> OPEN")
    );
    
    // return updated event
    return EventResponse.fromEvent(updatedEvent);
  }

	@CacheEvict(
		value = {
			"events", 
			"eventsByDomain", 
			"eventsByRegistration", 
			"eventsByType",
			"eventById"
		}, 
		allEntries = true
	)
	@Transactional
	public EventResponse updateEventImage(Long eventId, MultipartFile file, User user) {
		
		// validate file type and size
		String contentType = file.getContentType();
		if (file.isEmpty() || contentType == null || !contentType.startsWith("image/")) {
			LoggingUtil.logError(
				log,
				MessageConstant.Operation.UPDATE,
				AppConstant.EVENT_IMAGE,
				user,
				"Invalid file type or empty file for event ID: " + eventId,
				null
			);
			throw new IllegalArgumentException("Invalid file type or empty file.");
		}

		// check file size
		if (file.getSize() > AppConstant.MAX_IMAGE_SIZE) {
			LoggingUtil.logError(
				log,
				MessageConstant.Operation.UPDATE,
				AppConstant.EVENT_IMAGE,
				user,
				"File size exceeds limit for event ID: " + eventId + " - Size: " + file.getSize(),
				null
			);
			throw new IllegalArgumentException("File size exceeds limit.");
		}

		// log the operation
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.UPDATE,
			AppConstant.EVENT_IMAGE,
			user,
			"Updating image for event with ID: " + eventId + " - File: " + file.getOriginalFilename() + " (" + file.getSize() + " bytes)"
		);
		
		// validate user access
		checkUserAccess(user, "update image of");
		
		// fetch event by ID
		var existingEvent = eventRepository.findById(eventId)
			.orElseThrow(() -> {
				LoggingUtil.logError(
					log,
					MessageConstant.Operation.UPDATE,
					AppConstant.EVENT_IMAGE,
					user,
					EVENT_NOT_FOUND + eventId,
					null
				);
				return new EventNotFoundException(eventId);
			});
			
		// clean up previous image if it exists
		if (existingEvent.getEventPicturePublicId() != null) {
			try {
				LoggingUtil.logOperation(
					log,
					MessageConstant.Operation.DELETE,
					AppConstant.CLOUDINARY_IMAGE,
					user,
					"Removing previous image with public ID: " + existingEvent.getEventPicturePublicId()
				);
				cloudinaryService.deleteFile(existingEvent.getEventPicturePublicId());
			} catch (Exception e) {
				// Log but continue - failure to delete old image shouldn't stop upload of new image
				LoggingUtil.logError(
					log,
					MessageConstant.Operation.DELETE,
					AppConstant.CLOUDINARY_IMAGE,
					user,
					"Failed to delete previous image for event ID: " + eventId + " - Continuing with upload - Error: " + e.getMessage(),
					e
				);
				// continue with upload even if previous image deletion fails
			}
		}

		// upload image to cloudinary - exceptions will be propagated to GlobalExceptionHandler
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.CREATE,
			AppConstant.CLOUDINARY_IMAGE,
			user,
			"Uploading new image for event ID: " + eventId
		);
		
		var imageDetails = cloudinaryService.uploadFile(file);

		// update event image URL
		existingEvent.setEventPictureSecureUrl(imageDetails.get("secure_url"));
		existingEvent.setEventPicturePublicId(imageDetails.get("public_id"));
		existingEvent.setUpdatedBy(user);

		// save updated event to database
		var updatedEvent = eventRepository.save(existingEvent);
		
		// log the successful update of the event image
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.UPDATE,
			AppConstant.EVENT_IMAGE,
			user,
			"Successfully updated image for event ID: " + eventId + 
			" - New image public ID: " + imageDetails.get("public_id")
		);

		// return an EventResponse object
		return EventResponse.fromEvent(updatedEvent);
	}

	/*
	 * private methods - used internally only
	 */

  private void checkUserAccess(User user, String methodType) {
    // check if user is null
		if (user == null) {
			LoggingUtil.logSecurity(
				log,
				AppConstant.ACCESS_DENIED,
				null,
				AppConstant.FAILED,
				"Authentication required to " + methodType + EVENT
			);
			throw new ForbiddenAccessException(MessageConstant.UserMessage.ACCESS_DENIED);
    }
    
		// check if user has ROLE_USER
    if (user.getRole().equals(Role.ROLE_USER)) {
			LoggingUtil.logSecurity(
				log,
				AppConstant.ACCESS_DENIED,
				user,
				AppConstant.FAILED,
				"User lacks permission to " + methodType + EVENT
			);
			throw new ForbiddenAccessException(MessageConstant.UserMessage.ACCESS_DENIED);
    }
    
		// log the successful authorization
    LoggingUtil.logSecurity(
			log,
			AppConstant.ACCESS_GRANTED,
			user,
			AppConstant.SUCCESS,
			"User authorized to " + methodType + EVENT
    );
  }
}
