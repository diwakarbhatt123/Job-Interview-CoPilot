package com.jobcopilot.account_service.repository;

import com.jobcopilot.account_service.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
  Optional<User> findByEmail(String email);

  boolean existsByUserId(UUID userId);
}
