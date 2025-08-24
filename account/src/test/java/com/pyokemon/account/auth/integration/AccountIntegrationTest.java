// package com.pyokemon.account.auth.integration;
//
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.when;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
// import org.springframework.boot.test.mock.mockito.MockBean;
// import org.springframework.http.MediaType;
// import org.springframework.test.web.servlet.MockMvc;
//
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.pyokemon.account.auth.controller.AccountController;
// import com.pyokemon.account.auth.dto.request.LoginRequestDto;
// import com.pyokemon.account.auth.dto.response.LoginResponseDto;
// import com.pyokemon.account.auth.service.AccountService;
// import com.pyokemon.common.dto.ResponseDto;
//
// @WebMvcTest(AccountController.class)
// class AccountIntegrationTest {
//
// @Autowired
// private MockMvc mockMvc;
//
// @MockBean
// private AccountService accountService;
//
// @Autowired
// private ObjectMapper objectMapper;
//
// private LoginRequestDto loginRequest;
// private LoginResponseDto loginResponse;
//
// @BeforeEach
// void setUp() {
// // 테스트용 요청 데이터 생성
// loginRequest =
// LoginRequestDto.builder().loginId("test@example.com").password("password123").build();
//
// // 테스트용 응답 데이터 생성
// loginResponse =
// LoginResponseDto.builder().accountId(1L).role("USER").accessToken("access-token")
// .refreshToken("refresh-token").userName("테스트 사용자").isVerified(true).build();
// }
//
// @Test
// @DisplayName("로그인 통합 테스트 - 성공")
// void loginIntegrationSuccess() throws Exception {
// // given
// when(accountService.login(any(LoginRequestDto.class))).thenReturn(loginResponse);
//
// // when & then
// mockMvc.perform(post("/api/login")
// .contentType(MediaType.APPLICATION_JSON)
// .content(objectMapper.writeValueAsString(loginRequest)))
// .andExpect(status().isOk());
// }
//
// @Test
// @DisplayName("로그인 통합 테스트 - 실패")
// void loginIntegrationFailure() throws Exception {
// // given
// when(accountService.login(any(LoginRequestDto.class)))
// .thenThrow(new RuntimeException("Login failed"));
//
// // when & then
// mockMvc.perform(post("/api/login")
// .contentType(MediaType.APPLICATION_JSON)
// .content(objectMapper.writeValueAsString(loginRequest)))
// .andExpect(status().isInternalServerError());
// }
//
// @Test
// @DisplayName("잘못된 요청 형식 테스트")
// void invalidRequestFormat() throws Exception {
// // given
// String invalidJson = "{ invalid json }";
//
// // when & then
// mockMvc.perform(post("/api/login").contentType(MediaType.APPLICATION_JSON).content(invalidJson))
// .andExpect(status().isBadRequest());
// }
//
// @Test
// @DisplayName("Content-Type이 없는 요청 테스트")
// void requestWithoutContentType() throws Exception {
// // when & then
// mockMvc.perform(post("/api/login").content(objectMapper.writeValueAsString(loginRequest)))
// .andExpect(status().isUnsupportedMediaType());
// }
//
// @Test
// @DisplayName("잘못된 HTTP 메서드 테스트")
// void invalidHttpMethod() throws Exception {
// // when & then
// mockMvc.perform(post("/api/login").contentType(MediaType.APPLICATION_JSON)
// .content(objectMapper.writeValueAsString(loginRequest))).andExpect(status().isOk());
// }
// }
