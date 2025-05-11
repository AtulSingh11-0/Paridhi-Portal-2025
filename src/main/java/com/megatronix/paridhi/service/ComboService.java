package com.megatronix.paridhi.service;

import java.util.HashSet;
import java.util.List;

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
import com.megatronix.paridhi.model.Event;
import com.megatronix.paridhi.model.EventCombo;
import com.megatronix.paridhi.model.User;
import com.megatronix.paridhi.repository.ComboRepository;
import com.megatronix.paridhi.repository.EventRepository;
import com.megatronix.paridhi.util.LoggingUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing event combos including CRUD operations and status
 * management
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ComboService {
	private final ComboRepository comboRepository;
	private final EventRepository eventRepository;
	private final CloudinaryService cloudinaryService;

	/*
	 * Public methods - don't require authentication
	 */

	/**
	 * Retrieves all event combos from the database
	 *
	 * @return List of ComboResponse objects containing all combos
	 */
	public List<ComboResponse> getAllCombos() {
		// log the operation
		LoggingUtil.logOperation(
			log, 
			MessageConstant.Operation.READ, 
			AppConstant.COMBO_SERVICE, 
			null,
			"Fetching all combos"
		);

		// fetch all combos from the repository
		List<EventCombo> combos = comboRepository.findAllWithEvents();

		// log the successful retrieval of combos
		LoggingUtil.logOperation(
			log, 
			MessageConstant.Operation.READ, 
			AppConstant.COMBO_SERVICE, 
			null,
			String.format(MessageConstant.SuccessTemplate.FETCHED_COUNT, combos.size(), "combos")
		);

		// return the list of combos as ComboResponse objects
		return combos.stream()
			.map(ComboResponse::fromCombo)
			.toList();
	}

	/**
	 * Retrieves a specific combo by its ID
	 *
	 * @param id The ID of the combo to retrieve
	 * @return ComboResponse object containing the combo details
	 * @throws ComboNotFoundException if combo with given ID doesn't exist
	 */
	public ComboResponse getComboById(Long id) {
		// log the operation
		LoggingUtil.logOperation(
			log, 
			MessageConstant.Operation.READ, 
			AppConstant.COMBO_SERVICE, 
			null,
			"Fetching combo with ID: " + id
		);

		// find the combo
		var combo = fetchComboById(id, null);

		// log the successful retrieval of the combo
		LoggingUtil.logOperation(
			log, 
			MessageConstant.Operation.READ, 
			AppConstant.COMBO_SERVICE, 
			null,
			String.format(MessageConstant.SuccessTemplate.FETCHED, "combo with ID: " + id + " - " + combo.getName())
		);

		// return the combo as a ComboResponse object
		return ComboResponse.fromCombo(combo);
	}

	/**
	 * Retrieves all combos for a specific domain
	 *
	 * @param domain The domain to filter combos by
	 * @return List of ComboResponse objects for the specified domain
	 */
	public List<ComboResponse> getCombosByDomain(Domain domain) {
		// log the operation
		LoggingUtil.logOperation(
			log, 
			MessageConstant.Operation.READ, 
			AppConstant.COMBO_SERVICE, 
			null,
			"Fetching combos for domain: " + domain
		);

		// fetch combos by domain from the repository
		List<EventCombo> combos = comboRepository.findByDomainWithEvents(domain);

		// log the successful retrieval of combos
		LoggingUtil.logOperation(
			log, 
			MessageConstant.Operation.READ, 
			AppConstant.COMBO_SERVICE, 
			null,
			String.format(MessageConstant.SuccessTemplate.FETCHED_COUNT, combos.size(), "combos for domain: " + domain)
		);

		// return the list of combos as ComboResponse objects
		return combos.stream()
			.map(ComboResponse::fromCombo)
			.toList();
	}

	/**
	 * Retrieves combos filtered by their registration status
	 *
	 * @param isRegistrationOpen true to get combos with open registration, false
	 *                           for closed registration
	 * @return List of ComboResponse objects with the specified registration status
	 */
	public List<ComboResponse> getCombosByStatus(boolean isRegistrationOpen) {
		// log the operation
		LoggingUtil.logOperation(
			log, 
			MessageConstant.Operation.READ, 
			AppConstant.COMBO_SERVICE, 
			null,
			"Fetching combos by registration status: " + (isRegistrationOpen ? AppConstant.OPEN : AppConstant.CLOSED)
		);

		// fetch combos by registration status from the repository
		List<EventCombo> combos = comboRepository.findByIsRegistrationOpenWithEvents(isRegistrationOpen);

		// log the successful retrieval of combos
		LoggingUtil.logOperation(
			log, 
			MessageConstant.Operation.READ, 
			AppConstant.COMBO_SERVICE, 
			null,
			String.format(MessageConstant.SuccessTemplate.FETCHED_COUNT, combos.size(),"combos with registration status: " + (isRegistrationOpen ? AppConstant.OPEN : AppConstant.CLOSED))
		);

		// return the list of combos as ComboResponse objects
		return combos.stream()
			.map(ComboResponse::fromCombo)
			.toList();
	}

	/*
	 * Protected methods - require authentication
	 */

	/**
	 * Creates a new event combo
	 * 
	 * @param request The combo creation request containing all necessary details
	 * @param user    The authenticated user creating the combo
	 * @return ComboResponse object for the newly created combo
	 * @throws EventNotFoundException   if any event in the request doesn't exist
	 * @throws InvalidDomainException   if events are from different domains
	 * @throws ForbiddenAccessException if user lacks permission
	 */
	@Transactional
	public ComboResponse createCombo(ComboRequest request, User user) {
		// log the operation
		LoggingUtil.logOperation(
			log, 
			MessageConstant.Operation.CREATE,
			AppConstant.COMBO_SERVICE, 
			user,
			"Creating combo: " + request.getName()
		);

		// validate user access
		checkUserAccess(user, MessageConstant.Operation.CREATE);

		// validate combo request
		validateComboRequest(request);

		// get events from IDs
		var events = eventRepository.findAllById(request.getEventIds());

		// validate if all events exist and are from the same domain
		validateEvents(request, events, request.getDomain(), user, MessageConstant.Operation.CREATE);

		// create a new combo builder object
		var combo = EventCombo.builder()
			.name(request.getName())
			.description(request.getDescription())
			.domain(request.getDomain())
			.events(new HashSet<>(events))
			.comboPictureSecureUrl(AppConstant.DEFAULT_EVENT_IMAGE_SECURE_URL)
			.comboPicturePublicId(AppConstant.DEFAULT_EVENT_IMAGE_PUBLIC_ID)
			.registrationFee(request.getRegistrationFee())
			.createdBy(user)
			.build();

		// save the combo to the database
		var savedCombo = comboRepository.save(combo);

		// log the successful creation of the combo
		LoggingUtil.logOperation(
			log, 
			MessageConstant.Operation.CREATE, 
			AppConstant.COMBO_SERVICE, 
			user,
			String.format(MessageConstant.SuccessTemplate.CREATED, AppConstant.COMBO, savedCombo.getId()) + " - " + savedCombo.getName()
		);

		// return the ComboResponse object
		return ComboResponse.fromCombo(savedCombo);
	}

	/**
	 * Updates an existing combo with new information
	 * 
	 * @param id      ID of the combo to update
	 * @param request The combo update request containing all necessary details
	 * @param user    The authenticated user updating the combo
	 * @return ComboResponse object for the updated combo
	 * @throws ComboNotFoundException   if combo with given ID doesn't exist
	 * @throws EventNotFoundException   if any event in the request doesn't exist
	 * @throws InvalidDomainException   if events are from different domains
	 * @throws ForbiddenAccessException if user lacks permission
	 */
	@Transactional
	public ComboResponse updateCombo(Long id, ComboRequest request, User user) {
		// log the operation
		LoggingUtil.logOperation(
			log, 
			MessageConstant.Operation.UPDATE, 
			AppConstant.COMBO_SERVICE, 
			user,
			"Updating combo with ID: " + id
		);

		// validate user access
		checkUserAccess(user, MessageConstant.Operation.UPDATE);

		// validate combo request
		validateComboRequest(request);

		// find the combo
		var combo = fetchComboById(id, user);

		// get events from IDs
		var events = eventRepository.findAllById(request.getEventIds());

		// validate if all events exist
		validateEvents(request, events, request.getDomain(), user, MessageConstant.Operation.UPDATE);

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
			AppConstant.COMBO_SERVICE, 
			user,
			String.format(MessageConstant.SuccessTemplate.UPDATED, AppConstant.COMBO, id) + " - " + updatedCombo.getName()
		);

		// return the ComboResponse object
		return ComboResponse.fromCombo(updatedCombo);
	}

	/**
	 * Updates the image associated with a combo
	 * 
	 * @param id   ID of the combo to update
	 * @param file The image file to upload
	 * @param user The authenticated user updating the combo image
	 * @return ComboResponse object for the updated combo
	 * @throws ComboNotFoundException   if combo with given ID doesn't exist
	 * @throws ForbiddenAccessException if user lacks permission
	 */
	@Transactional
	public ComboResponse updateComboImage(Long id, MultipartFile file, User user) {
		// log the operation
		LoggingUtil.logOperation(
			log, 
			MessageConstant.Operation.UPDATE, 
			AppConstant.COMBO_IMAGE, 
			user,
			"Updating image for combo with ID: " + id + " - File: " + file.getOriginalFilename() + " (" + file.getSize() + " bytes)"
		);

		// validate user access
		checkUserAccess(user, "update image of");

		// get combo by ID
		var existingCombo = fetchComboById(id, user);

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

		var imageDetails = cloudinaryService.uploadFile(file, AppConstant.COMBO);

		// update combo image fields
		existingCombo.setComboPictureSecureUrl(imageDetails.get(AppConstant.SECURE_URL));
		existingCombo.setComboPicturePublicId(imageDetails.get(AppConstant.PUBLIC_ID));
		existingCombo.setUpdatedBy(user);

		// save updated combo
		var updatedCombo = comboRepository.save(existingCombo);

		// log the successful image update
		LoggingUtil.logOperation(
			log, 
			MessageConstant.Operation.UPDATE, 
			AppConstant.COMBO_IMAGE, 
			user,
			"Successfully updated image for combo ID: " + id + " - New image public ID: " + imageDetails.get(AppConstant.PUBLIC_ID)
		);

		// return the updated combo as a ComboResponse object
		return ComboResponse.fromCombo(updatedCombo);
	}

	/**
	 * Deletes a combo and its associated image
	 *
	 * @param id   ID of the combo to delete
	 * @param user The authenticated user deleting the combo
	 * @throws ComboNotFoundException   if combo with given ID doesn't exist
	 * @throws ForbiddenAccessException if user lacks permission
	 */
	@Transactional
	public void deleteCombo(Long id, User user) {
		// log the operation
		LoggingUtil.logOperation(
			log, 
			MessageConstant.Operation.DELETE, 
			AppConstant.COMBO_SERVICE, 
			user,
			"Deleting combo with ID: " + id
		);

		// validate user access
		checkUserAccess(user, MessageConstant.Operation.DELETE);

		// find the combo
		var existingCombo = fetchComboById(id, user);

		// delete image from Cloudinary if it exists
		if (existingCombo.getComboPicturePublicId() != null) {
			try {
				LoggingUtil.logOperation(
					log, 
					MessageConstant.Operation.DELETE, 
					AppConstant.CLOUDINARY_SERVICE, 
					user,
					"Deleting image with public ID: " + existingCombo.getComboPicturePublicId()
				);
				cloudinaryService.deleteFile(existingCombo.getComboPicturePublicId());
			} catch (Exception e) {
				// log but continue - failure to delete image shouldn't stop combo deletion
				LoggingUtil.logError(
					log, 
					MessageConstant.Operation.DELETE, 
					AppConstant.CLOUDINARY_SERVICE, 
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
			AppConstant.COMBO_SERVICE, 
			user,
			String.format(MessageConstant.SuccessTemplate.DELETED, AppConstant.COMBO, id) + " - " + existingCombo.getName()
		);
	}

	/**
	 * Toggles the registration status of a combo between open and closed
	 *
	 * @param id   ID of the combo to update
	 * @param user The authenticated user toggling the status
	 * @return ComboResponse object for the updated combo
	 * @throws ComboNotFoundException   if combo with given ID doesn't exist
	 * @throws ForbiddenAccessException if user lacks permission
	 */
	@Transactional
	public ComboResponse toggleComboStatus(Long id, User user) {
		// log the operation
		LoggingUtil.logOperation(
			log, 
			MessageConstant.Operation.UPDATE, 
			AppConstant.COMBO_SERVICE, 
			user,
			"Toggling registration status for combo with ID: " + id
		);

		// validate user access
		checkUserAccess(user, "toggle registration status of");

		// find the combo
		var combo = fetchComboById(id, user);

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
			AppConstant.COMBO_SERVICE, 
			user,
			String.format(MessageConstant.SuccessTemplate.STATUS_CHANGED, "registration status for combo ID: " + id, ( currentStatus ? AppConstant.OPEN : AppConstant.CLOSED ), ( newStatus ? AppConstant.OPEN : AppConstant.CLOSED )) + " - Name: " + updatedCombo.getName()
		);

		// return the updated combo as a ComboResponse object
		return ComboResponse.fromCombo(updatedCombo);
	}

	/*
	 * Private methods - used internally only
	 */

	/**
	 * Validates that the user has appropriate permissions to perform operations on
	 * combos
	 *
	 * @param user       The user attempting the operation
	 * @param methodType The operation type being performed
	 * @throws ForbiddenAccessException if the user lacks appropriate permissions
	 */
	private void checkUserAccess(User user, String methodType) {
		// check if user is null
		if (user == null) {
			LoggingUtil.logSecurity(
				log, 
				AppConstant.ACCESS_DENIED, 
				null, 
				AppConstant.FAILED,
				String.format(MessageConstant.ErrorTemplate.AUTHENTICATION_REQUIRED, methodType + " " + AppConstant.COMBO)
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
				String.format(MessageConstant.ErrorTemplate.NOT_AUTHORIZED, methodType + " " + AppConstant.COMBO)
			);
			throw new ForbiddenAccessException(MessageConstant.UserMessage.ACCESS_DENIED);
		}

		// log the successful authorization
		LoggingUtil.logSecurity(
			log, 
			AppConstant.ACCESS_GRANTED, 
			user, 
			AppConstant.SUCCESS,
			String.format(MessageConstant.SuccessTemplate.AUTHORIZED, methodType + " " + AppConstant.COMBO)
		);
	}

	/**
	 * Validates the combo creation request for required fields and constraints
	 *
	 * @param request The combo creation request to validate
	 * @throws IllegalArgumentException if any validation fails
	 */
	private void validateComboRequest(ComboRequest request) {
		// validate combo name
		if (request.getName() == null || request.getName().trim().isEmpty()) {
			throw new IllegalArgumentException("Combo name cannot be null or empty");
		}

		// validate combo description
		if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
			throw new IllegalArgumentException("Combo description cannot be null or empty");
		}

		// validate event IDs
		if (request.getEventIds() == null || request.getEventIds().isEmpty() || request.getEventIds().size() < 2) {
			throw new IllegalArgumentException("Event IDs cannot be null, empty or less than 2");
		}

		// validate domain
		if (request.getDomain() == null || request.getDomain().toString().isEmpty()) {
			throw new IllegalArgumentException("Domain cannot be null or empty");
		}

		// validate registration fee
		if (request.getRegistrationFee() < 0) {
			throw new IllegalArgumentException("Registration fee cannot be negative");
		}		
	}
	
	/**
	 * Validates that all events in the request exist and belong to the same domain
	 *
	 * @param request 	The combo creation request containing event IDs
	 * @param events  	List of events fetched from the database
	 * @param domain  	The domain to check against
	 * @param user    	The authenticated user creating the combo
	 * @param operation The operation being performed (create/update)
	 * @throws EventNotFoundException   if any event doesn't exist
	 * @throws InvalidDomainException   if events are from different domains
	 */
	private void validateEvents(ComboRequest request, List<Event> events, Domain domain, User user, String operation) {
		// check if all events exist
		if (events.size() != request.getEventIds().size()) {
			LoggingUtil.logError(
				log, 
				MessageConstant.Operation.CREATE, 
				AppConstant.COMBO_SERVICE, 
				user,
				"One or more events not found for combo creation", null
			);
			throw new EventNotFoundException(String.format(MessageConstant.ErrorTemplate.NOT_FOUND, AppConstant.EVENT));
		}

		// check if events are null or empty
		if (events == null || events.isEmpty()) {
			LoggingUtil.logError(
				log, 
				operation, 
				AppConstant.COMBO_SERVICE, 
				user, 
				"No events found for combo", 
				null
			);
			throw new EventNotFoundException(String.format(MessageConstant.ErrorTemplate.NOT_FOUND, AppConstant.EVENT));
		}

		// check if all events belong to the same domain
		boolean eventsAreFromSameDomain = events.stream()
			.allMatch(event -> event.getDomain().equals(domain));

		if (!eventsAreFromSameDomain) {
			LoggingUtil.logError(
				log, 
				operation, 
				AppConstant.COMBO_SERVICE, 
				user, 
				"All events must belong to the same domain: " + domain, 
				null
			);
			throw new InvalidDomainException(String.format(MessageConstant.ErrorTemplate.INVALID_DOMAIN, domain));
		}
	}

	/**
	 * Fetches a combo by its ID from the database
	 * 
	 * @param id		The ID of the combo to fetch
	 * @param user	The authenticated user requesting the combo
	 * @return EventCombo		object containing the combo details
	 * @throws ComboNotFoundException 	if combo with given ID doesn't exist
	 */
	private EventCombo fetchComboById(Long id, User user) {
		return comboRepository.findById(id)
			.orElseThrow(() -> {
				LoggingUtil.logError(
					log, 
					MessageConstant.Operation.READ, 
					AppConstant.COMBO_SERVICE, 
					user,
					String.format(MessageConstant.ErrorTemplate.NOT_FOUND, AppConstant.COMBO, id), 
					null
				);
				return new ComboNotFoundException(String.format(MessageConstant.ErrorTemplate.NOT_FOUND, AppConstant.COMBO));
			});
	}

}