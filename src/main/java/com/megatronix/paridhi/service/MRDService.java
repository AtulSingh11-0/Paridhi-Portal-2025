package com.megatronix.paridhi.service;

import java.util.List;
import java.util.UUID;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.megatronix.paridhi.constant.Role;
import com.megatronix.paridhi.dto.request.MRDRequest;
import com.megatronix.paridhi.dto.response.MRDResponse;
import com.megatronix.paridhi.exception.ForbiddenAccessException;
import com.megatronix.paridhi.exception.GIDNotFoundException;
import com.megatronix.paridhi.exception.UserNotFoundException;
import com.megatronix.paridhi.model.MRD;
import com.megatronix.paridhi.model.User;
import com.megatronix.paridhi.repository.MRDRepository;
import com.megatronix.paridhi.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MRDService {
  private final EmailService emailService;
  private final MRDRepository mrdRepository;
  private final UserRepository userRepository;

	@CacheEvict(
		value = {
			"mrds",
			"mrdsByUser",
			"gidsByUser"
		},
		allEntries = true
	)
  @Transactional
  public MRDResponse registerMRD(MRDRequest request) {
    log.info("MRD registration request: {}", request);

    // find the user
    var user = userRepository.findUserByEmail(request.getEmail())
      .orElseThrow(() -> {
        log.error("User not found with email: {}", request.getEmail());
        return new UserNotFoundException("User not found with email: " + request.getEmail());
      });

    // create new MRD registration with an unique GID
    var mrd = MRD.builder()
    .user(user)
    .gid(generateGID())
    .name(user.getName())
    .email(user.getEmail())
    .contact(user.getContact())
    .college(user.getCollege())
    .year(user.getYear())
    .department(user.getDepartment())
    .rollNo(user.getRollNo())
    .build();

    // save the MRD registration
    MRD savedMRD = mrdRepository.save(mrd);
    log.info("MRD registration completed for user {}: GID={}", user.getId(), savedMRD.getGid());

    // send email to the user
    emailService.sendMRDConfirmation(user.getEmail(), savedMRD.getGid(), user.getName());

    // return the response
    return mapToResponse(savedMRD);
  }

	@Caching(
		evict = {
			@CacheEvict(value = "mrdByGid", key = "#gid"),
			@CacheEvict(
				value = {
					"mrds",
					"mrdsByUser",
					"gidsByUser"
				}, 
				allEntries = true
			)
		}
	)
	@Transactional
	public MRDResponse updatePaymentStatus(String gid, User user) {
    // check if user has permission to update payment status
    checkUserAccess(user, "update payment status");
    
    log.info("Updating payment status for MRD with GID: {}", gid);
    var mrd = mrdRepository.findByGid(gid)
    .orElseThrow(() -> {
      log.error("GID not found: {}", gid);
      return new GIDNotFoundException("GID not found: " + gid);
    });
    
    mrd.setPaid(!mrd.isPaid());
    MRD updatedMRD = mrdRepository.save(mrd);
    log.info("Updated payment status for GID {}: {}", gid, updatedMRD.isPaid());

    return mapToResponse(updatedMRD);
  }

	@Cacheable(value = "mrdsByUser", key = "#email")
  public List<MRDResponse> getUserMRDs(String email, User user) {
    // check if the user has ROLE_USER and if USER then check if they have authority to access the resource
    checkUserAccess(user, "fetch MRDs");
    log.info("Fetching MRDs for user with email: {} and role: {}", email, user.getRole());

		// find the user
		var existingUser = userRepository.findUserByEmail(email)
			.orElseThrow(() -> {
				log.error("User not found with email: {}", email);
				return new UserNotFoundException("User not found with email: " + email);
			});

    var mrds = mrdRepository.findAllByUser(existingUser);
    log.info("Fetched {} MRDs for user with email: {}", mrds.size(), email);

    return mrds.stream()
			.map(this::mapToResponse)
			.toList();
  }

	@Cacheable(value = "gidsByUser", key = "#email")
  public List<String> getUserGids(String email, User user) {
    // check if the user has ROLE_USER and if USER then check if they have authority to access the resource
    checkUserAccess(user, "fetch GIDs");
    
    log.info("Fetching GIDs for user with email: {}", email);

		// find the user
		var existingUser = userRepository.findUserByEmail(email)
			.orElseThrow(() -> {
				log.error("User not found with email: {}", email);
				return new UserNotFoundException("User not found with email: " + email);
			});

    var mrds = mrdRepository.findAllByUser(existingUser);
    log.info("Fetched {} MRDs for user with email: {}", mrds.size(), email);

    List<String> gids = mrds.stream()
      .map(MRD::getGid)
      .toList();

    log.info("Fetched {} GIDs for user with email: {}", gids.size(), email);
    return gids;
  }

	@Cacheable(value = "mrdByGid", key = "#gid")
	public MRDResponse getMRDbyGID(String gid, User user) {
		// check if the user has ROLE_USER and if USER then check if they have authority to access the resource
		checkUserAccess(user, "fetch MRD by GID");
		
		log.info("Fetching MRD with GID: {}", gid);
		var mrd = mrdRepository.findByGid(gid)
			.orElseThrow(() -> {
				log.error("GID not found: {}", gid);
				return new GIDNotFoundException("GID not found: " + gid);
			});

		return mapToResponse(mrd);
	}

  private MRDResponse mapToResponse(MRD mrd) {
    return MRDResponse.builder()
    .id(mrd.getId())
    .gid(mrd.getGid())
    .name(mrd.getName())
    .email(mrd.getEmail())
    .contact(mrd.getContact())
    .college(mrd.getCollege())
    .year(mrd.getYear())
    .department(mrd.getDepartment())
    .rollNo(mrd.getRollNo())
    .hasPaid(mrd.isPaid())
    .registeredAt(mrd.getRegisteredAt())
    .build();
  }

  private String generateGID() {
    return "PD-2025-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
  }

  private void checkUserAccess(User user, String methodName) {
    if (user == null) {
			log.error("User is not authenticated to {}", methodName);
			throw new ForbiddenAccessException("You do not have permission to access this resource");
		}
		
		if (user.getRole().equals(Role.ROLE_USER)) {
      log.error("User with ID {} not authorized to {}", user.getId(), methodName);
      throw new ForbiddenAccessException("User not authorized to  " + methodName);
    }
  }
}
