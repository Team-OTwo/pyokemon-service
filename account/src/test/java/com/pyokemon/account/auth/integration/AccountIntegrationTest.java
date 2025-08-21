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

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
public class AccountIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private AccountRepository accountRepository;

  @Autowired
  private TokenGenerator tokenGenerator;

  private Account testAccount;
  private String validToken;

  @BeforeEach
  void setUp() {
    // 테스트용 계정 생성
    testAccount = new Account();
    testAccount.setLoginId("integration@test.com");
    testAccount.setPassword("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa"); // "password123"
    testAccount.setRole("USER");
    testAccount.setStatus(AccountStatus.ACTIVE);

    accountRepository.insert(testAccount);

    // 유효한 토큰 생성
    validToken =
        tokenGenerator.generateAccessToken(testAccount.getAccountId(), testAccount.getRole());
  }

  @Test
  @DisplayName("로그인 통합 테스트 - 성공")
  void loginIntegrationSuccess() throws Exception {
    // given
    LoginRequestDto request = new LoginRequestDto();
    request.setLoginId("integration@test.com");
    request.setPassword("password123");

    // when & then
    mockMvc
        .perform(post("/api/login").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("로그인 성공"))
        .andExpect(jsonPath("$.data.accessToken").exists())
        .andExpect(jsonPath("$.data.refreshToken").exists())
        .andExpect(jsonPath("$.data.role").value("USER"));
  }

  @Test
  @DisplayName("로그인 통합 테스트 - 실패 (잘못된 비밀번호)")
  void loginIntegrationFailure() throws Exception {
    // given
    LoginRequestDto request = new LoginRequestDto();
    request.setLoginId("integration@test.com");
    request.setPassword("wrongPassword");

    // when & then
    mockMvc
        .perform(post("/api/login").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest()).andExpect(jsonPath("$.success").value(false));
  }

  @Test
  @DisplayName("토큰 갱신 통합 테스트 - 성공")
  void refreshTokenIntegrationSuccess() throws Exception {
    // given
    String refreshToken =
        tokenGenerator.generateRefreshToken(testAccount.getAccountId(), testAccount.getRole());

    // when & then
    mockMvc
        .perform(post("/api/refresh").contentType(MediaType.APPLICATION_JSON).content(refreshToken))
        .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("토큰 갱신 성공"))
        .andExpect(jsonPath("$.data.accessToken").exists());
  }

  @Test
  @DisplayName("비밀번호 변경 통합 테스트 - 성공")
  void changePasswordIntegrationSuccess() throws Exception {
    // given
    UpdatePasswordRequestDto request = new UpdatePasswordRequestDto();
    request.setCurrentPassword("password123");
    request.setNewPassword("newPassword123");

    // when & then
    mockMvc
        .perform(put("/api/password").header("X-Auth-UserId", testAccount.getAccountId().toString())
            .header("X-Auth-UserRole", testAccount.getRole())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("비밀번호 변경 성공"));
  }

  @Test
  @DisplayName("로그아웃 통합 테스트 - 성공")
  void logoutIntegrationSuccess() throws Exception {
    // given
    String authHeader = "Bearer " + validToken;

    // when & then
    mockMvc.perform(post("/api/logout").header("Authorization", authHeader))
        .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("로그아웃 성공"));
  }
}
