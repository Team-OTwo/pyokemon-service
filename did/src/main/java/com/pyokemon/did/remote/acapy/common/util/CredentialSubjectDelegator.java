package com.pyokemon.did.remote.acapy.common.util;

import static com.pyokemon.common.exception.code.DidErrorCodes.VC_DELEGATION_FAILED;
import static com.pyokemon.common.exception.code.DidErrorCodes.VC_INVALID;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.common.exception.code.DidErrorCodes;
import com.pyokemon.did.remote.acapy.common.dto.request.credential.CredentialSubject;
import com.pyokemon.did.remote.acapy.common.dto.response.GetCredentialResponse;

/**
 * CredentialSubject 위임 처리를 위한 유틸리티 클래스
 *
 */
public final class CredentialSubjectDelegator {

  private CredentialSubjectDelegator() {
    // 인스턴스화 방지
  }

  /**
   * GetCredentialResponse에서 CredentialSubject를 추출합니다.
   * 
   * @param credentialResponse 자격 증명 응답
   * @return 추출된 CredentialSubject
   * @throws BusinessException 응답이 유효하지 않은 경우
   */
  public static CredentialSubject extractCredentialSubject(GetCredentialResponse credentialResponse)
      throws BusinessException {
    if (credentialResponse == null) {
      throw new BusinessException("자격 증명 응답이 null입니다.", VC_INVALID);
    }

    try {
      return credentialResponse.getByFormat().getCredOffer().getLdProof().getCredential()
          .getCredentialSubject();
    } catch (Exception e) {
      throw new BusinessException("자격 증명 주체 추출에 실패했습니다.", VC_INVALID);
    }
  }

  /**
   * GetCredentialResponse에서 Credential ID를 추출합니다.
   * 
   * @param credentialResponse 자격 증명 응답
   * @return 추출된 Credential ID
   * @throws BusinessException 응답이 유효하지 않은 경우
   */
  public static String extractCredentialId(GetCredentialResponse credentialResponse)
      throws BusinessException {
    if (credentialResponse == null) {
      throw new BusinessException("자격 증명 응답이 null입니다.", VC_INVALID);
    }

    try {
      return credentialResponse.getByFormat().getCredOffer().getLdProof().getCredential().getId();
    } catch (Exception e) {
      throw new BusinessException("자격 증명 ID 추출에 실패했습니다.", VC_INVALID);
    }
  }

  /**
   * CredentialSubject를 새로운 DID로 위임합니다.
   * 
   * @param credentialResponse 원본 자격 증명 응답
   * @param delegateeDid 위임받을 대상의 DID (피위임자)
   * @return 위임된 CredentialSubject
   * @throws BusinessException 위임 처리에 실패한 경우
   */
  public static CredentialSubject delegateTo(String delegateeDid,
      GetCredentialResponse credentialResponse) {
    try {
      CredentialSubject credentialSubject = extractCredentialSubject(credentialResponse);
      credentialSubject.setId(delegateeDid);
      return credentialSubject;
    } catch (Exception e) {
      throw new BusinessException("자격 증명 위임 처리에 실패했습니다.", VC_DELEGATION_FAILED);
    }
  }
}
