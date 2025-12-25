package com.jobcopilot.profile_service.controller;

import com.jobcopilot.profile_service.model.response.GetAllProfilesResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/profiles")
public class ProfileController {

  @GetMapping("/all")
  public ResponseEntity<GetAllProfilesResponse> allProfiles(
      @RequestHeader("X-User-Id") String userId) {
    log.info("Received request to get all profiles for userId: {}", userId);
    return ResponseEntity.ok(new GetAllProfilesResponse(null, 0));
  }
}
