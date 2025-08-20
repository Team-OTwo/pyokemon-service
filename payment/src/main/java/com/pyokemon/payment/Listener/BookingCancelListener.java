package com.pyokemon.payment.Listener;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.pyokemon.payment.dto.kafka.BookingEventDto;
import com.pyokemon.payment.service.PaymentCancelService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingCancelListener {

  private final PaymentCancelService paymentCancelService;

  @KafkaListener(
      topics = "#{T(com.pyokemon.common.kafka.KafkaTopicConstants).BOOKING_STATUS_UPDATED}",
      groupId = "${spring.application.name}")
  public void handleBookingStatusUpdate(BookingEventDto bookingEvent) {
    try {
      log.info("예약 상태 업데이트 이벤트 수신: {}", bookingEvent);
      processBookingEvent(bookingEvent);
    } catch (Exception e) {
      log.error("예약 상태 업데이트 메시지 처리 중 오류 발생: {}", bookingEvent, e);
    }
  }

  public void processBookingEvent(BookingEventDto bookingEvent) {
    if (!"CANCEL_REQUESTED".equals(bookingEvent.getStatus())
        && !"CANCELED".equals(bookingEvent.getStatus())) {
      log.debug("예약 상태 무시: {}", bookingEvent.getStatus());
      return;
    }

    paymentCancelService.cancelByBookingId(bookingEvent.getBookingId(), "예약 취소 요청");
    log.info("예약 취소 처리 완료: {}", bookingEvent.getBookingId());
  }
}
