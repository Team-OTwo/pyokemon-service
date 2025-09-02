package com.pyokemon.account.auth.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.pyokemon.account.auth.dto.request.AppLoginRequestDto;
import com.pyokemon.account.auth.dto.request.LoginRequestDto;
import com.pyokemon.account.auth.dto.request.UpdatePasswordRequestDto;
import com.pyokemon.account.auth.dto.response.AppLoginResponseDto;
import com.pyokemon.account.auth.dto.response.LoginResponseDto;
import com.pyokemon.account.auth.dto.response.TokenResponseDto;
import com.pyokemon.account.auth.service.AccountService;
import com.pyokemon.common.dto.ResponseDto;

@ExtendWith(MockitoExtension.class)
public class AccountControllerTest {

  @Mock
  private AccountService accountService;

  @InjectMocks
  private AccountController accountController;

  private LoginRequestDto loginRequest;
  private AppLoginRequestDto appLoginRequest;
  private LoginResponseDto loginResponse;
  private AppLoginResponseDto appLoginResponse;
  private UpdatePasswordRequestDto updatePasswordRequest;
  private TokenResponseDto tokenResponse;

  @BeforeEach
  void setUp() {
    // 테스트용 요청 데이터 생성
    loginRequest = new LoginRequestDto();
    loginRequest.setLoginId("test@example.com");
    loginRequest.setPassword("password123");

    appLoginRequest = new AppLoginRequestDto();
    appLoginRequest.setLoginId("app@example.com");
    appLoginRequest.setPassword("password123");
    appLoginRequest.setDeviceNumber("device123");

    // 테스트용 응답 데이터 생성
    loginResponse =
        LoginResponseDto.builder().accountId(1L).role("USER").accessToken("access-token")
            .refreshToken("refresh-token").userName("테스트 사용자").isVerified(true).build();

    appLoginResponse =
        AppLoginResponseDto.builder().accountId(1L).role("USER").accessToken("access-token")
            .refreshToken("refresh-token").deviceStatus("REGISTERED").build();

    updatePasswordRequest = new UpdatePasswordRequestDto();
    updatePasswordRequest.setCurrentPassword("oldPassword");
    updatePasswordRequest.setNewPassword("newPassword123");

    tokenResponse = TokenResponseDto.builder().accessToken("new-access-token").build();
  }

  // ========== 로그인 테스트 ==========

  @Test
    @DisplayName("로그인 성공 테스트")
    void loginSuccess() {
        // given
        when(accountService.login(loginRequest)).thenReturn(loginResponse);

        // when
        ResponseEntity<ResponseDto<LoginResponseDto>> response = accountController.login(loginRequest);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("로그인 성공", response.getBody().getMessage());
        assertEquals(loginResponse, response.getBody().getData());

        verify(accountService).login(loginRequest);
    }

  @Test
    @DisplayName("로그인 실패 테스트")
    void loginFailure() {
        // given
        when(accountService.login(loginRequest)).thenThrow(new RuntimeException("Login failed"));

        // when & then
        assertThrows(RuntimeException.class, () -> accountController.login(loginRequest));

        verify(accountService).login(loginRequest);
    }

  // ========== 앱 로그인 테스트 ==========

  @Test
    @DisplayName("앱 로그인 성공 테스트")
    void appLoginSuccess() {
        // given
        when(accountService.appLogin(appLoginRequest)).thenReturn(appLoginResponse);

        // when
        ResponseEntity<ResponseDto<AppLoginResponseDto>> response = accountController.appLogin(appLoginRequest);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("로그인 성공", response.getBody().getMessage());
        assertEquals(appLoginResponse, response.getBody().getData());

        verify(accountService).appLogin(appLoginRequest);
    }

  @Test
    @DisplayName("앱 로그인 실패 테스트")
    void appLoginFailure() {
        // given
        when(accountService.appLogin(appLoginRequest)).thenThrow(new RuntimeException("App login failed"));

        // when & then
        assertThrows(RuntimeException.class, () -> accountController.appLogin(appLoginRequest));

        verify(accountService).appLogin(appLoginRequest);
    }

  // ========== 로그아웃 테스트 ==========

