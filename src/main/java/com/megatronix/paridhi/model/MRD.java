package com.megatronix.paridhi.model;

import java.time.LocalDateTime;

import com.megatronix.paridhi.constant.Department;
import com.megatronix.paridhi.constant.Year;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
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
@Table(name = "mrd_registrations")
public class MRD {
  @Id
  @GeneratedValue( strategy = GenerationType.IDENTITY )
  private Long id;
  
  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(unique = true, nullable = false)
  private String gid;
  private String name;
  private String email;
  private String contact;
  private String college;

  @Enumerated(EnumType.STRING)
  private Year year;

  @Enumerated(EnumType.STRING)
  private Department department;

  private String rollNo;
  private boolean isPaid;

  private LocalDateTime registeredAt;
  private LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    this.registeredAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = LocalDateTime.now();
  }
}
