package com.pyokemon.did.service;

import com.pyokemon.common.dto.ResponseDto;
import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.common.exception.code.DidErrorCodes;
import com.pyokemon.did.domain.UserWallet;
import com.pyokemon.did.domain.repository.UserWalletRepository;
import com.pyokemon.did.remote.commonAcaPy.dto.response.WalletResponse.AcaPyCreateWalletResponse;
import com.pyokemon.did.remote.userAcaPy.RemoteUserAcaPyService;
import com.pyokemon.did.service.impl.UserWalletServiceImpl;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import feign.FeignException;
import feign.Request;
import java.util.Collections;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserWalletServiceTest {

    @Mock
    private UserWalletRepository userWalletRepository;

    @Mock
    private RemoteUserAcaPyService remoteUserAcaPyService;

    @InjectMocks
    private UserWalletServiceImpl userWalletService;

    private static final String TEST_USER_ID = "test-user-123";
    private static final String TEST_TOKEN = "test-token-123";
    private static final String TEST_WALLET_ID = "test-wallet-123";

    @BeforeEach
    void setUp() {
        // 기본 설정
    }

    @Test
    @DisplayName("사용자 지갑 생성 성공 테스트")
    void createUserWallet_Success() {
        // given
        when(userWalletRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.empty());
        
        AcaPyCreateWalletResponse mockResponse = new AcaPyCreateWalletResponse(
                LocalDateTime.now().toString(),
                LocalDateTime.now().toString(),
                TEST_WALLET_ID,
                "managed",
                null,
                TEST_TOKEN
        );
        
        when(remoteUserAcaPyService.acaPyCreateWallet(any())).thenReturn(mockResponse);
        when(userWalletRepository.saveAndReturn(any(UserWallet.class))).thenAnswer(invocation -> {
            UserWallet wallet = invocation.getArgument(0);
            wallet.setId(1L); // ID 설정
            return 1;
        });
        
        UserWallet savedWallet = UserWallet.builder()
                .id(1L)
                .userId(TEST_USER_ID)
                .token(TEST_TOKEN)
                .build();
        when(userWalletRepository.findById(anyLong())).thenReturn(Optional.of(savedWallet));

        // when
        ResponseEntity<ResponseDto<Map<String, String>>> response = userWalletService.createUserWallet(TEST_USER_ID);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData()).containsEntry("userId", TEST_USER_ID);
        assertThat(response.getBody().getMessage()).isEqualTo("사용자 지갑 생성 완료");

        verify(userWalletRepository).findByUserId(TEST_USER_ID);
        verify(remoteUserAcaPyService).acaPyCreateWallet(any());
        verify(userWalletRepository).saveAndReturn(any(UserWallet.class));
        verify(userWalletRepository).findById(1L);
    }

    @Test
    @DisplayName("이미 존재하는 사용자 지갑 생성 시도 시 CONFLICT 반환")
    void createUserWallet_AlreadyExists_ReturnsConflict() {
        // given
        UserWallet existingWallet = UserWallet.builder()
                .id(1L)
                .userId(TEST_USER_ID)
                .token(TEST_TOKEN)
                .build();
        when(userWalletRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.of(existingWallet));

        // when
        ResponseEntity<ResponseDto<Map<String, String>>> response = userWalletService.createUserWallet(TEST_USER_ID);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getErrorCode()).isEqualTo(DidErrorCodes.WALLET_ALREADY_EXISTS);
        assertThat(response.getBody().getMessage()).isEqualTo("이미 지갑이 존재하는 사용자입니다.");

        verify(userWalletRepository).findByUserId(TEST_USER_ID);
        verify(remoteUserAcaPyService, never()).acaPyCreateWallet(any());
        verify(userWalletRepository, never()).saveAndReturn(any());
    }

    @Test
    @DisplayName("빈 userId로 지갑 생성 시도 시 예외 발생")
    void createUserWallet_EmptyUserId_ThrowsException() {
        // given
        String emptyUserId = "";

        // when & then
        ResponseEntity<ResponseDto<Map<String, String>>> response = userWalletService.createUserWallet(emptyUserId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getErrorCode()).isEqualTo(DidErrorCodes.INVALID_REQUEST);
        assertThat(response.getBody().getMessage()).isEqualTo("사용자 ID는 필수입니다.");

        verify(userWalletRepository, never()).findByUserId(anyString());
        verify(remoteUserAcaPyService, never()).acaPyCreateWallet(any());
    }

    @Test
    @DisplayName("null userId로 지갑 생성 시도 시 예외 발생")
    void createUserWallet_NullUserId_ThrowsException() {
        // given
        String nullUserId = null;

        // when & then
        ResponseEntity<ResponseDto<Map<String, String>>> response = userWalletService.createUserWallet(nullUserId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getErrorCode()).isEqualTo(DidErrorCodes.INVALID_REQUEST);
        assertThat(response.getBody().getMessage()).isEqualTo("사용자 ID는 필수입니다.");

        verify(userWalletRepository, never()).findByUserId(anyString());
        verify(remoteUserAcaPyService, never()).acaPyCreateWallet(any());
    }

    @Test
    @DisplayName("ACA-Py BadRequest 예외 발생 시 적절한 에러 응답")
    void createUserWallet_AcaPyBadRequest_ReturnsBadRequest() {
        // given
        when(userWalletRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.empty());
        when(remoteUserAcaPyService.acaPyCreateWallet(any()))
                .thenThrow(new FeignException.BadRequest("Bad Request", 
                    Request.create(Request.HttpMethod.POST, "/", Collections.emptyMap(), 
                                 "Bad Request".getBytes(), null), 
                    "Bad Request".getBytes(), Collections.emptyMap()));

        // when
        ResponseEntity<ResponseDto<Map<String, String>>> response = userWalletService.createUserWallet(TEST_USER_ID);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getErrorCode()).isEqualTo(DidErrorCodes.INVALID_REQUEST);
        assertThat(response.getBody().getMessage()).isEqualTo("ACA-Py 지갑 생성 요청이 잘못되었습니다.");

        verify(userWalletRepository).findByUserId(TEST_USER_ID);
        verify(remoteUserAcaPyService).acaPyCreateWallet(any());
        verify(userWalletRepository, never()).saveAndReturn(any());
    }

    @Test
    @DisplayName("ACA-Py NotFound 예외 발생 시 적절한 에러 응답")
    void createUserWallet_AcaPyNotFound_ReturnsBadRequest() {
        // given
        when(userWalletRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.empty());
        when(remoteUserAcaPyService.acaPyCreateWallet(any()))
                .thenThrow(new FeignException.NotFound("Not Found", 
                    Request.create(Request.HttpMethod.POST, "/", Collections.emptyMap(), 
                                 "Not Found".getBytes(), null), 
                    "Not Found".getBytes(), Collections.emptyMap()));

        // when
        ResponseEntity<ResponseDto<Map<String, String>>> response = userWalletService.createUserWallet(TEST_USER_ID);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getErrorCode()).isEqualTo(DidErrorCodes.ACAPY_SERVICE_ERROR);
        assertThat(response.getBody().getMessage()).isEqualTo("ACA-Py 서비스를 찾을 수 없습니다.");

        verify(userWalletRepository).findByUserId(TEST_USER_ID);
        verify(remoteUserAcaPyService).acaPyCreateWallet(any());
        verify(userWalletRepository, never()).saveAndReturn(any());
    }

    @Test
    @DisplayName("지갑 저장 실패 시 적절한 에러 응답")
    void createUserWallet_SaveFailure_ReturnsInternalServerError() {
        // given
        when(userWalletRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.empty());
        
        AcaPyCreateWalletResponse mockResponse = new AcaPyCreateWalletResponse(
                LocalDateTime.now().toString(),
                LocalDateTime.now().toString(),
                TEST_WALLET_ID,
                "managed",
                null,
                TEST_TOKEN
        );
        
        when(remoteUserAcaPyService.acaPyCreateWallet(any())).thenReturn(mockResponse);
        when(userWalletRepository.saveAndReturn(any(UserWallet.class))).thenAnswer(invocation -> {
            UserWallet wallet = invocation.getArgument(0);
            wallet.setId(1L); // ID 설정
            return 0; // 저장 실패
        });
        
        when(userWalletRepository.findById(anyLong())).thenReturn(Optional.empty()); // 조회 실패

        // when
        ResponseEntity<ResponseDto<Map<String, String>>> response = userWalletService.createUserWallet(TEST_USER_ID);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getErrorCode()).isEqualTo(DidErrorCodes.WALLET_CREATION_FAILED);
        assertThat(response.getBody().getMessage()).isEqualTo("지갑 생성에 실패했습니다.");

        verify(userWalletRepository).findByUserId(TEST_USER_ID);
        verify(remoteUserAcaPyService).acaPyCreateWallet(any());
        verify(userWalletRepository).saveAndReturn(any());
    }

    @Test
    @DisplayName("사용자 지갑 조회 성공 테스트")
    void getUserWallet_Success() {
        // given
        UserWallet expectedWallet = UserWallet.builder()
                .id(1L)
                .userId(TEST_USER_ID)
                .token(TEST_TOKEN)
                .build();
        when(userWalletRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.of(expectedWallet));

        // when
        UserWallet result = userWalletService.getUserWallet(TEST_USER_ID);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(result.getToken()).isEqualTo(TEST_TOKEN);

        verify(userWalletRepository).findByUserId(TEST_USER_ID);
    }

    @Test
    @DisplayName("존재하지 않는 사용자 지갑 조회 시 예외 발생")
    void getUserWallet_NotFound_ThrowsException() {
        // given
        when(userWalletRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userWalletService.getUserWallet(TEST_USER_ID))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", DidErrorCodes.WALLET_NOTFOUND)
                .hasMessageContaining("해당 userId의 지갑을 찾을 수 없습니다");

        verify(userWalletRepository).findByUserId(TEST_USER_ID);
    }

    @Test
    @DisplayName("사용자 지갑 존재 여부 확인 테스트")
    void existsByUserId_ReturnsCorrectValue() {
        // given
        when(userWalletRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.of(new UserWallet()));

        // when
        boolean exists = userWalletService.existsByUserId(TEST_USER_ID);

        // then
        assertThat(exists).isTrue();
        verify(userWalletRepository).findByUserId(TEST_USER_ID);
    }

    @Test
    @DisplayName("사용자 지갑 존재하지 않음 확인 테스트")
    void existsByUserId_NotExists_ReturnsFalse() {
        // given
        when(userWalletRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.empty());

        // when
        boolean exists = userWalletService.existsByUserId(TEST_USER_ID);

        // then
        assertThat(exists).isFalse();
        verify(userWalletRepository).findByUserId(TEST_USER_ID);
    }
}
