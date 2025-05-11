package com.megatronix.paridhi.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.megatronix.paridhi.constant.AppConstant;
import com.megatronix.paridhi.constant.Domain;
import com.megatronix.paridhi.constant.MessageConstant;
import com.megatronix.paridhi.constant.Role;
import com.megatronix.paridhi.dto.response.DomainPosterResponse;
import com.megatronix.paridhi.exception.DomainPosterNotFoundException;
import com.megatronix.paridhi.exception.ForbiddenAccessException;
import com.megatronix.paridhi.model.DomainPoster;
import com.megatronix.paridhi.model.User;
import com.megatronix.paridhi.repository.DomainPosterRepository;
import com.megatronix.paridhi.util.LoggingUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing domain posters including creation, retrieval, update and
 * deletion
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DomainPosterService {
	private final CloudinaryService cloudinaryService;
	private final DomainPosterRepository domainPosterRepository;

	/*
	 * public methods - doesn't require authentication
	 */

	/**
	 * Retrieves a domain poster for a specific domain
	 *
	 * @param domain The domain to fetch a poster for
	 * @return DomainPosterResponse containing the poster details
	 * @throws DomainPosterNotFoundException if no poster exists for the domain
	 */
	public DomainPosterResponse getDomainPosterByDomain(Domain domain) {
		// log the operation
		LoggingUtil.logOperation(
			log, 
			MessageConstant.Operation.READ, 
			AppConstant.DOMAIN_POSTER_SERVICE, 
			null,
			"Fetching domain poster for domain: " + domain
		);

		// check if domain poster exists
		DomainPoster poster = domainPosterRepository.findByDomainName(domain)
			.orElseThrow(() -> {
				LoggingUtil.logError(
					log, 
					MessageConstant.Operation.READ, 
					AppConstant.DOMAIN_POSTER_SERVICE, 
					null,
					String.format(MessageConstant.ErrorTemplate.NOT_FOUND, AppConstant.DOMAIN_POSTER, domain), 
					null
				);
				return new DomainPosterNotFoundException(String.format(MessageConstant.ErrorTemplate.NOT_FOUND, AppConstant.DOMAIN_POSTER, domain));
			});

		// log the successful retrieval of domain poster
		LoggingUtil.logOperation(
			log, 
			MessageConstant.Operation.READ, 
			AppConstant.DOMAIN_POSTER_SERVICE, 
			null,
			String.format(MessageConstant.SuccessTemplate.FETCHED, "domain poster for domain: " + domain)
		);

		// return the DomainPosterResponse object
		return DomainPosterResponse.fromDomainPoster(poster);
	}

	/**
	 * Retrieves all domain posters
	 *
	 * @return List of DomainPosterResponse objects
	 * @throws DomainPosterNotFoundException if no posters exist
	 */
	public List<DomainPosterResponse> getAllDomainPosters() {
		// log the operation
		LoggingUtil.logOperation(
			log, 
			MessageConstant.Operation.READ, 
			AppConstant.DOMAIN_POSTER_SERVICE, 
			null,
			"Fetching all domain posters"
		);

		// check if domain posters exist
		var existingDomainPosters = domainPosterRepository.findAll();
		if (existingDomainPosters.isEmpty()) {
			LoggingUtil.logError(
				log, 
				MessageConstant.Operation.READ, 
				AppConstant.DOMAIN_POSTER_SERVICE, 
				null,
				"No domain posters found", null
			);
			throw new DomainPosterNotFoundException("No domain posters found");
		}

		// log the successful retrieval of domain posters
		LoggingUtil.logOperation(
			log, 
			MessageConstant.Operation.READ, 
			AppConstant.DOMAIN_POSTER_SERVICE, 
			null,
			String.format(MessageConstant.SuccessTemplate.FETCHED_COUNT, existingDomainPosters.size(), "domain posters")
		);

		// return the list of DomainPosterResponse objects
		return existingDomainPosters.stream()
			.map(DomainPosterResponse::fromDomainPoster)
			.toList();
	}

	/*
	 * protected methods - requires authentication
	 */

	/**
	 * Creates a new domain poster
	 * 
	 * @param domainName   The domain for which to create a poster
	 * @param domainPoster The image file to upload
	 * @param user         The authenticated user creating the poster
	 * @return DomainPosterResponse containing the created poster details
	 * @throws ForbiddenAccessException if user lacks permission
	 */
	@Transactional
	public DomainPosterResponse saveDomainPoster(Domain domainName, MultipartFile domainPoster, User user) {
		// log the operation
		LoggingUtil.logOperation(
			log, 
			MessageConstant.Operation.CREATE, 
			AppConstant.DOMAIN_POSTER_SERVICE, 
			user,
			"Creating domain poster for domain: " + domainName
		);

		// validate user access
		checkUserAccess(user, MessageConstant.Operation.CREATE);

		// check if poster already exists for the domain
		var existingPoster = domainPosterRepository.findByDomainName(domainName);

		if (existingPoster.isPresent()) {
			LoggingUtil.logOperation(
				log, 
				MessageConstant.Operation.UPDATE, 
				AppConstant.DOMAIN_POSTER_SERVICE, 
				user,
				"Found existing poster for domain: " + domainName + ", updating instead of creating new one"
			);
			return updateDomainPoster(existingPoster.get().getId(), domainName, domainPoster, user);
		}

		// upload domain poster to Cloudinary
		var imageDetails = cloudinaryService.uploadFile(domainPoster, AppConstant.DOMAIN_POSTER);

		// create a new domain poster object and save it
		var domainPosterObject = DomainPoster.builder()
			.domainName(domainName)
			.posterSecureUrl(imageDetails.get(AppConstant.SECURE_URL))
			.posterPublicId(imageDetails.get(AppConstant.PUBLIC_ID))
			.createdBy(user)
			.updatedBy(user)
			.build();
		
		var savedDomainPoster = domainPosterRepository.save(domainPosterObject);

		// log the successful save operation
		LoggingUtil.logOperation(
			log, 
			MessageConstant.Operation.CREATE, 
			AppConstant.DOMAIN_POSTER_SERVICE, 
			user,
			String.format(MessageConstant.SuccessTemplate.CREATED, AppConstant.DOMAIN_POSTER, savedDomainPoster.getId()) + AppConstant.FOR_DOMAIN + domainName
		);

		// return the DomainPosterResponse object
		return DomainPosterResponse.fromDomainPoster(savedDomainPoster);
	}

	/**
	 * Updates an existing domain poster
	 * 
	 * @param id           The ID of the poster to update
	 * @param domain       The domain to associate with the poster
	 * @param domainPoster The new image file
	 * @param user         The authenticated user updating the poster
	 * @return DomainPosterResponse containing the updated poster details
	 * @throws DomainPosterNotFoundException if poster with given ID doesn't exist
	 * @throws ForbiddenAccessException      if user lacks permission
	 */
	@Transactional
	public DomainPosterResponse updateDomainPoster(Long id, Domain domain, MultipartFile domainPoster, User user) {
		// log the operation
		LoggingUtil.logOperation(
			log, 
			MessageConstant.Operation.UPDATE, 
			AppConstant.DOMAIN_POSTER_SERVICE, 
			user,
			"Updating domain poster with ID: " + id + AppConstant.FOR_DOMAIN + domain
		);

		// validate user access
		checkUserAccess(user, MessageConstant.Operation.UPDATE);

		// find existing poster
		var existingPoster = fetchDomainPosterById(id, user);

		// delete old poster from Cloudinary
		if (existingPoster.getPosterPublicId() != null) {
			try {
				LoggingUtil.logOperation(
					log, 
					MessageConstant.Operation.DELETE, 
					AppConstant.CLOUDINARY_SERVICE, 
					user,
					"Removing previous image with public ID: " + existingPoster.getPosterPublicId()
				);
				cloudinaryService.deleteFile(existingPoster.getPosterPublicId());
			} catch (Exception e) {
				LoggingUtil.logError(
					log, 
					MessageConstant.Operation.DELETE, 
					AppConstant.CLOUDINARY_SERVICE, 
					user,
					"Failed to delete previous image for poster ID: " + id + " - Continuing with upload", 
					e
				);
				// continue with upload even if deletion fails
			}
		}

		// upload new domain poster to Cloudinary
		var imageDetails = cloudinaryService.uploadFile(domainPoster, AppConstant.DOMAIN_POSTER);

		// update domain poster object and save it
		existingPoster.setDomainName(domain);
		existingPoster.setPosterSecureUrl(imageDetails.get(AppConstant.SECURE_URL));
		existingPoster.setPosterPublicId(imageDetails.get(AppConstant.PUBLIC_ID));
		existingPoster.setUpdatedBy(user);
		var updatedDomainPoster = domainPosterRepository.save(existingPoster);

		// log the successful update operation
		LoggingUtil.logOperation(
			log, 
			MessageConstant.Operation.UPDATE, 
			AppConstant.DOMAIN_POSTER_SERVICE, 
			user,
			String.format(MessageConstant.SuccessTemplate.UPDATED, AppConstant.DOMAIN_POSTER, id) + AppConstant.FOR_DOMAIN + domain
		);

		// return the DomainPosterResponse object
		return DomainPosterResponse.fromDomainPoster(updatedDomainPoster);
	}

	/**
	 * Deletes a domain poster
	 * 
	 * @param id   The ID of the poster to delete
	 * @param user The authenticated user deleting the poster
	 * @throws DomainPosterNotFoundException if poster with given ID doesn't exist
	 * @throws ForbiddenAccessException      if user lacks permission
	 */
	@Transactional
	public void deleteDomainPoster(Long id, User user) {
		// log the operation
		LoggingUtil.logOperation(
			log, 
			MessageConstant.Operation.DELETE, 
			AppConstant.DOMAIN_POSTER_SERVICE, 
			user,
			"Deleting domain poster with ID: " + id
		);

		// validate user access
		checkUserAccess(user, MessageConstant.Operation.DELETE);

		// find existing poster
		var existingPoster = fetchDomainPosterById(id, user);

		// delete poster from Cloudinary
		if (existingPoster.getPosterPublicId() != null) {
			try {
				LoggingUtil.logOperation(
					log, 
					MessageConstant.Operation.DELETE, 
					AppConstant.CLOUDINARY_SERVICE, 
					user,
					"Deleting image with public ID: " + existingPoster.getPosterPublicId()
				);
				cloudinaryService.deleteFile(existingPoster.getPosterPublicId());
			} catch (Exception e) {
				LoggingUtil.logError(
					log, 
					MessageConstant.Operation.DELETE, 
					AppConstant.CLOUDINARY_SERVICE, 
					user,
					"Failed to delete image for poster ID: " + id + " - Continuing with deletion",
					e
				);
				// continue with domain poster deletion even if image deletion fails
			}
		}

		// delete domain poster from database
		domainPosterRepository.delete(existingPoster);

		// log the successful deletion operation
		LoggingUtil.logOperation(
			log, 
			MessageConstant.Operation.DELETE, 
			AppConstant.DOMAIN_POSTER_SERVICE, 
			user,
			String.format(MessageConstant.SuccessTemplate.DELETED, AppConstant.DOMAIN_POSTER, id) + AppConstant.FOR_DOMAIN + existingPoster.getDomainName()
		);
	}

	/*
	 * private methods - used internally
	 */

	/**
	 * Validates that the user has appropriate permissions to manage domain posters
	 *
	 * @param user      The user attempting the operation
	 * @param operation The operation type being performed
	 * @throws ForbiddenAccessException if the user lacks appropriate permissions
	 */
	private void checkUserAccess(User user, String operation) {
		// check if user is null
		if (user == null) {
			LoggingUtil.logSecurity(
				log, 
				AppConstant.ACCESS_DENIED, 
				null, 
				AppConstant.FAILED, 
				String.format(MessageConstant.ErrorTemplate.AUTHENTICATION_REQUIRED, operation + " " + AppConstant.DOMAIN_POSTER)
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
				String.format(MessageConstant.ErrorTemplate.NOT_AUTHORIZED, operation + " " + AppConstant.DOMAIN_POSTER)
			);
			throw new ForbiddenAccessException(MessageConstant.UserMessage.ACCESS_DENIED);
		}

		// log the successful authorization
		LoggingUtil.logSecurity(
			log, 
			AppConstant.ACCESS_GRANTED, 
			user, 
			AppConstant.SUCCESS,
			String.format(MessageConstant.SuccessTemplate.AUTHORIZED, operation + " " + AppConstant.DOMAIN_POSTER)
		);
	}

	/**
	 * Fetches a domain poster by its ID
	 * 
	 * @param id   The ID of the poster to fetch
	 * @param user The authenticated user requesting the poster
	 * @return DomainPoster object containing the poster details
	 * @throws DomainPosterNotFoundException if no poster exists with the given ID
	 */
	private DomainPoster fetchDomainPosterById(Long id, User user) {
		return domainPosterRepository.findById(id)
			.orElseThrow(() -> {
				LoggingUtil.logError(
					log, 
					MessageConstant.Operation.READ, 
					AppConstant.DOMAIN_POSTER_SERVICE, 
					user,
					String.format(MessageConstant.ErrorTemplate.NOT_FOUND, AppConstant.DOMAIN_POSTER, id), 
					null
				);
				return new DomainPosterNotFoundException(String.format(MessageConstant.ErrorTemplate.NOT_FOUND, AppConstant.DOMAIN_POSTER));
			});
	}
}