package com.pyokemon.did.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.did.domain.dto.request.VerificationRequest.CreateVerificationRequest;
import com.pyokemon.did.domain.dto.response.VerificationResponse.CreateVerificationResponse;
import com.pyokemon.did.remote.acapy.common.dto.response.JwtVerifyResponse;
import com.pyokemon.did.remote.acapy.service.RemoteTenantAcaPyService;
import com.pyokemon.did.service.impl.VerificationServiceImpl;


@ExtendWith(MockitoExtension.class)
class VerificationServiceImplTest {

  @Mock
  private RemoteTenantAcaPyService remoteTenantAcaPyService;

  @Mock
  private com.pyokemon.did.service.WalletService walletService;

  @Mock
  private com.pyokemon.did.service.DeviceConnectionService deviceConnectionService;

  @Mock
  private com.pyokemon.did.service.IssuedVcService issuedVcService;

  @InjectMocks
  private VerificationServiceImpl verificationService;

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
    JwtVerifyResponse.JwtPayload payload =
        JwtVerifyResponse.JwtPayload.builder().did("did:credo:test123").build();

    jwtVerifyResponse = JwtVerifyResponse.builder().valid(true).payload(payload).build();

    // VC 데이터 설정
    vcData = new HashMap<>();
    vcData.put("verifyInviUrl", "https://example.com/verify/123");
    vcData.put("presExId", "pres_ex_456");
  }

  @Test
    @DisplayName("정상적인 검증 URL 생성")
    void createVerificationUrl_Success() {
        // given
        when(walletService.getWalletToken(tenantId)).thenReturn(walletToken);
        when(remoteTenantAcaPyService.jwtVerify(eq(walletToken), any())).thenReturn(jwtVerifyResponse);
        when(deviceConnectionService.getUserIdByPublicDidOrThrow("did:credo:test123")).thenReturn(userId);
        when(issuedVcService.sendVerifiyInviUrlOrThrow(userId, tenantId, 123L)).thenReturn(vcData);

        // when
        CreateVerificationResponse response = verificationService.createVerificationUrl(request, tenantId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getVerifyInviUrl()).isEqualTo("https://example.com/verify/123");
        assertThat(response.getPresExId()).isEqualTo("pres_ex_456");
    }

  @Test
    @DisplayName("JWT 검증 응답이 null인 경우 예외 발생")
    void createVerificationUrl_JwtResponseNull_ThrowsException() {
        // given
        when(walletService.getWalletToken(tenantId)).thenReturn(walletToken);
        when(remoteTenantAcaPyService.jwtVerify(eq(walletToken), any())).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> verificationService.createVerificationUrl(request, tenantId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("JWT 검증 중 오류가 발생했습니다");
    }

  @Test
  @DisplayName("JWT 검증 응답의 payload가 null인 경우 예외 발생")
  void createVerificationUrl_JwtPayloadNull_ThrowsException() {
    // given
    JwtVerifyResponse invalidResponse =
        JwtVerifyResponse.builder().valid(true).payload(null).build();

    when(walletService.getWalletToken(tenantId)).thenReturn(walletToken);
    when(remoteTenantAcaPyService.jwtVerify(eq(walletToken), any())).thenReturn(invalidResponse);

    // when & then
    assertThatThrownBy(() -> verificationService.createVerificationUrl(request, tenantId))
        .isInstanceOf(BusinessException.class).hasMessageContaining("JWT 검증 중 오류가 발생했습니다");
  }

  @Test
  @DisplayName("JWT 검증 응답의 DID가 null인 경우 예외 발생")
  void createVerificationUrl_JwtDidNull_ThrowsException() {
    // given
    JwtVerifyResponse.JwtPayload invalidPayload =
        JwtVerifyResponse.JwtPayload.builder().did(null).build();

    JwtVerifyResponse invalidResponse =
        JwtVerifyResponse.builder().valid(true).payload(invalidPayload).build();

    when(walletService.getWalletToken(tenantId)).thenReturn(walletToken);
    when(remoteTenantAcaPyService.jwtVerify(eq(walletToken), any())).thenReturn(invalidResponse);

    // when & then
    assertThatThrownBy(() -> verificationService.createVerificationUrl(request, tenantId))
        .isInstanceOf(BusinessException.class).hasMessageContaining("JWT 검증 중 오류가 발생했습니다");
  }

  // @Test
  // @DisplayName("FeignException 발생 시 BusinessException으로 변환")
  // void createVerificationUrl_FeignException_ThrowsBusinessException() {
  // // given
  // when(walletService.getWalletToken(tenantId)).thenReturn(walletToken);
  //
  // // FeignException 생성 시 필요한 Request와 Response 객체 생성
  // Request feignRequest = Request.create(
  // Request.HttpMethod.GET,
  // "/test",
  // new HashMap<>(),
  // (byte[]) null,
  // null
  // );
  // Response feignResponse = Response.builder()
  // .status(400)
  // .request(feignRequest)
  // .build();
  //
  // when(remoteTenantAcaPyService.jwtVerify(eq(walletToken), any()))
  // .thenThrow(new FeignException.FeignClientException(400, "Bad Request", feignRequest,
  // feignResponse, null));
  //
  // // when & then
  // assertThatThrownBy(() -> verificationService.createVerificationUrl(request, tenantId))
  // .isInstanceOf(BusinessException.class)
  // .hasMessageContaining("JWT 서명 검증에 실패했습니다");
  // }

  @Test
    @DisplayName("BusinessException 발생 시 그대로 전파")
    void createVerificationUrl_BusinessException_Propagates() {
        // given
        when(walletService.getWalletToken(tenantId)).thenReturn(walletToken);
        when(remoteTenantAcaPyService.jwtVerify(eq(walletToken), any()))
                .thenThrow(new BusinessException("테스트 예외", null));

        // when & then
        assertThatThrownBy(() -> verificationService.createVerificationUrl(request, tenantId))
                .isInstanceOf(BusinessException.class)
                .hasMessage("테스트 예외");
    }

  @Test
    @DisplayName("예상치 못한 예외 발생 시 BusinessException으로 변환")
    void createVerificationUrl_UnexpectedException_ThrowsBusinessException() {
        // given
        when(walletService.getWalletToken(tenantId)).thenReturn(walletToken);
        when(remoteTenantAcaPyService.jwtVerify(eq(walletToken), any()))
                .thenThrow(new RuntimeException("예상치 못한 오류"));

        // when & then
        assertThatThrownBy(() -> verificationService.createVerificationUrl(request, tenantId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("JWT 검증 중 오류가 발생했습니다");
    }

  @Test
  @DisplayName("VC 데이터에서 verifyInviUrl이 null인 경우 예외 발생")
  void createVerificationUrl_VerifyInviUrlNull_ThrowsException() {
    // given
    Map<String, String> invalidVcData = new HashMap<>();
    invalidVcData.put("verifyInviUrl", null);
    invalidVcData.put("presExId", "pres_ex_456");

    when(walletService.getWalletToken(tenantId)).thenReturn(walletToken);
    when(remoteTenantAcaPyService.jwtVerify(eq(walletToken), any())).thenReturn(jwtVerifyResponse);
    when(deviceConnectionService.getUserIdByPublicDidOrThrow("did:credo:test123")).thenReturn(userId);
    when(issuedVcService.sendVerifiyInviUrlOrThrow(userId, tenantId, 123L))
        .thenReturn(invalidVcData);

    // when & then
    assertThatThrownBy(() -> verificationService.createVerificationUrl(request, tenantId))
        .isInstanceOf(BusinessException.class).hasMessageContaining("VC 정보에서 필요한 데이터를 찾을 수 없습니다");
  }

  @Test
  @DisplayName("VC 데이터에서 presExId가 null인 경우 예외 발생")
  void createVerificationUrl_PresExIdNull_ThrowsException() {
    // given
    Map<String, String> invalidVcData = new HashMap<>();
    invalidVcData.put("verifyInviUrl", "https://example.com/verify/123");
    invalidVcData.put("presExId", null);

    when(walletService.getWalletToken(tenantId)).thenReturn(walletToken);
    when(remoteTenantAcaPyService.jwtVerify(eq(walletToken), any())).thenReturn(jwtVerifyResponse);
    when(deviceConnectionService.getUserIdByPublicDidOrThrow("did:credo:test123")).thenReturn(userId);
    when(issuedVcService.sendVerifiyInviUrlOrThrow(userId, tenantId, 123L))
        .thenReturn(invalidVcData);

    // when & then
    assertThatThrownBy(() -> verificationService.createVerificationUrl(request, tenantId))
        .isInstanceOf(BusinessException.class).hasMessageContaining("VC 정보에서 필요한 데이터를 찾을 수 없습니다");
  }
}
