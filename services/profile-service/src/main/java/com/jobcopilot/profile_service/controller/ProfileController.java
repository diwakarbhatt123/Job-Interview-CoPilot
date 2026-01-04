package com.jobcopilot.profile_service.controller;

import com.jobcopilot.profile_service.enums.SourceType;
import com.jobcopilot.profile_service.exception.ProfileAlreadyExistsException;
import com.jobcopilot.profile_service.exception.ProfileNotFoundException;
import com.jobcopilot.profile_service.model.request.CreateProfileRequest;
import com.jobcopilot.profile_service.model.response.ErrorResponse;
import com.jobcopilot.profile_service.model.response.ProfileStatusResponse;
import com.jobcopilot.profile_service.model.response.ProfileSummaryResponse;
import com.jobcopilot.profile_service.model.response.ProfilesResponse;
import com.jobcopilot.profile_service.service.ProfileService;
import com.mongodb.DuplicateKeyException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/profile")
@Validated
public class ProfileController {

  private final ProfileService profileService;

  @Autowired
  public ProfileController(ProfileService profileService) {
    this.profileService = profileService;
  }

  @GetMapping("/all")
  public ResponseEntity<ProfilesResponse> allProfiles(@RequestHeader("X-User-Id") String userId) {
    log.info("Received request to get all profiles for userId: {}", userId);

    List<ProfileSummaryResponse> profileSummaryResponses = profileService.getProfiles(userId);
    return ResponseEntity.ok(
        new ProfilesResponse(profileSummaryResponses, profileSummaryResponses.size()));
  }

  @GetMapping("/{profileId}")
  public ResponseEntity<ProfileSummaryResponse> getProfile(
      @RequestHeader("X-User-Id") String userId, @PathVariable("profileId") String profileId) {
    log.info("Received request to get profile for userId: {} and profileId: {}", userId, profileId);
    return ResponseEntity.ok(profileService.getProfile(userId, profileId));
  }

  @PostMapping
  public ResponseEntity<ProfileStatusResponse> createProfile(
      @RequestBody @Valid CreateProfileRequest createProfileRequest,
      @RequestHeader("X-User-Id") String userId) {
    log.info(
        "Received request to create profile for userId: {} with displayName: {}",
        userId,
        createProfileRequest.displayName());
    ProfileStatusResponse profileStatusResponse =
        profileService.createProfile(createProfileRequest, userId);
    return ResponseEntity.status(HttpStatus.CREATED).body(profileStatusResponse);
  }

  @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<?> createProfileWithUpload(
      @RequestParam("displayName") @NotBlank String displayName,
      @RequestParam("sourceType") @NotNull SourceType sourceType,
      @RequestParam("resume") MultipartFile resume,
      @RequestHeader("X-User-Id") String userId)
      throws IOException {
    log.info(
        "Received request to create profile for userId: {} with displayName: {} and source {} and resume {}",
        userId,
        displayName,
        sourceType,
        resume);

    if (sourceType != SourceType.UPLOADED) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(new ErrorResponse("Invalid source type for upload."));
    }
    if (resume == null || resume.isEmpty()) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(new ErrorResponse("Resume file is required."));
    }

    ProfileStatusResponse profileStatusResponse =
        profileService.createProfileFromUpload(displayName, resume, userId);
    return ResponseEntity.status(HttpStatus.CREATED).body(profileStatusResponse);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException ex) {
    log.error("Validation failed: {}", ex.getMessage(), ex);
    String errorMessage = ex.getBindingResult().getAllErrors().getFirst().getDefaultMessage();
    ErrorResponse errorResponse = new ErrorResponse(errorMessage);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
  }

  @ExceptionHandler(exception = {DuplicateKeyException.class, ProfileAlreadyExistsException.class})
  public ResponseEntity<ErrorResponse> handleProfileAlreadyExistsException(Exception ex) {
    log.error("Profile creation failed: {}", ex.getMessage(), ex);
    ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
  }

  @ExceptionHandler(ProfileNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleProfileNotFoundException(ProfileNotFoundException ex) {
    log.error("Profile not found: {}", ex.getMessage(), ex);
    ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception ex) {
    log.error("An unexpected error occurred: {}", ex.getMessage(), ex);
    ErrorResponse errorResponse = new ErrorResponse("An unexpected error occurred.");
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
  }
}
