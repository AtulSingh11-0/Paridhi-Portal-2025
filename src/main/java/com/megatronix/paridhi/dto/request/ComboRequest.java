package com.megatronix.paridhi.dto.request;

import java.util.List;

import com.megatronix.paridhi.constant.Domain;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComboRequest {
  @NotBlank(message = "Name cannot be empty")
  private String name;
  private String description;
  
  @NotNull(message = "Domain cannot be null")
  private Domain domain;

  @NotEmpty(message = "Event IDs cannot be empty")
  private List<Long> eventIds;

  @NotNull(message = "Registration fee cannot be null")
  @Min(value = 0, message = "Registration fee cannot be negative")
  private Double registrationFee;
  private boolean isRegistrationOpen;
}
