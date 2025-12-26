package com.jobcopilot.profile_service.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.jobcopilot.profile_service.entity.Profile;
import com.jobcopilot.profile_service.enums.ProfileStatus;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.transitions.Mongod;
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess;
import de.flapdoodle.reverse.TransitionWalker.ReachedState;
import de.flapdoodle.reverse.transitions.Start;
import java.io.IOException;
import java.net.ServerSocket;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

@SpringBootTest
class ProfileRepositoryIT {

  private static ReachedState<RunningMongodProcess> mongod;
  private static int mongoPort;

  private final ProfileRepository repository;
  private final MongoTemplate mongoTemplate;

  @Autowired
  ProfileRepositoryIT(ProfileRepository repository, MongoTemplate mongoTemplate) {
    this.repository = repository;
    this.mongoTemplate = mongoTemplate;
  }

  @AfterAll
  static void stopMongo() {
    if (mongod != null) {
      mongod.close();
    }
  }

  @DynamicPropertySource
  static void mongoProperties(DynamicPropertyRegistry registry) throws IOException {
    ensureMongoRunning();
    registry.add(
        "spring.data.mongodb.uri", () -> "mongodb://localhost:" + mongoPort + "/profile_service");
    registry.add("spring.data.mongodb.auto-index-creation", () -> "true");
  }

  private static synchronized void ensureMongoRunning() throws IOException {
    if (mongod != null) {
      return;
    }
    try (ServerSocket socket = new ServerSocket(0)) {
      mongoPort = socket.getLocalPort();
    }
    Net net = Net.defaults().withPort(mongoPort);
    Mongod mongo = Mongod.builder().net(Start.to(Net.class).initializedWith(net)).build();
    mongod = mongo.start(Version.Main.V6_0);
  }

  @BeforeEach
  void clearCollection() {
    repository.deleteAll();
    IndexOperations indexOps = mongoTemplate.indexOps(Profile.class);
    indexOps.createIndex(
        new Index()
            .on("userId", Sort.Direction.ASC)
            .on("displayName", Sort.Direction.ASC)
            .unique());
  }

  @Test
  void savesAndFindsByUserId() {
    Profile profile = Profile.builder().build();
    ReflectionTestUtils.setField(profile, "userId", "user-1");
    ReflectionTestUtils.setField(profile, "displayName", "Primary");
    ReflectionTestUtils.setField(profile, "status", ProfileStatus.CREATED);

    repository.save(profile);

    assertThat(repository.findAllByUserId("user-1")).hasSize(1);
  }

  @Test
  void enforcesOwnershipOnFindById() {
    Profile profile = Profile.builder().build();
    ReflectionTestUtils.setField(profile, "userId", "user-1");
    ReflectionTestUtils.setField(profile, "displayName", "Primary");
    ReflectionTestUtils.setField(profile, "status", ProfileStatus.CREATED);

    Profile saved = repository.save(profile);
    String id = saved.getId();

    assertThat(repository.findByIdAndUserId(id, "user-1")).isPresent();
    assertThat(repository.findByIdAndUserId(id, "user-2")).isEmpty();
  }

  @Test
  void preventsDuplicateDisplayNamePerUser() {
    Profile first = Profile.builder().build();
    ReflectionTestUtils.setField(first, "userId", "user-1");
    ReflectionTestUtils.setField(first, "displayName", "Primary");
    ReflectionTestUtils.setField(first, "status", ProfileStatus.CREATED);
    repository.save(first);

    Profile second = Profile.builder().build();
    ReflectionTestUtils.setField(second, "userId", "user-1");
    ReflectionTestUtils.setField(second, "displayName", "Primary");
    ReflectionTestUtils.setField(second, "status", ProfileStatus.CREATED);

    assertThatThrownBy(() -> repository.save(second)).isInstanceOf(DuplicateKeyException.class);
  }
}
