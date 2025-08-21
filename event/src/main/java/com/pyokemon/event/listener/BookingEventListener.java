package com.pyokemon.event.listener;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.pyokemon.event.dto.kafka.BookingEventDto;
import com.pyokemon.event.dto.kafka.PaymentKafkaDto;
import com.pyokemon.event.dto.kafka.BookingInfoDto;
import com.pyokemon.event.service.SeatStatusService;
import com.pyokemon.event.repository.BookingRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingEventListener {

  private final SeatStatusService seatStatusService;
  private final BookingRepository bookingRepository;

  // booking 서비스가 발행하는 예약 상태 이벤트 구독
  @KafkaListener(topics = "booking-status-updated", containerFactory = "bookingEventListenerContainerFactory")
  public void handleBookingEvent(BookingEventDto event) {
    if (event == null) return;

    Long scheduleId = event.getEventScheduleId();
    Long seatId = event.getSeatId();
    String status = event.getStatus();

    log.info("[booking-status-updated] Received event: scheduleId={}, seatId={}, status={}", scheduleId, seatId, status);

    if (scheduleId == null || seatId == null) {
      log.warn("BookingEventDto missing coordinates: {}", event);
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
          log.info("[booking-events] ignore status: {} payload={} ", status, event);
      }
    } catch (Exception e) {
      log.error("Error processing BookingEventDto: {}", e.getMessage(), e);
    }
  }

  // PaymentKafkaDto 타입의 메시지도 처리 (bookingId로 좌석 정보 조회)
  @KafkaListener(topics = "booking-status-updated", containerFactory = "paymentEventListenerContainerFactory")
  public void handlePaymentKafkaDto(PaymentKafkaDto event) {
    if (event == null) return;

    log.info("[booking-status-updated] PaymentKafkaDto received: {}", event);
    
    Long bookingId = event.getBookingId();
    String status = event.getStatus();
    
    if (bookingId == null) {
      log.warn("PaymentKafkaDto missing bookingId: {}", event);
      return;
    }

    try {
      // bookingId로 좌석 정보 조회
      BookingInfoDto bookingInfo = bookingRepository.findSeatInfoByBookingId(bookingId);
      
      if (bookingInfo == null) {
        log.warn("Booking info not found for bookingId: {}", bookingId);
        return;
      }

      Long scheduleId = bookingInfo.getEventScheduleId();
      Long seatId = bookingInfo.getSeatId();
      
      if (scheduleId == null || seatId == null) {
        log.warn("Booking info missing coordinates: {}", bookingInfo);
        return;
      }

      log.info("[booking-status-updated] Found booking info: scheduleId={}, seatId={}, status={}", 
          scheduleId, seatId, status);

      // 결제 상태에 따라 좌석 상태 업데이트
      switch (status) {
        case "DONE":
          log.info("[booking-status-updated] Processing DONE status for scheduleId={}, seatId={}", scheduleId, seatId);
          seatStatusService.confirmSeat(scheduleId, seatId.intValue());
          log.info("[booking-status-updated] DONE → seat confirmed. scheduleId={}, seatId={}", scheduleId, seatId);
          break;
        case "CANCELED":
        case "FAILED":
          log.info("[booking-status-updated] Processing {} status for scheduleId={}, seatId={}", status, scheduleId, seatId);
          seatStatusService.clearSeatStatus(scheduleId, seatId.intValue());
          log.info("[booking-status-updated] {} → seat cleared. scheduleId={}, seatId={}", status, scheduleId, seatId);
          break;
        default:
          log.info("[booking-status-updated] Ignore payment status: {} for scheduleId={}, seatId={}", status, scheduleId, seatId);
      }

    } catch (Exception e) {
      log.error("Error processing PaymentKafkaDto: {}", e.getMessage(), e);
    }
  }
}


