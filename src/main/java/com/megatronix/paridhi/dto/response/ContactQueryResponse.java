package com.megatronix.paridhi.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.megatronix.paridhi.model.ContactQuery;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactQueryResponse {
	private Long id;
	private String name;
	private String email;
	private String contact;
	private String query;
	private boolean resolved;
	private String response;
	private String resolvedBy;
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createdAt;
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime updatedAt;
	
	public static ContactQueryResponse fromContactQuery(ContactQuery contactQuery) {
		return ContactQueryResponse.builder()
			.id(contactQuery.getId())
			.name(contactQuery.getName())
			.email(contactQuery.getEmail())
			.contact(contactQuery.getContact())
			.query(contactQuery.getQuery())
			.resolved(contactQuery.isResolved())
			.response(contactQuery.getResponse())
			.resolvedBy(contactQuery.getResolvedBy() != null ? contactQuery.getResolvedBy().getName() : null)
			.createdAt(contactQuery.getCreatedAt())
			.updatedAt(contactQuery.getUpdatedAt())
			.build();
	}
}