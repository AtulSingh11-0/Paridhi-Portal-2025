package com.megatronix.paridhi.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeamRequest {

	@NotBlank(message = "Team name cannot be empty")
	@Size(min = 2, max = 100, message = "Team name must be between 2 and 100 characters")
	private String teamName;
	
	@NotNull(message = "Event ID cannot be null")
	private Long eventId;
	
	@NotEmpty(message = "GID list cannot be empty")
	private List<String> gidList;
	
	@NotEmpty(message = "At least one contact is required")
	@Valid
	private List<ContactDTO> contacts;

	@Data
	@Builder
	@AllArgsConstructor
	public static class ContactDTO {
		@NotBlank(message = "Contact name cannot be empty")
		@Size(min = 2, max = 100, message = "Contact name must be between 2 and 100 characters")
		private String name;
		
		@NotBlank(message = "Contact number cannot be empty")
		@Pattern(regexp = "^\\d{10}$", message = "Contact must be 10 digits")
		private String contact;
	}
}
