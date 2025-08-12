package com.pyokemon.did.service;

import com.pyokemon.did.service.impl.WalletMetadataServiceImpl;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.common.exception.code.DidErrorCodes;
import com.pyokemon.did.domain.WalletMetadata;
import com.pyokemon.did.domain.dto.request.WalletMetadataRequest;
import com.pyokemon.did.domain.repository.WalletMetadataRepository;
import com.pyokemon.did.remote.tenant.RemoteTenantAcaPyService;
import com.pyokemon.did.remote.tenant.dto.request.WalletRequest;
import com.pyokemon.did.remote.tenant.dto.response.WalletResponse.AcaPyCreateWalletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WalletMetadataServiceTest {
    @Mock
    private WalletMetadataRepository walletMetadataRepository;

    @Mock
    private RemoteTenantAcaPyService remoteTenantAcaPyService;

    @InjectMocks
    private WalletMetadataServiceImpl walletMetadataService;

    private final Long TENANT_ID = 1L;
    private final WalletMetadataRequest.CreateWalletRequest request = new WalletMetadataRequest.CreateWalletRequest(TENANT_ID);
    @BeforeEach
    void setUp() {
        String WALLET_KEY = "test-wallet-key";
        ReflectionTestUtils.setField(walletMetadataService, "walletKey", WALLET_KEY);
    }

    @Test
    @DisplayName("지갑 생성 성공 테스트")
    void createWallet_Success() {
        // Given
        String TOKEN = "test-token";
        String WALLET_ID = "test-wallet-id";
        AcaPyCreateWalletResponse walletResponse = new AcaPyCreateWalletResponse(WALLET_ID, TOKEN);

        when(walletMetadataRepository.existsByTenantId(TENANT_ID)).thenReturn(false);
        when(remoteTenantAcaPyService.acaPyCreateWallet(any(WalletRequest.AcaPyCreateWalletRequest.class))).thenReturn(walletResponse);
        when(walletMetadataRepository.save(any(WalletMetadata.class))).thenReturn(1);

        // When
        walletMetadataService.createWallet(request);

        // Then
        verify(walletMetadataRepository).existsByTenantId(TENANT_ID);
        verify(remoteTenantAcaPyService).acaPyCreateWallet(any(WalletRequest.AcaPyCreateWalletRequest.class));
        verify(walletMetadataRepository).save(any(WalletMetadata.class));
    }

    @Test
    @DisplayName("지갑이 이미 존재하는 경우 예외 발생 테스트")
    void createWallet_WalletAlreadyExists() {
        // Given
        when(walletMetadataRepository.existsByTenantId(TENANT_ID)).thenReturn(true);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            walletMetadataService.createWallet(request);
        });

        assertEquals(DidErrorCodes.WALLET_ALREADY_EXISTS, exception.getErrorCode());
        verify(walletMetadataRepository).existsByTenantId(TENANT_ID);
        verify(remoteTenantAcaPyService, never()).acaPyCreateWallet(any(WalletRequest.AcaPyCreateWalletRequest.class));
        verify(walletMetadataRepository, never()).save(any(WalletMetadata.class));
    }

    @Test
    @DisplayName("ACA-PY 클라이언트 예외 처리 테스트")
    void createWallet_AcaPyClientException() {
        // Given
        RuntimeException acaPyException = new RuntimeException("ACA-PY 클라이언트 오류");

        when(walletMetadataRepository.existsByTenantId(TENANT_ID)).thenReturn(false);
        when(remoteTenantAcaPyService.acaPyCreateWallet(any(WalletRequest.AcaPyCreateWalletRequest.class))).thenThrow(acaPyException);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            walletMetadataService.createWallet(request);
        });

        // 예외 검증
        assertEquals(DidErrorCodes.WALLET_CREATION_FAILED, exception.getErrorCode());
        assertEquals("지갑 생성에 실패했습니다.", exception.getMessage());
        assertEquals(acaPyException, exception.getCause());

        verify(walletMetadataRepository).existsByTenantId(TENANT_ID);
        verify(remoteTenantAcaPyService).acaPyCreateWallet(any(WalletRequest.AcaPyCreateWalletRequest.class));
        verify(walletMetadataRepository, never()).save(any(WalletMetadata.class));
    }
}