  @Test
  @DisplayName("로그아웃 성공 테스트 - 디바이스 정보 포함")
  void logoutSuccess_withDevice() {
    // given
    String authHeader = "Bearer valid-token";
    doNothing().when(accountService).logout(authHeader, "account123", 1L);

    // when
    ResponseEntity<ResponseDto<Void>> response =
        accountController.logout(authHeader, "account123", 1L);

    // then
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("로그아웃 성공", response.getBody().getMessage());

    verify(accountService).logout(authHeader, "account123", 1L);
  }

  @Test
  @DisplayName("로그아웃 성공 테스트 - 디바이스 정보 없음 (웹 로그아웃)")
  void logoutSuccess_withoutDevice() {
    // given
    String authHeader = "Bearer valid-token";
    doNothing().when(accountService).logout(authHeader, "account123", null);

    // when
    ResponseEntity<ResponseDto<Void>> response =
        accountController.logout(authHeader, "account123", null); // request is null

    // then
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("로그아웃 성공", response.getBody().getMessage());

    verify(accountService).logout(authHeader, "account123", null);
  }

  @Test
  @DisplayName("로그아웃 성공 테스트 - 인증 헤더 없음")
  void logoutSuccess_withoutAuthHeader() {
    // given
    String authHeader = null;
    doNothing().when(accountService).logout(authHeader, null, 1L);

    // when
    ResponseEntity<ResponseDto<Void>> response = accountController.logout(authHeader, null, 1L);

    // then
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("로그아웃 성공", response.getBody().getMessage());

    verify(accountService).logout(authHeader, null, 1L);
  }

  // ========== 토큰 갱신 테스트 ==========

  @Test
  @DisplayName("토큰 갱신 성공 테스트 - Bearer 접두사 있음")
  void refreshTokenSuccess_WithBearer() {
    // given
    String authHeader = "Bearer refresh-token";
    when(accountService.refreshToken("refresh-token")).thenReturn(tokenResponse);

    // when
    ResponseEntity<ResponseDto<TokenResponseDto>> response =
        accountController.refreshToken(authHeader);

    // then
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("토큰 갱신 성공", response.getBody().getMessage());
    assertEquals(tokenResponse, response.getBody().getData());

    verify(accountService).refreshToken("refresh-token");
  }

  @Test
  @DisplayName("토큰 갱신 성공 테스트 - Bearer 접두사 없음")
  void refreshTokenSuccess_WithoutBearer() {
    // given
    String authHeader = "refresh-token";
    when(accountService.refreshToken("refresh-token")).thenReturn(tokenResponse);

    // when
    ResponseEntity<ResponseDto<TokenResponseDto>> response =
        accountController.refreshToken(authHeader);

    // then
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("토큰 갱신 성공", response.getBody().getMessage());
    assertEquals(tokenResponse, response.getBody().getData());

    verify(accountService).refreshToken("refresh-token");
  }

  @Test
  @DisplayName("토큰 갱신 실패 테스트")
  void refreshTokenFailure() {
    // given
    String authHeader = "Bearer invalid-token";
    when(accountService.refreshToken("invalid-token"))
        .thenThrow(new RuntimeException("Token refresh failed"));

    // when & then
    assertThrows(RuntimeException.class, () -> accountController.refreshToken(authHeader));

    verify(accountService).refreshToken("invalid-token");
  }

  // ========== 비밀번호 변경 테스트 ==========

  @Test
  @DisplayName("비밀번호 변경 성공 테스트")
  void changePasswordSuccess() {
    // given
    doNothing().when(accountService).changePassword(1L, updatePasswordRequest);

    // Mock GatewayRequestHeaderUtils
    try (MockedStatic<GatewayRequestHeaderUtils> mockedUtils =
        mockStatic(GatewayRequestHeaderUtils.class)) {
      mockedUtils.when(GatewayRequestHeaderUtils::getUserIdOrThrowException).thenReturn("1");

      // when
      ResponseEntity<ResponseDto<Void>> response =
          accountController.changePassword(updatePasswordRequest);

      // then
      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
      assertEquals("비밀번호 변경 성공", response.getBody().getMessage());

      verify(accountService).changePassword(1L, updatePasswordRequest);
    }
  }
}
