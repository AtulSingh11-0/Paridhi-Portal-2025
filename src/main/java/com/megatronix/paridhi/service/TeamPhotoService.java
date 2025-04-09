package com.megatronix.paridhi.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.megatronix.paridhi.constant.Category;
import com.megatronix.paridhi.constant.Role;
import com.megatronix.paridhi.dto.response.TeamPhotoResponse;
import com.megatronix.paridhi.exception.ForbiddenAccessException;
import com.megatronix.paridhi.exception.TeamPhotoNotFoundException;
import com.megatronix.paridhi.model.TeamPhoto;
import com.megatronix.paridhi.model.User;
import com.megatronix.paridhi.repository.TeamPhotoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamPhotoService {
	private final CloudinaryService cloudinaryService;
	private final TeamPhotoRepository teamPhotoRepository;

	@Transactional
	public TeamPhotoResponse saveTeamPhoto(Category category, MultipartFile teamPhotoImage, User user) {
		log.info("Saving team photo for category: {}, by User: {}", category, user.getUsername());

		// check if the user is allowed to upload a team photo for the given category
		checkUserAccess(user, "save");

		// upload team photo to Cloudinary
		var imageDetails = cloudinaryService.uploadFile(teamPhotoImage);

		// create a new team photo object and save it
		var teamPhoto = TeamPhoto.builder()
			.category(category)
			.imageSecureUrl(imageDetails.get("secure_url"))
			.imagePublicId(imageDetails.get("public_id"))
			.createdBy(user)
			.updatedBy(user)
			.build();
		var savedTeam = teamPhotoRepository.save(teamPhoto);
		log.info("Team photo saved successfully: {}", savedTeam);

		return TeamPhotoResponse.fromTeamPhoto(savedTeam);
	}

	@Transactional
	public TeamPhotoResponse updateTeamPhoto(Long id, Category category, MultipartFile teamPhotoImage, User user) {
		log.info("Updating team photo for category: {}, by User: {}", category, user.getUsername());

		// check if the user is allowed to update a team photo for the given category
		checkUserAccess(user, "update");

		// find the existing team photo by ID
		var existingTeamPhoto = teamPhotoRepository.findById(id)
			.orElseThrow(() -> {
				log.error("Team photo not found with ID: {}", id);
				return new TeamPhotoNotFoundException("Team photo not found with ID: " + id);
			});
		log.info("Existing team photo: {}", existingTeamPhoto);

		// delete old photo from Cloudinary
		cloudinaryService.deleteFile(existingTeamPhoto.getImagePublicId());

		// upload new team photo to Cloudinary
		var imageDetails = cloudinaryService.uploadFile(teamPhotoImage);
		log.info("New image details: {}", imageDetails);

		// update the existing team photo object with new details
		existingTeamPhoto.setCategory(category != null ? category : existingTeamPhoto.getCategory());
		existingTeamPhoto.setImageSecureUrl(imageDetails.get("secure_url"));
		existingTeamPhoto.setImagePublicId(imageDetails.get("public_id"));
		existingTeamPhoto.setUpdatedBy(user);

		var updatedTeamPhoto = teamPhotoRepository.save(existingTeamPhoto);
		log.info("Team photo updated successfully: {}", updatedTeamPhoto);

		return TeamPhotoResponse.fromTeamPhoto(updatedTeamPhoto);
	}

	@Transactional
	public void deleteTeamPhoto(Long id, User user) {
		log.info("Deleting team photo with ID: {}, by User: {}", id, user.getUsername());

		// check if the user is allowed to delete a team photo
		checkUserAccess(user, "delete");

		// find the existing team photo by ID
		var existingTeamPhoto = teamPhotoRepository.findById(id)
			.orElseThrow(() -> {
				log.error("Team photo not found with ID: {}", id);
				return new TeamPhotoNotFoundException("Team photo not found with ID: " + id);
			});
		log.info("Existing team photo: {}", existingTeamPhoto);

		// delete the team photo from Cloudinary
		cloudinaryService.deleteFile(existingTeamPhoto.getImagePublicId());

		// delete the team photo from the database
		teamPhotoRepository.delete(existingTeamPhoto);
		log.info("Team photo deleted successfully: {}", existingTeamPhoto);
	}

	public List<TeamPhotoResponse> getTeamPhotosByCategory(Category category) {
		log.info("Fetching team photo for category: {}", category);

		// find the team photo by category
		var teamPhotosByCategory = teamPhotoRepository.findByCategory(category);
		if (teamPhotosByCategory.isEmpty()) {
			log.error("No team photo found for category: {}", category);
			throw new TeamPhotoNotFoundException("No team photo found for category: " + category);
		}
		log.info("Team photo found: {}", teamPhotosByCategory.size());

		return teamPhotosByCategory.stream()
			.map(TeamPhotoResponse::fromTeamPhoto)
			.toList();
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
