package com.jobcopilot.job_analyzer_service.job;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jobcopilot.job_analyzer_service.entity.Job;
import com.jobcopilot.job_analyzer_service.job.service.JobAnalysisService;
import com.jobcopilot.job_analyzer_service.repository.JobRepository;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

class PendingJobAnalysisPollerTest {
  private ThreadPoolExecutor executor;

  @AfterEach
  void tearDown() {
    if (executor != null) {
      executor.shutdownNow();
    }
  }

  @Test
  void doesNotClaimWhenQueueFull() {
    executor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(1));
    CountDownLatch block = new CountDownLatch(1);
    executor.submit(
        () -> {
          try {
            block.await(2, TimeUnit.SECONDS);
          } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
          }
        });
    executor.submit(() -> {});
    assertThat(executor.getQueue().remainingCapacity()).isZero();

    JobRepository jobRepository = Mockito.mock(JobRepository.class);
    JobAnalysisService jobAnalysisService = Mockito.mock(JobAnalysisService.class);
    PendingJobAnalysisPoller poller =
        new PendingJobAnalysisPoller(
            "poller-1", 300000L, 3, executor, jobRepository, jobAnalysisService);

    ReflectionTestUtils.invokeMethod(poller, "getPendingJob");

    verify(jobRepository, never()).acquirePendingJob(anyString(), any(), any(), anyInt());
    block.countDown();
  }

  @Test
  void submitsJobWhenClaimed() throws Exception {
    executor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(1));
    JobRepository jobRepository = Mockito.mock(JobRepository.class);
    JobAnalysisService jobAnalysisService = Mockito.mock(JobAnalysisService.class);
    Job job = Job.builder().id("job-1").build();
    when(jobRepository.acquirePendingJob(anyString(), any(), any(), anyInt()))
        .thenReturn(Optional.of(job));

    CountDownLatch latch = new CountDownLatch(1);
    doAnswer(
            invocation -> {
              latch.countDown();
              return null;
            })
        .when(jobAnalysisService)
        .analyseJob(job);

    PendingJobAnalysisPoller poller =
        new PendingJobAnalysisPoller(
            "poller-1", 300000L, 3, executor, jobRepository, jobAnalysisService);

    ReflectionTestUtils.invokeMethod(poller, "getPendingJob");

    assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
  }
}
