package com.jobcopilot.profile_service.controller;

import com.jobcopilot.profile_service.model.response.GetAllProfilesResponse;
import com.jobcopilot.profile_service.model.response.ProfileSummaryResponse;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/profile")
public class ProfileController {

  @GetMapping("/all")
  public ResponseEntity<GetAllProfilesResponse> allProfiles(
      @RequestHeader("X-User-Id") String userId) {
    log.info("Received request to get all profiles for userId: {}", userId);
    return ResponseEntity.ok(
        new GetAllProfilesResponse(
            List.of(new ProfileSummaryResponse(UUID.randomUUID(), "random", "random")), 1));
  }
}
