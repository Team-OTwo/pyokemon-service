package com.pyokemon.did.service.impl;

import static com.pyokemon.common.exception.code.DidErrorCodes.VC_ISSUANCE_FAILED;
import static com.pyokemon.did.domain.IssuedVc.VcStatus.ISSUED;

import com.pyokemon.did.domain.IssuedVc;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.did.domain.AcaPyConnection;
import com.pyokemon.did.domain.IssuedProof;
import com.pyokemon.did.domain.Wallet;
import com.pyokemon.did.domain.repository.IssuedProofRepository;
import com.pyokemon.did.domain.repository.IssuedVcRepository;
import com.pyokemon.did.event.consumer.message.booking.BookingEvent;
import com.pyokemon.did.remote.acapy.common.dto.request.IssueCredentialRequest;
import com.pyokemon.did.remote.acapy.common.dto.request.credential.CredentialSubject;
import com.pyokemon.did.remote.acapy.common.dto.response.IssueCredentialResponse;
import com.pyokemon.did.remote.acapy.service.RemoteTenantAcaPyService;
import com.pyokemon.did.service.AcaPyConnectionService;
import com.pyokemon.did.service.IssuedVcService;
import com.pyokemon.did.service.WalletService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class IssuedVcServiceImpl implements IssuedVcService {
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

    log.info("VC 발급 시작 - userId: {}, tenantId: {}, bookingId: {}", userId, tenantId, bookingId);
    // 1. 기존 발급된 VC 있는지 확인
    boolean exists = issuedVcRepository.existsByBookingIdAndIssued(bookingId);
    if (exists) {
      log.info("VC 이미 발급됨 - bookingId: {}", bookingId);
      return;
    }

    // 2. 필수 데이터 조회
    AcaPyConnection connection =
        acaPyConnectionService.getActiveAcaPyConnectionOrThrow(tenantId, userId);

    log.info("테넌트 ID {}에 대한 지갑 조회", tenantId);
    Wallet tenantWallet = walletService.getWalletByAccountIdOrThrow(tenantId);

    log.info("사용자 ID {}에 대한 지갑 조회", userId);
    Wallet userWallet = walletService.getWalletByAccountIdOrThrow(userId);

    try {
      // 3. VC 발급 요청 전송
      CredentialSubject credentialSubject =
          CredentialSubject.of(userWallet, bookingId, eventScheduleId, seatId);

      IssueCredentialResponse response =
          requestCredentialIssuance(tenantWallet, connection, credentialSubject, bookingId);

      // TODO: 증명 요청 레코드 생성 요청
      // TODO: 증명 요청 첨부 초대장 생성 요청
      issuedProofRepository.save(IssuedProof.of("test-pres-ex-id", "123-456-789", 10000000L));

      // 4. VC 발급 정보 저장
      // issuedVcRepository.save(response.toEntity(tenantId, userId, bookingId));
      log.info("VC 발급 완료 - bookingId: {}, credExId: {}", bookingId, response.getCredExId());

    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      log.error("VC 발급 중 오류 발생 - bookingId: {}, error: {}", bookingId, e.getMessage(), e);
      throw new BusinessException("VC 발급 중 오류가 발생했습니다.", VC_ISSUANCE_FAILED);
    }
  }

  @Override
  public Map<String, String> sendVerifiyInviUrlOrThrow(Long userId, Long tenantId, Long bookingId) {
    IssuedVc issuedVc = issuedVcRepository.findByUserIdAndTenantIdAndBookingIdAndStatus(
            userId, tenantId, bookingId, ISSUED
    ).orElseThrow(() -> new BusinessException("발급된 VC를 찾을 수 없습니다.", VC_ISSUANCE_FAILED));
    
    return Map.of(
        "verifyInviUrl", issuedVc.getVerifyInviUrl(),
        "presExId", issuedVc.getPresExId()
    );
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
  private IssueCredentialResponse requestCredentialIssuance(Wallet tenantWallet,
      AcaPyConnection connection, CredentialSubject credentialSubject, Long bookingId) {
    String tenantToken = tenantWallet.getToken();
    String tenantPublicDid = tenantWallet.getPublicDid();
    String connectionId = connection.getConnectionId();

    log.info("VC 발급 요청 전송 - bookingId: {}, connectionId: {}", bookingId, connectionId);

    log.info("request={}", IssueCredentialRequest
        .createStandard(connectionId, tenantPublicDid, credentialSubject).toString());
    IssueCredentialResponse response = remoteTenantAcaPyService.issueCredential(tenantToken,
        IssueCredentialRequest.createStandard(connectionId, tenantPublicDid, credentialSubject));

    if (response == null || response.getCredExId() == null) {
      log.error("VC 발급 실패 - bookingId: {}, connectionId: {}", bookingId, connectionId);
      throw new BusinessException("VC 발급에 실패했습니다.", VC_ISSUANCE_FAILED);
    }

    log.debug("VC 발급 요청 성공 - bookingId: {}, credExId: {}", bookingId, response.getCredExId());
    return response;
  }

}
