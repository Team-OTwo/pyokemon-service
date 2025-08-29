package com.pyokemon.did.remote.acapy.common.util;

import com.pyokemon.did.common.util.UuidGenerator;
import com.pyokemon.did.remote.acapy.common.constants.AcaPyConstants;


/**
 * Credential_id(urn:booking:{booking_id} 생성을 위한 유틸리티 클래스
 *
 */
public class CredentialIdGenerator {
  private CredentialIdGenerator() {
    // 인스턴스화 방지
  }

  public static String generateCredentialId(Long bookingId) {
    return AcaPyConstants.Credential.CREDENTIAL_ID_PREFIX + bookingId;
  }

  public static String generateDelegateCredentialId(Long bookingId) {
    return generateCredentialId(bookingId) + ":delegate:" + UuidGenerator.generateUuid();
  }
}
