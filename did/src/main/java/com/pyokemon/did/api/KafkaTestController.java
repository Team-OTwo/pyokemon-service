package com.pyokemon.did.api;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import com.pyokemon.common.dto.ResponseDto;
import com.pyokemon.common.kafka.KafkaTopicConstants;
import com.pyokemon.did.event.consumer.message.booking.BookingEventDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/test/kafka")
@RequiredArgsConstructor
@Slf4j
public class KafkaTestController {

  private final KafkaTemplate<String, Object> kafkaTemplate;

  // ==================== DID 서비스 관련 Kafka 이벤트 테스트 ====================

  /**
   * 예매 완료 이벤트 발행 (DID 서비스에서 AcaPy 연결 생성 트리거) 토픽: booking-status-updated
   */
  @PostMapping("/booking-completed")
  public ResponseEntity<ResponseDto<String>> sendBookingCompletedEvent() {
    try {
      BookingEventDto event = BookingEventDto.builder().bookingId(1L).eventScheduleId(1L).seatId(1L)
          .accountId(1L).tenantId(1L).status("BOOKED").build();

      kafkaTemplate.send(KafkaTopicConstants.BOOKING_STATUS_UPDATED,
          String.valueOf(event.getBookingId()), event);

      log.info("예매 완료 Kafka 이벤트 발행 성공: {}", event);

      return ResponseEntity.ok(ResponseDto.success("예매 완료 Kafka 이벤트 발행 성공: " + event.toString()));
    } catch (Exception e) {
      log.error("예매 완료 Kafka 이벤트 발행 실패: {}", e.getMessage(), e);
      return ResponseEntity.badRequest().body(
          ResponseDto.error("예매 완료 Kafka 이벤트 발행 실패: " + e.getMessage(), "KAFKA_EVENT_SEND_FAILED"));
    }
  }

  /**
   * 이벤트 확정 이벤트 발행 (DID 서비스에서 자격증명 발급 트리거) 토픽: event-status-updated
   */
  @PostMapping("/event-confirmed")
  public ResponseEntity<ResponseDto<String>> sendEventConfirmedEvent() {
    try {
      Map<String, Object> event =
          Map.of("eventScheduleId", 1L, "accountId", 1L, "tenantId", 1L, "status", "CONFIRMED");

      kafkaTemplate.send(KafkaTopicConstants.EVENT_STATUS_UPDATED, UUID.randomUUID().toString(),
          event);

      log.info("이벤트 확정 Kafka 이벤트 발행 성공: {}", event);

      return ResponseEntity.ok(ResponseDto.success("이벤트 확정 Kafka 이벤트 발행 성공: " + event.toString()));
    } catch (Exception e) {
      log.error("이벤트 확정 Kafka 이벤트 발행 실패: {}", e.getMessage(), e);
      return ResponseEntity.badRequest().body(ResponseDto
          .error("이벤트 확정 Kafka 이벤트 발행 실패: " + e.getMessage(), "KAFKA_EVENT_SEND_FAILED"));
    }
  }

  /**
   * 이벤트 취소 이벤트 발행 토픽: event-status-updated
   */
  @PostMapping("/event-cancelled")
  public ResponseEntity<ResponseDto<String>> sendEventCancelledEvent() {
    try {
      Map<String, Object> event =
          Map.of("eventScheduleId", 1L, "accountId", 1L, "tenantId", 1L, "status", "CANCELLED");

      kafkaTemplate.send(KafkaTopicConstants.EVENT_STATUS_UPDATED, UUID.randomUUID().toString(),
          event);

      log.info("이벤트 취소 Kafka 이벤트 발행 성공: {}", event);

      return ResponseEntity.ok(ResponseDto.success("이벤트 취소 Kafka 이벤트 발행 성공: " + event.toString()));
    } catch (Exception e) {
      log.error("이벤트 취소 Kafka 이벤트 발행 실패: {}", e.getMessage(), e);
      return ResponseEntity.badRequest().body(ResponseDto
          .error("이벤트 취소 Kafka 이벤트 발행 실패: " + e.getMessage(), "KAFKA_EVENT_SEND_FAILED"));
    }
  }

