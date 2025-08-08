package com.pyokemon.did.service;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.common.exception.code.DidErrorCodes;
import com.pyokemon.did.domain.WalletMetadata;
import com.pyokemon.did.domain.repository.WalletMetadataRepository;
import com.pyokemon.did.dto.request.ProvisionWalletRequest;
import com.pyokemon.did.remote.tenant.dto.request.CreateWalletRequest;
import com.pyokemon.did.remote.tenant.TenantAcapyClient;
import com.pyokemon.did.service.impl.WalletServiceImpl;
import com.pyokemon.did.remote.tenant.dto.response.CreateWalletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletMetadataRepository walletMetadataRepository;

    @Mock
    private TenantAcapyClient tenantAcapyClient;

    @InjectMocks
    private WalletServiceImpl walletService;

    private final Long TENANT_ID = 1L;
    private final ProvisionWalletRequest request = new ProvisionWalletRequest(TENANT_ID);
    @BeforeEach
    void setUp() {
        String WALLET_KEY = "test-wallet-key";
        ReflectionTestUtils.setField(walletService, "walletKey", WALLET_KEY);
    }

    @Test
    @DisplayName("지갑 프로비저닝 성공 테스트")
    void provisionWallet_Success() {
        // Given
        String TOKEN = "test-token";
        String WALLET_ID = "test-wallet-id";
        CreateWalletResponse walletResponse = new CreateWalletResponse(WALLET_ID, TOKEN);
        
        when(walletMetadataRepository.existsByTenantId(TENANT_ID)).thenReturn(false);
        when(tenantAcapyClient.createWallet(any(CreateWalletRequest.class))).thenReturn(walletResponse);
        when(walletMetadataRepository.save(any(WalletMetadata.class))).thenReturn(1);

        // When
        walletService.provisionWallet(request);

        // Then
        verify(walletMetadataRepository).existsByTenantId(TENANT_ID);
        verify(tenantAcapyClient).createWallet(any(CreateWalletRequest.class));
        verify(walletMetadataRepository).save(any(WalletMetadata.class));
    }

    @Test
    @DisplayName("지갑이 이미 존재하는 경우 예외 발생 테스트")
    void provisionWallet_WalletAlreadyExists() {
        // Given
        when(walletMetadataRepository.existsByTenantId(TENANT_ID)).thenReturn(true);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            walletService.provisionWallet(request);
        });

        assertEquals(DidErrorCodes.WALLET_ALREADY_EXISTS, exception.getErrorCode());
        verify(walletMetadataRepository).existsByTenantId(TENANT_ID);
        verify(tenantAcapyClient, never()).createWallet(any(CreateWalletRequest.class));
        verify(walletMetadataRepository, never()).save(any(WalletMetadata.class));
    }

    @Test
    @DisplayName("ACA-PY 클라이언트 예외 처리 테스트")
    void provisionWallet_AcapyClientException() {
        // Given
        RuntimeException acapyException = new RuntimeException("ACA-PY 클라이언트 오류");
        
        when(walletMetadataRepository.existsByTenantId(TENANT_ID)).thenReturn(false);
        when(tenantAcapyClient.createWallet(any(CreateWalletRequest.class))).thenThrow(acapyException);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            walletService.provisionWallet(request);
        });

        // 예외 검증
        assertEquals(DidErrorCodes.WALLET_CREATION_FAILED, exception.getErrorCode());
        assertEquals("지갑 생성에 실패했습니다.", exception.getMessage());
        assertEquals(acapyException, exception.getCause());

        verify(walletMetadataRepository).existsByTenantId(TENANT_ID);
        verify(tenantAcapyClient).createWallet(any(CreateWalletRequest.class));
        verify(walletMetadataRepository, never()).save(any(WalletMetadata.class));
    }
}
