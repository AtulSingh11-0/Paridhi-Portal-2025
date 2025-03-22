package com.megatronix.paridhi.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.megatronix.paridhi.constant.Domain;
import com.megatronix.paridhi.constant.EventType;
import com.megatronix.paridhi.constant.Role;
import com.megatronix.paridhi.dto.request.EventRequest;
import com.megatronix.paridhi.dto.response.EventResponse;
import com.megatronix.paridhi.exception.EventNotFoundException;
import com.megatronix.paridhi.exception.ForbiddenAccessException;
import com.megatronix.paridhi.model.Event;
import com.megatronix.paridhi.model.User;
import com.megatronix.paridhi.repository.EventRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {
  private final EventRepository eventRepository;
  
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
      .eventPictureUrl(request.getEventPictureUrl())
      .ruleBook(request.getRuleBook())
      .minPlayers(request.getMinPlayers())
      .maxPlayers(request.getMaxPlayers())
      .registrationFee(request.getRegistrationFee())
      .isRegistrationOpen(request.isRegistrationOpen())
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
    existingEvent.setEventPictureUrl(request.getEventPictureUrl());
    existingEvent.setRuleBook(request.getRuleBook());
    existingEvent.setMinPlayers(request.getMinPlayers());
    existingEvent.setMaxPlayers(request.getMaxPlayers());
    existingEvent.setRegistrationFee(request.getRegistrationFee());
    existingEvent.setRegistrationOpen(request.isRegistrationOpen());
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

    // check if event exists
    if (!eventRepository.existsById(eventId)) {
      log.error("Event with ID {} not found", eventId);
      throw new EventNotFoundException("Event not found with ID: " + eventId);
    }

    // check if the user has permission to delete event
    checkUserAccess(user, "delete");

    // delete event from database
    eventRepository.deleteById(eventId);
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

  private void checkUserAccess(User user, String methodType) {
    log.info("User: {}", user);
    if ( user.getRole().equals(Role.ROLE_USER) ) {
      log.error("User with ID {} not authorized to {} the event", user.getId(), methodType);
      throw new ForbiddenAccessException("User not authorized to " + methodType + " the event");
    }
  }
}
