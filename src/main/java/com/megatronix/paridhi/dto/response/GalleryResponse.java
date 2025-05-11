package com.megatronix.paridhi.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.megatronix.paridhi.model.Gallery;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GalleryResponse {
	private Long id;
	private String batchYear;
	private Map<String, String> imageDetails;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createdAt;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime updatedAt;
	private String createdBy;
	private String updatedBy;

	public static GalleryResponse fromGallery(Gallery gallery) {
		return GalleryResponse.builder()
			.id(gallery.getId())
			.batchYear(gallery.getBatchYear())
			.imageDetails(Map.of("secureUrl", gallery.getImageSecureUrl(), "publicId", gallery.getImagePublicId()))
			.createdAt(gallery.getCreatedAt())
			.updatedAt(gallery.getUpdatedAt())
			.createdBy(gallery.getCreatedBy().getUsername())
			.updatedBy(gallery.getUpdatedBy().getUsername())
			.build();
	}
}
