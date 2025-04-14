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
import com.megatronix.paridhi.constant.AppConstant;
import com.megatronix.paridhi.constant.MessageConstant;
import com.megatronix.paridhi.exception.FileUploadException;
import com.megatronix.paridhi.util.LoggingUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryService {
	private final Cloudinary cloudinary;

	public Map<String, String> uploadFile(MultipartFile file) {
		// log the operation
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.CREATE,
			AppConstant.CLOUDINARY_IMAGE,
			null,
			"Uploading file: " + file.getOriginalFilename() + " (" + file.getSize() + " bytes)"
		);
		
		try {
			// create a folder structure for the file
			String folder = "paridhi-2025/events";

			// create a unique public id for the file
			String publicId = file.getOriginalFilename() + "/" + LocalDate.now() + "/" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

			// upload the file to cloudinary
			Map<?, ?> uploadResult = cloudinary.uploader().upload(
				file.getBytes(), 
				ObjectUtils.asMap(
					AppConstant.PUBLIC_ID, publicId,
					"resource_type", "image",
					"folder", folder,
					"overwrite", true
				)
			);

			// get the secure url of the uploaded file
			String secureImageUrl = (String) uploadResult.get(AppConstant.SECURE_URL);
			
			// log the successful upload
			LoggingUtil.logOperation(
				log,
				MessageConstant.Operation.CREATE,
				AppConstant.CLOUDINARY_IMAGE,
				null,
				"Successfully uploaded file to Cloudinary, URL: " + secureImageUrl
			);

			// return a map with the secure url and public id
			Map<String, String> result = new HashMap<>();
			result.put(AppConstant.SECURE_URL, secureImageUrl);
			result.put(AppConstant.PUBLIC_ID, folder + "/" + publicId);
			
			return result;
		} catch (IOException e) {
			LoggingUtil.logError(
				log,
				MessageConstant.Operation.CREATE,
				AppConstant.CLOUDINARY_IMAGE,
				null,
				"Failed to upload file: " + file.getOriginalFilename() + " - Error: " + e.getMessage(),
				e
			);
			throw new FileUploadException("Error uploading file to cloudinary: " + e.getMessage(), e.getCause());
		}
	}

	public void deleteFile(String publicId) {
		// log the operation
		LoggingUtil.logOperation(
			log,
			MessageConstant.Operation.DELETE,
			AppConstant.CLOUDINARY_IMAGE,
			null,
			"Deleting file from Cloudinary with public ID: " + publicId
		);
		
		try {
			// delete the file from cloudinary
			cloudinary.uploader().destroy(
				publicId, 
				ObjectUtils.asMap(
					"resource_type", "image",
					"invalidate", true
				)
			);
			
			// log the successful deletion
			LoggingUtil.logOperation(
				log,
				MessageConstant.Operation.DELETE,
				AppConstant.CLOUDINARY_IMAGE,
				null,
				"Successfully deleted file from Cloudinary with public ID: " + publicId
			);
		} catch (IOException e) {
			LoggingUtil.logError(
				log,
				MessageConstant.Operation.DELETE,
				AppConstant.CLOUDINARY_IMAGE,
				null,
				"Failed to delete file with public ID: " + publicId + " - Error: " + e.getMessage(),
				e
			);
			throw new FileUploadException("Error deleting file from cloudinary: " + e.getMessage(), e.getCause());
		}
	}
}