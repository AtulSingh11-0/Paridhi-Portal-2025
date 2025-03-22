package com.megatronix.paridhi.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.megatronix.paridhi.constant.Domain;
import com.megatronix.paridhi.constant.EventType;
import com.megatronix.paridhi.model.Event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {
  private Long id;
  private Domain domain;
  private String name;
  private EventType eventType; // MAIN or ON_SPOT
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime eventDate;
  private String description;
  private String venue;
  private List<String> coordinatorDetails;
  private String eventPictureUrl;
  private String ruleBook;
  private Integer minPlayers;
  private Integer maxPlayers;
  private double registrationFee;
  private boolean isRegistrationOpen;
  private Double prizePool;
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime createdAt;
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime updatedAt;
  private String createdByUserName; 

  public static EventResponse fromEvent(Event event) {
    return EventResponse.builder()
    .id(event.getId())
    .domain(event.getDomain())
    .name(event.getName())
    .eventType(event.getEventType())
    .eventDate(event.getEventDate())
    .description(event.getDescription())
    .venue(event.getVenue())
    .coordinatorDetails(event.getCoordinatorDetails())
    .eventPictureUrl(event.getEventPictureUrl())
    .ruleBook(event.getRuleBook())
    .minPlayers(event.getMinPlayers())
    .maxPlayers(event.getMaxPlayers())
    .registrationFee(event.getRegistrationFee())
    .isRegistrationOpen(event.isRegistrationOpen())
    .prizePool(event.getPrizePool())
    .createdAt(event.getCreatedAt())
    .updatedAt(event.getUpdatedAt())
    .createdByUserName(event.getCreatedBy() != null ? event.getCreatedBy().getName() : "System")
    .build();
  }
}
