package com.megatronix.paridhi.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactQueryRequest {
	@NotBlank(message = "Name cannot be empty")
	private String name;
	
	@NotBlank(message = "Email cannot be empty")
	@Email(message = "Email should be valid")
	private String email;
	
	@NotBlank(message = "Contact cannot be empty")
	@Pattern(regexp = "^\\d{10}$", message = "Contact must be 10 digits")
	private String contact;
	
	@NotBlank(message = "Query cannot be empty")
	@Size(min = 10, max = 1000, message = "Query must be between 10 and 1000 characters")
	private String query;
}