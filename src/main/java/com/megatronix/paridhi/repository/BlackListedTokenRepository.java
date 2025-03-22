package com.megatronix.paridhi.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.megatronix.paridhi.model.BlackListedToken;

@Repository
public interface BlackListedTokenRepository extends JpaRepository<BlackListedToken, Long> {
  boolean existsByToken(String token);
  
  @Query("SELECT bt FROM BlackListedToken bt WHERE bt.expiresAt < ?1")
  List<BlackListedToken> findAllExpiredTokens(LocalDateTime now);

  @Modifying
  @Transactional
  @Query("DELETE FROM BlackListedToken bt WHERE bt.expiresAt < ?1")
  void deleteAllExpiredTokens(LocalDateTime now);
}
