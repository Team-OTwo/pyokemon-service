package com.pyokemon.did.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.did.domain.dto.request.VerificationRequest.CreateVerificationRequest;
import com.pyokemon.did.domain.dto.response.VerificationResponse.CreateVerificationResponse;
import com.pyokemon.did.remote.acapy.common.dto.response.JwtVerifyResponse;
import com.pyokemon.did.remote.acapy.service.RemoteTenantAcaPyService;
import com.pyokemon.did.service.impl.VerificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VerificationServiceCreateVerificationUrlTest {

    @Mock
    private DeviceConnectionService deviceConnectionService;
    
    @Mock
    private IssuedVcService issuedVcService;
    
    @Mock
    private RemoteTenantAcaPyService remoteTenantAcaPyService;
    
    @Mock
    private WalletService walletService;

    @InjectMocks
    private VerificationServiceImpl verificationService;

    // 테스트 데이터
    private CreateVerificationRequest request;
    private Long tenantId;
    private String walletToken;
    private Long userId;
    private JwtVerifyResponse jwtVerifyResponse;
    private Map<String, String> vcData;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 설정
        request = new CreateVerificationRequest();
        request.setJwt("test.jwt.token");
        request.setBookingId(123L);

        tenantId = 456L;
        walletToken = "wallet_token_123";
        userId = 789L;

        // JWT 검증 응답 설정
        JwtVerifyResponse.JwtPayload payload = JwtVerifyResponse.JwtPayload.builder()
                .did("did:credo:test123")
                .build();

        jwtVerifyResponse = JwtVerifyResponse.builder()
                .valid(true)
                .payload(payload)
                .build();

        // VC 데이터 설정
        vcData = new HashMap<>();
        vcData.put("verifyInviUrl", "https://example.com/verify/123");
        vcData.put("presExId", "pres_ex_456");
    }

    @Test
    @DisplayName("createVerificationUrl - 정상적인 검증 URL 생성")
    void createVerificationUrl_Success() {
        // Given
        when(walletService.getWalletToken(tenantId)).thenReturn(walletToken);
        when(remoteTenantAcaPyService.jwtVerify(eq(walletToken), any())).thenReturn(jwtVerifyResponse);
        when(deviceConnectionService.getUserIdByDidOrThrow("did:credo:test123")).thenReturn(userId);
        when(issuedVcService.sendVerifiyInviUrlOrThrow(userId, tenantId, 123L)).thenReturn(vcData);

        // When
        CreateVerificationResponse response = verificationService.createVerificationUrl(request, tenantId);

        // Then
        assertNotNull(response);
        assertEquals("https://example.com/verify/123", response.getVerifyInviUrl());
        assertEquals("pres_ex_456", response.getPresExId());
    }

    @Test
    @DisplayName("createVerificationUrl - JWT 검증 응답이 null인 경우 예외 발생")
    void createVerificationUrl_JwtResponseNull_ThrowsException() {
        // Given
        when(walletService.getWalletToken(tenantId)).thenReturn(walletToken);
        when(remoteTenantAcaPyService.jwtVerify(eq(walletToken), any())).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> verificationService.createVerificationUrl(request, tenantId));

        assertTrue(exception.getMessage().contains("JWT 검증 중 오류가 발생했습니다"));
    }

    @Test
    @DisplayName("createVerificationUrl - JWT 검증 응답의 payload가 null인 경우 예외 발생")
    void createVerificationUrl_JwtPayloadNull_ThrowsException() {
        // Given
        JwtVerifyResponse invalidResponse = JwtVerifyResponse.builder()
                .valid(true)
                .payload(null)
                .build();

        when(walletService.getWalletToken(tenantId)).thenReturn(walletToken);
        when(remoteTenantAcaPyService.jwtVerify(eq(walletToken), any())).thenReturn(invalidResponse);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> verificationService.createVerificationUrl(request, tenantId));

        assertTrue(exception.getMessage().contains("JWT 검증 중 오류가 발생했습니다"));
    }

    @Test
    @DisplayName("createVerificationUrl - JWT 검증 응답의 DID가 null인 경우 예외 발생")
    void createVerificationUrl_JwtDidNull_ThrowsException() {
        // Given
        JwtVerifyResponse.JwtPayload invalidPayload = JwtVerifyResponse.JwtPayload.builder()
                .did(null)
                .build();
        JwtVerifyResponse invalidResponse = JwtVerifyResponse.builder()
                .valid(true)
                .payload(invalidPayload)
                .build();

        when(walletService.getWalletToken(tenantId)).thenReturn(walletToken);
        when(remoteTenantAcaPyService.jwtVerify(eq(walletToken), any())).thenReturn(invalidResponse);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> verificationService.createVerificationUrl(request, tenantId));

        assertTrue(exception.getMessage().contains("JWT 검증 중 오류가 발생했습니다"));
    }

    @Test
    @DisplayName("createVerificationUrl - BusinessException 발생 시 그대로 전파")
    void createVerificationUrl_BusinessException_Propagates() {
        // Given
        when(walletService.getWalletToken(tenantId)).thenReturn(walletToken);
        when(remoteTenantAcaPyService.jwtVerify(eq(walletToken), any()))
                .thenThrow(new BusinessException("테스트 예외", null));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> verificationService.createVerificationUrl(request, tenantId));

        assertEquals("테스트 예외", exception.getMessage());
    }

    @Test
    @DisplayName("createVerificationUrl - 예상치 못한 예외 발생 시 BusinessException으로 변환")
    void createVerificationUrl_UnexpectedException_ThrowsBusinessException() {
        // Given
        when(walletService.getWalletToken(tenantId)).thenReturn(walletToken);
        when(remoteTenantAcaPyService.jwtVerify(eq(walletToken), any()))
                .thenThrow(new RuntimeException("예상치 못한 오류"));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> verificationService.createVerificationUrl(request, tenantId));

        assertTrue(exception.getMessage().contains("JWT 검증 중 오류가 발생했습니다"));
    }

    @Test
    @DisplayName("createVerificationUrl - VC 데이터에서 verifyInviUrl이 null인 경우 예외 발생")
    void createVerificationUrl_VerifyInviUrlNull_ThrowsException() {
        // Given
        Map<String, String> invalidVcData = new HashMap<>();
        invalidVcData.put("verifyInviUrl", null);
        invalidVcData.put("presExId", "pres_ex_456");

        when(walletService.getWalletToken(tenantId)).thenReturn(walletToken);
        when(remoteTenantAcaPyService.jwtVerify(eq(walletToken), any())).thenReturn(jwtVerifyResponse);
        when(deviceConnectionService.getUserIdByDidOrThrow("did:credo:test123")).thenReturn(userId);
        when(issuedVcService.sendVerifiyInviUrlOrThrow(userId, tenantId, 123L))
                .thenReturn(invalidVcData);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> verificationService.createVerificationUrl(request, tenantId));

        assertTrue(exception.getMessage().contains("VC 정보에서 필요한 데이터를 찾을 수 없습니다"));
    }

    @Test
    @DisplayName("createVerificationUrl - VC 데이터에서 presExId가 null인 경우 예외 발생")
    void createVerificationUrl_PresExIdNull_ThrowsException() {
        // Given
        Map<String, String> invalidVcData = new HashMap<>();
        invalidVcData.put("verifyInviUrl", "https://example.com/verify/123");
        invalidVcData.put("presExId", null);

        when(walletService.getWalletToken(tenantId)).thenReturn(walletToken);
        when(remoteTenantAcaPyService.jwtVerify(eq(walletToken), any())).thenReturn(jwtVerifyResponse);
        when(deviceConnectionService.getUserIdByDidOrThrow("did:credo:test123")).thenReturn(userId);
        when(issuedVcService.sendVerifiyInviUrlOrThrow(userId, tenantId, 123L))
                .thenReturn(invalidVcData);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> verificationService.createVerificationUrl(request, tenantId));

        assertTrue(exception.getMessage().contains("VC 정보에서 필요한 데이터를 찾을 수 없습니다"));
    }
}
