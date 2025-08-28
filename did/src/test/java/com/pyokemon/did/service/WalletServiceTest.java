package com.pyokemon.did.service;

import static com.pyokemon.common.exception.code.DidErrorCodes.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.did.domain.Wallet;
import com.pyokemon.did.domain.dto.request.WalletRequest.RegisterWalletRequest;
import com.pyokemon.did.domain.repository.WalletRepository;
import com.pyokemon.did.remote.acapy.common.dto.request.CreatePublicDidRequest;
import com.pyokemon.did.remote.acapy.common.dto.request.CreateWalletRequest;
import com.pyokemon.did.remote.acapy.common.dto.response.CreatePublicDidResponse;
import com.pyokemon.did.remote.acapy.common.dto.response.CreateWalletResponse;
import com.pyokemon.did.remote.acapy.service.RemoteTenantAcaPyService;
import com.pyokemon.did.remote.acapy.service.RemoteUserAcaPyService;
import com.pyokemon.did.service.impl.WalletServiceImpl;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

  @Mock
  private RemoteTenantAcaPyService remoteTenantAcaPyService;
  @Mock
  private RemoteUserAcaPyService remoteUserAcaPyService;

  @Mock
  private WalletRepository walletRepository;

  @InjectMocks
  private WalletServiceImpl walletService;

  private RegisterWalletRequest tenantWalletRequest;
  private RegisterWalletRequest userWalletRequest;
  private RegisterWalletRequest invalidRoleRequest;
  private CreateWalletResponse walletResponse;
  private CreatePublicDidResponse publicDidResponse;
  private Wallet wallet;
  private final Long TENANT_ID = 1L;
  private final Long USER_ID = 2L;

  @BeforeEach
  void setUp() {
    // 테넌트 지갑 요청 객체 생성
    tenantWalletRequest = new RegisterWalletRequest();
    tenantWalletRequest.setAccountId(TENANT_ID);
    tenantWalletRequest.setAccountRole(Wallet.AccountRole.TENANT);

    // 사용자 지갑 요청 객체 생성
    userWalletRequest = new RegisterWalletRequest();
    userWalletRequest.setAccountId(USER_ID);
    userWalletRequest.setAccountRole(Wallet.AccountRole.USER);

    // 유효하지 않은 역할 요청 객체 생성
    invalidRoleRequest = new RegisterWalletRequest();
    invalidRoleRequest.setAccountId(3L);
    invalidRoleRequest.setAccountRole(null);

    // 지갑 응답 객체 생성
    walletResponse = new CreateWalletResponse();
    walletResponse.setToken("test-token");
    walletResponse.setWalletId("test-wallet-id");

    // DID 응답 객체 생성
    publicDidResponse = new CreatePublicDidResponse();
    publicDidResponse.getResult().setDid("test-did");
    publicDidResponse.getResult().setVerkey("test-ver-key");

    // 지갑 객체 생성
    wallet = Wallet.builder().accountId(TENANT_ID).accountRole(Wallet.AccountRole.TENANT)
        .token("test-token").publicDid("test-did").publicVerKey("test-ver-key").build();
  }

  @Test
    @DisplayName("테넌트 지갑 등록 성공 테스트")
    void tenant_registerWallet_Success() {
        // Given
        when(walletRepository.existsByAccountId(TENANT_ID)).thenReturn(false);
        when(remoteTenantAcaPyService.createWallet(any(CreateWalletRequest.class))).thenReturn(walletResponse);
        when(remoteTenantAcaPyService.createPublicDid(anyString(), any(CreatePublicDidRequest.class))).thenReturn(publicDidResponse);
        when(walletRepository.save(any(Wallet.class))).thenReturn(1L);

        // When
        assertDoesNotThrow(() -> walletService.registerWallet(tenantWalletRequest));

        // Then
        verify(walletRepository).existsByAccountId(TENANT_ID);
        verify(remoteTenantAcaPyService).createWallet(any(CreateWalletRequest.class));
        verify(remoteTenantAcaPyService).createPublicDid(anyString(), any(CreatePublicDidRequest.class));
        verify(remoteUserAcaPyService, never()).createWallet(any(CreateWalletRequest.class));
        verify(remoteUserAcaPyService, never()).createPublicDid(anyString(), any(CreatePublicDidRequest.class));
        verify(walletRepository).save(any(Wallet.class));
    }

  @Test
    @DisplayName("사용자 지갑 등록 성공 테스트")
    void user_registerWallet_Success() {
        // Given
        when(walletRepository.existsByAccountId(USER_ID)).thenReturn(false);
        when(remoteUserAcaPyService.createWallet(any(CreateWalletRequest.class))).thenReturn(walletResponse);
        when(remoteUserAcaPyService.createPublicDid(anyString(), any(CreatePublicDidRequest.class))).thenReturn(publicDidResponse);
        when(walletRepository.save(any(Wallet.class))).thenReturn(1L);

        // When
        assertDoesNotThrow(() -> walletService.registerWallet(userWalletRequest));

        // Then
        verify(walletRepository).existsByAccountId(USER_ID);
        verify(remoteUserAcaPyService).createWallet(any(CreateWalletRequest.class));
        verify(remoteUserAcaPyService).createPublicDid(anyString(), any(CreatePublicDidRequest.class));
        verify(remoteTenantAcaPyService, never()).createWallet(any(CreateWalletRequest.class));
        verify(remoteTenantAcaPyService, never()).createPublicDid(anyString(), any(CreatePublicDidRequest.class));
        verify(walletRepository).save(any(Wallet.class));
    }

  @Test
    @DisplayName("유효하지 않은 계정 역할로 지갑 등록 시 예외 발생 테스트")
    void registerWallet_InvalidAccountRole() {
        // Given
        when(walletRepository.existsByAccountId(anyLong())).thenReturn(false);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> walletService.registerWallet(invalidRoleRequest));

        assertEquals("유효하지 않은 계정입니다.", exception.getMessage());
        assertEquals(WALLET_CREATION_FAILED, exception.getErrorCode());

        verify(walletRepository).existsByAccountId(anyLong());
        verify(remoteTenantAcaPyService, never()).createWallet(any());
        verify(remoteUserAcaPyService, never()).createWallet(any());
        verify(walletRepository, never()).save(any());
    }

  @Test
    @DisplayName("이미 존재하는 테넌트 지갑 등록 시 예외 발생 테스트")
    void tenant_registerWallet_WalletAlreadyExists() {
        // Given
        when(walletRepository.existsByAccountId(TENANT_ID)).thenReturn(true);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> walletService.registerWallet(tenantWalletRequest));

        assertEquals("계정 지갑이 이미 존재합니다.", exception.getMessage());
        assertEquals(WALLET_ALREADY_EXISTS, exception.getErrorCode());

        verify(walletRepository).existsByAccountId(TENANT_ID);
        verify(remoteTenantAcaPyService, never()).createWallet(any(CreateWalletRequest.class));
        verify(remoteTenantAcaPyService, never()).createPublicDid(anyString(), any(CreatePublicDidRequest.class));
        verify(remoteUserAcaPyService, never()).createWallet(any(CreateWalletRequest.class));
        verify(remoteUserAcaPyService, never()).createPublicDid(anyString(), any(CreatePublicDidRequest.class));
        verify(walletRepository, never()).save(any(Wallet.class));
    }

  @Test
    @DisplayName("이미 존재하는 사용자 지갑 등록 시 예외 발생 테스트")
    void user_registerWallet_WalletAlreadyExists() {
        // Given
        when(walletRepository.existsByAccountId(USER_ID)).thenReturn(true);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> walletService.registerWallet(userWalletRequest));

        assertEquals("계정 지갑이 이미 존재합니다.", exception.getMessage());
        assertEquals(WALLET_ALREADY_EXISTS, exception.getErrorCode());

        verify(walletRepository).existsByAccountId(USER_ID);
        verify(remoteTenantAcaPyService, never()).createWallet(any(CreateWalletRequest.class));
        verify(remoteTenantAcaPyService, never()).createPublicDid(anyString(), any(CreatePublicDidRequest.class));
        verify(remoteUserAcaPyService, never()).createWallet(any(CreateWalletRequest.class));
        verify(remoteUserAcaPyService, never()).createPublicDid(anyString(), any(CreatePublicDidRequest.class));
        verify(walletRepository, never()).save(any(Wallet.class));
    }

  @Test
    @DisplayName("테넌트 지갑 생성 응답이 null인 경우 예외 발생 테스트")
    void registerWallet_TenantWalletResponseNull() {
        // Given
        when(walletRepository.existsByAccountId(TENANT_ID)).thenReturn(false);
        when(remoteTenantAcaPyService.createWallet(any(CreateWalletRequest.class))).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> walletService.registerWallet(tenantWalletRequest));

        assertEquals("지갑 생성에 실패했습니다.", exception.getMessage());
        assertEquals(WALLET_CREATION_FAILED, exception.getErrorCode());

        verify(walletRepository).existsByAccountId(TENANT_ID);
        verify(remoteTenantAcaPyService).createWallet(any(CreateWalletRequest.class));
        verify(remoteTenantAcaPyService, never()).createPublicDid(anyString(), any(CreatePublicDidRequest.class));
        verify(remoteUserAcaPyService, never()).createWallet(any(CreateWalletRequest.class));
        verify(remoteUserAcaPyService, never()).createPublicDid(anyString(), any(CreatePublicDidRequest.class));
        verify(walletRepository, never()).save(any(Wallet.class));
    }

  @Test
    @DisplayName("사용자 지갑 생성 응답이 null인 경우 예외 발생 테스트")
    void registerWallet_UserWalletResponseNull() {
        // Given
        when(walletRepository.existsByAccountId(USER_ID)).thenReturn(false);
        when(remoteUserAcaPyService.createWallet(any(CreateWalletRequest.class))).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> walletService.registerWallet(userWalletRequest));

        assertEquals("지갑 생성에 실패했습니다.", exception.getMessage());
        assertEquals(WALLET_CREATION_FAILED, exception.getErrorCode());

        verify(walletRepository).existsByAccountId(USER_ID);
        verify(remoteTenantAcaPyService, never()).createWallet(any(CreateWalletRequest.class));
        verify(remoteTenantAcaPyService, never()).createPublicDid(anyString(), any(CreatePublicDidRequest.class));
        verify(remoteUserAcaPyService).createWallet(any(CreateWalletRequest.class));
        verify(remoteUserAcaPyService, never()).createPublicDid(anyString(), any(CreatePublicDidRequest.class));
        verify(walletRepository, never()).save(any(Wallet.class));
    }

  @Test
    @DisplayName("지갑 생성 응답의 토큰이 null인 경우 예외 발생 테스트")
    void registerWallet_WalletTokenNull() {
        // Given
        when(walletRepository.existsByAccountId(TENANT_ID)).thenReturn(false);

        CreateWalletResponse nullTokenResponse = new CreateWalletResponse();
        nullTokenResponse.setToken(null);

        when(remoteTenantAcaPyService.createWallet(any(CreateWalletRequest.class))).thenReturn(nullTokenResponse);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> walletService.registerWallet(tenantWalletRequest));

        assertEquals("지갑 생성에 실패했습니다.", exception.getMessage());
        assertEquals(WALLET_CREATION_FAILED, exception.getErrorCode());

        verify(walletRepository).existsByAccountId(TENANT_ID);
        verify(remoteTenantAcaPyService).createWallet(any(CreateWalletRequest.class));
        verify(remoteTenantAcaPyService, never()).createPublicDid(anyString(), any(CreatePublicDidRequest.class));
        verify(remoteUserAcaPyService, never()).createWallet(any(CreateWalletRequest.class));
        verify(remoteUserAcaPyService, never()).createPublicDid(anyString(), any(CreatePublicDidRequest.class));
        verify(walletRepository, never()).save(any(Wallet.class));
    }

  @Test
    @DisplayName("테넌트 DID 생성 응답이 null인 경우 예외 발생 테스트")
    void registerWallet_TenantPublicDidResponseNull() {
        // Given
        when(walletRepository.existsByAccountId(TENANT_ID)).thenReturn(false);
        when(remoteTenantAcaPyService.createWallet(any(CreateWalletRequest.class))).thenReturn(walletResponse);
        when(remoteTenantAcaPyService.createPublicDid(anyString(), any(CreatePublicDidRequest.class))).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> walletService.registerWallet(tenantWalletRequest));

        assertEquals("공개 DID 생성에 실패했습니다.", exception.getMessage());
        assertEquals(DID_CREATION_FAILED, exception.getErrorCode());

        verify(walletRepository).existsByAccountId(TENANT_ID);
        verify(remoteTenantAcaPyService).createWallet(any(CreateWalletRequest.class));
        verify(remoteTenantAcaPyService).createPublicDid(anyString(), any(CreatePublicDidRequest.class));
        verify(remoteUserAcaPyService, never()).createWallet(any(CreateWalletRequest.class));
        verify(remoteUserAcaPyService, never()).createPublicDid(anyString(), any(CreatePublicDidRequest.class));
        verify(walletRepository, never()).save(any(Wallet.class));
    }

  @Test
    @DisplayName("사용자 DID 생성 응답이 null인 경우 예외 발생 테스트")
    void registerWallet_UserPublicDidResponseNull() {
        // Given
        when(walletRepository.existsByAccountId(USER_ID)).thenReturn(false);
        when(remoteUserAcaPyService.createWallet(any(CreateWalletRequest.class))).thenReturn(walletResponse);
        when(remoteUserAcaPyService.createPublicDid(anyString(), any(CreatePublicDidRequest.class))).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> walletService.registerWallet(userWalletRequest));

        assertEquals("공개 DID 생성에 실패했습니다.", exception.getMessage());
        assertEquals(DID_CREATION_FAILED, exception.getErrorCode());

        verify(walletRepository).existsByAccountId(USER_ID);
        verify(remoteTenantAcaPyService, never()).createWallet(any(CreateWalletRequest.class));
        verify(remoteTenantAcaPyService, never()).createPublicDid(anyString(), any(CreatePublicDidRequest.class));
        verify(remoteUserAcaPyService).createWallet(any(CreateWalletRequest.class));
        verify(remoteUserAcaPyService).createPublicDid(anyString(), any(CreatePublicDidRequest.class));
        verify(walletRepository, never()).save(any(Wallet.class));
    }

  @Test
    @DisplayName("DID 생성 응답의 DID가 null인 경우 예외 발생 테스트")
    void registerWallet_PublicDidNull() {
        // Given
        when(walletRepository.existsByAccountId(TENANT_ID)).thenReturn(false);
        when(remoteTenantAcaPyService.createWallet(any(CreateWalletRequest.class))).thenReturn(walletResponse);

        CreatePublicDidResponse nullDidResponse = new CreatePublicDidResponse();
        nullDidResponse.getResult().setDid(null);
        nullDidResponse.getResult().setVerkey("test-verkey");

        when(remoteTenantAcaPyService.createPublicDid(anyString(), any(CreatePublicDidRequest.class)))
                .thenReturn(nullDidResponse);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> walletService.registerWallet(tenantWalletRequest));

        assertEquals("공개 DID 생성에 실패했습니다.", exception.getMessage());
        assertEquals(DID_CREATION_FAILED, exception.getErrorCode());

        verify(walletRepository).existsByAccountId(TENANT_ID);
        verify(remoteTenantAcaPyService).createWallet(any(CreateWalletRequest.class));
        verify(remoteTenantAcaPyService).createPublicDid(anyString(), any(CreatePublicDidRequest.class));
        verify(remoteUserAcaPyService, never()).createWallet(any(CreateWalletRequest.class));
        verify(remoteUserAcaPyService, never()).createPublicDid(anyString(), any(CreatePublicDidRequest.class));
        verify(walletRepository, never()).save(any(Wallet.class));
    }

  @Test
    @DisplayName("테넌트 지갑 생성 API 호출 중 예외 발생 테스트")
    void registerWallet_TenantWalletApiException() {
        // Given
        when(walletRepository.existsByAccountId(TENANT_ID)).thenReturn(false);
        when(remoteTenantAcaPyService.createWallet(any(CreateWalletRequest.class)))
                .thenThrow(new RuntimeException("API 호출 실패"));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> walletService.registerWallet(tenantWalletRequest));

        assertEquals("외부 시스템 연동 중 오류가 발생했습니다", exception.getMessage());
        assertEquals(WALLET_CREATION_FAILED, exception.getErrorCode());

        verify(walletRepository).existsByAccountId(TENANT_ID);
        verify(remoteTenantAcaPyService).createWallet(any(CreateWalletRequest.class));
        verify(remoteTenantAcaPyService, never()).createPublicDid(anyString(), any(CreatePublicDidRequest.class));
        verify(remoteUserAcaPyService, never()).createWallet(any(CreateWalletRequest.class));
        verify(remoteUserAcaPyService, never()).createPublicDid(anyString(), any(CreatePublicDidRequest.class));
        verify(walletRepository, never()).save(any(Wallet.class));
    }

  @Test
    @DisplayName("사용자 지갑 생성 API 호출 중 예외 발생 테스트")
    void registerWallet_UserWalletApiException() {
        // Given
        when(walletRepository.existsByAccountId(USER_ID)).thenReturn(false);
        when(remoteUserAcaPyService.createWallet(any(CreateWalletRequest.class)))
                .thenThrow(new RuntimeException("API 호출 실패"));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> walletService.registerWallet(userWalletRequest));

        assertEquals("외부 시스템 연동 중 오류가 발생했습니다", exception.getMessage());
        assertEquals(WALLET_CREATION_FAILED, exception.getErrorCode());

        verify(walletRepository).existsByAccountId(USER_ID);
        verify(remoteTenantAcaPyService, never()).createWallet(any(CreateWalletRequest.class));
        verify(remoteTenantAcaPyService, never()).createPublicDid(anyString(), any(CreatePublicDidRequest.class));
        verify(remoteUserAcaPyService).createWallet(any(CreateWalletRequest.class));
        verify(remoteUserAcaPyService, never()).createPublicDid(anyString(), any(CreatePublicDidRequest.class));
        verify(walletRepository, never()).save(any(Wallet.class));
    }

  @Test
    @DisplayName("테넌트 DID 생성 API 호출 중 예외 발생 테스트")
    void registerWallet_TenantDidApiException() {
        // Given
        when(walletRepository.existsByAccountId(TENANT_ID)).thenReturn(false);
        when(remoteTenantAcaPyService.createWallet(any(CreateWalletRequest.class))).thenReturn(walletResponse);
        when(remoteTenantAcaPyService.createPublicDid(anyString(), any(CreatePublicDidRequest.class)))
                .thenThrow(new RuntimeException("API 호출 실패"));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> walletService.registerWallet(tenantWalletRequest));

        assertEquals("외부 시스템 연동 중 오류가 발생했습니다", exception.getMessage());
        assertEquals(WALLET_CREATION_FAILED, exception.getErrorCode());

        verify(walletRepository).existsByAccountId(TENANT_ID);
        verify(remoteTenantAcaPyService).createWallet(any(CreateWalletRequest.class));
        verify(remoteTenantAcaPyService).createPublicDid(anyString(), any(CreatePublicDidRequest.class));
        verify(remoteUserAcaPyService, never()).createWallet(any(CreateWalletRequest.class));
        verify(remoteUserAcaPyService, never()).createPublicDid(anyString(), any(CreatePublicDidRequest.class));
        verify(walletRepository, never()).save(any(Wallet.class));
    }

  @Test
    @DisplayName("사용자 DID 생성 API 호출 중 예외 발생 테스트")
    void registerWallet_UserDidApiException() {
        // Given
        when(walletRepository.existsByAccountId(USER_ID)).thenReturn(false);
        when(remoteUserAcaPyService.createWallet(any(CreateWalletRequest.class))).thenReturn(walletResponse);
        when(remoteUserAcaPyService.createPublicDid(anyString(), any(CreatePublicDidRequest.class)))
                .thenThrow(new RuntimeException("API 호출 실패"));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> walletService.registerWallet(userWalletRequest));

        assertEquals("외부 시스템 연동 중 오류가 발생했습니다", exception.getMessage());
        assertEquals(WALLET_CREATION_FAILED, exception.getErrorCode());

        verify(walletRepository).existsByAccountId(USER_ID);
        verify(remoteTenantAcaPyService, never()).createWallet(any(CreateWalletRequest.class));
        verify(remoteTenantAcaPyService, never()).createPublicDid(anyString(), any(CreatePublicDidRequest.class));
        verify(remoteUserAcaPyService).createWallet(any(CreateWalletRequest.class));
        verify(remoteUserAcaPyService).createPublicDid(anyString(), any(CreatePublicDidRequest.class));
        verify(walletRepository, never()).save(any(Wallet.class));
    }

  @Test
    @DisplayName("DB 저장 중 예외 발생 테스트")
    void registerWallet_DbSaveException() {
        // Given
        when(walletRepository.existsByAccountId(TENANT_ID)).thenReturn(false);
        when(remoteTenantAcaPyService.createWallet(any(CreateWalletRequest.class))).thenReturn(walletResponse);
        when(remoteTenantAcaPyService.createPublicDid(anyString(), any(CreatePublicDidRequest.class))).thenReturn(publicDidResponse);
        when(walletRepository.save(any(Wallet.class))).thenThrow(new RuntimeException("DB 저장 실패"));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> walletService.registerWallet(tenantWalletRequest));

        assertEquals("외부 시스템 연동 중 오류가 발생했습니다", exception.getMessage());
        assertEquals(WALLET_CREATION_FAILED, exception.getErrorCode());

        verify(walletRepository).existsByAccountId(TENANT_ID);
        verify(remoteTenantAcaPyService).createWallet(any(CreateWalletRequest.class));
        verify(remoteTenantAcaPyService).createPublicDid(anyString(), any(CreatePublicDidRequest.class));
        verify(remoteUserAcaPyService, never()).createWallet(any(CreateWalletRequest.class));
        verify(remoteUserAcaPyService, never()).createPublicDid(anyString(), any(CreatePublicDidRequest.class));
        verify(walletRepository).save(any(Wallet.class));
    }

  @Test
    @DisplayName("테넌트 지갑 조회 테스트 - 존재하는 경우")
    void getWalletByAccountId_Exists() {
        // Given
        when(walletRepository.findByAccountId(TENANT_ID)).thenReturn(Optional.of(wallet));

        // When
        Optional<Wallet> result = walletService.getWalletByAccountId(TENANT_ID);

        // Then
        assertTrue(result.isPresent());
        assertEquals(TENANT_ID, result.get().getAccountId());
        verify(walletRepository).findByAccountId(TENANT_ID);
    }

  @Test
    @DisplayName("테넌트 지갑 조회 테스트 - 존재하지 않는 경우")
    void getWalletByAccountId_NotExists() {

        // Given
        when(walletRepository.findByAccountId(TENANT_ID)).thenReturn(Optional.empty());

        // When
        Optional<Wallet> result = walletService.getWalletByAccountId(TENANT_ID);

        // Then
        assertFalse(result.isPresent());
        verify(walletRepository).findByAccountId(TENANT_ID);
    }

  @Test
    @DisplayName("지갑 조회 OrThrow 테스트 - 존재하는 경우")
    void getWalletByAccountIdOrThrow_Exists() {
        // Given
        when(walletRepository.findByAccountId(TENANT_ID)).thenReturn(Optional.of(wallet));

        // When
        Wallet result = walletService.getWalletByAccountIdOrThrow(TENANT_ID);

        // Then
        assertNotNull(result);
        assertEquals(TENANT_ID, result.getAccountId());
        verify(walletRepository).findByAccountId(TENANT_ID);
    }

  @Test
    @DisplayName("지갑 조회 OrThrow 테스트 - 존재하지 않는 경우 예외 발생")
    void getWalletByAccountIdOrThrow_NotExists() {
        // Given
        when(walletRepository.findByAccountId(TENANT_ID)).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> walletService.getWalletByAccountIdOrThrow(TENANT_ID));

        assertEquals("계정 ID: {" + TENANT_ID + "} 에 대한 지갑을 찾을 수 없습니다.", exception.getMessage());
        assertEquals(WALLET_NOT_FOUND, exception.getErrorCode());
        verify(walletRepository).findByAccountId(TENANT_ID);
    }
}
