package com.cloudx.cloudx_2025_app.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "images")
public class Image {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getS3Key() {
		return s3Key;
	}
	public void setS3Key(String s3Key) {
		this.s3Key = s3Key;
	}
	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	public Long getSize() {
		return size;
	}
	public void setSize(Long size) {
		this.size = size;
	}
	public LocalDateTime getUploadedAt() {
		return uploadedAt;
	}
	public void setUploadedAt(LocalDateTime uploadedAt) {
		this.uploadedAt = uploadedAt;
	}
	public String getFileExtension() {
		return fileExtension;
	}
	public void setFileExtension(String fileExtension) {
		this.fileExtension = fileExtension;
	}
	public LocalDateTime getLastUpdatedAt() {
		return lastUpdatedAt;
	}
	public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) {
		this.lastUpdatedAt = lastUpdatedAt;
	}
	private String name;
	private String s3Key;
	private String contentType;
	private Long size;
	private String fileExtension;
	private LocalDateTime uploadedAt;
	private LocalDateTime lastUpdatedAt;

}
