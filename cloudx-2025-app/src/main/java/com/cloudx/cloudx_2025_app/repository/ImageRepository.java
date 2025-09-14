package com.cloudx.cloudx_2025_app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cloudx.cloudx_2025_app.entity.Image;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
	Image findByName(String name);
}