  /**
   * 예매 취소 이벤트 발행 토픽: booking-status-updated
   */
  @PostMapping("/booking-cancelled")
  public ResponseEntity<ResponseDto<String>> sendBookingCancelledEvent() {
    try {
      BookingEventDto event = BookingEventDto.builder().bookingId(1L).eventScheduleId(1L).seatId(1L)
          .accountId(1L).tenantId(1L).status("CANCELLED").build();

      kafkaTemplate.send(KafkaTopicConstants.BOOKING_STATUS_UPDATED,
          String.valueOf(event.getBookingId()), event);

      log.info("예매 취소 Kafka 이벤트 발행 성공: {}", event);

      return ResponseEntity.ok(ResponseDto.success("예매 취소 Kafka 이벤트 발행 성공: " + event.toString()));
    } catch (Exception e) {
      log.error("예매 취소 Kafka 이벤트 발행 실패: {}", e.getMessage(), e);
      return ResponseEntity.badRequest().body(
          ResponseDto.error("예매 취소 Kafka 이벤트 발행 실패: " + e.getMessage(), "KAFKA_EVENT_SEND_FAILED"));
    }
  }

  /**
   * 예매 완료 이벤트 발행 (커스텀 데이터)
   */
  @PostMapping("/booking-completed/custom")
  public ResponseEntity<ResponseDto<String>> sendCustomBookingCompletedEvent(
      @RequestBody Map<String, Object> customEvent) {
    try {
      kafkaTemplate.send(KafkaTopicConstants.BOOKING_STATUS_UPDATED, UUID.randomUUID().toString(),
          customEvent);

      log.info("커스텀 예매 완료 Kafka 이벤트 발행 성공: {}", customEvent);

      return ResponseEntity
          .ok(ResponseDto.success("커스텀 예매 완료 Kafka 이벤트 발행 성공: " + customEvent.toString()));
    } catch (Exception e) {
      log.error("커스텀 예매 완료 Kafka 이벤트 발행 실패: {}", e.getMessage(), e);
      return ResponseEntity.badRequest().body(ResponseDto
          .error("커스텀 예매 완료 Kafka 이벤트 발행 실패: " + e.getMessage(), "KAFKA_EVENT_SEND_FAILED"));
    }
  }

  /**
   * 이벤트 확정 이벤트 발행 (커스텀 데이터)
   */
  @PostMapping("/event-confirmed/custom")
  public ResponseEntity<ResponseDto<String>> sendCustomEventConfirmedEvent(
      @RequestBody Map<String, Object> customEvent) {
    try {
      kafkaTemplate.send(KafkaTopicConstants.EVENT_STATUS_UPDATED, UUID.randomUUID().toString(),
          customEvent);

      log.info("커스텀 이벤트 확정 Kafka 이벤트 발행 성공: {}", customEvent);

      return ResponseEntity
          .ok(ResponseDto.success("커스텀 이벤트 확정 Kafka 이벤트 발행 성공: " + customEvent.toString()));
    } catch (Exception e) {
      log.error("커스텀 이벤트 확정 Kafka 이벤트 발행 실패: {}", e.getMessage(), e);
      return ResponseEntity.badRequest().body(ResponseDto
          .error("커스텀 이벤트 확정 Kafka 이벤트 발행 실패: " + e.getMessage(), "KAFKA_EVENT_SEND_FAILED"));
    }
  }

  // ==================== 기타 Kafka 이벤트 테스트 ====================

  /**
   * 결제 완료 이벤트 발행 토픽: payment-status-updated
   */
  @PostMapping("/payment-completed")
  public ResponseEntity<ResponseDto<String>> sendPaymentCompletedEvent() {
    try {
      Map<String, Object> event = Map.of("paymentId", 1L, "bookingId", 1L, "status", "DONE");

      kafkaTemplate.send(KafkaTopicConstants.PAYMENT_STATUS_UPDATED, UUID.randomUUID().toString(),
          event);

      log.info("결제 완료 Kafka 이벤트 발행 성공: {}", event);

      return ResponseEntity.ok(ResponseDto.success("결제 완료 Kafka 이벤트 발행 성공: " + event.toString()));
    } catch (Exception e) {
      log.error("결제 완료 Kafka 이벤트 발행 실패: {}", e.getMessage(), e);
      return ResponseEntity.badRequest().body(
          ResponseDto.error("결제 완료 Kafka 이벤트 발행 실패: " + e.getMessage(), "KAFKA_EVENT_SEND_FAILED"));
    }
  }

