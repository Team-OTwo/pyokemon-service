package com.pyokemon.did.service.impl;

import static com.pyokemon.common.exception.code.DidErrorCodes.*;
import static com.pyokemon.did.domain.Verification.VpStatus.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.did.domain.IssuedVc;
import com.pyokemon.did.domain.Verification.VpStatus;
import com.pyokemon.did.domain.dto.request.webhook.PresentProofWebhookRequest;

import com.pyokemon.did.remote.acapy.common.dto.response.VerifyPresentationResponse;
import com.pyokemon.did.remote.acapy.service.RemoteTenantAcaPyService;
import com.pyokemon.did.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;

@ExtendWith(MockitoExtension.class)
class TenantWebhookServiceImplTest {

    @Mock
    private IssuedProofService issuedProofService;

    @Mock
    private IssuedVcService issuedVcService;

    @Mock
    private WalletService walletService;

    @Mock
    private RemoteTenantAcaPyService remoteTenantAcaPyService;

    @Mock
    private VerificationService verificationService;

    @InjectMocks
    private TenantWebhookServiceImpl tenantWebhookService;

    private PresentProofWebhookRequest webhookRequest;

    @BeforeEach
    void setUp() {
        // WebhookRequest 설정
        webhookRequest = new PresentProofWebhookRequest();
        webhookRequest.setState("presentation-received");
        webhookRequest.setPresExId("pres-ex-id-123");
        webhookRequest.setChallenge("challenge-456");
    }

    @Test
    @DisplayName("성공적인 검증 플로우 - 모든 단계가 정상적으로 완료")
    void shouldCompleteVerificationFlowSuccessfully() {
        // Given
        IssuedVc mockIssuedVc = mock(IssuedVc.class);
        VerifyPresentationResponse mockVerifyResponse = mock(VerifyPresentationResponse.class);
        
        when(issuedProofService.getChallenge("pres-ex-id-123")).thenReturn("challenge-456");
        when(mockIssuedVc.getTenantId()).thenReturn(1L);
        when(mockIssuedVc.isIssued()).thenReturn(true);
        when(issuedVcService.getIssuedVcByPresExIdOrThrow("pres-ex-id-123")).thenReturn(mockIssuedVc);
        when(walletService.getWalletToken(1L)).thenReturn("bearer-token");
        when(mockVerifyResponse.verify()).thenReturn(true);
        when(remoteTenantAcaPyService.verifyPresentation("bearer-token", "pres-ex-id-123"))
                .thenReturn(mockVerifyResponse);

        // When
        tenantWebhookService.handleTenantPresentProofWebhook(webhookRequest);

        // Then
        verify(issuedProofService).getChallenge("pres-ex-id-123");
        verify(issuedVcService).getIssuedVcByPresExIdOrThrow("pres-ex-id-123");
        verify(issuedProofService).revokeIssuedProof("pres-ex-id-123");
        verify(walletService).getWalletToken(1L);
        verify(remoteTenantAcaPyService).verifyPresentation("bearer-token", "pres-ex-id-123");
        verify(mockIssuedVc).verified();
        verify(verificationService).saveVerification("pres-ex-id-123", SUCCESS);
    }

    @Test
    @DisplayName("presentation-received가 아닌 상태 - 조기 종료")
    void shouldReturnEarlyWhenStateIsNotPresentationReceived() {
        // Given
        webhookRequest.setState("request-sent");

        // When
        tenantWebhookService.handleTenantPresentProofWebhook(webhookRequest);

        // Then
        verifyNoInteractions(issuedProofService, issuedVcService, walletService, 
                remoteTenantAcaPyService, verificationService);
    }

