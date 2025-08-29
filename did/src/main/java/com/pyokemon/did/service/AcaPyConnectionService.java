package com.pyokemon.did.service;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.did.domain.AcaPyConnection;

/**
 * ACA-Py 연결 관리를 위한 서비스 인터페이스 테넌트와 사용자 간의 연결 생성 및 조회 기능을 제공합니다.
 */
public interface AcaPyConnectionService {

  /**
   * 테넌트와 사용자 간의 ACA-Py 연결을 생성합니다.
   *
   * @param tenantId 테넌트 ID
   * @param userId 사용자 ID
   * @throws BusinessException 연결 생성 중 오류가 발생한 경우
   */
  void createAcaPyConnection(Long tenantId, Long userId);

  /**
   * 테넌트와 사용자 간의 활성화된 ACA-Py 연결을 조회합니다. 활성화된 연결이 없는 경우 예외를 발생시킵니다.
   *
   * @param tenantId 테넌트 ID
   * @param userId 사용자 ID
   * @return 활성화된 ACA-Py 연결 객체
   * @throws BusinessException CONNECTION_NOT_FOUND - 활성화된 연결을 찾을 수 없는 경우
   */
  AcaPyConnection getActiveAcaPyConnectionOrThrow(Long tenantId, Long userId);
}
