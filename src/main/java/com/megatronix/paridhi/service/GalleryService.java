package com.megatronix.paridhi.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.megatronix.paridhi.constant.Role;
import com.megatronix.paridhi.dto.response.GalleryResponse;
import com.megatronix.paridhi.exception.ForbiddenAccessException;
import com.megatronix.paridhi.exception.GalleryNotFoundException;
import com.megatronix.paridhi.model.Gallery;
import com.megatronix.paridhi.model.User;
import com.megatronix.paridhi.repository.GalleryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GalleryService {
	private final GalleryRepository galleryRepository;
	private final CloudinaryService cloudinaryService;

	public Page<GalleryResponse> getAllImages(int page, int size) {
		log.info("Fetching all images with page: {} and size: {}", page, size);
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
		return galleryRepository.findAll(pageable)
			.map(GalleryResponse::fromGallery);
	}

	public List<GalleryResponse> getImageByParidhiYear(String paridhiYear) {
		log.info("Fetching images by Paridhi year: {}", paridhiYear);
		return galleryRepository.findByParidhiYear(paridhiYear).stream()
			.map(GalleryResponse::fromGallery)
			.toList();
	}

	public GalleryResponse getImageById(Long id) {
		log.info("Fetching image with id: {}", id);
		return galleryRepository.findById(id)
			.map(GalleryResponse::fromGallery)
			.orElseThrow(() -> {
				log.error("Image with id: {} not found", id);
				return new GalleryNotFoundException("Image not found");
			});
	}

	@Transactional
	public GalleryResponse uploadImage(String paridhiYear, MultipartFile image, User user) {
		log.info("Uploading image: {}, for Paridhi year: {}, by user: {}", image.getOriginalFilename(), paridhiYear, user.getUsername());
		
		// check if user has access
		checkUserAccess(user, "upload");

		// Check if the image is empty
		if (image.isEmpty()) {
			log.error("Image file is empty");
			throw new IllegalArgumentException("Image file is empty");
		}

		// upload image to cloudinary
		var result = cloudinaryService.uploadFile(image);

		// create and save gallery object
		Gallery gallery = Gallery.builder()
			.paridhiYear(paridhiYear)
			.imageSecureUrl(result.get("secure_url"))
			.imagePublicId(result.get("public_id"))
			.createdBy(user)
			.updatedBy(user)
			.build();

		Gallery savedImage = galleryRepository.save(gallery);
		log.info("Image uploaded successfully with id: {}", savedImage.getId());
		
		return GalleryResponse.fromGallery(savedImage);
	}

	@Transactional
	public GalleryResponse updateImageFile(Long id, MultipartFile image, User user) {
		log.info("Updating image with id: {} by user: {}", id, user.getUsername());
		
		// check if user has access
		checkUserAccess(user, "update");

		// Check if the image is empty
		if (image.isEmpty()) {
			log.error("Image file is empty");
			throw new IllegalArgumentException("Image file is empty");
		}

		// Check if the image exists
		var existingImage = galleryRepository.findById(id)
			.orElseThrow(() -> {
				log.error("Gallery Image not found with ID: {}", id);
				return new GalleryNotFoundException("Gallery Image not found with ID: " + id);
			});
		
		// delete image from cloudinary
		cloudinaryService.deleteFile(existingImage.getImagePublicId());
		log.info("Image with public ID: {} deleted from cloudinary", existingImage.getImagePublicId());

		// upload new image to cloudinary
		var result = cloudinaryService.uploadFile(image);
		
		existingImage.setImageSecureUrl(result.get("secure_url"));
		existingImage.setImagePublicId(result.get("public_id"));
		existingImage.setUpdatedBy(user);

		var savedImage = galleryRepository.save(existingImage);
		log.info("Image updated successfully with id: {}", savedImage.getId());
		
		return GalleryResponse.fromGallery(savedImage);
	}

	@Transactional
	public void deleteImage(Long id, User user) {
		log.info("Deleting image with id: {}", id);
		
		// check if user has access
		checkUserAccess(user, "delete");

		// Check if the image exists
		var existingImage = galleryRepository.findById(id)
			.orElseThrow(() -> {
				log.error("Gallery Image not found with ID: {}", id);
				return new GalleryNotFoundException("Gallery Image not found with ID: " + id);
			});
		
		// delete image from cloudinary
		cloudinaryService.deleteFile(existingImage.getImagePublicId());
		log.info("Image with public ID: {} deleted from cloudinary", existingImage.getImagePublicId());

		galleryRepository.delete(existingImage);
		log.info("Image with id: {} deleted successfully", id);
	}

	private void checkUserAccess(User user, String methodType) {
		if (user == null) {
			log.error("User is not authenticated to {} image", methodType);
			throw new ForbiddenAccessException("User not authenticated to " + methodType + " image");
		}

		if (user.getRole().equals(Role.ROLE_USER)) {
			log.error("User: {} not authorized to {} image", user == null ? "System" : user.getUsername(), methodType);
			throw new ForbiddenAccessException("User not authorized to " + methodType + " image");
		}
	}
}
