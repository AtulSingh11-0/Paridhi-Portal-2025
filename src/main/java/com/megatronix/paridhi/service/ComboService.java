package com.megatronix.paridhi.service;

import java.util.HashSet;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.megatronix.paridhi.constant.Domain;
import com.megatronix.paridhi.constant.Role;
import com.megatronix.paridhi.dto.request.ComboRequest;
import com.megatronix.paridhi.dto.response.ComboResponse;
import com.megatronix.paridhi.exception.ComboNotFoundException;
import com.megatronix.paridhi.exception.EventNotFoundException;
import com.megatronix.paridhi.exception.ForbiddenAccessException;
import com.megatronix.paridhi.exception.InvalidDomainException;
import com.megatronix.paridhi.model.EventCombo;
import com.megatronix.paridhi.model.User;
import com.megatronix.paridhi.repository.ComboRepository;
import com.megatronix.paridhi.repository.EventRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ComboService {
  private final ComboRepository comboRepository;
  private final EventRepository eventRepository;

  @Transactional
  public ComboResponse createCombo(ComboRequest request, User user) {
    log.info("Creating combo: {}", request);

    // check if user has permission to create combo
    if ( user.getRole().equals(Role.ROLE_USER) ) {
      log.error("User with ID {} not authorized to create combo", user.getId());
      throw new ForbiddenAccessException("User not authorized to create combo");
    }

    // get events from IDs
    var events = eventRepository.findAllById(request.getEventIds());

    // validate if all events are exist or not
    if ( events.size() != request.getEventIds().size() ) {
      log.error("One or more events not found");
      throw new EventNotFoundException("One or more events not found");
    }

    // validate all events are from the same domain
    var eventsAreFromSameDomain = events.stream()
      .allMatch(event -> event.getDomain().equals(request.getDomain()));
    
    if ( !eventsAreFromSameDomain ) {
      log.error("All events must belong to domain: {}", request.getDomain());
      throw new InvalidDomainException("All events must belong to the same domain");
    }

    // create and save the combo
    var combo = EventCombo.builder()
      .name(request.getName())
      .description(request.getDescription())
      .domain(request.getDomain())
      .events(new HashSet<>(events))
      .registrationFee(request.getRegistrationFee())
      .isRegistrationOpen(request.isRegistrationOpen())
      .createdBy(user)
      .build();
    var savedCombo = comboRepository.save(combo);
    log.info("Combo created: {}", savedCombo);

    return ComboResponse.fromCombo(savedCombo);
  }

  public List<ComboResponse> getAllCombos() {
    log.info("Fetching all combos");
    return comboRepository.findAll().stream()
      .map(ComboResponse::fromCombo)
      .toList();
  }

  public ComboResponse getComboById(Long id) {
    log.info("Fetching combo with ID: {}", id);
    return comboRepository.findById(id)
      .map(ComboResponse::fromCombo)
      .orElseThrow(() -> {
        log.error("Combo not found with ID: {}", id);
        return new ComboNotFoundException("Combo not found with ID: " + id);
      });
  }

  public List<ComboResponse> getCombosByDomain(Domain domain) {
    log.info("Fetching combos for domain: {}", domain);
    return comboRepository.findByDomain(domain).stream()
      .map(ComboResponse::fromCombo)
      .toList();
  }

  public List<ComboResponse> getCombosByStatus(boolean isRegistrationOpen) {
    log.info("Fetching combos by status: {}", isRegistrationOpen);
    return comboRepository.findByIsRegistrationOpen(isRegistrationOpen).stream()
      .map(ComboResponse::fromCombo)
      .toList();
  }

  @Transactional
  public ComboResponse updateCombo(Long id, ComboRequest request, User user) {
    log.info("Updating combo with ID: {} by: {}", id, user);

    // check if user has permission
    if (user.getRole().equals(Role.ROLE_USER)) {
      log.error("User with ID {} not authorized to update combo", user.getId());
      throw new ForbiddenAccessException("User not authorized to update combo");
    }

    // find the combo
    var combo = comboRepository.findById(id)
      .orElseThrow( () -> {
        log.error("Combo not found with ID: {}", id);
        return new ComboNotFoundException("Combo not found with ID: " + id);
      });

    // get events from IDs
    var events = eventRepository.findAllById(request.getEventIds());

    // validate if all events are exist or not
    if ( events.size() != request.getEventIds().size() ) {
      log.error("One or more events not found");
      throw new EventNotFoundException("One or more events not found");
    }

    // validate all events are from the same domain
    var eventsAreFromSameDomain = events.stream()
      .allMatch(event -> event.getDomain().equals(request.getDomain()));
  
    if ( !eventsAreFromSameDomain ) {
      log.error("All events must belong to domain: {}", request.getDomain());
      throw new InvalidDomainException("All events must belong to the same domain");
    }

    // update the combo
    combo.setName(request.getName());
    combo.setDescription(request.getDescription());
    combo.setDomain(request.getDomain());
    combo.setEvents(new HashSet<>(events));
    combo.setRegistrationFee(request.getRegistrationFee());
    combo.setRegistrationOpen(request.isRegistrationOpen());
    combo.setUpdatedBy(user);

    // save and return the combo
    var updatedCombo = comboRepository.save(combo);
    log.info("Combo updated: {}", updatedCombo);

    return ComboResponse.fromCombo(updatedCombo);
  }

  @Transactional
  public void deleteCombo(Long id, User user) {
    log.info("Deleting combo with ID: {} by: {}", id, user);

    // check if user has permission
    if (user.getRole().equals(Role.ROLE_USER)) {
      log.error("User with ID {} not authorized to delete combo", user.getId());
      throw new ForbiddenAccessException("User not authorized to delete combo");
    }

    // check if the combo exists
    if ( !comboRepository.existsById(id) ) {
      log.error("Combo not found with ID: {}", id);
      throw new ComboNotFoundException("Combo not found with ID: " + id);
    }

    // delete the combo
    comboRepository.deleteById(id);
    log.info("Combo with ID: {} deleted successfully", id);
  }

  @Transactional
  public ComboResponse toggleComboStatus(Long id, User user) {
    log.info("Toggling status for combo with ID: {} by: {}", id, user);

    // check if user has permission
    if (user.getRole().equals(Role.ROLE_USER)) {
      log.error("User with ID {} not authorized to toggle combo status", user.getId());
      throw new ForbiddenAccessException("User not authorized to toggle combo status");
    }

    // find the combo
    var combo = comboRepository.findById(id)
      .orElseThrow( () -> {
        log.error("Combo not found with ID: {}", id);
        return new ComboNotFoundException("Combo not found with ID: " + id);
      });

    // toggle the status
    combo.setRegistrationOpen(!combo.isRegistrationOpen());
    combo.setUpdatedBy(user);

    // save and return the combo
    var updatedCombo = comboRepository.save(combo);
    log.info("Combo status toggled: {}", updatedCombo);
    
    return ComboResponse.fromCombo(updatedCombo);
  }
}