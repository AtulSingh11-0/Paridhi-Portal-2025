package com.megatronix.paridhi.model;

import java.time.LocalDateTime;
import java.util.Random;

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
@Table(name = "otp_tokens")
public class OtpToken {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 6)
  private String otp;

  @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
  @JoinColumn(nullable = false, name = "user_id", unique = true)
  private User user;

  @Column(nullable = false)
  private LocalDateTime expiryDate;

  private boolean used;

  @PrePersist
  protected void onCreate() {
    if (otp == null) {
      // generate 6-digit OTP
      Random random = new Random();
      int otpNum = 100000 + random.nextInt(900000); // 6-digit number between 100000 and 999999
      this.otp = String.valueOf(otpNum);
    }
    // OTP valid for 10 minutes
    this.expiryDate = LocalDateTime.now().plusMinutes(10);
    this.used = false;
  }

  public boolean isExpired() {
    return LocalDateTime.now().isAfter(expiryDate);
  }
}
