package com.megatronix.paridhi.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.megatronix.paridhi.constant.Category;
import com.megatronix.paridhi.model.TeamPhoto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamPhotoResponse {
	private Long id;
	private Category category;
	private Map<String, String> imageDetails;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createdAt;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime updatedAt;
	private String createdBy;
	private String updatedBy;

	public static TeamPhotoResponse fromTeamPhoto(TeamPhoto teamPhoto) {
		return TeamPhotoResponse.builder()
			.id(teamPhoto.getId())
			.category(teamPhoto.getCategory())
			.imageDetails(Map.of("secureUrl", teamPhoto.getImageSecureUrl(), "publicId", teamPhoto.getImagePublicId()))
			.createdAt(teamPhoto.getCreatedAt())
			.updatedAt(teamPhoto.getUpdatedAt())
			.createdBy(teamPhoto.getCreatedBy().getUsername())
			.updatedBy(teamPhoto.getUpdatedBy().getUsername())
			.build();
	}
}
