package com.jobcopilot.job_analyzer_service.repository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

class JobRepositoryImplTest {
  private static boolean containsKeyRecursive(Object value, String key) {
    if (value instanceof Document doc) {
      if (doc.containsKey(key)) {
        return true;
      }
      for (Object nested : doc.values()) {
        if (containsKeyRecursive(nested, key)) {
          return true;
        }
      }
    } else if (value instanceof List<?> list) {
      for (Object nested : list) {
        if (containsKeyRecursive(nested, key)) {
          return true;
        }
      }
    }
    return false;
  }

  private static boolean containsValueRecursive(Object value, String needle) {
    if (value instanceof Document doc) {
      for (Object nested : doc.values()) {
        if (containsValueRecursive(nested, needle)) {
          return true;
        }
      }
    } else if (value instanceof List<?> list) {
      for (Object nested : list) {
        if (containsValueRecursive(nested, needle)) {
          return true;
        }
      }
    } else if (value instanceof Enum<?> enumValue) {
      if (enumValue.name().equals(needle)) {
        return true;
      }
    } else if (needle.equals(String.valueOf(value))) {
      return true;
    }
    return false;
  }

  @Test
  void acquirePendingJob_includesLockExpiryAndMaxAttempts() {
    MongoOperations mongoOperations = Mockito.mock(MongoOperations.class);
    ObjectProvider<MongoOperations> provider = Mockito.mock(ObjectProvider.class);
    when(provider.getObject()).thenReturn(mongoOperations);
    JobRepositoryImpl repository = new JobRepositoryImpl(provider);
    Instant now = Instant.parse("2026-01-18T10:15:30Z");
    Instant lockExpiry = Instant.parse("2026-01-18T10:10:30Z");

    when(mongoOperations.findAndModify(
            any(Query.class), any(Update.class), any(FindAndModifyOptions.class), any()))
        .thenReturn(null);

    repository.acquirePendingJob("poller-1", now, lockExpiry, 3);

    ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
    verify(mongoOperations)
        .findAndModify(
            queryCaptor.capture(), any(Update.class), any(FindAndModifyOptions.class), any());

    Document query = queryCaptor.getValue().getQueryObject();
    org.assertj.core.api.Assertions.assertThat(query.containsKey("$or")).isTrue();
    org.assertj.core.api.Assertions.assertThat(containsKeyRecursive(query, "analysis.lockedAt"))
        .isTrue();
    org.assertj.core.api.Assertions.assertThat(containsKeyRecursive(query, "analysis.attempt"))
        .isTrue();
    org.assertj.core.api.Assertions.assertThat(containsValueRecursive(query, "PENDING")).isTrue();
    org.assertj.core.api.Assertions.assertThat(containsValueRecursive(query, "PROCESSING"))
        .isTrue();
  }
}
