package com.megatronix.paridhi.service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.megatronix.paridhi.constant.AppConstant;
import com.megatronix.paridhi.constant.MessageConstant;
import com.megatronix.paridhi.exception.FileUploadException;
import com.megatronix.paridhi.exception.InvalidFileException;
import com.megatronix.paridhi.util.LoggingUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for handling file uploads and deletions with Cloudinary
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryService {
	private final Cloudinary cloudinary;

	@Value("${cloudinary.folder.base:paridhi-2025}")
	private String baseFolder;

	// allowed/valid content types for image uploads
	protected static final Set<String> VALID_RESOURCE_TYPES = new HashSet<>(
		Arrays.asList(
			"image/jpeg",
			"image/png", 
			"image/jpg", 
			"image/gif", 
			"image/webp", 
			"image/svg+xml",
			"image/heic",
			"image/heif",
			"image/tiff",
			"image/bmp"
		)
	);

	/**
	 * Upload a file to Cloudinary
	 *
	 * @param file The file to upload
	 * @return Map containing the secure URL and public ID of the uploaded file
	 * @throws FileUploadException if the upload fails
	 */
	public Map<String, String> uploadFile(MultipartFile file, String subFolder) {

		// validate the file before processing
		validateFile(file);

		// generate appropriate folder based on content type
		String folder = determineFolder(file.getContentType()) + "/" + ( (subFolder != null && !subFolder.isEmpty()) ? subFolder : "" );

		// log the upload operation
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.CREATE,
			AppConstant.CLOUDINARY_SERVICE, 
			null,
			String.format("Uploading file: %s (%d bytes) to folder: %s", file.getOriginalFilename(), file.getSize(), folder)
		);

		try {
			// create a unique public id for the file
			String publicId = generatePublicId( sanitizeFilename(file.getOriginalFilename()) );

			// upload the file to cloudinary
			Map<?, ?> uploadResult = cloudinary.uploader()
			.upload(
				file.getBytes(),
				ObjectUtils.asMap(
					AppConstant.PUBLIC_ID, publicId, 
					AppConstant.CLOUDINARY_RESOURCE_TYPE, AppConstant.IMAGE, 
					AppConstant.FOLDER_KEY, folder, 
					AppConstant.OVERWRITE_KEY, true
				)
			);

			// get the secure URL of the uploaded file
			String secureImageUrl = (String) uploadResult.get(AppConstant.SECURE_URL);

			// log the successful upload
			LoggingUtil.logOperation(
				log, 
				MessageConstant.Operation.CREATE, 
				AppConstant.CLOUDINARY_SERVICE, 
				null,
				String.format(MessageConstant.SuccessTemplate.CREATED, AppConstant.CLOUDINARY_IMAGE, secureImageUrl)
			);

			// return a map with the secure URL and public ID
			Map<String, String> result = new HashMap<>();
			result.put(AppConstant.SECURE_URL, secureImageUrl);
			result.put(AppConstant.PUBLIC_ID, folder + "/" + publicId);

			return result;
		} catch (IOException e) {
			// log the error
			LoggingUtil.logError(
				log, 
				MessageConstant.Operation.CREATE, 
				AppConstant.CLOUDINARY_SERVICE, 
				null,
				String.format(MessageConstant.ErrorTemplate.OPERATION_FAILED, "upload file", file.getOriginalFilename(), e.getMessage()), 
				e
			);
			throw new FileUploadException(MessageConstant.UserMessage.FILE_UPLOAD_ERROR, e);
		}
	}

	/**
	 * Delete a file from Cloudinary using its public ID
	 *
	 * @param publicId The public ID of the file to delete
	 * @throws FileUploadException if the deletion fails
	 */
	public void deleteFile(String publicId) {
		
		// validate the public ID before processing
		validatePublicId(publicId);

		// log the delete operation
		LoggingUtil.logOperation(
			log, 
			MessageConstant.Operation.DELETE, 
			AppConstant.CLOUDINARY_SERVICE, 
			null,
			String.format("Deleting file from Cloudinary with public ID: %s", publicId)
		);

		try {
			// delete the file from cloudinary
			cloudinary.uploader()
			.destroy(
				publicId, 
				ObjectUtils.asMap(
					AppConstant.RESOURCE_TYPE, AppConstant.IMAGE, 
					AppConstant.INVALIDATE_KEY, true
				)
			);

			// log the successful deletion
			LoggingUtil.logOperation(
				log, 
				MessageConstant.Operation.DELETE, 
				AppConstant.CLOUDINARY_SERVICE, 
				null,
				String.format(MessageConstant.SuccessTemplate.DELETED, AppConstant.CLOUDINARY_IMAGE, publicId)
			);
		} catch (IOException e) {
			// log the error
			LoggingUtil.logError(
				log, 
				MessageConstant.Operation.DELETE, 
				AppConstant.CLOUDINARY_SERVICE, 
				null,
				String.format(MessageConstant.ErrorTemplate.OPERATION_FAILED, "delete file", publicId, e.getMessage()), 
				e
			);
			throw new FileUploadException(MessageConstant.UserMessage.FILE_DELETION_ERROR, e);
		}
	}

	/**
	 * Generate a unique public ID for a file
	 *
	 * @param filename The original filename
	 * @return A unique public ID
	 */
	private String generatePublicId(String filename) {
		String baseFilename = ( filename != null && !filename.isEmpty() ) ? filename : "file";
		return baseFilename + "_" + LocalDate.now() + "_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
	}

	/**
	 * Sanitized the filename to remove Directory traversal, invalid characters and ensure a valid format
	 *
	 * @param filename The sanitized filename
	 * @return The sanitized filename
	 */
	private String sanitizeFilename(String filename) {
		// check if the filename is null or empty
		if (filename == null || filename.isEmpty()) {
			return "unnamed";
		}

		// remove path traversal characters and restrict to safe characters
		String sanitizedFileName = filename.replaceAll("[^a-zA-Z0-9.-]", "_")
		.replaceAll("\\.{2,}", ".") // replace multiple dots with a single dot
		.replaceAll("_+", "_") // replace multiple underscores with a single underscore
		.replaceAll("^[_.-]+", "") // remove leading underscores, dots, or hyphens
		.replaceAll("[_.-]+$", "") // remove trailing underscores, dots, or hyphens
		.toLowerCase();

		// return the sanitized filename or a default name if empty
		return sanitizedFileName.isEmpty() ? "file" : sanitizedFileName;
	}

	/**
	 * Determines the folder to upload the file based on its content type
	 * 
	 * @param contentType The content type of the file
	 * @return The folder path for the file
	 */
	private String determineFolder(String contentType) {
		// check if content type is not null
		if (contentType == null) {
			return baseFolder;
		}

		// determine the folder based on content type
		if (contentType.startsWith("image/")) {
			return baseFolder + "/images";
		} else if (contentType.startsWith("video/")) {
			return baseFolder + "/videos";
		} else if (contentType.startsWith("application/")) {
			return baseFolder + "/documents";
		} else {
			return baseFolder + "/others";
		}
	}

	/**
	 * Validates the public ID before processing
	 *
	 * @param publicId The public ID to validate
	 * @throws InvalidFileException if the public ID is invalid
	 */
	private void validatePublicId(String publicId) {
		// check if public ID is null or empty
		if (publicId == null || publicId.trim().isEmpty()) {
			throw new InvalidFileException("Public ID cannot be null or empty");
		}

		// check if public ID contains invalid characters
		// if (!publicId.matches("[a-zA-Z0-9/_-]+")) {
		// 	throw new InvalidFileException("Invalid public ID format: " + publicId);
		// }
	}

	/**
	 * Validates the public ID before processing
	 *
	 * @param publicId The public ID to validate
	 * @throws InvalidFileException if the public ID is invalid
	 */
	private void validateFile(MultipartFile file) {
		// check if file is null
		if (file == null) {
			throw new InvalidFileException("File cannot be null");
		}

		// check if file is empty
		if (file.isEmpty()) {
			throw new InvalidFileException("File cannot be empty");
		}

		// check if file size exceeds the limit (10MB in this case)
		if (file.getSize() > AppConstant.MAX_IMAGE_SIZE) {
			throw new InvalidFileException("File size exceeds the limit of 10MB");
		}

		// check if file content type is valid
		String contentType = file.getContentType();
		if (
			contentType != null &&
			!contentType.startsWith("image/") &&
			!VALID_RESOURCE_TYPES.contains(contentType)
		) {
			throw new InvalidFileException(String.format("Invalid image format: %s, Allowed formats: JPEG, PNG, GIF, WebP, SVG, HEIC, HEIF, TIFF, BMP", contentType));
		}
	}
}