  /**
   * 알림 요청 이벤트 발행 토픽: notification-request
   */
  @PostMapping("/notification-request")
  public ResponseEntity<ResponseDto<String>> sendNotificationRequestEvent() {
    try {
      Map<String, Object> event = Map.of("type", "EVENT_REMINDER", "userId", 1L, "message",
          "이벤트 시작 2시간 전입니다.", "data", Map.of("eventScheduleId", 1L));

      kafkaTemplate.send(KafkaTopicConstants.NOTIFICATION_REQUEST, UUID.randomUUID().toString(),
          event);

      log.info("알림 요청 Kafka 이벤트 발행 성공: {}", event);

      return ResponseEntity.ok(ResponseDto.success("알림 요청 Kafka 이벤트 발행 성공: " + event.toString()));
    } catch (Exception e) {
      log.error("알림 요청 Kafka 이벤트 발행 실패: {}", e.getMessage(), e);
      return ResponseEntity.badRequest().body(
          ResponseDto.error("알림 요청 Kafka 이벤트 발행 실패: " + e.getMessage(), "KAFKA_EVENT_SEND_FAILED"));
    }
  }

  /**
   * 이벤트 2시간 전 알림 이벤트 발행 토픽: event-schedule-2h-ahead
   */
  @PostMapping("/event-2h-ahead")
  public ResponseEntity<ResponseDto<String>> sendEventTwoHoursAheadEvent() {
    try {
      Map<String, Object> event = Map.of("eventScheduleId", 1L, "eventName", "테스트 이벤트", "startTime",
          "2024-01-01T14:00:00", "venueName", "테스트 장소");

      kafkaTemplate.send(KafkaTopicConstants.EVENT_SCHEDULE_2H_AHEAD, UUID.randomUUID().toString(),
          event);

      log.info("이벤트 2시간 전 알림 Kafka 이벤트 발행 성공: {}", event);

      return ResponseEntity
          .ok(ResponseDto.success("이벤트 2시간 전 알림 Kafka 이벤트 발행 성공: " + event.toString()));
    } catch (Exception e) {
      log.error("이벤트 2시간 전 알림 Kafka 이벤트 발행 실패: {}", e.getMessage(), e);
      return ResponseEntity.badRequest().body(ResponseDto
          .error("이벤트 2시간 전 알림 Kafka 이벤트 발행 실패: " + e.getMessage(), "KAFKA_EVENT_SEND_FAILED"));
    }
  }

  // ==================== 상태 조회 ====================

  /**
   * Kafka 테스트 컨트롤러 상태 조회
   */
  @GetMapping("/status")
  public ResponseEntity<ResponseDto<Map<String, Object>>> getKafkaTestStatus() {
    Map<String, Object> status = Map.of("controller", "Kafka Test Controller", "description",
        "DID 서비스에서 사용하는 Kafka 이벤트들을 테스트하기 위한 컨트롤러", "availableEndpoints",
        Map.of("booking-completed", "예매 완료 이벤트 (AcaPy 연결 생성 트리거)", "event-confirmed",
            "이벤트 확정 이벤트 (자격증명 발급 트리거)", "event-cancelled", "이벤트 취소 이벤트", "booking-cancelled",
            "예매 취소 이벤트", "payment-completed", "결제 완료 이벤트", "notification-request", "알림 요청 이벤트",
            "event-2h-ahead", "이벤트 2시간 전 알림 이벤트"),
        "kafkaTopics",
        Map.of("booking-status-updated", "예매 상태 업데이트", "event-status-updated", "이벤트 상태 업데이트",
            "payment-status-updated", "결제 상태 업데이트", "notification-request", "알림 요청",
            "event-schedule-2h-ahead", "이벤트 2시간 전 알림"));

    return ResponseEntity.ok(ResponseDto.success(status, "Kafka 테스트 컨트롤러 상태 조회 성공"));
  }
}
