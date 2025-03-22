package com.megatronix.paridhi.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.megatronix.paridhi.model.OtpToken;
import com.megatronix.paridhi.model.User;


@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {
  Optional<OtpToken> findByUser(User user);
  Optional<OtpToken> findByOtp(String otp);
  boolean existsByUserAndUsed(User user, boolean used);

  @Modifying
  @Transactional
  @Query("DELETE FROM OtpToken o WHERE o.expiryDate < ?1")
  void deleteAllExpiredTokens(LocalDateTime now);
}