    @Test
    @DisplayName("Challenge 불일치 - FAIL 상태로 저장")
    void shouldSaveFailStatusWhenChallengeMismatch() {
        // Given
        when(issuedProofService.getChallenge("pres-ex-id-123")).thenReturn("different-challenge");

        // When
        tenantWebhookService.handleTenantPresentProofWebhook(webhookRequest);

        // Then
        verify(issuedProofService).getChallenge("pres-ex-id-123");
        verify(verificationService).saveVerification("pres-ex-id-123", FAIL);
        verifyNoInteractions(issuedVcService, walletService, remoteTenantAcaPyService);
    }

    @Test
    @DisplayName("IssuedVc가 ISSUED 상태가 아님 - INVALID_VC 상태로 저장")
    void shouldSaveInvalidVcStatusWhenIssuedVcIsNotIssued() {
        // Given
        IssuedVc mockIssuedVc = mock(IssuedVc.class);
        
        when(issuedProofService.getChallenge("pres-ex-id-123")).thenReturn("challenge-456");
        when(mockIssuedVc.isIssued()).thenReturn(false);
        when(issuedVcService.getIssuedVcByPresExIdOrThrow("pres-ex-id-123")).thenReturn(mockIssuedVc);

        // When
        tenantWebhookService.handleTenantPresentProofWebhook(webhookRequest);

        // Then
        verify(issuedProofService).getChallenge("pres-ex-id-123");
        verify(issuedVcService).getIssuedVcByPresExIdOrThrow("pres-ex-id-123");
        verify(verificationService).saveVerification("pres-ex-id-123", INVALID_VC);
        verifyNoInteractions(walletService, remoteTenantAcaPyService);
    }

    @Test
    @DisplayName("원격 검증 API 호출 실패 - FAIL 상태로 저장")
    void shouldSaveFailStatusWhenRemoteApiCallFails() {
        // Given
        IssuedVc mockIssuedVc = mock(IssuedVc.class);
        
        when(issuedProofService.getChallenge("pres-ex-id-123")).thenReturn("challenge-456");
        when(mockIssuedVc.getTenantId()).thenReturn(1L);
        when(mockIssuedVc.isIssued()).thenReturn(true);
        when(issuedVcService.getIssuedVcByPresExIdOrThrow("pres-ex-id-123")).thenReturn(mockIssuedVc);
        when(walletService.getWalletToken(1L)).thenReturn("bearer-token");
        when(remoteTenantAcaPyService.verifyPresentation("bearer-token", "pres-ex-id-123"))
                .thenThrow(new RestClientException("API 호출 실패"));

        // When
        tenantWebhookService.handleTenantPresentProofWebhook(webhookRequest);

        // Then
        verify(issuedProofService).getChallenge("pres-ex-id-123");
        verify(issuedVcService).getIssuedVcByPresExIdOrThrow("pres-ex-id-123");
        verify(issuedProofService).revokeIssuedProof("pres-ex-id-123");
        verify(walletService).getWalletToken(1L);
        verify(remoteTenantAcaPyService).verifyPresentation("bearer-token", "pres-ex-id-123");
        verify(verificationService).saveVerification("pres-ex-id-123", FAIL);
    }

    @Test
    @DisplayName("ACA-Py 검증 실패 - FAIL 상태로 저장")
    void shouldSaveFailStatusWhenAcaPyVerificationFails() {
        // Given
        IssuedVc mockIssuedVc = mock(IssuedVc.class);
        VerifyPresentationResponse mockVerifyResponse = mock(VerifyPresentationResponse.class);
        
        when(issuedProofService.getChallenge("pres-ex-id-123")).thenReturn("challenge-456");
        when(mockIssuedVc.getTenantId()).thenReturn(1L);
        when(mockIssuedVc.isIssued()).thenReturn(true);
        when(issuedVcService.getIssuedVcByPresExIdOrThrow("pres-ex-id-123")).thenReturn(mockIssuedVc);
        when(walletService.getWalletToken(1L)).thenReturn("bearer-token");
        when(mockVerifyResponse.verify()).thenReturn(false);
        when(remoteTenantAcaPyService.verifyPresentation("bearer-token", "pres-ex-id-123"))
                .thenReturn(mockVerifyResponse);

        // When
        tenantWebhookService.handleTenantPresentProofWebhook(webhookRequest);

        // Then
        verify(issuedProofService).getChallenge("pres-ex-id-123");
        verify(issuedVcService).getIssuedVcByPresExIdOrThrow("pres-ex-id-123");
        verify(issuedProofService).revokeIssuedProof("pres-ex-id-123");
        verify(walletService).getWalletToken(1L);
        verify(remoteTenantAcaPyService).verifyPresentation("bearer-token", "pres-ex-id-123");
        verify(verificationService).saveVerification("pres-ex-id-123", FAIL);
    }

