package com.pyokemon.event.listener;

import org.springframework.kafka.annotation.KafkaListener;
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
      groupId = "${spring.application.name}")
  public void handleBookingEvent(BookingEventDto event) {
    if (event == null) {
      log.warn("[booking-status-updated] Received null event");
      return;
    }

    Long scheduleId = event.getEventScheduleId();
    Long seatId = event.getSeatId();
    String status = event.getStatus();

    log.info("[booking-status-updated] Received event: scheduleId={}, seatId={}, status={}, fullEvent={}", 
        scheduleId, seatId, status, event);

    if (scheduleId == null || seatId == null) {
      log.warn("[booking-status-updated] BookingEventDto missing required fields: {}", event);
      return;
    }

    try {
      switch (status) {
        case "BOOKED":
          log.info("[booking-status-updated] Processing BOOKED status for scheduleId={}, seatId={}", scheduleId, seatId);
          seatStatusService.confirmSeat(scheduleId, seatId.intValue());
          log.info("[booking-status-updated] BOOKED → seat confirmed. scheduleId={}, seatId={}", scheduleId, seatId);
          break;
        case "CANCELED":
          log.info("[booking-status-updated] Processing CANCELED status for scheduleId={}, seatId={}", scheduleId, seatId);
          seatStatusService.clearSeatStatus(scheduleId, seatId.intValue());
          log.info("[booking-status-updated] CANCELED → seat cleared. scheduleId={}, seatId={}", scheduleId, seatId);
          break;
        case "FAILED":
          log.info("[booking-status-updated] Processing FAILED status for scheduleId={}, seatId={}", scheduleId, seatId);
          seatStatusService.clearSeatStatus(scheduleId, seatId.intValue());
          log.info("[booking-status-updated] FAILED → seat cleared. scheduleId={}, seatId={}", scheduleId, seatId);
          break;
        case "PENDING":
          // 필요시 보강: 이벤트 기반 홀드 재설정(현재는 REST 홀드로 처리)
          log.info("[booking-status-updated] PENDING received. scheduleId={}, seatId={}", scheduleId, seatId);
          break;
        default:
          log.info("[booking-status-updated] Ignoring unknown status: {} for payload: {}", status, event);
      }
    } catch (Exception e) {
      log.error("[booking-status-updated] Error processing BookingEventDto: scheduleId={}, seatId={}, status={}, error={}", 
          scheduleId, seatId, status, e.getMessage(), e);
      // 에러가 발생해도 메시지 처리를 중단하지 않음
    }
  }
}


