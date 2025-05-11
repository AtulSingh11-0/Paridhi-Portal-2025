package com.megatronix.paridhi.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.megatronix.paridhi.constant.AppConstant;
import com.megatronix.paridhi.constant.MessageConstant;
import com.megatronix.paridhi.dto.request.OtpRequest;
import com.megatronix.paridhi.dto.request.OtpVerificationRequest;
import com.megatronix.paridhi.exception.InvalidOtpException;
import com.megatronix.paridhi.exception.OtpExpiredException;
import com.megatronix.paridhi.exception.UserAlreadyVerifiedException;
import com.megatronix.paridhi.exception.UserNotFoundException;
import com.megatronix.paridhi.model.OtpToken;
import com.megatronix.paridhi.repository.OtpTokenRepository;
import com.megatronix.paridhi.repository.UserRepository;
import com.megatronix.paridhi.util.LoggingUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {
  private final EmailService emailService;
  private final UserRepository userRepository;
  private final OtpTokenRepository otpTokenRepository;
	private static final String USER_NOT_FOUND_WITH_EMAIL = "User not found with email: ";
  private static final String OTP_SERVICE = "OtpService";

  @Transactional
  public void generateAndSendOtp(OtpRequest request) {
    String email = request.getEmail();
    
		// log the operation
    LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.CREATE,
			OTP_SERVICE,
			null,
			"Generating OTP for user: " + email
    );

    // find the user
    var user = userRepository.findUserByEmail(email)
			.orElseThrow(() -> {
				LoggingUtil.logError(
					log,
					MessageConstant.Operation.CREATE,
					OTP_SERVICE,
					null,
					USER_NOT_FOUND_WITH_EMAIL + email,
					null
				);
				return new UserNotFoundException(String.format(MessageConstant.ErrorTemplate.NOT_FOUND, AppConstant.USER));
			});

    // Check if the user is already verified
    if (user.isVerified()) {
			LoggingUtil.logError(
				log,
				MessageConstant.Operation.CREATE,
				OTP_SERVICE,
				null,
				"User is already verified: " + user.getEmail(),
				null
			);
			throw new UserAlreadyVerifiedException("User is already verified");
    }

    // delete any existing OTPs for this user
    Optional<OtpToken> existingToken = otpTokenRepository.findByUser(user);
    if (existingToken.isPresent()) {
			LoggingUtil.logOperation(
				log,
				MessageConstant.Operation.DELETE,
				OTP_SERVICE,
				null,
				"Deleting existing OTP for user: " + email
			);
			otpTokenRepository.delete(existingToken.get());
			otpTokenRepository.flush();
    }

    // create and save a new OTP
    var otp = OtpToken.builder()
			.user(user)
			.build();
    var savedOtp = otpTokenRepository.save(otp);

		// log the successful generation of OTP
    LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.CREATE,
			OTP_SERVICE,
			null,
			"OTP generated successfully for user: " + email
    );

    // Send the OTP via email
    emailService.sendOtp(user.getEmail(), user.getName(), savedOtp.getOtp());

		// log the successful sending of OTP
    LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.CREATE,
			OTP_SERVICE,
			null,
			"OTP sent successfully to: " + email
    );
  }

  @Transactional
  public boolean verifyOtp(OtpVerificationRequest request) {
    String email = request.getEmail();
    String otp = request.getOtp();

		// log the operation
    LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.AUTHENTICATE,
			OTP_SERVICE,
			null,
			"Verifying OTP for user: " + email
    );

    // find the user
    var user = userRepository.findUserByEmail(email)
			.orElseThrow(() -> {
				LoggingUtil.logError(
					log,
					MessageConstant.Operation.AUTHENTICATE,
					OTP_SERVICE,
					null,
					USER_NOT_FOUND_WITH_EMAIL + email,
					null
				);
				return new UserNotFoundException(String.format(MessageConstant.ErrorTemplate.NOT_FOUND, AppConstant.USER));
			});

    // check if the user is already verified
    if (user.isVerified()) {
			LoggingUtil.logError(
				log,
				MessageConstant.Operation.AUTHENTICATE,
				OTP_SERVICE,
				null,
				"User is already verified: " + email,
				null
			);
			throw new UserAlreadyVerifiedException("User is already verified");
    }

    // find OTP by user
    var otpByUser = otpTokenRepository.findByUser(user)
			.orElseThrow(() -> {
				LoggingUtil.logError(
					log,
					MessageConstant.Operation.AUTHENTICATE,
					OTP_SERVICE,
					null,
					"OTP not found for user: " + email,
					null
				);
				return new InvalidOtpException("Invalid OTP");
			});

    // check if the OTP is expired
    if (otpByUser.isExpired()) {
			LoggingUtil.logError(
				log,
				MessageConstant.Operation.AUTHENTICATE,
				OTP_SERVICE,
				null,
				"OTP expired for user: " + email,
				null
			);
			throw new OtpExpiredException("OTP has expired");
    }

    // check if the OTP is used
    if (otpByUser.isUsed()) {
			LoggingUtil.logError(
				log,
				MessageConstant.Operation.AUTHENTICATE,
				OTP_SERVICE,
				null,
				"OTP already used for user: " + email,
				null
			);
			throw new InvalidOtpException("OTP has already been used");
    }

    // check if the OTP is correct
    if (!otpByUser.getOtp().equals(otp)) {
			LoggingUtil.logError(
				log,
				MessageConstant.Operation.AUTHENTICATE,
				OTP_SERVICE,
				null,
				"Invalid OTP entered: " + otp,
				null
			);
			throw new InvalidOtpException("Invalid OTP");
    }

    // mark the OTP as used and user as verified and save it
    user.setVerified(true);
    userRepository.save(user);

    otpByUser.setUsed(true);
    otpTokenRepository.save(otpByUser);

		// log the successful verification of OTP
    LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.AUTHENTICATE,
			OTP_SERVICE,
			null,
			"OTP verified successfully for user: " + email + ", user is now verified"
    );
    
		// return true to indicate successful verification
    return true;
  }

  @Transactional
  public void resendOtp(OtpRequest request) {
    String email = request.getEmail();
    
		// log the operation
    LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.CREATE,
			OTP_SERVICE,
			null,
			"Resending OTP for user: " + email
    );
		
		// generate and send OTP
    generateAndSendOtp(request);
    
		// log the successful resending of OTP
    LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.CREATE,
			OTP_SERVICE,
			null,
			"OTP resent successfully for user: " + email
    );
  }

  // Schedule a cron job to delete expired OTPs every hour
  @Scheduled(cron = "0 0 * * * ?")
  public void deleteExpiredOtps() {
    // log the operation
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.DELETE,
			OTP_SERVICE,
			null,
			"Deleting expired OTPs"
    );
    
		// delete expired OTPs
    otpTokenRepository.deleteAllExpiredTokens(LocalDateTime.now());
    
		// log the successful deletion of expired OTPs
    LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.DELETE,
			OTP_SERVICE,
			null,
			"Successfully deleted expired OTPs"
    );
  }
}