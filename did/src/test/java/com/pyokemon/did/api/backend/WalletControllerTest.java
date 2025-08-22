package com.pyokemon.did.api.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pyokemon.did.domain.dto.request.UserWalletRequest.CreateUserWalletRequest;
import com.pyokemon.did.service.UserWalletService;
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

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class WalletControllerTest {

    @Mock
    private UserWalletService userWalletService;

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
        CreateUserWalletRequest request = new CreateUserWalletRequest(TEST_USER_ID);
        doNothing().when(userWalletService).createUserWallet(TEST_USER_ID);

        // when & then
        mockMvc.perform(post("/backend/wallets/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("사용자 지갑 생성 완료"))
                .andExpect(jsonPath("$.data.userId").value(String.valueOf(TEST_USER_ID)))
                .andExpect(jsonPath("$.errorCode").isEmpty());

        verify(userWalletService).createUserWallet(TEST_USER_ID);
    }

    @Test
    @DisplayName("잘못된 JSON 요청 시 BAD_REQUEST 반환")
    void createUserWallet_InvalidJson_ReturnsBadRequest() throws Exception {
        // given
        String invalidJson = "{\"invalid\": \"json\"";

        // when & then
        mockMvc.perform(post("/backend/wallets/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
}
