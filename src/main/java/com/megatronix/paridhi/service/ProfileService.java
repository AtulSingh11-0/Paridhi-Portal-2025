package com.megatronix.paridhi.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.megatronix.paridhi.dto.request.ProfileRequest;
import com.megatronix.paridhi.dto.response.AuthResponse;
import com.megatronix.paridhi.dto.response.ProfileResponse;
import com.megatronix.paridhi.exception.ForbiddenAccessException;
import com.megatronix.paridhi.exception.ProfileAlreadyExistsException;
import com.megatronix.paridhi.exception.ProfileNotYetCreatedException;
import com.megatronix.paridhi.exception.UserNotFoundException;
import com.megatronix.paridhi.constant.Role;
import com.megatronix.paridhi.model.User;
import com.megatronix.paridhi.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {
	private final UserRepository userRepository;

	@Transactional
	public ProfileResponse createProfile(ProfileRequest request, User user) {
		log.info("Profile creation request for email: {}, requested by user ID: {}", request.getEmail(), user.getId());

		// Check user permissions using extracted method
		checkUserPermissionForProfile(user, request.getEmail(), "create profile for");

		// Find the user by email
		User existingUser = userRepository.findUserByEmail(request.getEmail())
			.orElseThrow(() -> {
				log.error("User not found with email: {} during profile creation", request.getEmail());
				return new UserNotFoundException("User not found with email: " + request.getEmail());
			});

		// Check if profile already exists
		if (existingUser.isProfileCreated()) {
			log.error("User with ID {} already has a profile, cannot create another", existingUser.getId());
			throw new ProfileAlreadyExistsException("User with ID " + existingUser.getId() + " already has a profile");
		}

		// Set profile fields
		existingUser.setProfilePicture("https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_1280.png");
		existingUser.setContact(request.getContact());
		existingUser.setCollege(request.getCollege());
		existingUser.setYear(request.getYear());
		existingUser.setDepartment(request.getDepartment());
		existingUser.setRollNo(request.getRollNo());
		existingUser.setProfileCreated(true);

		// Save user
		User savedUser = userRepository.save(existingUser);
		log.info("Profile successfully created for user ID: {}, email: {}", savedUser.getId(), savedUser.getEmail());

		return ProfileResponse.builder()
			.profileDetails(AuthResponse.UserDto.fromUser(savedUser))
			.build();
	}

	public ProfileResponse getProfileById(Long id, User user) {
		log.info("Profile retrieval request for ID: {}, requested by user ID: {}", id, user.getId());

		// Check user permissions using extracted method
		checkUserOwnershipOrAdmin(user, id, "view");

		// Find user by ID
		User existingUser = userRepository.findById(id)
			.orElseThrow(() -> {
				log.error("User not found with ID: {} during profile retrieval", id);
				return new UserNotFoundException("User not found with ID: " + id);
			});

		log.info("Profile successfully retrieved for user ID: {}", existingUser.getId());
		return ProfileResponse.builder()
			.profileDetails(AuthResponse.UserDto.fromUser(existingUser))
			.build();
	}

	public List<ProfileResponse> getAllByIsProfileCreated(boolean isProfileCreated, User user) {
		log.info("Request to list all profiles with profileCreated={}, requested by user ID: {}", isProfileCreated, user.getId());

		// Only admins can list profiles
		if (user.getRole() == Role.ROLE_USER) {
			log.error("User with ID {} attempted to list all profiles without admin privileges", user.getId());
			throw new ForbiddenAccessException("Only administrators can list all profiles");
		}

		// Fetch and transform users
		List<User> users = userRepository.findAllByIsProfileCreated(isProfileCreated);
		log.info("Found {} users with profileCreated={}", users.size(), isProfileCreated);

		return users.stream()
			.map(existingUser -> ProfileResponse.builder()
				.profileDetails(AuthResponse.UserDto.fromUser(existingUser))
				.build()
			)
			.toList();
	}

	@Transactional
	public ProfileResponse updateProfile(Long id, ProfileRequest request, User user) {
		log.info("Profile update request for ID: {}, requested by user ID: {}", id, user.getId());

		// Check user permissions using extracted method
		checkUserOwnershipOrAdmin(user, id, "update");

		// Find user by ID
		User existingUser = userRepository.findById(id)
			.orElseThrow(() -> {
				log.error("User not found with ID: {} during profile update", id);
				return new UserNotFoundException("User not found with ID: " + id);
			});
		
		// Check if profile exists
		if (!existingUser.isProfileCreated()) {
			log.error("Profile update attempted for user ID: {} who doesn't have a profile", existingUser.getId());
			throw new ProfileNotYetCreatedException("User with ID " + id + " needs to create a profile first");
		}

		// Update profile fields
		existingUser.setContact(request.getContact());
		existingUser.setCollege(request.getCollege());
		existingUser.setYear(request.getYear());
		existingUser.setDepartment(request.getDepartment());
		existingUser.setRollNo(request.getRollNo());

		// Save user
		User savedUser = userRepository.save(existingUser);
		log.info("Profile successfully updated for user ID: {}", savedUser.getId());

		return ProfileResponse.builder()
			.profileDetails(AuthResponse.UserDto.fromUser(savedUser))
			.build();
	}

	private void checkUserPermissionForProfile(User user, String targetEmail, String operation) {
		// Regular users can only manage their own profiles
		if (user.getRole() == Role.ROLE_USER && !user.getEmail().equals(targetEmail)) {
			log.error("User {} (email: {}) attempted to {} different email: {}", user.getId(), user.getEmail(), operation, targetEmail);
			throw new ForbiddenAccessException("Regular users can only manage their own profiles");
		}
	}

	private void checkUserOwnershipOrAdmin(User user, Long profileId, String operation) {
		// Regular users can only access their own profiles
		if (user.getId() != profileId && user.getRole() == Role.ROLE_USER) {
			log.error("User {} attempted to {} profile ID: {} without permission", user.getId(), operation, profileId);
			throw new ForbiddenAccessException("User does not have permission to " + operation + " this profile");
		}
	}
}