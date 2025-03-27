package com.megatronix.paridhi.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.megatronix.paridhi.constant.Domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
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
@Table(name = "event_combos")
public class EventCombo {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @Enumerated(EnumType.STRING)
  private Domain domain;

  @ManyToMany(
		cascade = {
			CascadeType.PERSIST,
			CascadeType.MERGE
		}
	)
  @JoinTable(
    name = "event_combo_mappings",
    joinColumns = @JoinColumn(name = "combo_id"),
    inverseJoinColumns = @JoinColumn(name = "event_id")
  )
  @Builder.Default
  private Set<Event> events = new HashSet<>();

  private String name;
  
  @Column(length = 2000)
  private String description;

  private Double registrationFee;
  private boolean isRegistrationOpen;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  @ManyToOne
  @JoinColumn(name = "created_by")
  private User createdBy;

  @ManyToOne
  @JoinColumn(name = "updated_by")
  private User updatedBy;

  @PrePersist
  public void onCreate() {
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  public void onUpdate() {
    this.updatedAt = LocalDateTime.now();
  }
}
