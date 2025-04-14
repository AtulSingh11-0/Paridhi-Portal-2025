package com.megatronix.paridhi.service;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.megatronix.paridhi.constant.AppConstant;
import com.megatronix.paridhi.constant.MessageConstant;
import com.megatronix.paridhi.constant.Role;
import com.megatronix.paridhi.dto.request.ProfileRequest;
import com.megatronix.paridhi.dto.response.AuthResponse;
import com.megatronix.paridhi.dto.response.ProfileResponse;
import com.megatronix.paridhi.exception.ForbiddenAccessException;
import com.megatronix.paridhi.exception.ProfileAlreadyExistsException;
import com.megatronix.paridhi.exception.ProfileNotYetCreatedException;
import com.megatronix.paridhi.exception.UserNotFoundException;
import com.megatronix.paridhi.model.User;
import com.megatronix.paridhi.repository.UserRepository;
import com.megatronix.paridhi.util.LoggingUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {
	private final UserRepository userRepository;
	private static final String USER_ID = "User ID: ";
	private static final String PROFILE_ID = " profile ID: ";
	private static final String USER_NOT_FOUND = "User not found with ID: ";
	private static final String DEFAULT_PROFILE_PIC = "https://cdn-icons-png.flaticon.com/512/5951/5951752.png";

	@CacheEvict(
		value = {
			"profileById",
			"profilesByCreatedStatus"
		},
		allEntries = true
	)
	@Transactional
	public ProfileResponse createProfile(ProfileRequest request, User user) {
		// log the operation
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.CREATE,
			AppConstant.PROFILE,
			user,
			"Creating profile for email: " + request.getEmail()
		);

		// check user permissions using extracted method
		checkUserPermissionForProfile(user, request.getEmail(), "create profile for");

		// find the user by email
		User existingUser = userRepository.findUserByEmail(request.getEmail())
			.orElseThrow(() -> {
				LoggingUtil.logError(
					log,
					MessageConstant.Operation.CREATE,
					AppConstant.PROFILE,
					user,
					"User not found with email: " + request.getEmail(),
					null
				);
				return new UserNotFoundException("User not found with email: " + request.getEmail());
			});

		// Check if profile already exists
		if (existingUser.isProfileCreated()) {
			LoggingUtil.logError(
				log,
				MessageConstant.Operation.CREATE,
				AppConstant.PROFILE,
				user,
				"User with ID " + existingUser.getId() + " already has a profile, cannot create another",
				null
			);
			throw new ProfileAlreadyExistsException("User with ID " + existingUser.getId() + " already has a profile");
		}

		// update profile fields for the user
		existingUser.setProfilePicture(DEFAULT_PROFILE_PIC);
		existingUser.setContact(request.getContact());
		existingUser.setCollege(request.getCollege());
		existingUser.setYear(request.getYear());
		existingUser.setDepartment(request.getDepartment());
		existingUser.setRollNo(request.getRollNo());
		existingUser.setProfileCreated(true);

		// save the user to the database
		User savedUser = userRepository.save(existingUser);
		
		// log the successful creation of the profile
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.CREATE,
			AppConstant.PROFILE,
			user,
			"Profile successfully created for user ID: " + savedUser.getId() + ", email: " + savedUser.getEmail()
		);

		// return the ProfileResponse object
		return ProfileResponse.builder()
			.profileDetails(AuthResponse.UserDto.fromUser(savedUser))
			.build();
	}

	@Cacheable(value = "profileById", key = "#id")
	public ProfileResponse getProfileById(Long id, User user) {
		// log the operation
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.READ,
			AppConstant.PROFILE,
			user,
			"Fetching profile for user ID: " + id
		);

		// check user permissions using extracted method
		checkUserOwnershipOrAdmin(user, id, "view");

		// find user by ID
		User existingUser = userRepository.findById(id)
			.orElseThrow(() -> {
				LoggingUtil.logError(
					log,
					MessageConstant.Operation.READ,
					AppConstant.PROFILE,
					user,
					USER_NOT_FOUND + id,
					null
				);
				return new UserNotFoundException(USER_NOT_FOUND + id);
			});

		// log the successful retrieval of the profile
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.READ,
			AppConstant.PROFILE,
			user,
			"Profile successfully retrieved for user ID: " + existingUser.getId()
		);
		
		// return the ProfileResponse object
		return ProfileResponse.builder()
			.profileDetails(AuthResponse.UserDto.fromUser(existingUser))
			.build();
	}

	@Cacheable(value = "profilesByCreatedStatus", key = "#isProfileCreated")
	public List<ProfileResponse> getAllByIsProfileCreated(boolean isProfileCreated, User user) {
		// log the operation
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.READ,
			AppConstant.PROFILE,
			user,
			"Fetching profiles with created status: " + (isProfileCreated ? "CREATED" : "NOT_CREATED")
		);

		// only admins can list profiles
		if (user.getRole() == Role.ROLE_USER) {
			LoggingUtil.logSecurity(
				log,
				AppConstant.ACCESS_DENIED,
				user,
				AppConstant.FAILED,
				"Attempted to list all profiles without admin privileges"
			);
			throw new ForbiddenAccessException(MessageConstant.UserMessage.ACCESS_DENIED);
		}

		// fetch and transform users
		List<User> users = userRepository.findAllByIsProfileCreated(isProfileCreated);
		
		// log the successful retrieval of profiles
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.READ,
			AppConstant.PROFILE,
			user,
			"Successfully retrieved " + users.size() + " profiles with status: " + 
			(isProfileCreated ? "CREATED" : "NOT_CREATED")
		);

		// return the list of ProfileResponse objects
		return users.stream()
			.map(existingUser -> ProfileResponse.builder()
				.profileDetails(AuthResponse.UserDto.fromUser(existingUser))
				.build()
			)
			.toList();
	}

	@CacheEvict(
		value = {
			"profileById",
			"profilesByCreatedStatus"
		},
		allEntries = true
	)
	@Transactional
	public ProfileResponse updateProfile(Long id, ProfileRequest request, User user) {
		// log the operation
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.UPDATE,
			AppConstant.PROFILE,
			user,
			"Updating profile for user ID: " + id
		);

		// check user permissions using extracted method
		checkUserOwnershipOrAdmin(user, id, MessageConstant.Operation.UPDATE);

		// find user by ID
		User existingUser = userRepository.findById(id)
			.orElseThrow(() -> {
				LoggingUtil.logError(
					log,
					MessageConstant.Operation.UPDATE,
					AppConstant.PROFILE,
					user,
					USER_NOT_FOUND + id,
					null
				);
				return new UserNotFoundException(USER_NOT_FOUND + id);
			});
		
		// check if profile exists
		if (!existingUser.isProfileCreated()) {
			LoggingUtil.logError(
				log,
				MessageConstant.Operation.UPDATE,
				AppConstant.PROFILE,
				user,
				"Profile update attempted for user ID: " + existingUser.getId() + " who doesn't have a profile",
				null
			);
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
		
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.UPDATE,
			AppConstant.PROFILE,
			user,
			"Profile successfully updated for user ID: " + savedUser.getId()
		);

		return ProfileResponse.builder()
			.profileDetails(AuthResponse.UserDto.fromUser(savedUser))
			.build();
	}

	/*
	 * private methods - used internally only
	 */

	private void checkUserPermissionForProfile(User user, String targetEmail, String operation) {
		// if user is null, log the error and throw an exception
		if (user == null) {
			LoggingUtil.logSecurity(
				log,
				AppConstant.ACCESS_DENIED,
				null,
				AppConstant.FAILED,
				"Authentication required to " + operation + " profile for email: " + targetEmail
			);
			throw new ForbiddenAccessException(MessageConstant.UserMessage.ACCESS_DENIED);
		}
		
		// regular users can only manage their own profiles
		if (user.getRole() == Role.ROLE_USER && !user.getEmail().equals(targetEmail)) {
			LoggingUtil.logSecurity(
				log,
				AppConstant.ACCESS_DENIED,
				user,
				AppConstant.FAILED,
				USER_ID + user.getId() + " (email: " + user.getEmail() + ") attempted to " + 
				operation + " profile for different email: " + targetEmail
			);
			throw new ForbiddenAccessException(MessageConstant.UserMessage.ACCESS_DENIED);
		}
		
		// log successful permission check
		LoggingUtil.logSecurity(
			log,
			AppConstant.ACCESS_GRANTED,
			user,
			AppConstant.SUCCESS,
			USER_ID + user.getId() + " authorized to " + operation + " profile for email: " + targetEmail
		);
	}

	private void checkUserOwnershipOrAdmin(User user, Long profileId, String operation) {
		// if user is null, log the error and throw an exception
		if (user == null) {
			LoggingUtil.logSecurity(
				log,
				AppConstant.ACCESS_DENIED,
				null,
				AppConstant.FAILED,
				"Authentication required to " + operation + PROFILE_ID + profileId
			);
			throw new ForbiddenAccessException(MessageConstant.UserMessage.ACCESS_DENIED);
		}
		
		// regular users can only access their own profiles
		if (!user.getId().equals(profileId) && user.getRole() == Role.ROLE_USER) {
			LoggingUtil.logSecurity(
				log,
				AppConstant.ACCESS_DENIED,
				user,
				AppConstant.FAILED,
				USER_ID + user.getId() + " attempted to " + operation + 
				PROFILE_ID + profileId + " without permission"
			);
			throw new ForbiddenAccessException(MessageConstant.UserMessage.ACCESS_DENIED);
		}
		
		// log successful permission check
		LoggingUtil.logSecurity(
			log,
			AppConstant.ACCESS_GRANTED,
			user,
			AppConstant.SUCCESS,
			USER_ID + user.getId() + " authorized to " + operation + PROFILE_ID + profileId
		);
	}

}