package com.megatronix.paridhi.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
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
@Table(name = "teams")
public class Team {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String teamName;
  private String tid;

  @ManyToOne
  @JoinColumn(
    name = "event_id", 
    nullable = false
  )
  private Event event;

	@ElementCollection
	@CollectionTable(
		name = "team_contacts",
		joinColumns = @JoinColumn(name = "team_id")
	)
	@Builder.Default
  private List<Contact> contacts = new ArrayList<>();

  @ElementCollection
  @CollectionTable(
    name = "team_gids", 
    joinColumns = @JoinColumn(name = "team_id")
  )
	@Column(name = "gid")
	@Builder.Default
  private List<String> gidList = new ArrayList<>();

  private boolean isPaid;
  private boolean hasPlayed;
  private LocalDateTime registeredAt;
  private LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    this.tid = "TID-" + this.event.getId() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    this.registeredAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = LocalDateTime.now();
  }

	@Data
	@Embeddable
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Contact {
		private String name;
		private String contact;
	}
}
