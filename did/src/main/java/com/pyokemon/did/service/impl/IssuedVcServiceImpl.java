package com.pyokemon.did.service.impl;

import static com.pyokemon.common.exception.code.DidErrorCodes.VC_ISSUANCE_FAILED;
import static com.pyokemon.did.domain.IssuedVc.VcStatus.ISSUED;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.did.common.util.UuidGenerator;
import com.pyokemon.did.domain.AcaPyConnection;
import com.pyokemon.did.domain.IssuedProof;
import com.pyokemon.did.domain.IssuedVc;
import com.pyokemon.did.domain.Wallet;
import com.pyokemon.did.domain.repository.IssuedProofRepository;
import com.pyokemon.did.domain.repository.IssuedVcRepository;
import com.pyokemon.did.event.consumer.message.booking.BookingEvent;
import com.pyokemon.did.remote.acapy.common.dto.request.CreateInvitationRequest;
import com.pyokemon.did.remote.acapy.common.dto.request.IssueCredentialRequest;
import com.pyokemon.did.remote.acapy.common.dto.request.PresentProofRequest;
import com.pyokemon.did.remote.acapy.common.dto.request.credential.CredentialSubject;
import com.pyokemon.did.remote.acapy.common.dto.response.CreateInvitationResponse;
import com.pyokemon.did.remote.acapy.common.dto.response.IssueCredentialResponse;
import com.pyokemon.did.remote.acapy.common.dto.response.PresentProofResponse;
import com.pyokemon.did.remote.acapy.service.RemoteTenantAcaPyService;
import com.pyokemon.did.service.AcaPyConnectionService;
import com.pyokemon.did.service.IssuedVcService;
import com.pyokemon.did.service.WalletService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class IssuedVcServiceImpl implements IssuedVcService {
  // TODO: 상수로 분리. 공연 준비 완료 이벤트 스케줄 시간과 맞춰야함.
  // ttl 12시간전 발급 + 1시간
  private static final long PROOF_TIME_TO_LIVE_SECONDS = 60L * 60 * (12 + 1);

  private final IssuedVcRepository issuedVcRepository;
  private final IssuedProofRepository issuedProofRepository;
  private final WalletService walletService;
  private final AcaPyConnectionService acaPyConnectionService;
  private final RemoteTenantAcaPyService remoteTenantAcaPyService;

  @Override
  @Transactional
  public void issueCredential(BookingEvent bookingEvent) throws BusinessException {
    Long userId = bookingEvent.getAccountId();
    Long tenantId = bookingEvent.getTenantId();
    Long bookingId = bookingEvent.getBookingId();
    Long eventScheduleId = bookingEvent.getEventScheduleId();
    Long seatId = bookingEvent.getSeatId();

    log.info("VC 발급 시작 - tenantId: {}, userId: {}, bookingId: {}", tenantId, userId, bookingId);
    // 1. 기존 발급된 VC 있는지 확인
    boolean exists = issuedVcRepository.existsByBookingIdAndIssued(bookingId);
    if (exists) {
      log.info("VC 이미 발급됨 - bookingId: {}", bookingId);
      return;
    }

    // 2. 필수 데이터 조회
    log.info("테넌트 ID {} 사용자 ID {}에 대한 연결 조회", tenantId, userId);
    AcaPyConnection connection =
        acaPyConnectionService.getActiveAcaPyConnectionOrThrow(tenantId, userId);

    log.info("테넌트 ID {}에 대한 지갑 조회", tenantId);
    Wallet tenantWallet = walletService.getWalletByAccountIdOrThrow(tenantId);

    log.info("사용자 ID {}에 대한 지갑 조회", userId);
    Wallet userWallet = walletService.getWalletByAccountIdOrThrow(userId);

    try {
      // 3. 자격 증명 주체 생성
      CredentialSubject credentialSubject =
          CredentialSubject.of(userWallet, bookingId, eventScheduleId, seatId);

      // 4. 자격 증명 발급 요청
      IssueCredentialResponse issueCredentialResponse =
          issueCredential(tenantWallet, connection, credentialSubject, bookingId);

      // 5. 자격 증명 검증 요청
      String challenge = UuidGenerator.generateChallenge();
      PresentProofResponse presentProofResponse =
          presentProof(tenantWallet, userWallet, challenge, bookingId);

      // 6. 검증 첨부 초대장 요청
      CreateInvitationResponse createInvitationResponse =
          createInvitationForProof(tenantWallet, presentProofResponse.getPresExId());

      // 7. 검증 증명 정보 저장
      log.info("VC 검증 증명 정보 저장 - presExId: {}", presentProofResponse.getPresExId());
      issuedProofRepository.save(IssuedProof.of(presentProofResponse.getPresExId(), challenge,
          PROOF_TIME_TO_LIVE_SECONDS));

      // 8. 자격 증명 정보 저장
      log.info("VC 발급 정보 저장 - bookintId: {}, presExId: {}", bookingId,
          presentProofResponse.getPresExId());
      issuedVcRepository.save(issueCredentialResponse.toEntity(tenantWallet.getAccountId(),
          userWallet.getAccountId(), bookingId, presentProofResponse.getPresExId(),
          createInvitationResponse.getInvitationUrl()));

      log.info("VC 발급 완료 - bookingId: {}, presExId: {}, credExId: {}", bookingId,
          presentProofResponse.getPresExId(), issueCredentialResponse.getCredExId());

    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      log.error("VC 발급 중 오류 발생 - bookingId: {}, error: {}", bookingId, e.getMessage(), e);
      throw new BusinessException("VC 발급 중 오류가 발생했습니다.", VC_ISSUANCE_FAILED);
    }
  }

  @Override
  public Map<String, String> sendVerifiyInviUrlOrThrow(Long userId, Long tenantId, Long bookingId) {
    IssuedVc issuedVc = issuedVcRepository
        .findByUserIdAndTenantIdAndBookingIdAndStatus(userId, tenantId, bookingId, ISSUED)
        .orElseThrow(() -> new BusinessException("발급된 VC를 찾을 수 없습니다.", VC_ISSUANCE_FAILED));

    return Map.of("verifyInviUrl", issuedVc.getVerifyInviUrl(), "presExId", issuedVc.getPresExId());
  }

  /**
   * ACA-Py에 자격 증명 발급을 요청합니다.
   *
   * @param tenantWallet 테넌트 지갑 정보
   * @param connection 활성화된 연결
   * @param credentialSubject 자격 증명 주체 정보
   * @param bookingId 예약 ID (로깅용)
   * @return 자격 증명 발급 응답
   * @throws BusinessException 자격 증명 발급 실패 시
   */
  private IssueCredentialResponse issueCredential(Wallet tenantWallet, AcaPyConnection connection,
      CredentialSubject credentialSubject, Long bookingId) {
    String tenantToken = tenantWallet.getToken();
    String tenantPublicDid = tenantWallet.getPublicDid();
    String connectionId = connection.getConnectionId();

    log.info("VC 발급 요청 전송 - bookingId: {}, connectionId: {}", bookingId, connectionId);

    log.debug("VC 발급 요청 - bookingId: {}, connectionId: {}", bookingId, connectionId);

    IssueCredentialRequest request =
        IssueCredentialRequest.createStandard(connectionId, tenantPublicDid, credentialSubject);

    IssueCredentialResponse response =
        remoteTenantAcaPyService.issueCredential(tenantToken, request);

    if (response == null || response.getCredExId() == null) {
      log.error("VC 발급 실패 - bookingId: {}, connectionId: {}", bookingId, connectionId);
      throw new BusinessException("VC 발급에 실패했습니다.", VC_ISSUANCE_FAILED);
    }

    log.debug("VC 발급 요청 성공 - bookingId: {}, credExId: {}", bookingId, response.getCredExId());
    return response;
  }

  /**
   * ACA-Py에 자격 검증 증명 발급을 요청합니다.
   *
   * @param tenantWallet 테넌트 지갑 정보
   * @param userWallet 사용자 지갑 정보
   * @param challenge 챌린지 난수
   * @param bookingId 예약 ID
   * @return 자격 검증 증명 발급 응답
   * @throws BusinessException 자격 검증 증명 발급 실패 시
   */
  private PresentProofResponse presentProof(Wallet tenantWallet, Wallet userWallet,
      String challenge, Long bookingId) {
    String tenantToken = tenantWallet.getToken();
    String userPublicDid = userWallet.getPublicDid();

    log.info("VC 검증 증명 요청 전송 - userPublicDid:{} bookingId: {}", userPublicDid, bookingId);

    PresentProofRequest request =
        PresentProofRequest.forTicketVerification(challenge, userPublicDid, bookingId);

    PresentProofResponse response = remoteTenantAcaPyService.presentProof(tenantToken, request);

    if (response == null || response.getPresExId() == null) {
      log.error("VC 검증 증명 발급 실패 - userPublicDid:{} bookingId: {}", userPublicDid, bookingId);
      throw new BusinessException("VC 발급에 실패했습니다.", VC_ISSUANCE_FAILED);
    }

    log.debug("VC 검증 증명 발급 성공 - userPublicDid:{} bookingId: {}", userPublicDid, bookingId);
    return response;
  }

  /**
   * ACA-Py에 검증 첨부 초대장 발급을 요청합니다.
   *
   * @param tenantWallet 테넌트 지갑 정보
   * @param presentationExchangeId 검증 증명 교환 식별자
   * @return 검증 첨부 초대장 응답
   * @throws BusinessException 검증 첨부 초대장 발급 실패 시
   */
  private CreateInvitationResponse createInvitationForProof(Wallet tenantWallet,
      String presentationExchangeId) {
    String tenantToken = tenantWallet.getToken();

    CreateInvitationRequest request = CreateInvitationRequest.forProof(presentationExchangeId);
    CreateInvitationResponse response =
        remoteTenantAcaPyService.createInvitation(tenantToken, request);

    if (response == null || response.getInvitationUrl() == null
        || response.getInvitationUrl().isEmpty()) {
      throw new BusinessException("VC 발급에 실패했습니다.", VC_ISSUANCE_FAILED);
    }
    return response;
  }

  @Override
  public IssuedVc getIssuedVcByPresExIdOrThrow(String presExId) {
    IssuedVc issuedVc = issuedVcRepository.findByPresExId(presExId)
            .orElseThrow(() -> new BusinessException("발급된 VC를 찾을 수 없습니다.", VC_ISSUANCE_FAILED));
    return issuedVc;
  }
}
