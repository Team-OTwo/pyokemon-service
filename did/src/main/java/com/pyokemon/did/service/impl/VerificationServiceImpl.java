package com.pyokemon.did.service.impl;

import static com.pyokemon.common.exception.code.DidErrorCodes.*;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.did.domain.DeviceConnection;
import com.pyokemon.did.domain.IssuedVc;
import com.pyokemon.did.domain.Verification;
import com.pyokemon.did.domain.Verification.VpStatus;
import com.pyokemon.did.domain.Wallet;
import com.pyokemon.did.domain.dto.request.VerificationRequest.CreateVerificationRequest;
import com.pyokemon.did.domain.dto.request.VerificationRequest.HandleVerificationRequest;
import com.pyokemon.did.domain.dto.response.VerificationResponse.CreateVerificationResponse;
import com.pyokemon.did.domain.dto.response.VerificationResponse.HandleVerificationResponse;
import com.pyokemon.did.domain.event.BookingVerifiedEvent;
import com.pyokemon.did.domain.repository.DeviceConnectionRepository;
import com.pyokemon.did.domain.repository.VerificationRepository;
import com.pyokemon.did.event.producer.KafkaMessageProducer;
import com.pyokemon.did.remote.acapy.common.constants.AcaPyConstants;
import com.pyokemon.did.remote.acapy.common.dto.request.IssueCredentialRequest;
import com.pyokemon.did.remote.acapy.common.dto.request.JwtVerifyRequest;
import com.pyokemon.did.remote.acapy.common.dto.request.credential.CredentialSubject;
import com.pyokemon.did.remote.acapy.common.dto.response.GetCredentialResponse;
import com.pyokemon.did.remote.acapy.common.dto.response.IssueCredentialResponse;
import com.pyokemon.did.remote.acapy.common.dto.response.JwtVerifyResponse;
import com.pyokemon.did.remote.acapy.common.util.CredentialIdGenerator;
import com.pyokemon.did.remote.acapy.common.util.CredentialSubjectDelegator;
import com.pyokemon.did.remote.acapy.service.RemoteTenantAcaPyService;
import com.pyokemon.did.remote.acapy.service.RemoteUserAcaPyService;
import com.pyokemon.did.service.DeviceConnectionService;
import com.pyokemon.did.service.IssuedVcService;
import com.pyokemon.did.service.VerificationService;
import com.pyokemon.did.service.WalletService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class VerificationServiceImpl implements VerificationService {

  private final KafkaMessageProducer kafkaMessageProducer;

  private final RemoteTenantAcaPyService remoteTenantAcaPyService;
  private final DeviceConnectionService deviceConnectionService;
  private final DeviceConnectionRepository deviceConnectionRepository;
  private final IssuedVcService issuedVcService;
  private final RemoteUserAcaPyService remoteUserAcaPyService;
  private final WalletService walletService;
  private final VerificationRepository verificationRepository;


  @Override
  public CreateVerificationResponse createVerificationUrl(CreateVerificationRequest request,
      Long tenantId) {

    Long bookingId = request.getBookingId();
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
    Long userId = deviceConnectionService.getUserIdByPublicDidOrThrow(credoPublicDid);
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
  public void delegateCredential(Long bookingId, Long userId, String deviceId) {
    try {
      log.info("VC 위임 처리 시작 - bookingId: {}, userId: {}, deviceId: {}", bookingId, userId,
          deviceId);

      // 1. VC 데이터 검증 (가장 중요한 데이터 존재 확인)
      IssuedVc issuedVc = issuedVcService.getIssuedVcByBookingIdOrThrow(bookingId);
      validateIssuedVc(issuedVc, userId);

      // 2. 디바이스 연결 검증 (위임 대상 존재 확인)
      DeviceConnection deviceConnection = deviceConnectionRepository.findByDeviceId(deviceId)
          .orElseThrow(() -> new BusinessException("디바이스 연결이 존재하지 않습니다.", CONNECTION_NOT_FOUND));
      validateDeviceConnection(deviceConnection, userId);

      // 3. 사용자 지갑 조회 (모든 검증 통과 후 권한 확인)
      Wallet userWallet = walletService.getWalletByAccountIdOrThrow(userId);

      // 4. 원본 자격 증명 조회
      String delegatorToken = userWallet.getToken();
      String credentialIdStored = issuedVc.getCredIdStored();

      GetCredentialResponse sourceCredential = remoteUserAcaPyService.getCredential(delegatorToken, // 위임자
          // 지갑
          // 토큰
          credentialIdStored // 원본 자격 증명 교환 식별자
      );

      // 5. 자격 증명 주체 위임
      String delegateeDid = deviceConnection.getPublicDid();

      CredentialSubject delegatedCredentialSubject =
          CredentialSubjectDelegator.delegateTo(delegateeDid, // 피위임자 DID
              sourceCredential // 원본 자격 증명
          );

      // 6. 위임된 자격 증명 발급
      String connectionId = deviceConnection.getConnectionId();
      // TODO - 원본 subjectcredential에서 뽑기
      String sourceCredentialId = CredentialIdGenerator.generateCredentialId(bookingId);
      String delegatorDid = userWallet.getPublicDid();

      IssueCredentialResponse issueCredentialResponse =
          remoteUserAcaPyService.issueCredential(delegatorToken,
              IssueCredentialRequest.createWithEvidence(connectionId, // 디바이스 연결 ID
                  sourceCredentialId, // 원본 자격 증명 ID
                  delegatorDid, // 위임자 DID
                  delegatedCredentialSubject // 위임된 자격 증명 주체
              ));

      confirmDelegation(issueCredentialResponse);

      log.info("VC 위임 처리 완료 - bookingId: {}", bookingId);
    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      log.error("VC 위임 처리 중 오류 발생: {}", e.getMessage(), e);
      throw new BusinessException("VC 위임에 실패했습니다.", VC_DELEGATION_FAILED);
    }
  }

  private void validateIssuedVc(IssuedVc issuedVc, Long userId) throws BusinessException {
    String credIdStored = issuedVc.getCredIdStored();
    if (credIdStored == null || credIdStored.isEmpty()) {
      throw new BusinessException("VC 정보가 유효하지 않습니다.", VC_INVALID);
    }

    if (!issuedVc.getUserId().equals(userId)) {
      throw new BusinessException("접근 권한이 없습니다.", PERMISSION_DENIED);
    }
  }

  private void validateDeviceConnection(DeviceConnection deviceConnection, Long userId) {
    String publicDid = deviceConnection.getPublicDid();
    if (publicDid == null || publicDid.trim().isEmpty()) {
      throw new BusinessException("디바이스 연결이 유효하지 않습니다.", CONNECTION_INVALID);
    }

    if (!userId.equals(deviceConnection.getUserId())) {
      throw new BusinessException("접근 권한이 없습니다.", PERMISSION_DENIED);
    }
  }

  public void confirmDelegation(IssueCredentialResponse issueCredentialResponse) {
    if (!AcaPyConstants.IssuanceState.OFFER_SENT.equals(issueCredentialResponse.getState())) {
      throw new BusinessException("VC 위임에 실패했습니다.", VC_DELEGATION_FAILED);
    }
  }


  @Override
  public HandleVerificationResponse handleVerification(Long tenantId, String presExId,
      HandleVerificationRequest request) {

    Verification verification = verificationRepository.findByPresExId(presExId)
        .orElseThrow(() -> new BusinessException("해당 presExId를 가진 검증 정보를 찾을 수 없습니다: " + presExId,
            VP_VERIFICATION_FAILED));

    kafkaMessageProducer.send(BookingVerifiedEvent.Topic,
        BookingVerifiedEvent.toEntity(request.getBookingId()));

    HandleVerificationResponse response =
        new HandleVerificationResponse(verification.getStatus().toString());
    return response;
  }

  @Override
  @Transactional
  public void saveVerification(String PresExId, VpStatus status) {
    Verification verification = Verification.of(PresExId, status);
    verificationRepository.save(verification);
  }
}
