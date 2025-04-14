package com.megatronix.paridhi.service;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.megatronix.paridhi.constant.AppConstant;
import com.megatronix.paridhi.constant.Category;
import com.megatronix.paridhi.constant.MessageConstant;
import com.megatronix.paridhi.constant.Role;
import com.megatronix.paridhi.dto.response.TeamPhotoResponse;
import com.megatronix.paridhi.exception.ForbiddenAccessException;
import com.megatronix.paridhi.exception.TeamPhotoNotFoundException;
import com.megatronix.paridhi.model.TeamPhoto;
import com.megatronix.paridhi.model.User;
import com.megatronix.paridhi.repository.TeamPhotoRepository;
import com.megatronix.paridhi.util.LoggingUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamPhotoService {
	private final CloudinaryService cloudinaryService;
	private final TeamPhotoRepository teamPhotoRepository;
	private static final String TEAM_PHOTO = " team photo";
	private static final String TEAM_PHOTO_NOT_FOUND = "Team photo not found with ID: ";

	/*
	 * public methods - doesn't require authentication
	 */

	@Cacheable(value = "teamPhotosByCategory", key = "#category.name()")
	public List<TeamPhotoResponse> getTeamPhotosByCategory(Category category) {
		// log the operation
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.READ,
			AppConstant.TEAM_PHOTO,
			null,
			"Fetching team photos for category: " + category
		);
		
		// fetch team photos by category from the database
		List<TeamPhoto> teamPhotos = teamPhotoRepository.findByCategory(category);
		
		// check if any team photos were found
		if (teamPhotos.isEmpty()) {
			LoggingUtil.logError(
				log,
				MessageConstant.Operation.READ,
				AppConstant.TEAM_PHOTO,
				null,
				"No team photos found for category: " + category,
				null
			);
			throw new TeamPhotoNotFoundException("No team photos found for category: " + category);
		}
		
		// log the successful retrieval of team photos
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.READ,
			AppConstant.TEAM_PHOTO,
			null,
			"Successfully retrieved " + teamPhotos.size() + " team photos for category: " + category
		);
		
		// return the list of TeamPhotoResponse objects
		return teamPhotos.stream()
			.map(TeamPhotoResponse::fromTeamPhoto)
			.toList();
	}

	/*
	 * protected methods - requires authentication
	 */

	@CacheEvict(
		value = {
			"teamPhotos",
			"teamPhotosByCategory"
		},
		allEntries = true
	)
	@Transactional
	public TeamPhotoResponse saveTeamPhoto(Category category, MultipartFile teamPhotoImage, User user) {
		// log the operation
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.CREATE,
			AppConstant.TEAM_PHOTO,
			user,
			"Saving team photo for category: " + category
		);
		
		// validate user access
		checkUserAccess(user, MessageConstant.Operation.CREATE);
		
		// upload team photo to Cloudinary
		var imageDetails = cloudinaryService.uploadFile(teamPhotoImage);
		
		// create team photo builder object
		var teamPhoto = TeamPhoto.builder()
			.category(category)
			.imageSecureUrl(imageDetails.get(AppConstant.SECURE_URL))
			.imagePublicId(imageDetails.get(AppConstant.PUBLIC_ID))
			.createdBy(user)
			.updatedBy(user)
			.build();
		
		// save team photo to database
		var savedTeam = teamPhotoRepository.save(teamPhoto);
		
		// log the successful save operation
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.CREATE,
			AppConstant.TEAM_PHOTO,
			user,
			"Successfully saved team photo with ID: " + savedTeam.getId() + " for category: " + category
		);
		
		// return the saved team photo response
		return TeamPhotoResponse.fromTeamPhoto(savedTeam);
	}

	@CacheEvict(
		value = {
			"teamPhotos",
			"teamPhotosByCategory"
		},
		allEntries = true
	)
	@Transactional
	public TeamPhotoResponse updateTeamPhoto(Long id, Category category, MultipartFile teamPhotoImage, User user) {
		// log the operation
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.UPDATE,
			AppConstant.TEAM_PHOTO,
			user,
			"Updating team photo with ID: " + id + (category != null ? ", new category: " + category : "")
		);
		
		// validate user access
		checkUserAccess(user, MessageConstant.Operation.UPDATE);
		
		// find the existing team photo by ID
		var existingTeamPhoto = teamPhotoRepository.findById(id)
			.orElseThrow(() -> {
				LoggingUtil.logError(
					log,
					MessageConstant.Operation.UPDATE,
					AppConstant.TEAM_PHOTO,
					user,
					TEAM_PHOTO_NOT_FOUND + id,
					null
				);
				return new TeamPhotoNotFoundException(TEAM_PHOTO_NOT_FOUND + id);
			});
		
		// delete old photo from Cloudinary
		try {
			LoggingUtil.logOperation(
				log,
				MessageConstant.Operation.DELETE,
				AppConstant.CLOUDINARY_IMAGE,
				user,
				"Removing previous image with public ID: " + existingTeamPhoto.getImagePublicId()
			);
			cloudinaryService.deleteFile(existingTeamPhoto.getImagePublicId());
		} catch (Exception e) {
			LoggingUtil.logError(
				log,
				MessageConstant.Operation.DELETE,
				AppConstant.CLOUDINARY_IMAGE,
				user,
				"Failed to delete previous image for team photo ID: " + id + " - Continuing with upload",
				e
			);
			// continue with upload even if deletion fails
		}
		
		// upload new team photo to Cloudinary
		var imageDetails = cloudinaryService.uploadFile(teamPhotoImage);
		
		// update the existing team photo object with new details
		existingTeamPhoto.setCategory(category != null ? category : existingTeamPhoto.getCategory());
		existingTeamPhoto.setImageSecureUrl(imageDetails.get(AppConstant.SECURE_URL));
		existingTeamPhoto.setImagePublicId(imageDetails.get(AppConstant.PUBLIC_ID));
		existingTeamPhoto.setUpdatedBy(user);
		
		// save the updated team photo to database
		var updatedTeamPhoto = teamPhotoRepository.save(existingTeamPhoto);
		
		// log the successful update operation
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.UPDATE,
			AppConstant.TEAM_PHOTO,
			user,
			"Successfully updated team photo with ID: " + id + 
			" - Category: " + updatedTeamPhoto.getCategory() + 
			" - New image public ID: " + imageDetails.get(AppConstant.PUBLIC_ID)
		);
		
		// return the updated team photo response
		return TeamPhotoResponse.fromTeamPhoto(updatedTeamPhoto);
	}

	@CacheEvict(
		value = {
			"teamPhotos",
			"teamPhotosByCategory"
		},
		allEntries = true
	)
	@Transactional
	public void deleteTeamPhoto(Long id, User user) {
		// log the operation
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.DELETE,
			AppConstant.TEAM_PHOTO,
			user,
			"Deleting team photo with ID: " + id
		);
		
		// check if the user is allowed to delete a team photo
		checkUserAccess(user, MessageConstant.Operation.DELETE);
		
		// find the existing team photo by ID
		var existingTeamPhoto = teamPhotoRepository.findById(id)
			.orElseThrow(() -> {
				LoggingUtil.logError(
					log,
					MessageConstant.Operation.DELETE,
					AppConstant.TEAM_PHOTO,
					user,
					TEAM_PHOTO_NOT_FOUND + id,
					null
				);
				return new TeamPhotoNotFoundException(TEAM_PHOTO_NOT_FOUND + id);
			});
		
		// delete the team photo from Cloudinary
		try {
			LoggingUtil.logOperation(
				log,
				MessageConstant.Operation.DELETE,
				AppConstant.CLOUDINARY_IMAGE,
				user,
				"Deleting image with public ID: " + existingTeamPhoto.getImagePublicId()
			);
			cloudinaryService.deleteFile(existingTeamPhoto.getImagePublicId());
		} catch (Exception e) {
			LoggingUtil.logError(
				log,
				MessageConstant.Operation.DELETE,
				AppConstant.CLOUDINARY_IMAGE,
				user,
				"Failed to delete image for team photo ID: " + id + " - Continuing with deletion",
				e
			);
			// continue with deletion even if Cloudinary deletion fails
		}
		
		// delete the team photo from the database
		teamPhotoRepository.delete(existingTeamPhoto);
		
		// log the successful deletion operation
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.DELETE,
			AppConstant.TEAM_PHOTO,
			user,
			"Successfully deleted team photo with ID: " + id + " - Category: " + existingTeamPhoto.getCategory()
		);
	}

	/*
	 * private methods - used internally only
	 */

	private void checkUserAccess(User user, String operation) {
		// log the operation
		if (user == null) {
			LoggingUtil.logSecurity(
				log,
				AppConstant.ACCESS_DENIED,
				null,
				AppConstant.FAILED,
				"Authentication required to " + operation + TEAM_PHOTO
			);
			throw new ForbiddenAccessException(MessageConstant.UserMessage.ACCESS_DENIED);
		}
		
		// check if the user has the required role to perform the operation
		if (user.getRole().equals(Role.ROLE_USER)) {
			LoggingUtil.logSecurity(
				log,
				AppConstant.ACCESS_DENIED,
				user,
				AppConstant.FAILED,
				"User lacks permission to " + operation + TEAM_PHOTO
			);
			throw new ForbiddenAccessException(MessageConstant.UserMessage.ACCESS_DENIED);
		}
		
		// log the successful authorization
		LoggingUtil.logSecurity(
			log,
			AppConstant.ACCESS_GRANTED,
			user,
			AppConstant.SUCCESS,
			"User authorized to " + operation + TEAM_PHOTO
		);
	}
}