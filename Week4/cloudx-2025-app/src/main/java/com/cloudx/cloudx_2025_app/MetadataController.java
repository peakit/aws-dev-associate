package com.cloudx.cloudx_2025_app;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import software.amazon.awssdk.regions.internal.util.EC2MetadataUtils;

@RestController
public class MetadataController {

	@GetMapping("/location")
	public Map<String, String> getRegionAndAz() {
		Map<String, String> location = new HashMap<>();
		location.put("region", EC2MetadataUtils.getEC2InstanceRegion());
		location.put("availabilityZone", EC2MetadataUtils.getAvailabilityZone());
		return location;
	}

}
