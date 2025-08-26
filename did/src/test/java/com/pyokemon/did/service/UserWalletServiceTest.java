package com.pyokemon.did.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.common.exception.code.DidErrorCodes;
import com.pyokemon.did.domain.UserWallet;
import com.pyokemon.did.domain.repository.UserWalletRepository;
import com.pyokemon.did.remote.commonAcaPy.dto.response.WalletResponse.AcaPyCreateWalletResponse;
import com.pyokemon.did.remote.userAcaPy.RemoteUserAcaPyService;
import com.pyokemon.did.service.impl.UserWalletServiceImpl;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserWalletServiceTest {

  @Mock
  private UserWalletRepository userWalletRepository;

  @Mock
  private RemoteUserAcaPyService remoteUserAcaPyService;

  @InjectMocks
  private UserWalletServiceImpl userWalletService;

  private static final Long TEST_USER_ID = 123L;
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
        when(userWalletRepository.saveAndReturn(any(UserWallet.class))).thenReturn(1);

        // when & then
        assertThatCode(() -> userWalletService.createUserWallet(TEST_USER_ID))
                .doesNotThrowAnyException();

        verify(userWalletRepository).findByUserId(TEST_USER_ID);
        verify(remoteUserAcaPyService).acaPyCreateWallet(any());
        verify(userWalletRepository).saveAndReturn(any(UserWallet.class));
    }

  @Test
  @DisplayName("이미 존재하는 사용자 지갑 생성 시도 시 예외 발생")
  void createUserWallet_AlreadyExists_ThrowsException() {
    // given
    UserWallet existingWallet =
        UserWallet.builder().userId(TEST_USER_ID).token(TEST_TOKEN).build();
    when(userWalletRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.of(existingWallet));

    // when & then
    assertThatThrownBy(() -> userWalletService.createUserWallet(TEST_USER_ID))
        .isInstanceOf(BusinessException.class)
        .hasFieldOrPropertyWithValue("errorCode", DidErrorCodes.WALLET_ALREADY_EXISTS)
        .hasMessage("사용자 지갑이 이미 존재합니다.");

    verify(userWalletRepository).findByUserId(TEST_USER_ID);
    verify(remoteUserAcaPyService, never()).acaPyCreateWallet(any());
    verify(userWalletRepository, never()).saveAndReturn(any());
  }

  @Test
    @DisplayName("ACA-Py 서비스 오류 시 예외 발생")
    void createUserWallet_AcaPyServiceError_ThrowsException() {
        // given
        when(userWalletRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.empty());
        when(remoteUserAcaPyService.acaPyCreateWallet(any()))
                .thenThrow(new RuntimeException("ACA-Py 서비스 오류"));

        // when & then
        assertThatThrownBy(() -> userWalletService.createUserWallet(TEST_USER_ID))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", DidErrorCodes.WALLET_CREATION_FAILED)
                .hasMessage("지갑 생성 중 오류가 발생했습니다");

        verify(userWalletRepository).findByUserId(TEST_USER_ID);
        verify(remoteUserAcaPyService).acaPyCreateWallet(any());
        verify(userWalletRepository, never()).saveAndReturn(any());
    }

  @Test
    @DisplayName("ACA-Py 응답이 null인 경우 예외 발생")
    void createUserWallet_NullResponse_ThrowsException() {
        // given
        when(userWalletRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.empty());
        when(remoteUserAcaPyService.acaPyCreateWallet(any())).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> userWalletService.createUserWallet(TEST_USER_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("지갑 생성에 실패했습니다.");

        verify(userWalletRepository).findByUserId(TEST_USER_ID);
        verify(remoteUserAcaPyService).acaPyCreateWallet(any());
        verify(userWalletRepository, never()).saveAndReturn(any());
    }

  @Test
    @DisplayName("ACA-Py 응답의 토큰이 null인 경우 예외 발생")
    void createUserWallet_NullToken_ThrowsException() {
        // given
        when(userWalletRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.empty());
        
        AcaPyCreateWalletResponse mockResponse = new AcaPyCreateWalletResponse(
                LocalDateTime.now().toString(),
                LocalDateTime.now().toString(),
                TEST_WALLET_ID,
                "managed",
                null,
                null // 토큰이 null
        );
        
        when(remoteUserAcaPyService.acaPyCreateWallet(any())).thenReturn(mockResponse);

        // when & then
        assertThatThrownBy(() -> userWalletService.createUserWallet(TEST_USER_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("지갑 생성에 실패했습니다.");

        verify(userWalletRepository).findByUserId(TEST_USER_ID);
        verify(remoteUserAcaPyService).acaPyCreateWallet(any());
        verify(userWalletRepository, never()).saveAndReturn(any());
    }

  @Test
    @DisplayName("지갑 저장 실패 시 예외 발생")
    void createUserWallet_SaveFailure_ThrowsException() {
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
        when(userWalletRepository.saveAndReturn(any(UserWallet.class))).thenReturn(0); // 저장 실패

        // when & then
        assertThatThrownBy(() -> userWalletService.createUserWallet(TEST_USER_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("지갑 생성에 실패했습니다.");

        verify(userWalletRepository).findByUserId(TEST_USER_ID);
        verify(remoteUserAcaPyService).acaPyCreateWallet(any());
        verify(userWalletRepository).saveAndReturn(any());
    }

  @Test
    @DisplayName("데이터베이스 조회 오류 시 예외 발생")
    void createUserWallet_DatabaseQueryError_ThrowsException() {
        // given
        when(userWalletRepository.findByUserId(TEST_USER_ID))
                .thenThrow(new RuntimeException("데이터베이스 연결 오류"));

        // when & then
        assertThatThrownBy(() -> userWalletService.createUserWallet(TEST_USER_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("지갑 조회 중 오류가 발생했습니다: 데이터베이스 연결 오류");

        verify(userWalletRepository).findByUserId(TEST_USER_ID);
        verify(remoteUserAcaPyService, never()).acaPyCreateWallet(any());
        verify(userWalletRepository, never()).saveAndReturn(any());
    }
}
