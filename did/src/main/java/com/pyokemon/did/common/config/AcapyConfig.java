package com.pyokemon.did.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "acapy")
@Data
public class AcapyConfig {

  private Tenant tenant;
  private Mediator mediator;
  private Webhook webhook;
  private Wallet wallet;

  @Data
  public static class Tenant {
    // Booking ACA-Py 서비스 URL
    private String baseUrl;
    private String walletId;
  }

  @Data
  public static class Mediator {
    private String baseUrl;
    private String walletId;
  }

  @Data
  public static class Webhook {
    // DID 서비스 webhook URL
    private String baseUrl;
    // Webhook 토픽 접두사 (예: /topic/)
    private String topicPrefix;
  }

  @Data
  public static class Wallet {
    private String key;
  }
}
