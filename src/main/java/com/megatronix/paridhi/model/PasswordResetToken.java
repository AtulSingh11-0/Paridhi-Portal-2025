package com.megatronix.paridhi.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "password_reset_tokens")
public class PasswordResetToken {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String token;

  @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
  @JoinColumn(nullable = false, name = "user_id")
  private User user;

  @Column(name = "expiry_date", nullable = false)
  private LocalDateTime expiryDate;
  private boolean used;

  @PrePersist
  protected void onCreate() {
    if ( token == null ) {
      this.token = UUID.randomUUID().toString().substring(0, 6);
    }
    // token will be valid for 10 minutes
    this.expiryDate = LocalDateTime.now().plusMinutes(10);
    this.used = false;
  }

  public boolean isExpired() {
    return LocalDateTime.now().isAfter(this.expiryDate);
  }
}