    @Test
    @DisplayName("IssuedVc 조회 실패 - FAIL 상태로 저장")
    void shouldSaveFailStatusWhenIssuedVcNotFound() {
        // Given
        when(issuedProofService.getChallenge("pres-ex-id-123")).thenReturn("challenge-456");
        when(issuedVcService.getIssuedVcByPresExIdOrThrow("pres-ex-id-123"))
                .thenThrow(new BusinessException("IssuedVc를 찾을 수 없습니다", VC_CONSUMED_OR_REVOKED));

        // When
        tenantWebhookService.handleTenantPresentProofWebhook(webhookRequest);

        // Then
        verify(issuedProofService).getChallenge("pres-ex-id-123");
        verify(issuedVcService).getIssuedVcByPresExIdOrThrow("pres-ex-id-123");
        verify(verificationService).saveVerification("pres-ex-id-123", INVALID_VC);
        verifyNoInteractions(walletService, remoteTenantAcaPyService);
    }

    @Test
    @DisplayName("VC_CONSUMED_OR_REVOKED 예외 발생 시 - INVALID_VC 상태로 저장")
    void shouldSaveInvalidVcStatusWhenVcConsumedOrRevokedException() {
        // Given
        when(issuedProofService.getChallenge("pres-ex-id-123")).thenReturn("challenge-456");
        when(issuedVcService.getIssuedVcByPresExIdOrThrow("pres-ex-id-123"))
                .thenThrow(new BusinessException("VC가 소비되었거나 취소되었습니다", VC_CONSUMED_OR_REVOKED));

        // When
        tenantWebhookService.handleTenantPresentProofWebhook(webhookRequest);

        // Then
        verify(issuedProofService).getChallenge("pres-ex-id-123");
        verify(issuedVcService).getIssuedVcByPresExIdOrThrow("pres-ex-id-123");
        verify(verificationService).saveVerification("pres-ex-id-123", INVALID_VC);
        verifyNoInteractions(walletService, remoteTenantAcaPyService);
    }

    @Test
    @DisplayName("성공적인 검증 후 IssuedVc 상태가 CONSUMED로 변경")
    void shouldMarkIssuedVcAsConsumedWhenVerificationSucceeds() {
        // Given
        IssuedVc mockIssuedVc = mock(IssuedVc.class);
        VerifyPresentationResponse mockVerifyResponse = mock(VerifyPresentationResponse.class);
        
        when(issuedProofService.getChallenge("pres-ex-id-123")).thenReturn("challenge-456");
        when(mockIssuedVc.getTenantId()).thenReturn(1L);
        when(mockIssuedVc.isIssued()).thenReturn(true);
        when(issuedVcService.getIssuedVcByPresExIdOrThrow("pres-ex-id-123")).thenReturn(mockIssuedVc);
        when(walletService.getWalletToken(1L)).thenReturn("bearer-token");
        when(mockVerifyResponse.verify()).thenReturn(true);
        when(remoteTenantAcaPyService.verifyPresentation("bearer-token", "pres-ex-id-123"))
                .thenReturn(mockVerifyResponse);

        // When
        tenantWebhookService.handleTenantPresentProofWebhook(webhookRequest);

        // Then
        verify(mockIssuedVc).verified();
        verify(verificationService).saveVerification("pres-ex-id-123", SUCCESS);
    }
}
