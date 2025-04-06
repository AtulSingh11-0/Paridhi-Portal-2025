package com.megatronix.paridhi.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.megatronix.paridhi.constant.Domain;
import com.megatronix.paridhi.constant.EventType;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
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
@Table(name = "events")
public class Event {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  private Domain domain;

  private String name;
	
	@Enumerated(EnumType.STRING)
  private EventType eventType; // MAIN or ON_SPOT
  private LocalDateTime eventDate;
  
  @Column(length = 2000)
  private String description;
  private String venue;

  @ElementCollection(
		fetch = FetchType.EAGER
	)
  @CollectionTable(
    name = "event_coordinator_details", 
    joinColumns = @JoinColumn(name = "event_id")
  )
	@Builder.Default
  private List<String> coordinatorDetails = new ArrayList<>();
  private String eventPictureSecureUrl;
	private String eventPicturePublicId;
  private String ruleBook; // PDF drive link
  private Integer minPlayers;
  private Integer maxPlayers;
  private Double registrationFee;
  private boolean isRegistrationOpen;
  private Double prizePool;

  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  @ManyToOne
  @JoinColumn(name = "created_by")
  private User createdBy;

  @ManyToOne
  @JoinColumn(name = "updated_by")
  private User updatedBy;

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = LocalDateTime.now();
  }
}