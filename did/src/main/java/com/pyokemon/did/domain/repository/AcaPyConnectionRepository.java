package com.pyokemon.did.domain.repository;

import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;

import com.pyokemon.did.domain.AcaPyConnection;

/**
 * AcaPy Connection 데이터에 접근하기 위한 Repository 인터페이스
 */
@Mapper
public interface AcaPyConnectionRepository {

  /**
   * AcaPyConnection 객체를 저장합니다.
   *
   * @param acaPyConnection 저장할 AcaPyConnection 객체
   * @return 저장된 AcaPyConnection의 ID
   */
  Long save(AcaPyConnection acaPyConnection);

  /**
   * 테넌트 ID와 사용자 ID로 AcaPyConnection을 조회합니다. 생성일 기준 내림차순으로 정렬됩니다.
   *
   * @param tenantId 테넌트 ID
   * @param userId 사용자 ID
   * @return 조회된 AcaPyConnection (Optional)
   */
  Optional<AcaPyConnection> findByTenantIdAndUserId(Long tenantId, Long userId);

  /**
   * 테넌트 ID, 사용자 ID, 상태가 ACTIVE인 AcaPyConnection 존재 여부를 확인합니다.
   *
   * @param tenantId 테넌트 ID
   * @param userId 사용자 ID
   * @return 활성화된 연결 존재 여부 (존재하면 true, 없으면 false)
   */
  boolean existsByTenantIdAndUserId(Long tenantId, Long userId);

  /**
   * alias로 AcaPyConnection을 조회합니다.
   *
   * @param inviMsgId 조회할 inviMsgId
   * @return AcaPyConnection (Optional)
   */
  Optional<AcaPyConnection> findByInviMsgId(String inviMsgId);

  /**
   * AcaPyConnection 객체를 업데이트합니다.
   *
   * @param acaPyConnection 업데이트할 AcaPyConnection 객체
   * @return 업데이트된 행 수
   */
  int update(AcaPyConnection acaPyConnection);

  /**
   * ID로 AcaPyConnection을 삭제합니다.
   *
   * @param id 삭제할 AcaPyConnection의 ID
   * @return 삭제된 행 수
   */
  int deleteById(Long id);

}
