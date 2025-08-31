package com.pyokemon.did.remote.acapy.common.dto.request;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.pyokemon.did.remote.acapy.common.constants.AcaPyConstants;
import com.pyokemon.did.remote.acapy.common.dto.base.BaseCredentialRequest;
import com.pyokemon.did.remote.acapy.common.dto.request.credential.*;
import com.pyokemon.did.remote.acapy.common.dto.request.credential.EvidenceCredential.Evidence;
import com.pyokemon.did.remote.acapy.common.util.CredentialIdGenerator;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * ACA-Py에 자격 증명 발급을 요청하기 위한 DTO
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class IssueCredentialRequest extends BaseCredentialRequest {

  private CredentialFilter filter;

  /**
   * 표준 예매 자격 증명 발급 요청 생성
   */
  public static IssueCredentialRequest createStandard(String connectionId, String issuerDid,
      CredentialSubject subject) {
    StandardCredential credential = createStandardCredential(issuerDid, subject);

    return IssueCredentialRequest.builder().connectionId(connectionId).autoOffer(true)
        .filter(CredentialFilter.withStandardCredential(credential)).build();
  }

  /**
   * 증거가 포함된 예매 자격 증명 발급 요청 생성
   */
  public static IssueCredentialRequest createWithEvidence(String connectionId, String sourceCredentialId,
                                                          String issuerDid, CredentialSubject subject) {
    EvidenceCredential credential = createEvidenceCredential(issuerDid, subject, sourceCredentialId);

    return IssueCredentialRequest.builder().connectionId(connectionId).autoOffer(true)
            .filter(CredentialFilter.withEvidenceCredential(credential)).build();
  }

  /**
   * 표준 자격 증명 객체 생성
   */
  private static StandardCredential createStandardCredential(String issuerDid,
      CredentialSubject subject) {
    return StandardCredential.builder().context(createBasicContext())
        .id(CredentialIdGenerator.generateCredentialId(subject.getBookingId()))
        .type(Collections.singletonList(AcaPyConstants.CredentialType.VERIFIABLE_CREDENTIAL))
        .issuer(issuerDid).issuanceDate(Instant.now().toString()).credentialSubject(subject)
        .build();
  }

  /**
   * 증거가 포함된 자격 증명 객체 생성
   */
  private static EvidenceCredential createEvidenceCredential(String issuerDid,
      CredentialSubject delegatedSubject, String sourceCredentialId) {
    return EvidenceCredential.builder().context(createExtendedContext())
        .id(CredentialIdGenerator.generateDelegateCredentialId(sourceCredentialId))
        .type(Collections.singletonList(AcaPyConstants.CredentialType.VERIFIABLE_CREDENTIAL))
        .issuer(issuerDid).issuanceDate(Instant.now().toString()).credentialSubject(delegatedSubject)
        .evidence(Collections.singletonList(Evidence.derivedFrom(sourceCredentialId))).build();
  }

  /**
   * 기본 컨텍스트 생성
   */
  private static List<Object> createBasicContext() {
    return List.of(AcaPyConstants.Context.CREDENTIALS_V1, AcaPyConstants.Context.ED25519_V1,
        Map.of("booking_id", AcaPyConstants.Context.PYOKEMON_BOOKING_ID, "event_schedule_id",
            AcaPyConstants.Context.PYOKEMON_EVENT_SCHEDULE_ID, "seat_id",
            AcaPyConstants.Context.PYOKEMON_SEAT_ID));
  }

  /**
   * 확장된 컨텍스트 생성 (증거 포함)
   */
  private static List<Object> createExtendedContext() {
    return List.of(AcaPyConstants.Context.CREDENTIALS_V1, AcaPyConstants.Context.ED25519_V1,
        Map.of("booking_id", AcaPyConstants.Context.PYOKEMON_BOOKING_ID, "event_schedule_id",
            AcaPyConstants.Context.PYOKEMON_EVENT_SCHEDULE_ID, "seat_id",
            AcaPyConstants.Context.PYOKEMON_SEAT_ID, "evidence",
            AcaPyConstants.Context.SCHEMA_ORG_EVIDENCE, "sourceCredentialId",
            AcaPyConstants.Context.SCHEMA_ORG_IDENTIFIER));
  }
}
