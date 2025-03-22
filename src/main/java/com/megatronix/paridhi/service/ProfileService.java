package com.megatronix.paridhi.service;

import com.megatronix.paridhi.exception.UserNotFoundException;
import com.megatronix.paridhi.dto.request.ProfileRequest;
import com.megatronix.paridhi.dto.response.AuthResponse;
import com.megatronix.paridhi.dto.response.ProfileResponse;
import com.megatronix.paridhi.model.User;
import com.megatronix.paridhi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {
	private final UserRepository userRepository;

	public ProfileResponse createProfile( ProfileRequest request ) {
		log.info("Profile request: {}", request);

		// check if user exists with the email: if false then return orElse continue
		User user = userRepository.findUserByEmail(request.getEmail())
				.orElseThrow(() -> {
					log.error("User not found with email: {}", request.getEmail());
					return new UserNotFoundException("User not found");
				});

		// set the profile fields of the user from the request
		user.setProfilePicture("https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_1280.png");
		user.setContact(request.getContact());
		user.setCollege(request.getCollege());
		user.setYear(request.getYear());
		user.setDepartment(request.getDepartment());
		user.setRollNo(request.getRollNo());

		// save the user and return an ProfileResponse builder object
		User savedUser = userRepository.save(user);
		log.info("User {} profile: {}", savedUser.getId(),savedUser);

		return ProfileResponse.builder()
				.profileDetails(AuthResponse.UserDto.fromUser(savedUser))
				.build();
	}

	public ProfileResponse getProfileById( Long id ) {
		log.info("ID to search for: {}", id);

		// check if user exists by id: if true continue orElse return
		User user = userRepository.findById(id)
				.orElseThrow( () -> {
					log.error("User not found with ID: {}", id);
					return new UserNotFoundException("User not found");
				});

		// return a ProfileResponse builder object for the found user
		return ProfileResponse.builder()
				.profileDetails(AuthResponse.UserDto.fromUser(user))
				.build();
	}
}
