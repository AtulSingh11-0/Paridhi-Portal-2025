package com.megatronix.paridhi.service;

import java.util.List;
import java.util.UUID;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.megatronix.paridhi.constant.AppConstant;
import com.megatronix.paridhi.constant.MessageConstant;
import com.megatronix.paridhi.constant.Role;
import com.megatronix.paridhi.dto.request.MRDRequest;
import com.megatronix.paridhi.dto.response.MRDResponse;
import com.megatronix.paridhi.exception.EmailNotVerifiedException;
import com.megatronix.paridhi.exception.ForbiddenAccessException;
import com.megatronix.paridhi.exception.GIDNotFoundException;
import com.megatronix.paridhi.exception.ProfileNotYetCreatedException;
import com.megatronix.paridhi.exception.UserNotFoundException;
import com.megatronix.paridhi.model.MRD;
import com.megatronix.paridhi.model.User;
import com.megatronix.paridhi.repository.MRDRepository;
import com.megatronix.paridhi.repository.UserRepository;
import com.megatronix.paridhi.util.LoggingUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MRDService {
  private final EmailService emailService;
  private final MRDRepository mrdRepository;
  private final UserRepository userRepository;
	private static final String FETCHED = "Fetched ";
	private static final String MRD_SERVICE = "MRDService";
	private static final String GID_NOT_FOUND = "GID not found: ";
	private static final String USER_NOT_FOUND_WITH_EMAIL = "User not found with email: ";

	/*
	 * public methods - doesn't require authentication
	 */

  @CacheEvict(
    value = {
      "mrds",
			"mrdByGId",
      "mrdsByUser",
      "gidsByUser"
    },
    allEntries = true
  )
  @Transactional
  public MRDResponse registerMRD(MRDRequest request) {
    // log the operation
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.CREATE,
			MRD_SERVICE,
			null,
			"MRD registration request: " + request
    );

    // find the user
    var user = userRepository.findUserByEmail(request.getEmail())
		.orElseThrow(() -> {
			LoggingUtil.logError(
				log,
				MessageConstant.Operation.CREATE,
				MRD_SERVICE,
				null,
				USER_NOT_FOUND_WITH_EMAIL + request.getEmail(),
				null
			);
			return new UserNotFoundException(USER_NOT_FOUND_WITH_EMAIL + request.getEmail());
		});

		// check if the email is verified
		if (!user.isVerified()) {
			LoggingUtil.logError(
				log,
				MessageConstant.Operation.CREATE,
				MRD_SERVICE,
				user,
				"Email not verified for user with ID: " + user.getId(),
				null
			);
			throw new EmailNotVerifiedException("Email not verified for user with ID: " + user.getId());
		}

		// check if the user has created its profile
		if (!user.isProfileCreated()) {
			LoggingUtil.logError(
				log,
				MessageConstant.Operation.CREATE,
				MRD_SERVICE,
				user,
				"User with ID: " + user.getId() + " has not created its profile",
				null
			);
			throw new ProfileNotYetCreatedException("User with ID: " + user.getId() + " has not created its profile");
		}

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
    
		// log the successful registration
    LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.CREATE,
			MRD_SERVICE,
			null,
			"MRD registration completed for user " + user.getId() + ": GID=" + savedMRD.getGid()
    );

    // send email to the user
    emailService.sendMRDWelcome(user.getEmail(), user.getName(), savedMRD.getGid());

    // return the response
    return mapToResponse(savedMRD);
  }

	/*
	 * protected methods - requires authentication
	 */

  @Caching(
    evict = {
      @CacheEvict(value = "mrdByGid", key = "#gid"),
      @CacheEvict(
        value = {
          "mrds",
					"mrdByGId",
          "mrdsByUser",
          "gidsByUser"
        }, 
        allEntries = true
      )
    }
  )
  @Transactional
  public MRDResponse updatePaymentStatus(String gid, User user) {
    // log the operation
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.UPDATE,
			MRD_SERVICE,
			user,
			"Updating payment status for MRD with GID: " + gid
    );
    
    // validate user access
    checkUserAccess(user, "update payment status");
    
		// find the MRD by GID
    var mrd = mrdRepository.findByGid(gid)
      .orElseThrow(() -> {
        LoggingUtil.logError(
					log,
					MessageConstant.Operation.UPDATE,
					MRD_SERVICE,
					user,
					GID_NOT_FOUND + gid,
					null
        );
        return new GIDNotFoundException(GID_NOT_FOUND + gid);
      });
    
		// toggle the payment status and save the MRD
    mrd.setPaid(!mrd.isPaid());
    MRD updatedMRD = mrdRepository.save(mrd);
    
		// log the successful update
    LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.UPDATE,
			MRD_SERVICE,
			user,
			"Updated payment status for GID " + gid + ": " + updatedMRD.isPaid()
    );

		// return the response
    return mapToResponse(updatedMRD);
  }

  @Cacheable(value = "mrdsByUser", key = "#email")
  public List<MRDResponse> getUserMRDs(String email, User user) {
    // log the operation
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.READ,
			MRD_SERVICE,
			user,
			"Fetching MRDs for user with email: " + email + " and role: " + user.getRole()
    );
    
    // validate user access
    checkUserAccess(user, "fetch MRDs");

    // find the user
    var existingUser = userRepository.findUserByEmail(email)
      .orElseThrow(() -> {
        LoggingUtil.logError(
					log,
					MessageConstant.Operation.READ,
					MRD_SERVICE,
					user,
					USER_NOT_FOUND_WITH_EMAIL + email,
					null
        );
        return new UserNotFoundException(USER_NOT_FOUND_WITH_EMAIL + email);
      });

		// fetch MRDs for the user
    var mrds = mrdRepository.findAllByUser(existingUser);
    
		// log the successful retrieval
    LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.READ,
			MRD_SERVICE,
			user,
			FETCHED + mrds.size() + " MRDs for user with email: " + email
    );

		// return the list of MRDResponse objects
    return mrds.stream()
      .map(this::mapToResponse)
      .toList();
  }

  @Cacheable(value = "gidsByUser", key = "#email")
  public List<String> getUserGids(String email, User user) {
    // log the operation
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.READ,
			MRD_SERVICE,
			user,
			"Fetching GIDs for user with email: " + email
    );
    
    // validate user access
    checkUserAccess(user, "fetch GIDs");

    // find the user
    var existingUser = userRepository.findUserByEmail(email)
      .orElseThrow(() -> {
        LoggingUtil.logError(
					log,
					MessageConstant.Operation.READ,
					MRD_SERVICE,
					user,
					USER_NOT_FOUND_WITH_EMAIL + email,
					null
        );
        return new UserNotFoundException(USER_NOT_FOUND_WITH_EMAIL + email);
      });

		// fetch MRDs for the user
    var mrds = mrdRepository.findAllByUser(existingUser);
    
		// log the successful retrieval
    LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.READ,
			MRD_SERVICE,
			user,
			FETCHED + mrds.size() + " MRDs for user with email: " + email
    );

		// map MRDs to GIDs
    List<String> gids = mrds.stream()
      .map(MRD::getGid)
      .toList();

		// log the successful retrieval of GIDs
    LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.READ,
			MRD_SERVICE,
			user,
			FETCHED + gids.size() + " GIDs for user with email: " + email
    );
    
		// return the list of GIDs
    return gids;
  }

  @Cacheable(value = "mrdByGid", key = "#gid")
  public MRDResponse getMRDbyGID(String gid, User user) {
    // log the operation
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.READ,
			MRD_SERVICE,
			user,
			"Fetching MRD with GID: " + gid
    );
    
    // validate user access
    checkUserAccess(user, "fetch MRD by GID");
    
		// find the MRD by GID
    var mrd = mrdRepository.findByGid(gid)
      .orElseThrow(() -> {
        LoggingUtil.logError(
					log,
					MessageConstant.Operation.READ,
					MRD_SERVICE,
					user,
					GID_NOT_FOUND + gid,
					null
        );
        return new GIDNotFoundException(GID_NOT_FOUND + gid);
      });

		// log the successful retrieval
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.READ,
			MRD_SERVICE,
			user,
			"Fetched MRD with GID: " + gid
		);

		// return the MRDResponse object
    return mapToResponse(mrd);
  }

  @Cacheable(value = "mrds")
  public List<MRDResponse> getAllMRDs(User user) {
    // log the operation
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.READ,
			MRD_SERVICE,
			user,
			"Fetching all MRDs for user with role: " + user.getRole()
    );

    // validate user access
    checkUserAccess(user, "fetch all MRDs");

    // find all MRDs
    var mrds = mrdRepository.findAll();

		// log the successful retrieval
    LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.READ,
			MRD_SERVICE,
			user,
			FETCHED + mrds.size() + " MRDs"
    );

		// return the list of MRDResponse objects
    return mrds.stream()
      .map(this::mapToResponse)
      .toList();
  }

	/*
	 * private methods - used internally
	 */

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
    // check if the user is null
		if (user == null) {
      LoggingUtil.logSecurity(
				log,
				AppConstant.ACCESS_DENIED,
				null,
				AppConstant.FAILED,
				"User is not authenticated to " + methodName
      );
      throw new ForbiddenAccessException("You do not have permission to access this resource");
    }
    
		// check if the user has ROLE_USER
    if (user.getRole().equals(Role.ROLE_USER)) {
      LoggingUtil.logSecurity(
				log,
				AppConstant.ACCESS_DENIED,
				user,
				AppConstant.FAILED,
				"User with ID " + user.getId() + " not authorized to " + methodName
      );
      throw new ForbiddenAccessException("User not authorized to " + methodName);
    }
    
		// log the successful authorization
    LoggingUtil.logSecurity(
			log,
			AppConstant.ACCESS_GRANTED,
			user,
			AppConstant.SUCCESS,
			"User with ID " + user.getId() + " authorized to " + methodName
    );
  }
}