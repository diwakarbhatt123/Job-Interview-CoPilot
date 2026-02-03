package com.jobcopilot.job_analyzer_service.repository;

import com.jobcopilot.job_analyzer_service.entity.Job;
import com.jobcopilot.job_analyzer_service.entity.values.Error;
import com.jobcopilot.job_analyzer_service.entity.values.Extracted;
import com.jobcopilot.job_analyzer_service.enums.AnalysisStatus;
import java.time.Instant;
import java.util.Optional;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

@Repository
public class JobRepositoryImpl implements JobRepositoryCustom {
  private final ObjectProvider<MongoOperations> mongoOperationsProvider;

  public JobRepositoryImpl(ObjectProvider<MongoOperations> mongoOperationsProvider) {
    this.mongoOperationsProvider = mongoOperationsProvider;
  }

  @Override
  public Optional<Job> acquirePendingJob(
      String pollerId, Instant now, Instant lockExpiry, int maxAttempts) {
    MongoOperations mongoOperations = mongoOperationsProvider.getObject();
    Criteria attemptOk =
        new Criteria()
            .orOperator(
                Criteria.where("analysis.attempt").lt(maxAttempts),
                Criteria.where("analysis.attempt").exists(false));
    Criteria pending =
        new Criteria()
            .andOperator(Criteria.where("analysis.status").is(AnalysisStatus.PENDING), attemptOk);
    Criteria expiredLock =
        new Criteria()
            .andOperator(
                Criteria.where("analysis.status").is(AnalysisStatus.PROCESSING),
                Criteria.where("analysis.lockedAt").lte(lockExpiry),
                attemptOk);
    Query query =
        new Query(new Criteria().orOperator(pending, expiredLock))
            .with(Sort.by(Sort.Direction.ASC, "createdAt"));
    Update update =
        new Update()
            .set("analysis.status", AnalysisStatus.PROCESSING)
            .set("analysis.lockedBy", pollerId)
            .set("analysis.lockedAt", now)
            .set("analysis.startedAt", now)
            .inc("analysis.attempt", 1)
            .set("updatedAt", now);
    Job job =
        mongoOperations.findAndModify(
            query, update, FindAndModifyOptions.options().returnNew(true), Job.class);
    return Optional.ofNullable(job);
  }

  @Override
  public void markCompletedWithExtracted(
      String jobId, String lockedBy, Instant now, String normalizedText, Extracted extracted) {
    MongoOperations mongoOperations = mongoOperationsProvider.getObject();
    Query query =
        new Query(
            Criteria.where("_id")
                .is(jobId)
                .and("analysis.status")
                .is(AnalysisStatus.PROCESSING)
                .and("analysis.lockedBy")
                .is(lockedBy));
    Update update =
        new Update()
            .set("input.normalizedText", normalizedText)
            .set("extracted", extracted)
            .set("analysis.status", AnalysisStatus.COMPLETED)
            .set("analysis.completedAt", now)
            .set("analysis.failedAt", null)
            .set("analysis.error", null)
            .set("analysis.lockedBy", null)
            .set("analysis.lockedAt", null)
            .set("updatedAt", now);
    mongoOperations.updateFirst(query, update, Job.class);
  }

  @Override
  public void markFailed(String jobId, String lockedBy, Instant now, Error error) {
    MongoOperations mongoOperations = mongoOperationsProvider.getObject();
    Query query =
        new Query(
            Criteria.where("_id")
                .is(jobId)
                .and("analysis.status")
                .is(AnalysisStatus.PROCESSING)
                .and("analysis.lockedBy")
                .is(lockedBy));
    Update update =
        new Update()
            .set("analysis.status", AnalysisStatus.FAILED)
            .set("analysis.failedAt", now)
            .set("analysis.completedAt", null)
            .set("analysis.error", error)
            .set("analysis.lockedBy", null)
            .set("analysis.lockedAt", null)
            .set("updatedAt", now);
    mongoOperations.updateFirst(query, update, Job.class);
  }
}
