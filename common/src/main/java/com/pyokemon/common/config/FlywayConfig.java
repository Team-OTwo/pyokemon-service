package com.pyokemon.common.config;

import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class FlywayConfig {

  @Bean
  @Profile("!test")
  public FlywayMigrationStrategy flywayMigrationStrategy() {
    return flyway -> {
      // 개발환경에서는 migrate 실행
      flyway.migrate();
    };
  }

  @Bean
  @Profile("test")
  public FlywayMigrationStrategy testFlywayMigrationStrategy() {
    return flyway -> {
      // 테스트 환경에서는 clean 후 migrate
      flyway.clean();
      flyway.migrate();
    };
  }
}
