package com.megatronix.paridhi.dto.response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
	private int status;
	private String message;
	private String error;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
	private LocalDateTime timestamp;
	
	private String path;
	
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private List<ValidationError> validationErrors;

	// Inner class for validation errors with field-specific details
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ValidationError {
		private String field;
		private String message;
	}
	
	// Helper method to add field validation errors
	public void addValidationError(String field, String message) {
		if (validationErrors == null) {
			validationErrors = new ArrayList<>();
		}
		validationErrors.add(new ValidationError(field, message));
	}
}
