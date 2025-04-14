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
    LoggingUtil.logOperation(
        log,
        MessageConstant.Operation.CREATE,
        AppConstant.EVENT,
        user,
        "Creating new event: " + request.getName() + " for domain: " + request.getDomain()
    );
    
    // Validate user access
    checkUserAccess(user, "create");
    
    // Create Event Builder object
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

    // Save event to database
    Event savedEvent = eventRepository.save(event);
    
    LoggingUtil.logOperation(
        log,
        MessageConstant.Operation.CREATE,
        AppConstant.EVENT,
        user,
        "Successfully created event ID: " + savedEvent.getId() + " - " + savedEvent.getName()
    );

    // Return an EventResponse object
    return EventResponse.fromEvent(savedEvent);
  }

	@Cacheable(value = "events")
  public List<EventResponse> getAllEvents() {
    LoggingUtil.logOperation(
        log,
        MessageConstant.Operation.READ,
        AppConstant.EVENT,
        null,
        "Fetching all events"
    );
    
    List<Event> events = eventRepository.findAll();
    
    LoggingUtil.logOperation(
        log,
        MessageConstant.Operation.READ,
        AppConstant.EVENT,
        null,
        AppConstant.SUCCESSFULLY_RETRIEVED + events.size() + " events"
    );
    
    return events.stream()
        .map(EventResponse::fromEvent)
        .toList();
  }

	@Cacheable(value = "eventsByDomain", key = "#domain.name()")
  public List<EventResponse> getEventsByDomain(Domain domain) {
    LoggingUtil.logOperation(
        log,
        MessageConstant.Operation.READ,
        AppConstant.EVENT,
        null,
        "Fetching events by domain: " + domain
    );
    
    List<Event> events = eventRepository.findByDomain(domain);
    
    LoggingUtil.logOperation(
        log,
        MessageConstant.Operation.READ,
        AppConstant.EVENT,
        null,
        AppConstant.SUCCESSFULLY_RETRIEVED + events.size() + " events for domain: " + domain
    );
    
    return events.stream()
        .map(EventResponse::fromEvent)
        .toList();
  }

	@Cacheable(value = "eventsByRegistration", key = "#isRegistrationOpen")
  public List<EventResponse> getEventsByRegistration(boolean isRegistrationOpen) {
    LoggingUtil.logOperation(
        log,
        MessageConstant.Operation.READ,
        AppConstant.EVENT,
        null,
        "Fetching events by registration status: " + (isRegistrationOpen ? "OPEN" : "CLOSED")
    );
    
    List<Event> events = eventRepository.findByIsRegistrationOpen(isRegistrationOpen);
    
    LoggingUtil.logOperation(
        log,
        MessageConstant.Operation.READ,
        AppConstant.EVENT,
        null,
        AppConstant.SUCCESSFULLY_RETRIEVED + events.size() + " events with registration status: " + 
        (isRegistrationOpen ? "OPEN" : "CLOSED")
    );
    
    return events.stream()
        .map(EventResponse::fromEvent)
        .toList();
  }

	@Cacheable(value = "eventsByType", key = "#eventType.name()")
  public List<EventResponse> getEventsByType(EventType eventType) {
    LoggingUtil.logOperation(
        log,
        MessageConstant.Operation.READ,
        AppConstant.EVENT,
        null,
        "Fetching events by type: " + eventType
    );
    
    List<Event> events = eventRepository.findByEventType(eventType);
    
    LoggingUtil.logOperation(
        log,
        MessageConstant.Operation.READ,
        AppConstant.EVENT,
        null,
        AppConstant.SUCCESSFULLY_RETRIEVED + events.size() + " events of type: " + eventType
    );
    
    return events.stream()
        .map(EventResponse::fromEvent)
        .toList();
  }

	@Cacheable(value = "eventById", key = "#eventId")
  public EventResponse getEventById(Long eventId) {
    LoggingUtil.logOperation(
        log,
        MessageConstant.Operation.READ,
        AppConstant.EVENT,
        null,
        "Fetching event with ID: " + eventId
    );
    
    Event event = eventRepository.findById(eventId)
        .orElseThrow(() -> {
            LoggingUtil.logError(
                log,
                MessageConstant.Operation.READ,
                AppConstant.EVENT,
                null,
								"Event not found with ID: " + eventId,
                null
            );
            return new EventNotFoundException(eventId);
        });
    
    return EventResponse.fromEvent(event);
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
    LoggingUtil.logOperation(
        log,
        MessageConstant.Operation.UPDATE,
        AppConstant.EVENT,
        user,
        "Updating event with ID: " + eventId
    );
    
    // Validate user access
    checkUserAccess(user, "update");
    
    // Check if the event exists
    var existingEvent = eventRepository.findById(eventId)
        .orElseThrow(() -> {
            LoggingUtil.logError(
                log,
                MessageConstant.Operation.UPDATE,
                AppConstant.EVENT,
                user,
                "Event not found with ID: " + eventId,
                null
            );
            return new EventNotFoundException(eventId);
        });
    
    // Update Event fields
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
    
    // Save updated event to database
    var updatedEvent = eventRepository.save(existingEvent);
    
    LoggingUtil.logOperation(
        log,
        MessageConstant.Operation.UPDATE,
        AppConstant.EVENT,
        user,
        "Successfully updated event ID: " + eventId + " - " + updatedEvent.getName()
    );
    
    // Return an EventResponse object
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
    LoggingUtil.logOperation(
        log,
        MessageConstant.Operation.DELETE,
        AppConstant.EVENT,
        user,
        "Deleting event with ID: " + eventId
    );
    
    // Validate user access
    checkUserAccess(user, "delete");
    
    // Get event by ID
    var existingEvent = eventRepository.findById(eventId)
        .orElseThrow(() -> {
            LoggingUtil.logError(
                log,
                MessageConstant.Operation.DELETE,
                AppConstant.EVENT,
                user,
                "Event not found with ID: " + eventId,
                null
            );
            return new EventNotFoundException(eventId);
        });
    
    // Handle teams that reference this event
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
    
    // Handle many-to-many relationship with combo
    Set<EventCombo> combos = comboRepository.findByEventsContaining(existingEvent);
    if (!combos.isEmpty()) {
        LoggingUtil.logOperation(
            log,
            MessageConstant.Operation.UPDATE,
            "EventCombo",
            user,
            "Updating " + combos.size() + " combos that contain event ID: " + eventId
        );
        
        for (EventCombo combo : combos) {
            combo.getEvents().remove(existingEvent);
            if (combo.getEvents().size() < 2) {
                comboRepository.delete(combo);
                LoggingUtil.logOperation(
                    log,
                    MessageConstant.Operation.DELETE,
                    "EventCombo",
                    user,
                    "Deleting combo ID: " + combo.getId() + " as it has fewer than 2 events"
                );
            } else {
                comboRepository.save(combo);
            }
        }
    }
    
    // Delete image from cloudinary if exists
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
            // Continue with event deletion even if image deletion fails
        }
    }
    
    // Delete event from database
    eventRepository.delete(existingEvent);
    
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
  public EventResponse toggleRegistrationStatus(Long id, User user) {
    LoggingUtil.logOperation(
        log,
        MessageConstant.Operation.UPDATE,
        AppConstant.EVENT,
        user,
        "Toggling registration status for event with ID: " + id
    );
    
    // Validate user access
    checkUserAccess(user, "toggle registration status of");
    
    // Fetch event by ID
    var existingEvent = eventRepository.findById(id)
        .orElseThrow(() -> {
            LoggingUtil.logError(
                log,
                MessageConstant.Operation.UPDATE,
                AppConstant.EVENT,
                user,
                "Event not found with ID: " + id,
                null
            );
            return new EventNotFoundException(id);
        });
    
    // Get current status for logging
    boolean currentStatus = existingEvent.isRegistrationOpen();
    boolean newStatus = !currentStatus;
    
    // Update registration status
    existingEvent.setRegistrationOpen(newStatus);
    var updatedEvent = eventRepository.save(existingEvent);
    
    LoggingUtil.logOperation(
        log,
        MessageConstant.Operation.UPDATE,
        AppConstant.EVENT,
        user,
        "Successfully updated registration status for event ID: " + id + 
        " - Name: " + updatedEvent.getName() + 
        " - Status: " + (currentStatus ? "OPEN → CLOSED" : "CLOSED → OPEN")
    );
    
    // Return updated event
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
	public EventResponse updateEventImage(Long id, MultipartFile file, User user) {
		LoggingUtil.logOperation(
            log,
            MessageConstant.Operation.UPDATE,
            AppConstant.EVENT_IMAGE,
            user,
            "Updating image for event with ID: " + id + " - File: " + file.getOriginalFilename() + " (" + file.getSize() + " bytes)"
        );
		
		// Validate user access
		checkUserAccess(user, "update image of");
		
		// Fetch event by ID
		var existingEvent = eventRepository.findById(id)
			.orElseThrow(() -> {
				LoggingUtil.logError(
					log,
					MessageConstant.Operation.UPDATE,
					AppConstant.EVENT_IMAGE,
					user,
					"Event not found with ID: " + id,
					null
				);
				return new EventNotFoundException(id);
			});
			
		// Clean up previous image if it exists
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
					"Failed to delete previous image for event ID: " + id + " - Continuing with upload - Error: " + e.getMessage(),
					e
				);
				// Continue with upload even if previous image deletion fails
			}
		}

		// Upload image to cloudinary - exceptions will be propagated to GlobalExceptionHandler
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.CREATE,
			AppConstant.CLOUDINARY_IMAGE,
			user,
			"Uploading new image for event ID: " + id
		);
		
		var imageDetails = cloudinaryService.uploadFile(file);

		// Update event image URL
		existingEvent.setEventPictureSecureUrl(imageDetails.get("secure_url"));
		existingEvent.setEventPicturePublicId(imageDetails.get("public_id"));
		existingEvent.setUpdatedBy(user);

		// Save updated event to database
		var updatedEvent = eventRepository.save(existingEvent);
		
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.UPDATE,
			AppConstant.EVENT_IMAGE,
			user,
			"Successfully updated image for event ID: " + id + 
			" - New image public ID: " + imageDetails.get("public_id")
		);

		// Return an EventResponse object
		return EventResponse.fromEvent(updatedEvent);
	}

  private void checkUserAccess(User user, String methodType) {
    if (user == null) {
        LoggingUtil.logSecurity(
            log,
            "access_denied",
            null,
            "FAILED",
            "Authentication required to " + methodType + " event"
        );
        throw new ForbiddenAccessException(MessageConstant.UserMessage.ACCESS_DENIED);
    }
    
    if (user.getRole().equals(Role.ROLE_USER)) {
        LoggingUtil.logSecurity(
            log,
            "access_denied",
            user,
            "FAILED",
            "User lacks permission to " + methodType + " event"
        );
        throw new ForbiddenAccessException(MessageConstant.UserMessage.ACCESS_DENIED);
    }
    
    LoggingUtil.logSecurity(
        log,
        "access_granted",
        user,
        "SUCCESS",
        "User authorized to " + methodType + " event"
    );
  }
}
