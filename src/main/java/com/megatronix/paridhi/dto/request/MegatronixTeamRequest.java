package com.megatronix.paridhi.dto.request;

import com.megatronix.paridhi.constant.Designation;
import com.megatronix.paridhi.constant.Year;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MegatronixTeamRequest {
	@NotBlank(message = "Name is required")
	private String name;
	
	@NotBlank(message = "Email is required")
	@Email(message = "Invalid email format")
	private String email;
	
	@NotNull(message = "Year is required")
	private Year year;
	
	@Pattern(regexp = "^(https?://.*|N/A)?$", message = "LinkedIn link must be a valid URL or empty")
	private String linkedInLink;
	
	@Pattern(regexp = "^(https?://.*|N/A)?$", message = "Facebook link must be a valid URL or empty")
	private String facebookLink;
	
	@Pattern(regexp = "^(https?://.*|N/A)?$", message = "Instagram link must be a valid URL or empty")
	private String instagramLink;
	
	@Pattern(regexp = "^(https?://.*|N/A)?$", message = "GitHub link must be a valid URL or empty")
	private String githubLink;
	
	@Pattern(regexp = "^(https?://.*)?$", message = "Image link must be a valid URL or empty")
	@NotBlank(message = "Image link is required")
	private String imageLink;

	@NotNull(message = "Designation is required")
	private Designation designation;
}