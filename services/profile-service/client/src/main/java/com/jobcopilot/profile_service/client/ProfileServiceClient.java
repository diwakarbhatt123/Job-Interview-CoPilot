package com.jobcopilot.profile_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "profile-service", url = "${profile-service.base-url}")
public interface ProfileServiceClient {
  @GetMapping("/profile/{profileId}")
  ResponseEntity<Void> getProfile(
      @PathVariable String profileId, @RequestHeader("X-User-Id") String userId);
}
