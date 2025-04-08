package com.megatronix.paridhi.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.megatronix.paridhi.constant.Domain;
import com.megatronix.paridhi.model.DomainPoster;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomainPosterResponse {
	private Long id;
	private Domain domainName;
	private Map<String, String> posterDetails;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createdAt;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime updatedAt;
	private String createdBy;
	private String updatedBy;

	public static DomainPosterResponse fromDomainPoster(DomainPoster domainPoster) {
		return DomainPosterResponse.builder()
			.id(domainPoster.getId())
			.domainName(domainPoster.getDomainName())
			.posterDetails(Map.of("secureUrl", domainPoster.getPosterSecureUrl(), "publicId", domainPoster.getPosterPublicId()))
			.createdAt(domainPoster.getCreatedAt())
			.updatedAt(domainPoster.getUpdatedAt())
			.createdBy(domainPoster.getCreatedBy().getUsername())
			.updatedBy(domainPoster.getUpdatedBy().getUsername())
			.build();
	}
}
