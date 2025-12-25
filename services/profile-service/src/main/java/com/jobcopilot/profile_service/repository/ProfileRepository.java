package com.jobcopilot.profile_service.repository;

import com.jobcopilot.profile_service.entity.Profile;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProfileRepository extends MongoRepository<Profile, String> {
  List<Profile> findAllByUserId(String userId);

  Optional<Profile> findByUserIdAndDisplayName(String userId, String displayName);

  Optional<Profile> findByIdAndUserId(String id, String userId);
}
