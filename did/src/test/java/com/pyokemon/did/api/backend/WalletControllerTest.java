package com.pyokemon.did.api.backend;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pyokemon.did.domain.Wallet;
import com.pyokemon.did.domain.dto.request.WalletRequest.RegisterWalletRequest;
import com.pyokemon.did.service.WalletService;

@ExtendWith(MockitoExtension.class)
class WalletControllerTest {

  @Mock
  private WalletService walletService;

  @InjectMocks
  private WalletController walletController;

  private MockMvc mockMvc;
  private ObjectMapper objectMapper;

  private static final Long TEST_USER_ID = 123L;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(walletController).build();
    objectMapper = new ObjectMapper();
  }

  @Test
  @DisplayName("사용자 지갑 생성 성공 테스트")
  void createUserWallet_Success() throws Exception {
    // given
    RegisterWalletRequest request =
        new RegisterWalletRequest(TEST_USER_ID, Wallet.AccountRole.USER);
    doNothing().when(walletService).registerWallet(request);

    // when & then
    mockMvc
        .perform(post("/backend/wallets/user").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("사용자 지갑 생성 완료"))
        .andExpect(jsonPath("$.data.userId").value(String.valueOf(TEST_USER_ID)))
        .andExpect(jsonPath("$.errorCode").isEmpty());

    verify(walletService).registerWallet(request);
  }

  @Test
  @DisplayName("잘못된 JSON 요청 시 BAD_REQUEST 반환")
  void createUserWallet_InvalidJson_ReturnsBadRequest() throws Exception {
    // given
    String invalidJson = "{\"invalid\": \"json\"";

    // when & then
    mockMvc.perform(
        post("/backend/wallets/user").contentType(MediaType.APPLICATION_JSON).content(invalidJson))
        .andExpect(status().isBadRequest());
  }
}
