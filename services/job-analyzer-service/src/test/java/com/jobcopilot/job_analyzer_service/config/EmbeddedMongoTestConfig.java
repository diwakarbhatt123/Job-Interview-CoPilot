package com.jobcopilot.job_analyzer_service.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.transitions.Mongod;
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess;
import de.flapdoodle.reverse.Transition;
import de.flapdoodle.reverse.TransitionWalker;
import de.flapdoodle.reverse.transitions.Start;
import java.io.IOException;
import java.net.ServerSocket;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class EmbeddedMongoTestConfig {
  private TransitionWalker.ReachedState<RunningMongodProcess> running;

  private static int findFreePort() {
    try (ServerSocket socket = new ServerSocket(0)) {
      socket.setReuseAddress(true);
      return socket.getLocalPort();
    } catch (IOException ex) {
      throw new IllegalStateException("Failed to allocate a free port for embedded MongoDB", ex);
    }
  }

  @Bean(destroyMethod = "close")
  public TransitionWalker.ReachedState<RunningMongodProcess> embeddedMongo() {
    if (running == null) {
      Net net = Net.of("localhost", findFreePort(), false);
      Transition<Net> netTransition = Start.to(Net.class).initializedWith(net);
      running = Mongod.builder().net(netTransition).build().start(Version.Main.V6_0);
    }
    return running;
  }

  @Bean(destroyMethod = "close")
  public MongoClient mongoClient(
      TransitionWalker.ReachedState<RunningMongodProcess> embeddedMongo) {
    var address = embeddedMongo.current().getServerAddress();
    return MongoClients.create("mongodb://" + address.getHost() + ":" + address.getPort());
  }
}
