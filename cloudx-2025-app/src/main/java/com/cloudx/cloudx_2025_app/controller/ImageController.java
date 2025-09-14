package com.cloudx.cloudx_2025_app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.cloudx.cloudx_2025_app.entity.Image;
import com.cloudx.cloudx_2025_app.service.ImageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/images")
public class ImageController {

	@Autowired
	private ImageService imageService;

	@PostMapping("/upload")
	public ResponseEntity<Image> upload(@RequestParam("file") MultipartFile file) throws Exception {
		Image img = imageService.upload(file.getOriginalFilename(), file.getContentType(), file.getSize(),
				file.getInputStream());
		return ResponseEntity.ok(img);
	}

	@GetMapping("/download/{name}")
	public ResponseEntity<byte[]> download(@PathVariable String name) {
		byte[] data = imageService.download(name);
		return ResponseEntity.ok().body(data);
	}

	@GetMapping("/metadata/{name}")
	public ResponseEntity<Image> metadata(@PathVariable String name) {
		return ResponseEntity.ok(imageService.metadata(name));
	}

	@GetMapping("/metadata/random")
	public ResponseEntity<Image> random() {
		return ResponseEntity.ok(imageService.randomMetadata());
	}

	@DeleteMapping("/{name}")
	public ResponseEntity<Void> delete(@PathVariable String name) {
		imageService.delete(name);
		return ResponseEntity.noContent().build();
	}
}
