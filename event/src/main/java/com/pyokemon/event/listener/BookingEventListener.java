package com.pyokemon.event.listener;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.pyokemon.event.dto.kafka.BookingEventDto;
import com.pyokemon.event.service.SeatStatusService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingEventListener {

  private final SeatStatusService seatStatusService;

  // booking 서비스가 발행하는 예약 상태 이벤트 구독
  @KafkaListener(
      topics = "#{T(com.pyokemon.common.kafka.KafkaTopicConstants).BOOKING_STATUS_UPDATED}",
      groupId = "${spring.application.name}", errorHandler = "kafkaErrorHandler")
  public void handleBookingEvent(@Payload BookingEventDto event,
      @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
      @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
      @Header(KafkaHeaders.OFFSET) long offset, Acknowledgment ack) {

    log.info("[booking-status-updated] 이벤트 수신: topic={}, partition={}, offset={}, event={}", topic,
        partition, offset, event);

    validateBookingEvent(event);
    processBookingEvent(event);

    log.info(
        "[booking-status-updated] 이벤트 처리 완료: bookingId={}, scheduleId={}, seatId={}, status={}",
        event.getBookingId(), event.getEventScheduleId(), event.getSeatId(), event.getStatus());

    ack.acknowledge();
  }

  private void validateBookingEvent(BookingEventDto event) {
    if (event == null) {
      throw new IllegalArgumentException("예약 이벤트가 null입니다.");
    }

    if (event.getBookingId() == null) {
      throw new IllegalArgumentException("예약 ID가 null입니다.");
    }

    if (event.getEventScheduleId() == null) {
      throw new IllegalArgumentException("이벤트 스케줄 ID가 null입니다.");
    }

    if (event.getSeatId() == null) {
      throw new IllegalArgumentException("좌석 ID가 null입니다.");
    }

    if (event.getStatus() == null || event.getStatus().trim().isEmpty()) {
      throw new IllegalArgumentException("예약 상태가 null이거나 비어있습니다.");
    }
  }

  private void processBookingEvent(BookingEventDto event) {
    Long scheduleId = event.getEventScheduleId();
    Long seatId = event.getSeatId();
    String status = event.getStatus();

    log.info("[booking-status-updated] 이벤트 처리 시작: scheduleId={}, seatId={}, status={}", scheduleId,
        seatId, status);

    switch (status) {
      case "BOOKED":
        log.info("[booking-status-updated] BOOKED 상태 처리: scheduleId={}, seatId={}", scheduleId,
            seatId);
        seatStatusService.confirmSeat(scheduleId, seatId.intValue());
        log.info("[booking-status-updated] BOOKED → 좌석 확정 완료: scheduleId={}, seatId={}", scheduleId,
            seatId);
        break;
      case "CANCELED":
        log.info("[booking-status-updated] CANCELED 상태 처리: scheduleId={}, seatId={}", scheduleId,
            seatId);
        seatStatusService.clearSeatStatus(scheduleId, seatId.intValue());
        log.info("[booking-status-updated] CANCELED → 좌석 상태 해제 완료: scheduleId={}, seatId={}",
            scheduleId, seatId);
        break;
      case "FAILED":
        log.info("[booking-status-updated] FAILED 상태 처리: scheduleId={}, seatId={}", scheduleId,
            seatId);
        seatStatusService.clearSeatStatus(scheduleId, seatId.intValue());
        log.info("[booking-status-updated] FAILED → 좌석 상태 해제 완료: scheduleId={}, seatId={}",
            scheduleId, seatId);
        break;
      case "PENDING":
        // 필요시 보강: 이벤트 기반 홀드 재설정(현재는 REST 홀드로 처리)
        log.info("[booking-status-updated] PENDING 상태 수신: scheduleId={}, seatId={}", scheduleId,
            seatId);
        break;
      default:
        log.warn("[booking-status-updated] 알 수 없는 상태 무시: status={}, scheduleId={}, seatId={}",
            status, scheduleId, seatId);
    }
  }
}
