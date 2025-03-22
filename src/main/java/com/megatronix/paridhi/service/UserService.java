package com.megatronix.paridhi.service;

import java.time.LocalDateTime;
import java.util.Collections;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.megatronix.paridhi.constant.Role;
import com.megatronix.paridhi.exception.UserAlreadyExistsException;
import com.megatronix.paridhi.exception.UserNotFoundException;
import com.megatronix.paridhi.dto.request.AdminCreationRequest;
import com.megatronix.paridhi.dto.request.LoginRequest;
import com.megatronix.paridhi.dto.request.RegisterRequest;
import com.megatronix.paridhi.dto.response.AuthResponse;
import com.megatronix.paridhi.model.BlackListedToken;
import com.megatronix.paridhi.model.User;
import com.megatronix.paridhi.repository.BlackListedTokenRepository;
import com.megatronix.paridhi.repository.UserRepository;
import com.megatronix.paridhi.security.JwtService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
	private final JwtService jwtService;
	private final UserRepository userRepository;
	private final BCryptPasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final BlackListedTokenRepository blackListedTokenRepository;

	@Transactional
	public AuthResponse register( RegisterRequest request ) {
		log.info("Register request: {}", request);

		// check if user already exists by email: if true then return orElse continue
		if ( userRepository.existsByEmail(request.getEmail()) ) {
			log.error("User already exists with email: {}", request.getEmail());
			throw new UserAlreadyExistsException("User already exists");
		}

		// create a new User and save it
		User user = User.builder()
				.name(request.getName())
				.email(request.getEmail())
				.password(passwordEncoder.encode(request.getPassword()))
				.role(request.getRole())
				.createdAt(LocalDateTime.now())
				.isVerified(false)
				.build();

		User savedUser = userRepository.save(user);
		log.info("Registered user: {}", savedUser);

		// generate JWT token
		var jwtToken = jwtService.generateToken(org.springframework.security.core.userdetails.User
			.builder()
				.username(savedUser.getEmail())
				.password(savedUser.getPassword())
				.authorities(
					Collections.singletonList(
						new SimpleGrantedAuthority(savedUser.getRole().name())
					)
				)
				.build()
		);

		// return an AuthResponse builder object
		return AuthResponse.builder()
				.token(jwtToken)
				.user(AuthResponse.UserDto.fromUser(savedUser))
				.build();
	}

	public AuthResponse login( LoginRequest request ) {
		// authenticate the user
		authenticationManager.authenticate(
			new UsernamePasswordAuthenticationToken(
				request.getEmail(), request.getPassword()
			)
		);

		// check if user exists with that email or not: if not then return orElse continue
		User user = userRepository.findUserByEmail(request.getEmail())
				.orElseThrow(() -> {
					log.error("User not found with email: {}", request.getEmail());
					return new UserNotFoundException("User not found");
				});

		// set user last login to current LocalDateTime
		user.setLastLogin(LocalDateTime.now());

		// generate JWT token
		var jwtToken = jwtService.generateToken(org.springframework.security.core.userdetails.User
			.builder()
				.username(user.getEmail())
				.password(user.getPassword())
				.authorities(
					Collections.singletonList(
						new SimpleGrantedAuthority(user.getRole().name())
					)
				)
				.build()
		);

		// save the user and return an AuthResponse builder object
		User savedUser = userRepository.save(user);
		log.info("LoggedIn user: {}", savedUser);

		return AuthResponse.builder()
				.token(jwtToken)
				.user(AuthResponse.UserDto.fromUser(savedUser))
				.build();
	}

	public AuthResponse createAdmin(AdminCreationRequest request) {
		log.info("Admin creation request: {}", request);

		// check if user already exists by email: if true then return orElse continue
		if ( userRepository.existsByEmail(request.getEmail()) ) {
			log.error("User already exists with email: {}", request.getEmail());
			throw new UserAlreadyExistsException("User already exists");
		}

		// validate the role is either ADMIN or SUPERADMIN
		if ( !request.getRole().equals(Role.ROLE_ADMIN) && !request.getRole().equals(Role.ROLE_SUPERADMIN) ) {
			log.error("Invalid role: {}", request.getRole());
			throw new IllegalArgumentException("Invalid role");
		}

		// create a new Admin user and save it
		User user = User.builder()
				.name(request.getName())
				.email(request.getEmail())
				.password(passwordEncoder.encode(request.getPassword()))
				.role(request.getRole())
				.createdAt(LocalDateTime.now())
				.isVerified(false)
				.build();

		User savedUser = userRepository.save(user);
		log.info("Admin created: {}", savedUser);

		// generate JWT token
		var jwtToken = jwtService.generateToken(org.springframework.security.core.userdetails.User
			.builder()
				.username(savedUser.getEmail())
				.password(savedUser.getPassword())
				.authorities(
					Collections.singletonList(
						new SimpleGrantedAuthority(savedUser.getRole().name())
					)
				)
				.build()
		);

		// return an AuthResponse builder object
		return AuthResponse.builder()
				.token(jwtToken)
				.user(AuthResponse.UserDto.fromUser(savedUser))
				.build();		
	}

	@Transactional
	public void logout(String token) {
		// extract the JWT without Bearer prefix
		String jwt = token.startsWith("Bearer ") ? token.substring(7) : token;

		// get expiration date from token
		LocalDateTime expiresAt = jwtService.getExpirationDateFromToken(jwt);

		// create a BlackListedToken object and save it
		var blackListedToken = BlackListedToken.builder()
			.token(jwt)
			.blackListedAt(LocalDateTime.now())
			.expiresAt(expiresAt)
			.build();
		var savedBlackListedToken = blackListedTokenRepository.save(blackListedToken);
		log.info("Token BlackListed Successfully: {}", savedBlackListedToken);
	}

	// Run daily at midnight (12:00 AM) to clean up expired tokens
	@Scheduled(cron = "0 0 0 * * ?")
	public void cleanUpExpiredTokens() {
		log.info("Cleaning up expired BlackListed tokens");
		blackListedTokenRepository.deleteAllExpiredTokens(LocalDateTime.now());
	}
}
