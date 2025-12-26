package com.jobcopilot.profile_service.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration(proxyBeanMethods = false)
public class MongoConfiguration {

  @Bean
  @ConditionalOnProperty(name = "spring.data.mongodb.uri")
  @ConditionalOnMissingBean(MongoClient.class)
  MongoClient mongoClient(Environment environment) {
    String uri = environment.getProperty("spring.data.mongodb.uri");
    return MongoClients.create(uri);
  }

  @Bean
  @ConditionalOnProperty(name = "spring.data.mongodb.uri")
  @ConditionalOnMissingBean(MongoTemplate.class)
  MongoTemplate mongoTemplate(MongoClient mongoClient, Environment environment) {
    String database = environment.getProperty("spring.data.mongodb.database");
    return new MongoTemplate(mongoClient, database);
  }
}
