package com.megatronix.paridhi.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.megatronix.paridhi.exception.InvalidOtpException;
import com.megatronix.paridhi.exception.OtpExpiredException;
import com.megatronix.paridhi.exception.UserAlreadyVerifiedException;
import com.megatronix.paridhi.exception.UserNotFoundException;
import com.megatronix.paridhi.dto.request.OtpRequest;
import com.megatronix.paridhi.dto.request.OtpVerificationRequest;
import com.megatronix.paridhi.model.OtpToken;
import com.megatronix.paridhi.repository.OtpTokenRepository;
import com.megatronix.paridhi.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {
  private final EmailService emailService;
  private final UserRepository userRepository;
  private final OtpTokenRepository otpTokenRepository;

  @Transactional
  public void generateAndSendOtp(OtpRequest request) {
    String email = request.getEmail();
    log.info("Generating OTP for user: {}", email);

    // find the user
    var user = userRepository.findUserByEmail(email)
      .orElseThrow( () -> {
        log.error("User not found: {}", email);
        return new UserNotFoundException("User not found with email: " + email);
      });

    // check if the user is already verified
    if (user.isVerified()) {
      log.warn("User is already verified: {}", user.getEmail());
      throw new UserAlreadyVerifiedException("User is already verified");
    }

    // delete any existing OTPs for this user
    Optional<OtpToken> existingToken = otpTokenRepository.findByUser(user);
    if (existingToken.isPresent()) {
      otpTokenRepository.delete(existingToken.get());
      otpTokenRepository.flush();
    }

    // create and save a new OTP
    var otp = OtpToken.builder()
      .user(user)
      .build();
    var savedOtp = otpTokenRepository.save(otp);

    // send the OTP via email
    emailService.sendVerificationOtp(user.getEmail(), savedOtp.getOtp(), user.getName());

    log.info("OTP generated and sent successfully for user: {}", user.getEmail());
  }

  @Transactional
  public boolean verifyOtp(OtpVerificationRequest request) {
    String email = request.getEmail();
    String otp = request.getOtp();

    log.info("Verifying OTP for user: {}", email);

    // find the user
    var user = userRepository.findUserByEmail(email).
      orElseThrow( () -> {
        log.error("User not found: {}", email);
        return new UserNotFoundException("User not found with email: " + email);
      });

    // check if the user is already verified
    if ( user.isVerified() ) {
      log.warn("User is already verified: {}", email);
      throw new UserAlreadyVerifiedException("User is already verified");
    }

    // find OTP by user
    var otpByUser = otpTokenRepository.findByUser(user)
      .orElseThrow( () -> {
        log.error("OTP not found for user: {}", email);
        return new InvalidOtpException("OTP not found for user: " + email);
      });

    // check if the OTP is expired
    if (otpByUser.isExpired()) {
      log.error("OTP expired for user: {}", email);
      throw new OtpExpiredException("OTP expired for user: " + email);
    }

    // check if the OTP is used
    if (otpByUser.isUsed()) {
      log.error("OTP already used for user: {}", email);
      throw new InvalidOtpException("OTP already used for user: " + email);
    }

    // check if the OTP is correct
    if (!otpByUser.getOtp().equals(otp)) {
      log.error("Invalid OTP entered: {}", otp);
      throw new InvalidOtpException("Invalid OTP");
    }

    // mark the OTP as used and user as verified and save it
    user.setVerified(true);
    userRepository.save(user);

    otpByUser.setUsed(true);
    otpTokenRepository.save(otpByUser);

    log.info("OTP verified successfully for user: {}", email);
    return true;
  }

  @Transactional
  public void resendOtp(OtpRequest request) {
    String email = request.getEmail();
    log.info("Resending OTP for user: {}", email);

    // generate and send OTP
    generateAndSendOtp(request);
    log.info("OTP resent successfully for user: {}", email);
  }

  // schedule a cron job to delete expired OTPs every hour
  @Scheduled(cron = "0 0 * * * ?")
  public void deleteExpiredOtps() {
    log.info("Deleting expired OTPs");
    otpTokenRepository.deleteAllExpiredTokens(LocalDateTime.now());
  }
}
