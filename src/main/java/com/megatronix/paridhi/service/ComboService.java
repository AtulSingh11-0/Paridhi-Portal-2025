package com.megatronix.paridhi.service;

import java.util.HashSet;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.megatronix.paridhi.constant.Domain;
import com.megatronix.paridhi.constant.Role;
import com.megatronix.paridhi.dto.request.ComboRequest;
import com.megatronix.paridhi.dto.response.ComboResponse;
import com.megatronix.paridhi.exception.ComboNotFoundException;
import com.megatronix.paridhi.exception.EventNotFoundException;
import com.megatronix.paridhi.exception.FileUploadException;
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
	private final CloudinaryService cloudinaryService;

  @Transactional
  public ComboResponse createCombo(ComboRequest request, User user) {
    log.info("Creating combo: {}", request);

    // check if user has permission to create combo
    checkUserAccess(user, "create");

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
			.comboPictureSecureUrl("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSxrgoLK49zGt45fybNVJfpDUt4otbtAfmWbg&s")
      .registrationFee(request.getRegistrationFee())
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
    checkUserAccess(user, "update");

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
    combo.setUpdatedBy(user);

    // save and return the combo
    var updatedCombo = comboRepository.save(combo);
    log.info("Combo updated: {}", updatedCombo);

    return ComboResponse.fromCombo(updatedCombo);
  }

	@Transactional
	public ComboResponse updateComboImage(Long id, MultipartFile file, User user) {
		log.info("Updating combo image for combo with ID: {}, by: {}", id, user.getEmail());

		// Check access permissions
		checkUserAccess(user, "update image of");

		// Get combo by ID
		var existingCombo = comboRepository.findById(id)
			.orElseThrow(() -> {
				log.error("Combo with ID {} not found", id);
				return new ComboNotFoundException("Combo not found with ID: " + id);
			});
		
		try {
			// Delete old image if it exists
			if (existingCombo.getComboPicturePublicId() != null) {
				cloudinaryService.deleteFile(existingCombo.getComboPicturePublicId());
			}

			// Upload new image to Cloudinary
			var imageDetails = cloudinaryService.uploadFile(file);
			
			// Update combo image fields
			existingCombo.setComboPictureSecureUrl(imageDetails.get("secure_url"));
			existingCombo.setComboPicturePublicId(imageDetails.get("public_id"));
			existingCombo.setUpdatedBy(user);
			
			// Save updated combo
			var updatedCombo = comboRepository.save(existingCombo);
			log.info("Combo image updated successfully for ID: {}", updatedCombo.getId());
			
			return ComboResponse.fromCombo(updatedCombo);
		} catch (Exception e) {
			log.error("Error updating combo image: {}", e.getMessage());
			throw new FileUploadException("Error updating combo image: " + e.getMessage(), e.getCause());
		}
	}

  @Transactional
  public void deleteCombo(Long id, User user) {
    log.info("Deleting combo with ID: {} by: {}", id, user);

    // check if user has permission
    checkUserAccess(user, "delete");

    // check if the combo exists
    var existingCombo = comboRepository.findById(id)
			.orElseThrow( () -> {
				log.error("Combo not found with ID: {}", id);
				return new ComboNotFoundException("Combo not found with ID: " + id);
			});

		// Delete image from Cloudinary if it exists
    if (existingCombo.getComboPicturePublicId() != null) {
			cloudinaryService.deleteFile(existingCombo.getComboPicturePublicId());
		}

    // delete the combo
    comboRepository.deleteById(id);
    log.info("Combo with ID: {} deleted successfully", id);
  }

  @Transactional
  public ComboResponse toggleComboStatus(Long id, User user) {
    log.info("Toggling status for combo with ID: {} by: {}", id, user);

    // check if user has permission
    checkUserAccess(user, "toggle status of");

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

	private void checkUserAccess(User user, String action) {
		if (user == null) {
			log.error("Authentication required to {} the combo", action);
			throw new ForbiddenAccessException("Authentication required to " + action + " the combo");
		}

		if (user.getRole().equals(Role.ROLE_USER)) {
			log.error("User with ID {} not authorized to {} combo", user.getId(), action);
			throw new ForbiddenAccessException("User not authorized to " + action + " combo");
		}
	}
}