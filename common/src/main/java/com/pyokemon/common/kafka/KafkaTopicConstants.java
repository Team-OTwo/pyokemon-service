package com.pyokemon.common.kafka;

/**
 * Kafka 토픽 이름을 상수로 관리하는 클래스 모든 서비스에서 동일한 토픽 이름을 사용할 수 있도록 합니다.
 */
public final class KafkaTopicConstants {

  private KafkaTopicConstants() {
    // 인스턴스 생성 방지
  }

  // 결제 관련 토픽
  public static final String PAYMENT_CREATED = "payment-created";
  public static final String PAYMENT_CONFIRMED = "payment-confirmed";
  public static final String PAYMENT_FAILED = "payment-failed";
  public static final String PAYMENT_STATUS_UPDATED = "payment-status-updated";

  // 이벤트 관련 토픽
  public static final String EVENT_CREATED = "event-created";
  public static final String EVENT_UPDATED = "event-updated";
  public static final String EVENT_DELETED = "event-deleted";

  // 예약 관련 토픽
  public static final String BOOKING_CREATED = "booking-created";
  public static final String BOOKING_CONFIRMED = "booking-confirmed";
  public static final String BOOKING_CANCELED = "booking-canceled";
  public static final String BOOKING_STATUS_UPDATED = "booking-status-updated";

  // 계정 관련 토픽
  public static final String USER_CREATED = "user-created";
  public static final String USER_UPDATED = "user-updated";
}
