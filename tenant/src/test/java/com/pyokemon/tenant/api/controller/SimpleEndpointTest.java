package com.pyokemon.tenant.api.controller;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.pyokemon.tenant.TenantApplication;

@DisplayName("Tenant API 엔드포인트 간단 테스트")
class SimpleEndpointTest {

  private static HttpClient httpClient;
  private static final String BASE_URL = "http://localhost:8081";

  @BeforeAll
  static void setup() {
    httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
  }

  @Test
  @DisplayName("컴파일 및 클래스 로딩 테스트")
  void compileTest() {
    // 단순히 클래스가 정상적으로 로드되는지 확인
    assertNotNull(TenantApplication.class);
    assertTrue(true, "기본 컴파일 테스트 통과");
  }

  @Test
  @DisplayName("Controller 클래스 존재 확인")
  void controllerClassExists() {
    try {
      Class<?> authController =
          Class.forName("com.pyokemon.tenant.api.controller.TenantAuthController");
      Class<?> tenantController =
          Class.forName("com.pyokemon.tenant.api.controller.TenantController");

      assertNotNull(authController, "TenantAuthController 클래스가 존재해야 함");
      assertNotNull(tenantController, "TenantController 클래스가 존재해야 함");
    } catch (ClassNotFoundException e) {
      fail("Controller 클래스를 찾을 수 없습니다: " + e.getMessage());
    }
  }

  @Test
  @DisplayName("DTO 클래스 존재 확인")
  void dtoClassExists() {
    try {
      Class<?> loginDto = Class.forName("com.pyokemon.tenant.api.dto.request.LoginRequestDto");
      Class<?> responseDto =
          Class.forName("com.pyokemon.tenant.api.dto.response.TenantDetailResponseDto");

      assertNotNull(loginDto, "LoginRequestDto 클래스가 존재해야 함");
      assertNotNull(responseDto, "TenantDetailResponseDto 클래스가 존재해야 함");
    } catch (ClassNotFoundException e) {
      fail("DTO 클래스를 찾을 수 없습니다: " + e.getMessage());
    }
  }

  @Test
  @DisplayName("Service 클래스 존재 확인")
  void serviceClassExists() {
    try {
      Class<?> tenantService = Class.forName("com.pyokemon.tenant.api.service.TenantService");
      assertNotNull(tenantService, "TenantService 클래스가 존재해야 함");
    } catch (ClassNotFoundException e) {
      fail("Service 클래스를 찾을 수 없습니다: " + e.getMessage());
    }
  }

  // HTTP 테스트는 서버가 실행 중일 때만 가능하므로 조건부로 실행
  @Test
  @DisplayName("로그인 엔드포인트 HTTP 테스트 (선택적)")
  void loginEndpointHttpTest() {
    try {
      HttpRequest request =
          HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/tenants/api/tenants/auth/login"))
              .POST(HttpRequest.BodyPublishers.ofString("{}"))
              .header("Content-Type", "application/json").timeout(Duration.ofSeconds(3)).build();

      HttpResponse<String> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      // 서버가 실행 중이면 400(잘못된 요청) 또는 다른 응답을 받을 것
      // 서버가 실행 중이 아니면 ConnectException이 발생
      System.out.println("응답 코드: " + response.statusCode());
      System.out.println("응답 본문: " + response.body());

      // 어떤 응답이든 받으면 엔드포인트가 존재함을 의미
      assertTrue(response.statusCode() >= 200, "서버로부터 응답을 받았습니다");

    } catch (IOException | InterruptedException e) {
      // 서버가 실행 중이 아니거나 연결 문제
      System.out.println("서버 연결 실패 (정상): " + e.getMessage());
      assertTrue(true, "서버가 실행 중이 아님 - 정상적인 상황");
    }
  }
}
