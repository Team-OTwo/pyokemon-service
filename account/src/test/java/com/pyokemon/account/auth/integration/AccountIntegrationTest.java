package com.pyokemon.account.auth.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pyokemon.account.auth.dto.request.LoginRequestDto;
import com.pyokemon.account.auth.dto.request.UpdatePasswordRequestDto;
import com.pyokemon.account.auth.entity.Account;
import com.pyokemon.account.auth.entity.AccountStatus;
import com.pyokemon.account.auth.repository.AccountRepository;
import com.pyokemon.account.auth.secret.jwt.TokenGenerator;
import com.pyokemon.account.user.entity.User;
import com.pyokemon.account.user.repository.UserRepository;
import com.pyokemon.account.user.entity.UserDevice;
import com.pyokemon.account.user.repository.UserDeviceRepository;

/**
 * 통합 테스트:
 * - /api/login
 * - /api/refresh
 * - /api/password
 * - /api/logout (Authorization + X-Auth-AccountId + deviceNumber 반영)
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
public class AccountIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Autowired private AccountRepository accountRepository;
    @Autowired private TokenGenerator tokenGenerator;

    // 로그아웃 성공을 위해 필요한 추가 리포지토리
    @Autowired private UserRepository userRepository;
    @Autowired private UserDeviceRepository userDeviceRepository;

    private Account testAccount;
    private String validToken;

    // 디바이스 번호(요청 파라미터)
    private final String deviceNumber = "device-001";

    @BeforeEach
    void setUp() {
        // 1) 계정 생성
        testAccount = new Account();
        testAccount.setLoginId("integration@test.com");
        testAccount.setPassword("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa"); // "password123"
        testAccount.setRole("USER");
        testAccount.setStatus(AccountStatus.ACTIVE);
        accountRepository.insert(testAccount);

        // 2) 유효한 액세스 토큰 생성
        validToken = tokenGenerator.generateAccessToken(testAccount.getAccountId(), testAccount.getRole());

        // 3) 로그아웃 로직이 타는 분기 대비: User / UserDevice 준비
        //    (서비스는 role == USER || deviceNumber != null 이면 User, UserDevice 갱신 시도)
        User user = new User();
        user.setAccountId(testAccount.getAccountId());
        user.setName("통합테스트유저");
        userRepository.insert(user); // userId 생성

        UserDevice device = new UserDevice();
        device.setUserId(user.getUserId());
        device.setDeviceNumber(deviceNumber);
        device.setIsValid(true);  // 서비스가 findByUserIdAndIsValid(true)로 조회함
        device.setIsLogin(true);  // 초기 상태
        userDeviceRepository.insert(device);
    }

    // ========== 로그인 ==========

    @Test
    @DisplayName("로그인 통합 테스트 - 성공")
    void loginIntegrationSuccess() throws Exception {
        LoginRequestDto request = new LoginRequestDto();
        request.setLoginId("integration@test.com");
        request.setPassword("password123");

        mockMvc.perform(
                        post("/api/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("로그인 성공"))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists())
                .andExpect(jsonPath("$.data.role").value("USER"));
    }

    @Test
    @DisplayName("로그인 통합 테스트 - 실패 (잘못된 비밀번호)")
    void loginIntegrationFailure() throws Exception {
        LoginRequestDto request = new LoginRequestDto();
        request.setLoginId("integration@test.com");
        request.setPassword("wrongPassword");

        mockMvc.perform(
                        post("/api/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ========== 토큰 갱신 ==========

    @Test
    @DisplayName("토큰 갱신 통합 테스트 - 성공")
    void refreshTokenIntegrationSuccess() throws Exception {
        String refreshToken =
                tokenGenerator.generateRefreshToken(testAccount.getAccountId(), testAccount.getRole());

        mockMvc.perform(
                        post("/api/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(refreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("토큰 갱신 성공"))
                .andExpect(jsonPath("$.data.accessToken").exists());
    }

    // ========== 비밀번호 변경 ==========

    @Test
    @DisplayName("비밀번호 변경 통합 테스트 - 성공")
    void changePasswordIntegrationSuccess() throws Exception {
        UpdatePasswordRequestDto request = new UpdatePasswordRequestDto();
        request.setCurrentPassword("password123");
        request.setNewPassword("newPassword123");

        mockMvc.perform(
                        put("/api/password")
                                .header("X-Auth-UserId", testAccount.getAccountId().toString())
                                .header("X-Auth-UserRole", testAccount.getRole())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("비밀번호 변경 성공"));
    }

    // ========== 로그아웃 (변경된 시그니처 반영) ==========

    @Test
    @DisplayName("로그아웃 통합 테스트 - 성공 (Authorization + X-Auth-AccountId + deviceNumber)")
    void logoutIntegrationSuccess() throws Exception {
        mockMvc.perform(
                        post("/api/logout")
                                .header("Authorization", "Bearer " + validToken)
                                .header("X-Auth-AccountId", testAccount.getAccountId().toString())
                                .param("deviceNumber", deviceNumber))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("로그아웃 성공"));
    }

    @Test
    @DisplayName("로그아웃 통합 테스트 - Authorization 헤더 없음 (토큰 없으면 서비스가 조기 반환)")
    void logoutIntegrationWithoutAuthHeader() throws Exception {
        // 토큰이 없으면 서비스는 바로 return 하므로, 계정/디바이스가 없어도 OK 응답을 기대
        mockMvc.perform(
                        post("/api/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("로그아웃 성공"));
    }
}
