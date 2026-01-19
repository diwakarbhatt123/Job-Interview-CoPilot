package com.jobcopilot.job_analyzer_service.repository;

import com.jobcopilot.job_analyzer_service.entity.Job;
import com.jobcopilot.job_analyzer_service.enums.AnalysisStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface JobRepository extends MongoRepository<Job, String>, JobRepositoryCustom {
  Optional<Job> findByUserIdAndProfileId(String userId, String profileId);

  List<Job> findByAnalysis_Status(AnalysisStatus status);
}
