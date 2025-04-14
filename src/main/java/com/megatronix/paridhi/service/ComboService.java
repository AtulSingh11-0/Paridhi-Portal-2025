package com.megatronix.paridhi.service;

import java.util.HashSet;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.megatronix.paridhi.constant.AppConstant;
import com.megatronix.paridhi.constant.Domain;
import com.megatronix.paridhi.constant.MessageConstant;
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
import com.megatronix.paridhi.util.LoggingUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ComboService {
  private final ComboRepository comboRepository;
  private final EventRepository eventRepository;
  private final CloudinaryService cloudinaryService;
  private static final String COMBO = " combo";
  private static final String COMBO_NOT_FOUND = "Combo not found with ID: ";

  /*
   * Public methods - don't require authentication
   */

  @Cacheable(value = "combos")
  public List<ComboResponse> getAllCombos() {
		// log the operation
    LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.READ,
			AppConstant.COMBO,
			null,
			"Fetching all combos"
    );
    
		// fetch all combos from the repository
    List<EventCombo> combos = comboRepository.findAll();
    
		// log the successful retrieval of combos
    LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.READ,
			AppConstant.COMBO,
			null,
			AppConstant.SUCCESSFULLY_RETRIEVED + combos.size() + " combos"
    );
    
		// return the list of combos as ComboResponse objects
    return combos.stream()
			.map(ComboResponse::fromCombo)
			.toList();
  }

  @Cacheable(value = "combosById", key = "#id")
  public ComboResponse getComboById(Long id) {
    // log the operation
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.READ,
			AppConstant.COMBO,
			null,
			"Fetching combo with ID: " + id
    );
    
		// fetch the combo by ID from the repository
    EventCombo combo = comboRepository.findById(id)
			.orElseThrow(() -> {
				LoggingUtil.logError(
					log,
					MessageConstant.Operation.READ,
					AppConstant.COMBO,
					null,
					COMBO_NOT_FOUND + id,
					null
				);
				return new ComboNotFoundException(COMBO_NOT_FOUND + id);
			});
    
		// log the successful retrieval of the combo
    LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.READ,
			AppConstant.COMBO,
			null,
			"Successfully retrieved combo ID: " + id + " - " + combo.getName()
    );
    
		// return the combo as a ComboResponse object
    return ComboResponse.fromCombo(combo);
  }

  @Cacheable(value = "combosByDomain", key = "#domain.name()")
  public List<ComboResponse> getCombosByDomain(Domain domain) {
    // log the operation
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.READ,
			AppConstant.COMBO,
			null,
			"Fetching combos for domain: " + domain
    );
    
		// fetch combos by domain from the repository
    List<EventCombo> combos = comboRepository.findByDomain(domain);
    
		// log the successful retrieval of combos
    LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.READ,
			AppConstant.COMBO,
			null,
			AppConstant.SUCCESSFULLY_RETRIEVED + combos.size() + " combos for domain: " + domain
    );
    
		// return the list of combos as ComboResponse objects
    return combos.stream()
			.map(ComboResponse::fromCombo)
			.toList();
  }

  @Cacheable(value = "combosByStatus", key = "#isRegistrationOpen")
  public List<ComboResponse> getCombosByStatus(boolean isRegistrationOpen) {
    // log the operation
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.READ,
			AppConstant.COMBO,
			null,
			"Fetching combos by registration status: " + (isRegistrationOpen ? AppConstant.OPEN : AppConstant.CLOSED)
    );
    
		// fetch combos by registration status from the repository
    List<EventCombo> combos = comboRepository.findByIsRegistrationOpen(isRegistrationOpen);
    
		// log the successful retrieval of combos
    LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.READ,
			AppConstant.COMBO,
			null,
			AppConstant.SUCCESSFULLY_RETRIEVED + combos.size() + " combos with registration status: " +
			(isRegistrationOpen ? AppConstant.OPEN : AppConstant.CLOSED)
    );
    
		// return the list of combos as ComboResponse objects
    return combos.stream()
			.map(ComboResponse::fromCombo)
			.toList();
  }

  /*
   * Protected methods - require authentication
   */

  @CacheEvict(
		value = {
			"combos",
			"combosById",
			"combosByDomain",
			"combosByStatus"
		},
		allEntries = true
  )
  @Transactional
  public ComboResponse createCombo(ComboRequest request, User user) {
    // log the operation
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.CREATE,
			AppConstant.COMBO,
			user,
			"Creating combo: " + request.getName()
    );
    
    // validate user access
    checkUserAccess(user, MessageConstant.Operation.CREATE);
    
    // get events from IDs
    var events = eventRepository.findAllById(request.getEventIds());
    
    // validate if all events exist
    if (events.size() != request.getEventIds().size()) {
			LoggingUtil.logError(
				log,
				MessageConstant.Operation.CREATE,
				AppConstant.COMBO,
				user,
				"One or more events not found for combo creation",
				null
			);
			throw new EventNotFoundException("One or more events not found");
    }
    
    // validate all events are from the same domain
    var eventsAreFromSameDomain = events.stream()
			.allMatch(event -> event.getDomain().equals(request.getDomain()));
    
    if (!eventsAreFromSameDomain) {
			LoggingUtil.logError(
				log,
				MessageConstant.Operation.CREATE,
				AppConstant.COMBO,
				user,
				"All events must belong to domain: " + request.getDomain(),
				null
			);
			throw new InvalidDomainException("All events must belong to the same domain");
    }
    
    // create a new combo builder object
    var combo = EventCombo.builder()
			.name(request.getName())
			.description(request.getDescription())
			.domain(request.getDomain())
			.events(new HashSet<>(events))
			.comboPictureSecureUrl("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSxrgoLK49zGt45fybNVJfpDUt4otbtAfmWbg&s")
			.registrationFee(request.getRegistrationFee())
			.createdBy(user)
			.build();
    
		// save the combo to the database
    var savedCombo = comboRepository.save(combo);
    
		// log the successful creation of the combo
    LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.CREATE,
			AppConstant.COMBO,
			user,
			"Successfully created combo ID: " + savedCombo.getId() + " - " + savedCombo.getName()
    );
    
		// return the ComboResponse object
    return ComboResponse.fromCombo(savedCombo);
  }

  @CacheEvict(
		value = {
			"combos",
			"combosById",
			"combosByDomain",
			"combosByStatus"
		},
		allEntries = true
  )
  @Transactional
  public ComboResponse updateCombo(Long id, ComboRequest request, User user) {
    // log the operation
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.UPDATE,
			AppConstant.COMBO,
			user,
			"Updating combo with ID: " + id
    );
    
    // validate user access
    checkUserAccess(user, MessageConstant.Operation.UPDATE);
    
    // find the combo
    var combo = comboRepository.findById(id)
			.orElseThrow(() -> {
				LoggingUtil.logError(
					log,
					MessageConstant.Operation.UPDATE,
					AppConstant.COMBO,
					user,
					COMBO_NOT_FOUND + id,
					null
				);
				return new ComboNotFoundException(COMBO_NOT_FOUND + id);
			});
    
    // get events from IDs
    var events = eventRepository.findAllById(request.getEventIds());
    
    // validate if all events exist
    if (events.size() != request.getEventIds().size()) {
			LoggingUtil.logError(
				log,
				MessageConstant.Operation.UPDATE,
				AppConstant.COMBO,
				user,
				"One or more events not found for combo update",
				null
			);
			throw new EventNotFoundException("One or more events not found");
    }
    
    // validate all events are from the same domain
    var eventsAreFromSameDomain = events.stream()
			.allMatch(event -> event.getDomain().equals(request.getDomain()));
    
    if (!eventsAreFromSameDomain) {
			LoggingUtil.logError(
				log,
				MessageConstant.Operation.UPDATE,
				AppConstant.COMBO,
				user,
				"All events must belong to domain: " + request.getDomain(),
				null
			);
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
    
    LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.UPDATE,
			AppConstant.COMBO,
			user,
			"Successfully updated combo ID: " + id + " - " + updatedCombo.getName()
    );
    
		// return the ComboResponse object
    return ComboResponse.fromCombo(updatedCombo);
  }

  @CacheEvict(
		value = {
			"combos",
			"combosById",
			"combosByDomain",
			"combosByStatus"
		},
		allEntries = true
  )
  @Transactional
  public ComboResponse updateComboImage(Long id, MultipartFile file, User user) {
    // log the operation
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.UPDATE,
			AppConstant.COMBO_IMAGE,
			user,
			"Updating image for combo with ID: " + id + " - File: " + file.getOriginalFilename() + 
			" (" + file.getSize() + " bytes)"
    );
    
    // validate user access
    checkUserAccess(user, "update image of");
    
    // get combo by ID
    var existingCombo = comboRepository.findById(id)
			.orElseThrow(() -> {
				LoggingUtil.logError(
					log,
					MessageConstant.Operation.UPDATE,
					AppConstant.COMBO_IMAGE,
					user,
					COMBO_NOT_FOUND + id,
					null
				);
				return new ComboNotFoundException(COMBO_NOT_FOUND + id);
			});
    
    // delete old image if it exists
    if (existingCombo.getComboPicturePublicId() != null) {
			try {
				LoggingUtil.logOperation(
					log,
					MessageConstant.Operation.DELETE,
					AppConstant.CLOUDINARY_IMAGE,
					user,
					"Removing previous image with public ID: " + existingCombo.getComboPicturePublicId()
				);
				cloudinaryService.deleteFile(existingCombo.getComboPicturePublicId());
			} catch (Exception e) {
				// log but continue - failure to delete old image shouldn't stop upload of new image
				LoggingUtil.logError(
					log,
					MessageConstant.Operation.DELETE,
					AppConstant.CLOUDINARY_IMAGE,
					user,
					"Failed to delete previous image for combo ID: " + id + " - Continuing with upload - Error: " + e.getMessage(),
					e
				);
			}
    }
    
    // upload new image to Cloudinary
    LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.CREATE,
			AppConstant.CLOUDINARY_IMAGE,
			user,
			"Uploading new image for combo ID: " + id
    );
    
    var imageDetails = cloudinaryService.uploadFile(file);
    
    // update combo image fields
    existingCombo.setComboPictureSecureUrl(imageDetails.get("secure_url"));
    existingCombo.setComboPicturePublicId(imageDetails.get("public_id"));
    existingCombo.setUpdatedBy(user);
    
    // save updated combo
    var updatedCombo = comboRepository.save(existingCombo);
    
		// log the successful image update
    LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.UPDATE,
			AppConstant.COMBO_IMAGE,
			user,
			"Successfully updated image for combo ID: " + id + 
			" - New image public ID: " + imageDetails.get("public_id")
    );
    
		// return the updated combo as a ComboResponse object
    return ComboResponse.fromCombo(updatedCombo);
  }

  @CacheEvict(
		value = {
			"combos",
			"combosById",
			"combosByDomain",
			"combosByStatus"
		},
		allEntries = true
  )
  @Transactional
  public void deleteCombo(Long id, User user) {
    // log the operation
		LoggingUtil.logOperation(
        log,
        MessageConstant.Operation.DELETE,
        AppConstant.COMBO,
        user,
        "Deleting combo with ID: " + id
    );
    
    // validate user access
    checkUserAccess(user, MessageConstant.Operation.DELETE);
    
    // find the combo
    var existingCombo = comboRepository.findById(id)
		.orElseThrow(() -> {
				LoggingUtil.logError(
					log,
					MessageConstant.Operation.DELETE,
					AppConstant.COMBO,
					user,
					COMBO_NOT_FOUND + id,
					null
				);
				return new ComboNotFoundException(COMBO_NOT_FOUND + id);
			});
    
    // delete image from Cloudinary if it exists
    if (existingCombo.getComboPicturePublicId() != null) {
			try {
				LoggingUtil.logOperation(
					log,
					MessageConstant.Operation.DELETE,
					AppConstant.CLOUDINARY_IMAGE,
					user,
					"Deleting image with public ID: " + existingCombo.getComboPicturePublicId()
				);
				cloudinaryService.deleteFile(existingCombo.getComboPicturePublicId());
			} catch (Exception e) {
				// log but continue - failure to delete image shouldn't stop combo deletion
				LoggingUtil.logError(
					log,
					MessageConstant.Operation.DELETE,
					AppConstant.CLOUDINARY_IMAGE,
					user,
					"Failed to delete image for combo ID: " + id + " - Error: " + e.getMessage(),
					e
				);
			}
    }
    
    // delete the combo
    comboRepository.deleteById(id);
    
		// log the successful deletion of the combo
    LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.DELETE,
			AppConstant.COMBO,
			user,
			"Successfully deleted combo ID: " + id + " - " + existingCombo.getName()
    );
  }

  @CacheEvict(
		value = {
			"combos",
			"combosById",
			"combosByDomain",
			"combosByStatus"
		},
		allEntries = true
  )
  @Transactional
  public ComboResponse toggleComboStatus(Long id, User user) {
    // log the operation
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.UPDATE,
			AppConstant.COMBO,
			user,
			"Toggling registration status for combo with ID: " + id
    );
    
    // validate user access
    checkUserAccess(user, "toggle registration status of");
    
    // find the combo
    var combo = comboRepository.findById(id)
			.orElseThrow(() -> {
				LoggingUtil.logError(
					log,
					MessageConstant.Operation.UPDATE,
					AppConstant.COMBO,
					user,
					COMBO_NOT_FOUND + id,
					null
				);
				return new ComboNotFoundException(COMBO_NOT_FOUND + id);
			});
    
    // get current status for logging
    boolean currentStatus = combo.isRegistrationOpen();
    boolean newStatus = !currentStatus;
    
    // toggle the status
    combo.setRegistrationOpen(newStatus);
    combo.setUpdatedBy(user);
    
    // save and return the combo
    var updatedCombo = comboRepository.save(combo);
    
		// log the successful status update
    LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.UPDATE,
			AppConstant.COMBO,
			user,
			"Successfully updated registration status for combo ID: " + id + 
			" - Name: " + updatedCombo.getName() + 
			" - Status: " + (currentStatus ? "OPEN -> CLOSED" : "CLOSED -> OPEN")
    );
    
    return ComboResponse.fromCombo(updatedCombo);
  }

  /*
   * Private methods - used internally only
   */

  private void checkUserAccess(User user, String methodType) {
    // check if user is null
		if (user == null) {
			LoggingUtil.logSecurity(
				log,
				AppConstant.ACCESS_DENIED,
				null,
				AppConstant.FAILED,
				"Authentication required to " + methodType + COMBO
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
				"User lacks permission to " + methodType + COMBO
			);
			throw new ForbiddenAccessException(MessageConstant.UserMessage.ACCESS_DENIED);
    }
    
		// log the successful authorization
    LoggingUtil.logSecurity(
			log,
			AppConstant.ACCESS_GRANTED,
			user,
			AppConstant.SUCCESS,
			"User authorized to " + methodType + COMBO
    );
  }
}