package com.jobcopilot.account_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth.jwt")
public record TokenConfig(
    String publicKeyPath,
    String issuer,
    long expirationSeconds,
    String privateKeyPath,
    String privateKeyAlias,
    String privateKeyPassword) {}
