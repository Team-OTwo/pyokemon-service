package com.pyokemon.account.auth.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import com.pyokemon.account.auth.dto.request.LoginRequestDto;
import com.pyokemon.account.auth.dto.request.UpdatePasswordRequestDto;
import com.pyokemon.account.auth.dto.response.LoginResponseDto;
import com.pyokemon.account.auth.dto.response.TokenResponseDto;
import com.pyokemon.account.auth.service.AccountService;
import com.pyokemon.account.common.web.context.GatewayRequestHeaderUtils;
import com.pyokemon.common.dto.ResponseDto;

@ExtendWith(MockitoExtension.class)
public class AccountControllerTest {

    @Mock
    private AccountService accountService;
    
    @InjectMocks
    private AccountController accountController;
    
    private LoginRequestDto loginRequest;
    private LoginResponseDto loginResponse;
    private UpdatePasswordRequestDto updatePasswordRequest;
    
    @BeforeEach
    void setUp() {
        // 테스트용 요청 데이터 생성
        loginRequest = new LoginRequestDto();
        loginRequest.setLoginId("test@example.com");
        loginRequest.setPassword("password123");
        
        // 테스트용 응답 데이터 생성
        loginResponse = LoginResponseDto.builder()
            .accountId(1L)
            .role("USER")
            .accessToken("access-token")
            .refreshToken("refresh-token")
            .build();
        
        updatePasswordRequest = new UpdatePasswordRequestDto();
        updatePasswordRequest.setCurrentPassword("oldPassword");
        updatePasswordRequest.setNewPassword("newPassword123");
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
    
    // ========== 로그아웃 테스트 ==========
    
    @Test
    @DisplayName("로그아웃 성공 테스트")
    void logoutSuccess() {
        // given
        String authHeader = "Bearer valid-token";
        doNothing().when(accountService).logout(authHeader);
        
        // when
        ResponseEntity<ResponseDto<Void>> response = accountController.logout(authHeader);
        
        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("로그아웃 성공", response.getBody().getMessage());
        
        verify(accountService).logout(authHeader);
    }
    
    @Test
    @DisplayName("로그아웃 - Authorization 헤더 없음")
    void logoutWithoutAuthHeader() {
        // given
        String authHeader = null;
        doNothing().when(accountService).logout(null);
        
        // when
        ResponseEntity<ResponseDto<Void>> response = accountController.logout(authHeader);
        
        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("로그아웃 성공", response.getBody().getMessage());
        
        verify(accountService).logout(null);
    }
    
    // ========== 토큰 갱신 테스트 ==========
    
    @Test
    @DisplayName("토큰 갱신 성공 테스트")
    void refreshTokenSuccess() {
        // given
        String refreshToken = "valid-refresh-token";
        TokenResponseDto tokenResponse = TokenResponseDto.builder()
            .accessToken("new-access-token")
            .build();
        
        when(accountService.refreshToken(refreshToken)).thenReturn(tokenResponse);
        
        // when
        ResponseEntity<ResponseDto<TokenResponseDto>> response = accountController.refreshToken(refreshToken);
        
        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("토큰 갱신 성공", response.getBody().getMessage());
        assertEquals(tokenResponse, response.getBody().getData());
        
        verify(accountService).refreshToken(refreshToken);
    }
    
    @Test
    @DisplayName("토큰 갱신 실패 테스트")
    void refreshTokenFailure() {
        // given
        String refreshToken = "invalid-refresh-token";
        when(accountService.refreshToken(refreshToken)).thenThrow(new RuntimeException("Token refresh failed"));
        
        // when & then
        assertThrows(RuntimeException.class, () -> accountController.refreshToken(refreshToken));
        
        verify(accountService).refreshToken(refreshToken);
    }
    
    // ========== 비밀번호 변경 테스트 ==========
    
    @Test
    @DisplayName("비밀번호 변경 성공 테스트")
    void changePasswordSuccess() {
        // given
        // GatewayRequestHeaderUtils 모킹을 위해 정적 메소드 모킹 설정
        try (var gatewayUtilsMock = mockStatic(GatewayRequestHeaderUtils.class)) {
            gatewayUtilsMock.when(GatewayRequestHeaderUtils::getUserIdOrThrowException).thenReturn("1");
            
            doNothing().when(accountService).changePassword(1L, updatePasswordRequest);
            
            // when
            ResponseEntity<ResponseDto<Void>> response = accountController.changePassword(updatePasswordRequest);
            
            // then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("비밀번호 변경 성공", response.getBody().getMessage());
            
            verify(accountService).changePassword(1L, updatePasswordRequest);
        }
    }
    
    @Test
    @DisplayName("비밀번호 변경 실패 - 인증 정보 없음")
    void changePasswordNoAuthInfo() {
        // given
        try (var gatewayUtilsMock = mockStatic(GatewayRequestHeaderUtils.class)) {
            gatewayUtilsMock.when(GatewayRequestHeaderUtils::getUserIdOrThrowException)
                .thenThrow(new RuntimeException("No auth info"));
            
            // when & then
            assertThrows(RuntimeException.class, () -> accountController.changePassword(updatePasswordRequest));
            
            verifyNoInteractions(accountService);
        }
    }
}
