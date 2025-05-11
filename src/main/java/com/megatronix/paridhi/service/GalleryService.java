package com.megatronix.paridhi.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.megatronix.paridhi.constant.AppConstant;
import com.megatronix.paridhi.constant.MessageConstant;
import com.megatronix.paridhi.constant.Role;
import com.megatronix.paridhi.dto.response.GalleryResponse;
import com.megatronix.paridhi.exception.ForbiddenAccessException;
import com.megatronix.paridhi.exception.GalleryNotFoundException;
import com.megatronix.paridhi.model.Gallery;
import com.megatronix.paridhi.model.User;
import com.megatronix.paridhi.repository.GalleryRepository;
import com.megatronix.paridhi.util.LoggingUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GalleryService {
	private final GalleryRepository galleryRepository;
	private final CloudinaryService cloudinaryService;
	
	/*
	 * public methods - doesn't require authentication
	 */
		
	public Page<GalleryResponse> getAllImages(int page, int size) {
		// log the operation
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.READ,
			AppConstant.GALLERY_SERVICE,
			null,
			"Fetching gallery images with pagination: page=" + page + ", size=" + size
		);

		// create a pageable object with sorting by createdAt in descending order
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
		Page<Gallery> galleryPage = galleryRepository.findAll(pageable);

		// log the number of images retrieved
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.READ,
			AppConstant.GALLERY_SERVICE,
			null,
			"Retrieved " + galleryPage.getNumberOfElements() + " gallery images (page " + 
			(page + 1) + " of " + galleryPage.getTotalPages() + ")"
		);
    
		// return the gallery images as a list of GalleryResponse objects
		return galleryPage.map(GalleryResponse::fromGallery);
	}

	public List<GalleryResponse> getImageByBatchYear(String batchYear) {
		// log the operation
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.READ,
			AppConstant.GALLERY_SERVICE,
			null,
			"Fetching gallery images for Paridhi year: " + batchYear
		);

		// fetch gallery images by batchYear from the database
		List<Gallery> gallery = galleryRepository.findByBatchYear(batchYear);

		// log the number of images retrieved
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.READ,
			AppConstant.GALLERY_SERVICE,
			null,
			"Retrieved " + gallery.size() + " gallery images for Paridhi year: " + batchYear
		);

		// return the gallery images as a list of GalleryResponse objects
		return gallery.stream()
			.map(GalleryResponse::fromGallery)
			.toList();
	}

	public GalleryResponse getImageById(Long id) {
		// log the operation
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.READ,
			AppConstant.GALLERY_SERVICE,
			null,
			"Fetching gallery image with ID: " + id
		);
		
		// fetch gallery image by ID from the database
		var existingImage = fetchGalleryById(id, null);
		
		// log the successful retrieval of the image
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.READ,
			AppConstant.GALLERY_SERVICE,
			null,
			"Successfully retrieved gallery image with ID: " + id
		);
		
		// return the GalleryResponse object
		return GalleryResponse.fromGallery(existingImage);
	}

	/*
	 * protected methods - requires authentication
	 */

	@Transactional
	public GalleryResponse uploadImage(String batchYear, MultipartFile image, User user) {
		// log the operation
		LoggingUtil.logOperation(
				log,
				MessageConstant.Operation.CREATE,
				AppConstant.GALLERY_SERVICE,
				user,
				"Uploading new gallery image: " + image.getOriginalFilename() + 
				" (" + image.getSize() + " bytes)" + " for batchYear year: " + batchYear
			);
		
		// validate user access
		checkUserAccess(user, MessageConstant.Operation.CREATE);

		// upload image to cloudinary
		var result = cloudinaryService.uploadFile(image, AppConstant.GALLERY + "/" + batchYear);

		// create and save gallery object
		Gallery gallery = Gallery.builder()
			.batchYear(batchYear)
			.imageSecureUrl(result.get(AppConstant.SECURE_URL))
			.imagePublicId(result.get(AppConstant.PUBLIC_ID))
			.createdBy(user)
			.updatedBy(user)
			.build();
		Gallery savedGallery = galleryRepository.save(gallery);
		
		// log the successful upload of the image
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.CREATE,
			AppConstant.GALLERY_SERVICE,
			user,
			"Successfully uploaded gallery image with ID: " + savedGallery.getId()
		);
		
		// return the GalleryResponse object
		return GalleryResponse.fromGallery(savedGallery);
	}

	@Transactional
	public GalleryResponse updateImageFile(Long id, MultipartFile image, User user) {
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.UPDATE,
			AppConstant.GALLERY_SERVICE,
			user,
			"Updating gallery image with ID: " + id
		);  
		
		// validate user access
		checkUserAccess(user, MessageConstant.Operation.UPDATE);

		// check if the image exists
		var existingImage = fetchGalleryById(id, user);
		
		// delete image from cloudinary
		cloudinaryService.deleteFile(existingImage.getImagePublicId());

		// upload new image to cloudinary and save it
		var result = cloudinaryService.uploadFile(image, AppConstant.GALLERY + "/" + existingImage.getBatchYear());
		existingImage.setImageSecureUrl(result.get(AppConstant.SECURE_URL));
		existingImage.setImagePublicId(result.get(AppConstant.PUBLIC_ID));
		existingImage.setUpdatedBy(user);

		var savedImage = galleryRepository.save(existingImage);
		
		// log the successful update of the image
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.UPDATE,
			AppConstant.GALLERY_SERVICE,
			user,
			"Successfully updated gallery image with ID: " + savedImage.getId()
		);
		
		// return the GalleryResponse object
		return GalleryResponse.fromGallery(savedImage);
	}

	@Transactional
	public void deleteImage(Long id, User user) {
		// log the operation
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.DELETE,
			AppConstant.GALLERY_SERVICE,
			user,
			"Deleting gallery image with ID: " + id
		);
		
		// validate user access
		checkUserAccess(user, MessageConstant.Operation.DELETE);

		// check if the image exists
		var existingGallery = fetchGalleryById(id, user);
		
		try {
			LoggingUtil.logOperation(
				log,
				MessageConstant.Operation.DELETE,
				AppConstant.CLOUDINARY_IMAGE,
				user,
				"Deleting image with public ID: " + existingGallery.getImagePublicId()
			);
			cloudinaryService.deleteFile(existingGallery.getImagePublicId());
		} catch (Exception e) {
			LoggingUtil.logError(
				log,
				MessageConstant.Operation.DELETE,
				AppConstant.CLOUDINARY_IMAGE,
				user,
				"Failed to delete image from Cloudinary with public ID: " + existingGallery.getImagePublicId() + 
				" - Continuing with deletion from database",
				e
			);
			// continue with gallery deletion even if cloudinary deletion fails
		}
		
		// delete the gallery image from the database
		galleryRepository.delete(existingGallery);
		
		// log the successful deletion of the image
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.DELETE,
			AppConstant.GALLERY_SERVICE,
			user,
			"Successfully deleted gallery image with ID: " + id
		);
	}

	/*
	 * private methods - used internally only
	 */

	private void checkUserAccess(User user, String operation) {
		// check if user is null
		if (user == null) {
			LoggingUtil.logSecurity(
				log,
				AppConstant.ACCESS_DENIED,
				null,
				AppConstant.FAILED,
				"Authentication required to " + operation + " " + AppConstant.GALLERY
			);
			throw new ForbiddenAccessException(MessageConstant.UserMessage.ACCESS_DENIED);
		}
		
		// check if user has ROLE_USER
		if (user.getRole().equals(Role.ROLE_USER)) {
			LoggingUtil.logSecurity(
				log,
				AppConstant.ACCESS_DENIED,
				user,
				AppConstant.FAILED,
				"User lacks permission to " + operation + " " + AppConstant.GALLERY
			);
			throw new ForbiddenAccessException(MessageConstant.UserMessage.ACCESS_DENIED);
		}
	
		// log the successful authorization
		LoggingUtil.logSecurity(
			log,
			AppConstant.ACCESS_GRANTED,
			user,
			AppConstant.SUCCESS,
			"User authorized to " + operation + " " + AppConstant.GALLERY
		);
	}

	private Gallery fetchGalleryById(Long id, User user) {
		return galleryRepository.findById(id)
			.orElseThrow(() -> {
				LoggingUtil.logError(
					log,
					MessageConstant.Operation.READ,
					AppConstant.GALLERY_SERVICE,
					user,
					String.format(MessageConstant.ErrorTemplate.NOT_FOUND, AppConstant.GALLERY_IMAGE, id),
					null
				);
				return new GalleryNotFoundException(String.format(MessageConstant.ErrorTemplate.NOT_FOUND, AppConstant.GALLERY_IMAGE));
			});
	}
}
