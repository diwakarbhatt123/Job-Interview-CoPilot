package com.jobcopilot.profile_service.repository;

import com.jobcopilot.profile_service.entity.Profile;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface ProfileRepository extends MongoRepository<Profile, String> {
  List<Profile> findAllByUserId(String userId);

  @Query(
      value = "{'userId': ?0}",
      fields =
          "{'_id': 1, 'displayName': 1, 'status': 1, 'createdAt': 1, 'updatedAt': 1, 'derived': 1, 'resume.parsed.yearsOfExperience': 1}")
  List<ProfileSummaryView> findSummariesByUserId(String userId);

  Optional<Profile> findByUserIdAndDisplayName(String userId, String displayName);

  Optional<Profile> findByIdAndUserId(String id, String userId);

  boolean existsByUserIdAndDisplayName(String userId, String displayName);
}
