package com.megatronix.paridhi.dto.request;

import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComboTeamRequest {
  @NotNull(message = "Combo ID cannot be null")
  private Long comboId;

  @NotBlank(message = "Team name cannot be empty")
  @Size(min = 2, max = 100, message = "Team name must be between 2 and 100 characters")
  private String teamName;

  @NotEmpty(message = "Event GID mappings cannot be empty")
  private Map<Long, List<String>> eventGidMap;

	@NotEmpty(message = "At least one contact is required")
	@Valid
	private List<TeamRequest.ContactDTO> contacts;
}
