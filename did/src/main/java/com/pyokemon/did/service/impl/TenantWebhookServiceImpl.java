package com.pyokemon.did.service.impl;

import static com.pyokemon.did.domain.IssuedVc.VcStatus.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pyokemon.did.api.backend.dto.IssueCredentialWebhookDto;
import com.pyokemon.did.api.backend.dto.LdProofWebhookDto;
import com.pyokemon.did.domain.IssuedVc;
import com.pyokemon.did.domain.repository.IssuedVcRepository;
import com.pyokemon.did.service.TenantWebhookService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TenantWebhookServiceImpl implements TenantWebhookService {

  private final IssuedVcRepository issuedVcRepository;

  @Override
  public void handleIssueCredentialWebhook(IssueCredentialWebhookDto webhookDto) {
    log.info("일반 Webhook 처리 시작: {}", webhookDto);

    try {
      handleGeneralWebhook(webhookDto);

    } catch (Exception e) {
      log.error("일반 Webhook 처리 중 오류 발생: {}", e.getMessage(), e);
      throw new RuntimeException("일반 Webhook 처리 실패", e);
    }
  }

  @Override
  public void handleLdProofWebhook(LdProofWebhookDto webhookDto) {
    log.info("LD Proof Webhook 처리 시작: {}", webhookDto);

    try {
      handleLdProofWebhook(webhookDto);

    } catch (Exception e) {
      log.error("LD Proof Webhook 처리 중 오류 발생: {}", e.getMessage(), e);
      throw new RuntimeException("LD Proof Webhook 처리 실패", e);
    }
  }

  private void handleLdProofWebhook(LdProofWebhookDto webhookDto) {
    String credExId = webhookDto.getCredExId();
    String credIdStored = webhookDto.getCredIdStored();

    log.info("LD Proof Webhook 처리 - credExId: {}, credIdStored: {}", credExId, credIdStored);

    if (credExId == null || credExId.isEmpty()) {
      log.warn("credential_exchange_id가 없습니다");
      return;
    }

    // LD Proof webhook은 credential_id를 업데이트하고 상태를 CREDENTIAL_ISSUED로 변경
    updateVcStatus(credExId, credIdStored, CREDENTIAL_ISSUED);
  }

  private void handleGeneralWebhook(IssueCredentialWebhookDto webhookDto) {
    String state = webhookDto.getState();
    String credExId = webhookDto.getCredExId();

    log.info("일반 Webhook 처리 - 상태: {}, credExId: {}", state, credExId);

    if (credExId == null || credExId.isEmpty()) {
      log.warn("credential_exchange_id가 없습니다");
      return;
    }

    // 상태에 따른 처리
    switch (state) {
      case "credential-received":
        // credential-received 상태에서 credential_exchange_id 업데이트
        updateVcCredentialExchangeId(webhookDto);
        updateVcStatus(credExId, null, CREDENTIAL_RECEIVED);
        break;
      case "done":
        updateVcStatus(credExId, null, CREDENTIAL_ISSUED);
        break;
      default:
        log.info("처리하지 않는 상태: {}", state);
    }
  }

  private void updateVcCredentialExchangeId(IssueCredentialWebhookDto webhookDto) {
    String credExId = webhookDto.getCredExId();
    log.info("VC credential_exchange_id 업데이트 - credExId: {}", credExId);

    try {
      // webhook에서 booking_id 추출
      String bookingIdStr = webhookDto.getByFormat().getCredOffer().getLdProof().getCredential()
          .getCredentialSubject().getBookingId();
      if (bookingIdStr == null || bookingIdStr.isEmpty()) {
        log.warn("webhook에서 booking_id를 추출할 수 없습니다");
        return;
      }

      // "urn:booking:1" 형태에서 "1" 추출
      String bookingId = bookingIdStr.replace("urn:booking:", "");
      Long bookingIdLong = Long.parseLong(bookingId);

      // booking_id로 CREDENTIAL_SENT 상태인 VC 찾기
      var issuedVcOpt = issuedVcRepository.findByBookingIdAndStatus(bookingIdLong, CREDENTIAL_SENT);
      if (issuedVcOpt.isEmpty()) {
        log.warn("업데이트할 VC를 찾을 수 없습니다 - bookingId: {}, status: CREDENTIAL_SENT", bookingIdLong);
        return;
      }

      IssuedVc issuedVc = issuedVcOpt.get();

      // credential_exchange_id 업데이트
      issuedVc.setCredentialExchangeId(credExId);

      issuedVcRepository.update(issuedVc);
      log.info("VC credential_exchange_id 업데이트 완료 - bookingId: {}, credExId: {}",
          issuedVc.getBookingId(), credExId);

    } catch (Exception e) {
      log.error("VC credential_exchange_id 업데이트 중 오류 발생: {}", e.getMessage(), e);
    }
  }

  private void updateVcStatus(String credExId, String credentialId, IssuedVc.VcStatus status) {
    log.info("VC 상태 업데이트 - credExId: {}, credentialId: {}, status: {}", credExId, credentialId,
        status);

    // credential_exchange_id로 IssuedVc 찾기
    var issuedVcOpt = issuedVcRepository.findByCredentialExchangeId(credExId);
    if (issuedVcOpt.isEmpty()) {
      log.warn("credential_exchange_id에 해당하는 VC를 찾을 수 없습니다: {}", credExId);
      return;
    }

    IssuedVc issuedVc = issuedVcOpt.get();

    // 상태 업데이트
    issuedVc.setStatus(status);

    // credential_id가 있으면 업데이트
    if (credentialId != null && !credentialId.isEmpty()) {
      issuedVc.setCredentialId(credentialId);
    }

    issuedVcRepository.update(issuedVc);
    log.info("VC 상태 업데이트 완료 - bookingId: {}, status: {}, credentialId: {}", issuedVc.getBookingId(),
        status, credentialId);
  }
}
