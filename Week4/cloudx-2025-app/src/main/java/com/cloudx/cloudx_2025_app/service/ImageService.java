package com.cloudx.cloudx_2025_app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cloudx.cloudx_2025_app.entity.Image;
import com.cloudx.cloudx_2025_app.repository.ImageRepository;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageService {

	@Autowired
	private S3Client s3Client;

	@Autowired
	private ImageRepository imageRepo;
	
	@Value("${cloud.aws.s3.bucket-name}")
	private String bucketName;

	public Image upload(String name, String contentType, long size, InputStream data) {
		String key = UUID.randomUUID() + "-" + name;
		s3Client.putObject(PutObjectRequest.builder().bucket(bucketName).key(key).contentType(contentType).build(),
				RequestBody.fromInputStream(data, size));

		Image img = new Image();
		img.setName(name);
		img.setS3Key(key);
		img.setContentType(contentType);
		img.setSize(size);
		// Extract file extension
		String ext = "";
		int i = name.lastIndexOf('.');
		if (i > 0 && i < name.length() - 1) {
			ext = name.substring(i + 1);
		}
		img.setFileExtension(ext);
		// Set timestamps
		java.time.LocalDateTime now = java.time.LocalDateTime.now();
		img.setUploadedAt(now);
		img.setLastUpdatedAt(now);
		return imageRepo.save(img);
	}

	public byte[] download(String name) {
		Image img = imageRepo.findByName(name);
		if (img == null)
			throw new RuntimeException("Image not found");

		GetObjectResponse res;
		try (var s3obj = s3Client
				.getObject(GetObjectRequest.builder().bucket(bucketName).key(img.getS3Key()).build())) {
			return s3obj.readAllBytes();
		} catch (Exception e) {
			throw new RuntimeException("Download failed", e);
		}
	}

	public void delete(String name) {
		Image img = imageRepo.findByName(name);
		if (img == null)
			return;

		s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(img.getS3Key()).build());
		imageRepo.delete(img);
	}

	public Image metadata(String name) {
		return imageRepo.findByName(name);
	}

	public Image randomMetadata() {
		return imageRepo.findAll().stream().skip((int) (Math.random() * imageRepo.count())).findFirst().orElse(null);
	}
}
