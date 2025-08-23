package com.pyokemon.did.service;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.did.domain.TenantWallet;
import com.pyokemon.did.domain.dto.request.TenantWalletRequest.CreateWalletRequest;
import com.pyokemon.did.domain.repository.TenantWalletRepository;
import com.pyokemon.did.remote.commonAcaPy.dto.request.WalletRequest.AcaPyCreatePublicDidRequest;
import com.pyokemon.did.remote.commonAcaPy.dto.request.WalletRequest.AcaPyCreateWalletRequest;
import com.pyokemon.did.remote.commonAcaPy.dto.response.WalletResponse;
import com.pyokemon.did.remote.commonAcaPy.dto.response.WalletResponse.AcaPyCreatePublicDidResponse;
import com.pyokemon.did.remote.commonAcaPy.dto.response.WalletResponse.AcaPyCreatePublicDidResponse.Result;
import com.pyokemon.did.remote.commonAcaPy.dto.response.WalletResponse.AcaPyCreateWalletResponse;
import com.pyokemon.did.remote.tenantAcaPy.RemoteTenantAcaPyService;
import com.pyokemon.did.service.impl.TenantWalletServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.pyokemon.common.exception.code.DidErrorCodes.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantWalletServiceTest {

    @Mock
    private RemoteTenantAcaPyService remoteTenantAcaPyService;

    @Mock
    private TenantWalletRepository tenantWalletRepository;

    @InjectMocks
    private TenantWalletServiceImpl tenantWalletService;

    private CreateWalletRequest createWalletRequest;
    private AcaPyCreateWalletResponse walletResponse;
    private AcaPyCreatePublicDidResponse publicDidResponse;
    private TenantWallet tenantWallet;
    private final Long TENANT_ID = 1L;

    @BeforeEach
    void setUp() {
        // 테스트 요청 객체 생성
        createWalletRequest = new CreateWalletRequest();
        createWalletRequest.setTenantId(TENANT_ID);

        // 지갑 응답 객체 생성
        walletResponse = new AcaPyCreateWalletResponse();
        walletResponse.setToken("test-token");
        walletResponse.setWalletId("test-wallet-id");

        // DID 응답 객체 생성
        Result didResult = new Result();
        didResult.setDid("test-did");
        didResult.setVerkey("test-verkey");
        
        publicDidResponse = new AcaPyCreatePublicDidResponse();
        publicDidResponse.setResult(didResult);

        // 테넌트 지갑 객체 생성
        tenantWallet = TenantWallet.builder()
                .tenantId(TENANT_ID)
                .token("test-token")
                .publicDid("test-did")
                .publicVerkey("test-verkey")
                .build();
    }

    @Test
    @DisplayName("테넌트 지갑 등록 성공 테스트")
    void registerTenantWallet_Success() {
        // Given
        when(tenantWalletRepository.existsByTenantId(TENANT_ID)).thenReturn(false);
        when(remoteTenantAcaPyService.acaPyCreateWallet(any(AcaPyCreateWalletRequest.class))).thenReturn(walletResponse);
        when(remoteTenantAcaPyService.acaPyCreatePublicDid(anyString(), any(AcaPyCreatePublicDidRequest.class))).thenReturn(publicDidResponse);
        when(tenantWalletRepository.save(any(TenantWallet.class))).thenReturn(1L);

        // When
        assertDoesNotThrow(() -> tenantWalletService.registerTenantWallet(createWalletRequest));

        // Then
        verify(tenantWalletRepository).existsByTenantId(TENANT_ID);
        verify(remoteTenantAcaPyService).acaPyCreateWallet(any(AcaPyCreateWalletRequest.class));
        verify(remoteTenantAcaPyService).acaPyCreatePublicDid(anyString(), any(AcaPyCreatePublicDidRequest.class));
        verify(tenantWalletRepository).save(any(TenantWallet.class));
    }

    @Test
    @DisplayName("이미 존재하는 테넌트 지갑 등록 시 예외 발생 테스트")
    void registerTenantWallet_WalletAlreadyExists() {
        // Given
        when(tenantWalletRepository.existsByTenantId(TENANT_ID)).thenReturn(true);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> tenantWalletService.registerTenantWallet(createWalletRequest));
        
        assertEquals("테넌트 지갑이 이미 존재합니다.", exception.getMessage());
        assertEquals(WALLET_ALREADY_EXISTS, exception.getErrorCode());
        
        verify(tenantWalletRepository).existsByTenantId(TENANT_ID);
        verify(remoteTenantAcaPyService, never()).acaPyCreateWallet(any(AcaPyCreateWalletRequest.class));
        verify(remoteTenantAcaPyService, never()).acaPyCreatePublicDid(anyString(), any(AcaPyCreatePublicDidRequest.class));
        verify(tenantWalletRepository, never()).save(any(TenantWallet.class));
    }

    @Test
    @DisplayName("지갑 생성 응답이 null인 경우 예외 발생 테스트")
    void registerTenantWallet_WalletResponseNull() {
        // Given
        when(tenantWalletRepository.existsByTenantId(TENANT_ID)).thenReturn(false);
        when(remoteTenantAcaPyService.acaPyCreateWallet(any(AcaPyCreateWalletRequest.class))).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> tenantWalletService.registerTenantWallet(createWalletRequest));
        
        assertEquals("지갑 생성에 실패했습니다.", exception.getMessage());
        assertEquals(WALLET_CREATION_FAILED, exception.getErrorCode());
        
        verify(tenantWalletRepository).existsByTenantId(TENANT_ID);
        verify(remoteTenantAcaPyService).acaPyCreateWallet(any(AcaPyCreateWalletRequest.class));
        verify(remoteTenantAcaPyService, never()).acaPyCreatePublicDid(anyString(), any(AcaPyCreatePublicDidRequest.class));
        verify(tenantWalletRepository, never()).save(any(TenantWallet.class));
    }

    @Test
    @DisplayName("지갑 생성 응답의 토큰이 null인 경우 예외 발생 테스트")
    void registerTenantWallet_WalletTokenNull() {
        // Given
        when(tenantWalletRepository.existsByTenantId(TENANT_ID)).thenReturn(false);
        
        AcaPyCreateWalletResponse nullTokenResponse = new AcaPyCreateWalletResponse();
        nullTokenResponse.setToken(null);
        
        when(remoteTenantAcaPyService.acaPyCreateWallet(any(AcaPyCreateWalletRequest.class))).thenReturn(nullTokenResponse);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> tenantWalletService.registerTenantWallet(createWalletRequest));
        
        assertEquals("지갑 생성에 실패했습니다.", exception.getMessage());
        assertEquals(WALLET_CREATION_FAILED, exception.getErrorCode());
        
        verify(tenantWalletRepository).existsByTenantId(TENANT_ID);
        verify(remoteTenantAcaPyService).acaPyCreateWallet(any(AcaPyCreateWalletRequest.class));
        verify(remoteTenantAcaPyService, never()).acaPyCreatePublicDid(anyString(), any(AcaPyCreatePublicDidRequest.class));
        verify(tenantWalletRepository, never()).save(any(TenantWallet.class));
    }

    @Test
    @DisplayName("DID 생성 응답이 null인 경우 예외 발생 테스트")
    void registerTenantWallet_PublicDidResponseNull() {
        // Given
        when(tenantWalletRepository.existsByTenantId(TENANT_ID)).thenReturn(false);
        when(remoteTenantAcaPyService.acaPyCreateWallet(any(AcaPyCreateWalletRequest.class))).thenReturn(walletResponse);
        when(remoteTenantAcaPyService.acaPyCreatePublicDid(anyString(), any(AcaPyCreatePublicDidRequest.class))).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> tenantWalletService.registerTenantWallet(createWalletRequest));
        
        assertEquals("공개 DID 생성에 실패했습니다.", exception.getMessage());
        assertEquals(DID_CREATION_FAILED, exception.getErrorCode());
        
        verify(tenantWalletRepository).existsByTenantId(TENANT_ID);
        verify(remoteTenantAcaPyService).acaPyCreateWallet(any(AcaPyCreateWalletRequest.class));
        verify(remoteTenantAcaPyService).acaPyCreatePublicDid(anyString(), any(AcaPyCreatePublicDidRequest.class));
        verify(tenantWalletRepository, never()).save(any(TenantWallet.class));
    }

    @Test
    @DisplayName("DID 생성 응답의 결과가 null인 경우 예외 발생 테스트")
    void registerTenantWallet_PublicDidResultNull() {
        // Given
        when(tenantWalletRepository.existsByTenantId(TENANT_ID)).thenReturn(false);
        when(remoteTenantAcaPyService.acaPyCreateWallet(any(AcaPyCreateWalletRequest.class))).thenReturn(walletResponse);

        AcaPyCreatePublicDidResponse nullResultResponse = new AcaPyCreatePublicDidResponse();
        nullResultResponse.setResult(null);
        
        when(remoteTenantAcaPyService.acaPyCreatePublicDid(anyString(), any(AcaPyCreatePublicDidRequest.class)))
            .thenReturn(nullResultResponse);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> tenantWalletService.registerTenantWallet(createWalletRequest));
        
        assertEquals("공개 DID 생성에 실패했습니다.", exception.getMessage());
        assertEquals(DID_CREATION_FAILED, exception.getErrorCode());
        
        verify(tenantWalletRepository).existsByTenantId(TENANT_ID);
        verify(remoteTenantAcaPyService).acaPyCreateWallet(any(AcaPyCreateWalletRequest.class));
        verify(remoteTenantAcaPyService).acaPyCreatePublicDid(anyString(), any(AcaPyCreatePublicDidRequest.class));
        verify(tenantWalletRepository, never()).save(any(TenantWallet.class));
    }

    @Test
    @DisplayName("DID 생성 응답의 DID가 null인 경우 예외 발생 테스트")
    void registerTenantWallet_PublicDidNull() {
        // Given
        when(tenantWalletRepository.existsByTenantId(TENANT_ID)).thenReturn(false);
        when(remoteTenantAcaPyService.acaPyCreateWallet(any(AcaPyCreateWalletRequest.class))).thenReturn(walletResponse);
        
        Result nullDidResult = new Result();
        nullDidResult.setDid(null);
        nullDidResult.setVerkey("test-verkey");
        
        AcaPyCreatePublicDidResponse nullDidResponse = new AcaPyCreatePublicDidResponse();
        nullDidResponse.setResult(nullDidResult);
        
        when(remoteTenantAcaPyService.acaPyCreatePublicDid(anyString(), any(AcaPyCreatePublicDidRequest.class)))
            .thenReturn(nullDidResponse);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> tenantWalletService.registerTenantWallet(createWalletRequest));
        
        assertEquals("공개 DID 생성에 실패했습니다.", exception.getMessage());
        assertEquals(DID_CREATION_FAILED, exception.getErrorCode());
        
        verify(tenantWalletRepository).existsByTenantId(TENANT_ID);
        verify(remoteTenantAcaPyService).acaPyCreateWallet(any(AcaPyCreateWalletRequest.class));
        verify(remoteTenantAcaPyService).acaPyCreatePublicDid(anyString(), any(AcaPyCreatePublicDidRequest.class));
        verify(tenantWalletRepository, never()).save(any(TenantWallet.class));
    }

    @Test
    @DisplayName("지갑 생성 API 호출 중 예외 발생 테스트")
    void registerTenantWallet_WalletApiException() {
        // Given
        when(tenantWalletRepository.existsByTenantId(TENANT_ID)).thenReturn(false);
        when(remoteTenantAcaPyService.acaPyCreateWallet(any(AcaPyCreateWalletRequest.class)))
            .thenThrow(new RuntimeException("API 호출 실패"));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> tenantWalletService.registerTenantWallet(createWalletRequest));
        
        assertEquals("외부 시스템 연동 중 오류가 발생했습니다", exception.getMessage());
        assertEquals(WALLET_CREATION_FAILED, exception.getErrorCode());
        
        verify(tenantWalletRepository).existsByTenantId(TENANT_ID);
        verify(remoteTenantAcaPyService).acaPyCreateWallet(any(AcaPyCreateWalletRequest.class));
        verify(remoteTenantAcaPyService, never()).acaPyCreatePublicDid(anyString(), any(AcaPyCreatePublicDidRequest.class));
        verify(tenantWalletRepository, never()).save(any(TenantWallet.class));
    }

    @Test
    @DisplayName("DID 생성 API 호출 중 예외 발생 테스트")
    void registerTenantWallet_DidApiException() {
        // Given
        when(tenantWalletRepository.existsByTenantId(TENANT_ID)).thenReturn(false);
        when(remoteTenantAcaPyService.acaPyCreateWallet(any(AcaPyCreateWalletRequest.class))).thenReturn(walletResponse);
        when(remoteTenantAcaPyService.acaPyCreatePublicDid(anyString(), any(AcaPyCreatePublicDidRequest.class)))
            .thenThrow(new RuntimeException("API 호출 실패"));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> tenantWalletService.registerTenantWallet(createWalletRequest));
        
        assertEquals("외부 시스템 연동 중 오류가 발생했습니다", exception.getMessage());
        assertEquals(WALLET_CREATION_FAILED, exception.getErrorCode());
        
        verify(tenantWalletRepository).existsByTenantId(TENANT_ID);
        verify(remoteTenantAcaPyService).acaPyCreateWallet(any(AcaPyCreateWalletRequest.class));
        verify(remoteTenantAcaPyService).acaPyCreatePublicDid(anyString(), any(AcaPyCreatePublicDidRequest.class));
        verify(tenantWalletRepository, never()).save(any(TenantWallet.class));
    }

    @Test
    @DisplayName("DB 저장 중 예외 발생 테스트")
    void registerTenantWallet_DbSaveException() {
        // Given
        when(tenantWalletRepository.existsByTenantId(TENANT_ID)).thenReturn(false);
        when(remoteTenantAcaPyService.acaPyCreateWallet(any(AcaPyCreateWalletRequest.class))).thenReturn(walletResponse);
        when(remoteTenantAcaPyService.acaPyCreatePublicDid(anyString(), any(AcaPyCreatePublicDidRequest.class))).thenReturn(publicDidResponse);
        when(tenantWalletRepository.save(any(TenantWallet.class))).thenThrow(new RuntimeException("DB 저장 실패"));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> tenantWalletService.registerTenantWallet(createWalletRequest));
        
        assertEquals("외부 시스템 연동 중 오류가 발생했습니다", exception.getMessage());
        assertEquals(WALLET_CREATION_FAILED, exception.getErrorCode());
        
        verify(tenantWalletRepository).existsByTenantId(TENANT_ID);
        verify(remoteTenantAcaPyService).acaPyCreateWallet(any(AcaPyCreateWalletRequest.class));
        verify(remoteTenantAcaPyService).acaPyCreatePublicDid(anyString(), any(AcaPyCreatePublicDidRequest.class));
        verify(tenantWalletRepository).save(any(TenantWallet.class));
    }

    @Test
    @DisplayName("테넌트 지갑 조회 테스트 - 존재하는 경우")
    void getTenantWalletByTenantId_Exists() {
        // Given
        when(tenantWalletRepository.findByTenantId(TENANT_ID)).thenReturn(Optional.of(tenantWallet));

        // When
        Optional<TenantWallet> result = tenantWalletService.getWalletByTenantId(TENANT_ID);

        // Then
        assertTrue(result.isPresent());
        assertEquals(TENANT_ID, result.get().getTenantId());
        verify(tenantWalletRepository).findByTenantId(TENANT_ID);
    }

    @Test
    @DisplayName("테넌트 지갑 조회 테스트 - 존재하지 않는 경우")
    void getTenantWalletByTenantId_NotExists() {
        // Given
        when(tenantWalletRepository.findByTenantId(TENANT_ID)).thenReturn(Optional.empty());

        // When
        Optional<TenantWallet> result = tenantWalletService.getWalletByTenantId(TENANT_ID);

        // Then
        assertFalse(result.isPresent());
        verify(tenantWalletRepository).findByTenantId(TENANT_ID);
    }
}

