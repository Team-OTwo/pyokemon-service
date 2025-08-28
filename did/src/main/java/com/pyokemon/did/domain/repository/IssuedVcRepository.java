package com.pyokemon.did.domain.repository;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;

import com.pyokemon.did.domain.IssuedVc;

@Mapper
public interface IssuedVcRepository {

  /**
   * IssuedVc을 저장합니다.
   *
   * @param issuedVc 저장할 IssuedVc
   * @return 저장된 IssuedVc의 ID
   */
  Long save(IssuedVc issuedVc);

  /**
   * ID로 IssuedVc을 조회합니다.
   *
   * @param id 조회할 IssuedVc의 ID
   * @return IssuedVc (Optional)
   */
  Optional<IssuedVc> findById(Long id);

  /**
   * credential_exchange_id로 IssuedVc을 조회합니다.
   *
   * @param credentialExchangeId 조회할 credential_exchange_id
   * @return IssuedVc (Optional)
   */
  Optional<IssuedVc> findByCredentialExchangeId(String credentialExchangeId);

  /**
   * credential_exchange_id와 status로 IssuedVc을 조회합니다.
   *
   * @param credentialExchangeId 조회할 credential_exchange_id
   * @param status 조회할 status
   * @return IssuedVc (Optional)
   */
  Optional<IssuedVc> findByCredentialExchangeIdAndStatus(String credentialExchangeId,
      IssuedVc.VcStatus status);

  /**
   * credential_id로 IssuedVc을 조회합니다.
   *
   * @param credentialId 조회할 credential_id
   * @return IssuedVc (Optional)
   */
  Optional<IssuedVc> findByCredentialId(String credentialId);

  /**
   * booking_id로 IssuedVc 목록을 조회합니다.
   *
   * @param bookingId 조회할 booking_id
   * @return IssuedVc 목록
   */
  Optional<IssuedVc> findByBookingId(Long bookingId);

  /**
   * tenant_id로 IssuedVc 목록을 조회합니다.
   *
   * @param tenantId 조회할 tenant_id
   * @return IssuedVc 목록
   */
  List<IssuedVc> findByTenantId(Long tenantId);

  /**
   * status로 IssuedVc 목록을 조회합니다.
   *
   * @param status 조회할 status
   * @return IssuedVc 목록
   */
  List<IssuedVc> findByStatus(IssuedVc.VcStatus status);

  /**
   * tenant_id와 status로 IssuedVc 목록을 조회합니다.
   *
   * @param tenantId 조회할 tenant_id
   * @param status 조회할 status
   * @return IssuedVc 목록
   */
  List<IssuedVc> findByTenantIdAndStatus(Long tenantId, IssuedVc.VcStatus status);

  /**
   * booking_id와 status로 IssuedVc을 조회합니다.
   *
   * @param bookingId 조회할 booking_id
   * @param status 조회할 status
   * @return IssuedVc (Optional)
   */
  Optional<IssuedVc> findByBookingIdAndStatus(Long bookingId, IssuedVc.VcStatus status);

  /**
   * booking_id로 발급 완료된 VC가 있는지 확인합니다.
   *
   * @param bookingId 조회할 booking_id
   * @return 발급 완료된 VC 존재 여부
   */
  boolean existsByBookingIdAndIssued(Long bookingId);

  /**
   * IssuedVc을 업데이트합니다.
   *
   * @param issuedVc 업데이트할 IssuedVc
   * @return 업데이트된 행 수
   */
  int update(IssuedVc issuedVc);

  /**
   * ID로 IssuedVc을 삭제합니다.
   *
   * @param id 삭제할 IssuedVc의 ID
   * @return 삭제된 행 수
   */
  int deleteById(Long id);

  /**
   * credential_exchange_id로 IssuedVc을 삭제합니다.
   *
   * @param credentialExchangeId 삭제할 credential_exchange_id
   * @return 삭제된 행 수
   */
  int deleteByCredentialExchangeId(String credentialExchangeId);

  /**
   * booking_id로 IssuedVc을 삭제합니다.
   *
   * @param bookingId 삭제할 booking_id
   * @return 삭제된 행 수
   */
  int deleteByBookingId(Long bookingId);
}
