package com.pyokemon.did.service.impl;


import static com.pyokemon.common.exception.code.DidErrorCodes.ACAPY_SERVICE_ERROR;
import static com.pyokemon.common.exception.code.DidErrorCodes.VP_VERIFICATION_FAILED;

import java.util.Map;
import java.util.Optional;

import com.pyokemon.did.domain.Verification;
import com.pyokemon.did.domain.Verification.VpStatus;
import com.pyokemon.did.domain.dto.response.VerificationResponse.HandleVerificationResponse;
import com.pyokemon.did.domain.repository.VerificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.did.domain.dto.request.VerificationRequest.CreateVerificationRequest;
import com.pyokemon.did.domain.dto.response.VerificationResponse.CreateVerificationResponse;
import com.pyokemon.did.remote.acapy.common.dto.request.JwtVerifyRequest;
import com.pyokemon.did.remote.acapy.common.dto.response.JwtVerifyResponse;
import com.pyokemon.did.remote.acapy.service.RemoteTenantAcaPyService;
import com.pyokemon.did.service.DeviceConnectionService;
import com.pyokemon.did.service.IssuedVcService;
import com.pyokemon.did.service.VerificationService;
import com.pyokemon.did.service.WalletService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class VerificationServiceImpl implements VerificationService {

  private final RemoteTenantAcaPyService remoteTenantAcaPyService;

  private final WalletService walletService;
  private final DeviceConnectionService deviceConnectionService;
  private final IssuedVcService issuedVcService;
  private final VerificationRepository verificationRepository;

  @Override
  public CreateVerificationResponse createVerificationUrl(CreateVerificationRequest request,
      Long tenantId) {

    Long bookingId = request.getBooking_id();
    JwtVerifyRequest jwtVerifyRequest = JwtVerifyRequest.of(request.getJwt());
    String authorization = walletService.getWalletToken(tenantId);

    // Jwt 서명 검증 - admin API 호출
    JwtVerifyResponse jwtVerifyResponse;
    String credoPublicDid;
    try {
      jwtVerifyResponse = remoteTenantAcaPyService.jwtVerify(authorization, jwtVerifyRequest);
      if (jwtVerifyResponse == null || jwtVerifyResponse.getPayload() == null
          || jwtVerifyResponse.getPayload().getDid() == null) {
        throw new RuntimeException("JWT 검증 응답이 유효하지 않습니다.");
      }
      credoPublicDid = jwtVerifyResponse.getPayload().getDid();
    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      log.error("JWT 검증 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
      throw new BusinessException("JWT 검증 중 오류가 발생했습니다.", ACAPY_SERVICE_ERROR);
    }

    // invi_url, pres_ex_id 조회
    Long userId = deviceConnectionService.getUserIdByDidOrThrow(credoPublicDid);
    Map<String, String> stringMap =
        issuedVcService.sendVerifiyInviUrlOrThrow(userId, tenantId, bookingId);

    // Map 값 안전성 개선
    String verifyInviUrl = stringMap.get("verifyInviUrl");
    String presExId = stringMap.get("presExId");

    if (verifyInviUrl == null || presExId == null) {
      throw new BusinessException("VC 정보에서 필요한 데이터를 찾을 수 없습니다.", "VC_DATA_NOT_FOUND");
    }

    return new CreateVerificationResponse(verifyInviUrl, presExId);
  }

  @Override
  public HandleVerificationResponse handleVerification(Long tenantId, String presExId) {
    VpStatus status = getStatusOrThrow(presExId);
    HandleVerificationResponse response = new HandleVerificationResponse(status.toString());
    return response;
  }

  public VpStatus getStatusOrThrow(String presExId) {
    Verification verification = verificationRepository.findByPresExId(presExId)
            .orElseThrow(() -> new BusinessException("해당 presExId를 가진 검증 정보를 찾을 수 없습니다: " + presExId, VP_VERIFICATION_FAILED));
    return verification.getStatus();
  }
}
