package com.megatronix.paridhi.service;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.megatronix.paridhi.constant.Domain;
import com.megatronix.paridhi.constant.Role;
import com.megatronix.paridhi.dto.response.DomainPosterResponse;
import com.megatronix.paridhi.exception.DomainPosterAlreadyExistsExcpetion;
import com.megatronix.paridhi.exception.DomainPosterNotFoundException;
import com.megatronix.paridhi.exception.ForbiddenAccessException;
import com.megatronix.paridhi.model.DomainPoster;
import com.megatronix.paridhi.model.User;
import com.megatronix.paridhi.repository.DomainPosterRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DomainPosterService {
	private final CloudinaryService cloudinaryService;
	private final DomainPosterRepository domainPosterRepository;

	@CacheEvict(
		value = {
			"domainPosters",
			"domainPosterByDomain"
		},
		allEntries = true
	)
	public DomainPosterResponse saveDomainPoster (
		Domain domainName,
		MultipartFile domainPoster,
		User user
	) {
		log.info("Saving domain poster for domain:{}, by user: {}", domainName, user.getName());

		// Check if user has access to save domain poster
		checkUserAccess(user, "save");

		// Check if domain poster already exists
		if (domainPosterRepository.findByDomainName(domainName) != null) {
			log.error("Domain poster already exists for domain: {}", domainName);
			throw new DomainPosterAlreadyExistsExcpetion("Domain poster already exists for domain: " + domainName);
		}

		// upload domain poster to Cloudinary
		var imageDetails = cloudinaryService.uploadFile(domainPoster);
		
		// create a new domain poster object and save it 
		var domainPosterObject = DomainPoster.builder()
			.domainName(domainName)
			.posterSecureUrl(imageDetails.get("secure_url"))
			.posterPublicId(imageDetails.get("public_id"))
			.createdBy(user)
			.updatedBy(user)
			.build();
		var savedDomainPoster = domainPosterRepository.save(domainPosterObject);
		log.info("Domain poster saved successfully for domain: {}", domainName);

		return DomainPosterResponse.fromDomainPoster(savedDomainPoster);
	}

	@CacheEvict(
		value = {
			"domainPosters",
			"domainPosterByDomain"
		},
		allEntries = true
	)
	public DomainPosterResponse updateDomainPoster(
		Long id,
		Domain domainName,
		MultipartFile domainPoster,
		User user
	) {
		log.info("Updating domain poster for domain:{}, by user: {}", domainName, user.getName());

		// Check if user has access to update domain poster
		checkUserAccess(user, "update");

		// Check if domain poster exists
		var existingDomainPoster = domainPosterRepository.findById(id)
			.orElseThrow(() -> {
				log.error("Domain poster not found for domain: {}", domainName);
				return new DomainPosterNotFoundException("Domain poster not found for domain: " + domainName);
			});

		// delete existing domain poster from Cloudinary
		cloudinaryService.deleteFile(existingDomainPoster.getPosterPublicId());

		// upload new domain poster to Cloudinary
		var imageDetails = cloudinaryService.uploadFile(domainPoster);

		// update domain poster object and save it
		existingDomainPoster.setDomainName(domainName);
		existingDomainPoster.setPosterSecureUrl(imageDetails.get("secure_url"));
		existingDomainPoster.setPosterPublicId(imageDetails.get("public_id"));
		existingDomainPoster.setUpdatedBy(user);
		var updatedDomainPoster = domainPosterRepository.save(existingDomainPoster);
		log.info("Domain poster updated successfully for domain: {}", domainName);

		return DomainPosterResponse.fromDomainPoster(updatedDomainPoster);
	}

	@Cacheable(value = "domainPosterByDomain", key = "#domainName")
	public DomainPosterResponse getDomainPoster(Domain domainName) {
		log.info("Getting domain poster for domain: {}", domainName);

		// Check if domain poster exists
		var existingDomainPoster = domainPosterRepository.findByDomainName(domainName);
		if (existingDomainPoster == null) {
			log.error("Domain poster not found for domain: {}", domainName);
			throw new DomainPosterNotFoundException("Domain poster not found for domain: " + domainName);
		}

		log.info("Domain poster found for domain: {}", domainName);
		return DomainPosterResponse.fromDomainPoster(existingDomainPoster);
	}

	@Cacheable(value = "domainPosters")
	public List<DomainPosterResponse> getAllDomainPosters() {
		log.info("Getting all domain posters");

		// Check if domain posters exist
		var existingDomainPosters = domainPosterRepository.findAll();
		if (existingDomainPosters.isEmpty()) {
			log.error("No domain posters found");
			throw new DomainPosterNotFoundException("No domain posters found");
		}

		log.info("Domain posters found: {}", existingDomainPosters.size());
		return existingDomainPosters.stream()
			.map(DomainPosterResponse::fromDomainPoster)
			.toList();
	}

	@CacheEvict(
		value = {
			"domainPosters",
			"domainPosterByDomain"
		},
		allEntries = true
	)
	public void deleteDomainPoster(
		Long id,
		User user
	) {
		log.info("Deleting domain poster for domain:{}, by user: {}", id, user.getName());

		// Check if user has access to delete domain poster
		checkUserAccess(user, "delete");

		// Check if domain poster exists
		var existingDomainPoster = domainPosterRepository.findById(id)
			.orElseThrow(() -> {
				log.error("Domain poster not found for domain: {}", id);
				return new DomainPosterNotFoundException("Domain poster not found for domain: " + id);
			});

		// delete existing domain poster from Cloudinary
		cloudinaryService.deleteFile(existingDomainPoster.getPosterPublicId());

		// delete domain poster from database
		domainPosterRepository.delete(existingDomainPoster);
		log.info("Domain poster deleted successfully for domain: {}", id);
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
