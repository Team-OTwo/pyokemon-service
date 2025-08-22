package com.pyokemon.did.api.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pyokemon.common.dto.ResponseDto;
import com.pyokemon.did.domain.dto.request.UserWalletRequest.CreateUserWalletRequest;
import com.pyokemon.did.service.UserWalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
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

    private static final String TEST_USER_ID = "test-user-123";

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
        Map<String, String> responseData = Map.of("userId", TEST_USER_ID);
        ResponseDto<Map<String, String>> responseDto = ResponseDto.success(responseData, "사용자 지갑 생성 완료");
        ResponseEntity<ResponseDto<Map<String, String>>> responseEntity = ResponseEntity.ok(responseDto);

        when(userWalletService.createUserWallet(TEST_USER_ID)).thenReturn(responseEntity);

        // when & then
        mockMvc.perform(post("/backend/wallets/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("사용자 지갑 생성 완료"))
                .andExpect(jsonPath("$.data.userId").value(TEST_USER_ID))
                .andExpect(jsonPath("$.errorCode").isEmpty());
    }

    @Test
    @DisplayName("이미 존재하는 사용자 지갑 생성 시도 시 CONFLICT 반환")
    void createUserWallet_AlreadyExists_ReturnsConflict() throws Exception {
        // given
        CreateUserWalletRequest request = new CreateUserWalletRequest(TEST_USER_ID);
        ResponseDto<Map<String, String>> responseDto = ResponseDto.error("이미 지갑이 존재하는 사용자입니다.", "WALLET_ALREADY_EXISTS");
        ResponseEntity<ResponseDto<Map<String, String>>> responseEntity = ResponseEntity.status(HttpStatus.CONFLICT).body(responseDto);

        when(userWalletService.createUserWallet(TEST_USER_ID)).thenReturn(responseEntity);

        // when & then
        mockMvc.perform(post("/backend/wallets/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("이미 지갑이 존재하는 사용자입니다."))
                .andExpect(jsonPath("$.errorCode").value("WALLET_ALREADY_EXISTS"));
    }

    @Test
    @DisplayName("빈 userId로 지갑 생성 시도 시 BAD_REQUEST 반환")
    void createUserWallet_EmptyUserId_ReturnsBadRequest() throws Exception {
        // given
        CreateUserWalletRequest request = new CreateUserWalletRequest("");
        ResponseDto<Map<String, String>> responseDto = ResponseDto.error("사용자 ID는 필수입니다.", "INVALID_REQUEST");
        ResponseEntity<ResponseDto<Map<String, String>>> responseEntity = ResponseEntity.badRequest().body(responseDto);

        when(userWalletService.createUserWallet("")).thenReturn(responseEntity);

        // when & then
        mockMvc.perform(post("/backend/wallets/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("사용자 ID는 필수입니다."))
                .andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST"));
    }

    @Test
    @DisplayName("ACA-Py 서비스 오류 시 INTERNAL_SERVER_ERROR 반환")
    void createUserWallet_AcaPyServiceError_ReturnsInternalServerError() throws Exception {
        // given
        CreateUserWalletRequest request = new CreateUserWalletRequest(TEST_USER_ID);
        ResponseDto<Map<String, String>> responseDto = ResponseDto.error("지갑 생성에 실패했습니다.", "WALLET_CREATION_FAILED");
        ResponseEntity<ResponseDto<Map<String, String>>> responseEntity = ResponseEntity.internalServerError().body(responseDto);

        when(userWalletService.createUserWallet(TEST_USER_ID)).thenReturn(responseEntity);

        // when & then
        mockMvc.perform(post("/backend/wallets/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("지갑 생성에 실패했습니다."))
                .andExpect(jsonPath("$.errorCode").value("WALLET_CREATION_FAILED"));
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

    @Test
    @DisplayName("Content-Type이 application/json이 아닐 때 처리")
    void createUserWallet_InvalidContentType_ReturnsBadRequest() throws Exception {
        // given
        CreateUserWalletRequest request = new CreateUserWalletRequest(TEST_USER_ID);

        // when & then
        mockMvc.perform(post("/backend/wallets/user")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    @DisplayName("null userId로 지갑 생성 시도 시 BAD_REQUEST 반환")
    void createUserWallet_NullUserId_ReturnsBadRequest() throws Exception {
        // given
        CreateUserWalletRequest request = new CreateUserWalletRequest(null);
        ResponseDto<Map<String, String>> responseDto = ResponseDto.error("사용자 ID는 필수입니다.", "INVALID_REQUEST");
        ResponseEntity<ResponseDto<Map<String, String>>> responseEntity = ResponseEntity.badRequest().body(responseDto);

        when(userWalletService.createUserWallet(null)).thenReturn(responseEntity);

        // when & then
        mockMvc.perform(post("/backend/wallets/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("사용자 ID는 필수입니다."))
                .andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST"));
    }
}
