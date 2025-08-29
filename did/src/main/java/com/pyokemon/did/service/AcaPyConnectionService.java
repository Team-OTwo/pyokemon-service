package com.pyokemon.did.service;

import org.springframework.retry.RetryException;

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
   * ACA-Py 'connections' 웹훅을 통해 수신된 정보로 기존 연결 레코드를 업데이트합니다.
   * <p>
   * 이 메서드는 Out-of-Band 초대장이 수락되어 연결이 활성화(active)될 때 호출됩니다. 최초 초대 메시지 ID({@code inviMsgId})를 사용하여 보류
   * 중인 연결 레코드를 찾고, 최종적으로 확정된 연결 ID({@code connectionId})를 저장합니다. 또한 연결 상태를 'ACTIVE' 로 변경하여 연결을
   * 완료합니다.
   *
   * @param inviMsgId 최초 Out-of-Band 초대 메시지에 포함된 고유 식별자. 이 ID를 통해 업데이트할 대상 연결 레코드를 조회합니다.
   * @param connectionId 연결이 활성화되면서 최종적으로 부여된 고유 연결 식별자.
   * @throws RetryException {@code inviMsgId}에 해당하는 연결 레코드를 즉시 찾을 수 없을 때 발생합니다. 이는 웹훅 처리 순서나 네트워크
   *         지연으로 인한 일시적인 상태일 수 있으므로, 재시도를 통해 해결될 수 있습니다.
   */
  void updateConnectionId(String inviMsgId, String connectionId) throws RetryException;

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
