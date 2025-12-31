package com.jobcopilot.profile_service.service;

import com.jobcopilot.profile_service.enums.SourceType;
import com.jobcopilot.profile_service.model.request.CreateProfileRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class ResumeParsingService {

  public void parseResume(CreateProfileRequest createProfileRequest, String userId) {
    log.info("Received request for parsing resume for user id {}", userId);
  }

  public void parseResumeUpload(MultipartFile resume, SourceType sourceType, String userId) {
    log.info("Received upload parsing request for user id {} with source {}", userId, sourceType);
  }
}
