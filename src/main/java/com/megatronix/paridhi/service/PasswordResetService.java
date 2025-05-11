package com.megatronix.paridhi.service;

import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.megatronix.paridhi.exception.InvalidResetTokenException;
import com.megatronix.paridhi.exception.TokenExpiredException;
import com.megatronix.paridhi.exception.UserNotFoundException;
import com.megatronix.paridhi.constant.AppConstant;
import com.megatronix.paridhi.constant.MessageConstant;
import com.megatronix.paridhi.dto.request.PasswordResetConfirmationRequest;
import com.megatronix.paridhi.dto.request.PasswordResetRequest;
import com.megatronix.paridhi.model.PasswordResetToken;
import com.megatronix.paridhi.repository.PasswordResetTokenRepository;
import com.megatronix.paridhi.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {
  private final EmailService emailService;
  private final UserRepository userRepository;
  private final BCryptPasswordEncoder passwordEncoder;
  private final PasswordResetTokenRepository passwordResetTokenRepository;

  @Transactional
  public String requestPasswordReset(PasswordResetRequest request) {
    // get email from request
    String email = request.getEmail();
    log.info("Processing password reset request for email: {}", email);

    var user = userRepository.findUserByEmail(email)
      .orElseThrow( () -> {
        log.error("User not found with email: {}", email);
        return new UserNotFoundException(String.format(MessageConstant.ErrorTemplate.NOT_FOUND, AppConstant.USER));
      });

    // delete any existing password reset token for the user
    Optional<PasswordResetToken> existingToken = passwordResetTokenRepository.findByUser(user);
    
    if ( existingToken.isPresent() ) {
      passwordResetTokenRepository.delete(existingToken.get());
      passwordResetTokenRepository.flush();
    }

    // create new password reset token and save it
    var resetToken = PasswordResetToken.builder()
      .user(user)
      .build();
    var savedResetToken = passwordResetTokenRepository.save(resetToken);
    
    // send email with the token to the user
    emailService.sendPasswordResetToken(user.getEmail(), user.getName(), savedResetToken.getToken());

    log.info("Password reset token generated and sent successfully to {}", email);
    return "Password reset instructions sent successfully to your email";
  }

  @Transactional
  public void confirmPasswordReset(PasswordResetConfirmationRequest request) {
    // get token from request
    String token = request.getToken();
    String newPassword = request.getNewPassword();
    log.info("Processing password reset confirmation for token: {}", token);

    // find token in database
    var passwordResetToken = passwordResetTokenRepository.findByToken(token)
      .orElseThrow( () -> {
        log.error("Password reset token not found: {}", token);
        return new InvalidResetTokenException("Invalid password reset token");
      });

    // check if token is expired or already used
    if ( passwordResetToken.isExpired() ) {
      log.error("Password reset token has expired: {}", token);
      passwordResetTokenRepository.delete(passwordResetToken);
      throw new TokenExpiredException("Password reset token has expired");
    }
    if (passwordResetToken.isUsed()) {
      log.error("Password reset token has already been used: {}", token);
      throw new InvalidResetTokenException("Password reset token has already been used");
    }

    // update user password
    var user = passwordResetToken.getUser();
    user.setPassword(passwordEncoder.encode(newPassword));
    userRepository.save(user);

    // mark token as used
    passwordResetToken.setUsed(true);
    passwordResetTokenRepository.save(passwordResetToken);

    log.info("Password reset successful for user: {}", user.getEmail());
  }

  public boolean validateToken(String token) {
    log.info("Validating password reset token: {}", token);
    
    // check if token exists and is not expired or used
    return passwordResetTokenRepository.findByToken(token)
      .map(resetToken -> !resetToken.isExpired() && !resetToken.isUsed())
      .orElse(false);
  }
}
