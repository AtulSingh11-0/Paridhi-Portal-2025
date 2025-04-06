package com.megatronix.paridhi.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.megatronix.paridhi.dto.response.GalleryResponse;
import com.megatronix.paridhi.model.User;
import com.megatronix.paridhi.service.GalleryService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/galleries")
public class GalleryController {
	private final GalleryService galleryService;

	// Public endpoints
	
	@GetMapping
	public ResponseEntity<Page<GalleryResponse>> getAllImages(
		@RequestParam(name = "page", defaultValue = "0") int page, 
		@RequestParam(name = "size", defaultValue = "12") int size
	) {
		return ResponseEntity.ok(galleryService.getAllImages(page, size));
	}

	@GetMapping("/year/{paridhiYear}")
	public ResponseEntity<List<GalleryResponse>> getImageByParidhiYear(
		@PathVariable String paridhiYear
	) {
		return ResponseEntity.ok(galleryService.getImageByParidhiYear(paridhiYear));
	}

	@GetMapping("/{id}")
	public ResponseEntity<GalleryResponse> getImageById(
		@PathVariable Long id
	) {
		return ResponseEntity.ok(galleryService.getImageById(id));
	}

	// Authorized endpoints

	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
	@PostMapping(
		consumes = MediaType.MULTIPART_FORM_DATA_VALUE
	)
	public ResponseEntity<GalleryResponse> uploadImage(
		@RequestPart("paridhiYear") String paridhiYear,
		@RequestPart("image") MultipartFile image,
		@AuthenticationPrincipal User user
	) {
		return ResponseEntity.status(HttpStatus.CREATED).body(galleryService.uploadImage(paridhiYear, image, user));
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
	@PutMapping(
		value = "/{id}",
		consumes = MediaType.MULTIPART_FORM_DATA_VALUE
	)
	public ResponseEntity<GalleryResponse> updateImage(
		@PathVariable Long id,
		@RequestPart(value = "image") MultipartFile image,
		@AuthenticationPrincipal User user
	) {
		return ResponseEntity.ok(galleryService.updateImageFile(id, image, user));
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteImage(
		@PathVariable Long id,
		@AuthenticationPrincipal User user
	) {
		galleryService.deleteImage(id, user);
		return ResponseEntity.noContent().build();
	}
}
