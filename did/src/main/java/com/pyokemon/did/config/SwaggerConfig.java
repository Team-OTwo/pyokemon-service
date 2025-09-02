package com.pyokemon.did.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class SwaggerConfig {

  @Value("${server.port:8084}")
  private String serverPort;

  @Value("${server.servlet.context-path:/did}")
  private String contextPath;

  @Value("${springdoc.packages-to-scan:com.pyokemon.did.api}")
  private String packagesToScan;

  @Bean
  public OpenAPI didServiceOpenAPI() {
    return new OpenAPI()
        .info(new Info().title("Pyokemon DID Service API")
            .description("DID (Decentralized Identifier) 서비스 API 문서").version("1.0.0")
            .contact(new Contact().name("Pyokemon Team").email("dev@pyokemon.com")
                .url("https://pyokemon.com"))
            .license(new License().name("MIT License").url("https://opensource.org/licenses/MIT")))
        .servers(List.of(
            new Server().url("http://localhost:" + serverPort + contextPath)
                .description("로컬 개발 환경"),
            new Server().url("https://api.pyokemon.com" + contextPath).description("프로덕션 환경")))
        .components(new Components().addSecuritySchemes("bearerAuth",
            new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
                .description("Bearer을 제외한 JWT 토큰을 입력하세요")))
        .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
  }
}
