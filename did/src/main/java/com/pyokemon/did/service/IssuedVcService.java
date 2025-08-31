package com.pyokemon.did.service;

import java.util.Map;

import com.pyokemon.did.event.consumer.message.booking.BookingEventDto;
import org.springframework.retry.RetryException;

import com.pyokemon.did.domain.IssuedVc;

public interface IssuedVcService {

  void issueCredential(BookingEventDto bookingEvent);

  /**
   * ACA-Py 'issue-credential' 웹훅을 통해 수신된 정보로 기존 VC 발급 레코드를 업데이트합니다.
   * <p>
   * 이 메서드는 VC 발급 절차가 진행됨에 따라 ACA-Py 로부터 받은 증명 교환 ID({@code credExId})를 {@code bookingId}를 통해 조회한
   * {@code IssuedVc} 레코드에 저장합니다. 또한, 해당 레코드의 상태를 'ISSUED' 로 변경하여 발급 절차를 완료 처리합니다.
   *
   * @param bookingId 업데이트할 대상 {@code IssuedVc} 레코드를 식별하기 위한 비즈니스 ID (예: 예약 ID).
   * @param credStoredId ACA-Py의 'issue-credential' 프로토콜 인스턴스를 식별하는 고유 증명 교환 ID.
   * @throws RetryException 데이터베이스에 해당 {@code bookingId}의 레코드가 아직 존재하지 않거나 기타 일시적인 오류가 발생했을 때, 재시도를
   *         유도하기 위해 발생합니다. 특정 비즈니스 오류(예: VC_INVALID)의 경우 재시도하지 않을 수 있습니다.
   */
  void updateCredIdStored(Long bookingId, String credStoredId) throws RetryException;

  IssuedVc getIssuedVcByPresExIdOrThrow(String presExId);

  IssuedVc getIssuedVcByBookingIdOrThrow(Long bookingId);

  Map<String, String> sendVerifiyInviUrlOrThrow(Long UserId, Long TenantId, Long BookingId);

}
