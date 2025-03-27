package com.megatronix.paridhi.service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.megatronix.paridhi.exception.FileUploadException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryService {
	private final Cloudinary cloudinary;

	public Map<String, String> uploadFile(MultipartFile file) {
		try {
			// create a folder structure for the file
			String folder = "paridhi-2025/events";

			// create a unique public id for the file
			String publicId = file.getOriginalFilename() + "/" + LocalDate.now() + "/" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

			// upload the file to cloudinary
			Map<?, ?> uploadResult = cloudinary.uploader().upload(
				file.getBytes(), 
				ObjectUtils.asMap(
					"public_id", publicId,
					"resource_type", "image",
					"folder", folder,
					"overwrite", true
				)
			);

			// get the secure url of the uploaded file
			String secureImageUrl = (String) uploadResult.get("secure_url");
			log.info("File uploaded successfully to cloudinary, URL: {}", secureImageUrl);

			// return a map with the secure url and public id
			Map<String, String> result = new HashMap<>();
			result.put("secure_url", secureImageUrl);
			result.put("public_id", folder + "/" + publicId);
			return result;
		} catch (IOException e) {
			log.error("Error uploading file to cloudinary: {}", e.getMessage());
			throw new FileUploadException("Error uploading file to cloudinary: " + e.getMessage(), e.getCause());
		}
	}

	public void deleteFile(String publicId) {
		log.info("Deleting file from cloudinary with public id: {}", publicId);
		try {
			cloudinary.uploader().destroy(
				publicId, 
				ObjectUtils.asMap(
					"resource_type", "image",
					"invalidate", true
				)
			);
			log.info("File deleted successfully from cloudinary with public id: {}", publicId);
		} catch (IOException e) {
			log.error("Error deleting file from cloudinary: {}", e.getMessage());
			throw new FileUploadException("Error deleting file from cloudinary: " + e.getMessage(), e.getCause());
		}
	}
}
