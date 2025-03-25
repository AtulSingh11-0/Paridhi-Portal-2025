package com.megatronix.paridhi.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.megatronix.paridhi.constant.Domain;
import com.megatronix.paridhi.model.EventCombo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComboResponse {
  private Long id;
  private String name;
  private String description;
  private Domain domain;
  private List<EventResponse> events;
  private Double registrationFee;
  private boolean isRegistrationOpen;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime createdAt;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime updatedAt;

  private String createdByUsername;

  public static ComboResponse fromCombo(EventCombo combo) {
    return ComboResponse.builder()
      .id(combo.getId())
      .name(combo.getName())
      .description(combo.getDescription())
      .domain(combo.getDomain())
      .events(combo.getEvents().stream()
              .map(EventResponse::fromEvent)
              .toList())
      .registrationFee(combo.getRegistrationFee())
      .isRegistrationOpen(combo.isRegistrationOpen())
      .createdAt(combo.getCreatedAt())
      .updatedAt(combo.getUpdatedAt())
      .createdByUsername(combo.getCreatedBy() != null ? combo.getCreatedBy().getName() : "System")
      .build();
  }
}
