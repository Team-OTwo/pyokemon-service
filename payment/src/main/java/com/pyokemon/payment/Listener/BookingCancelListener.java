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
      groupId = "${spring.application.name}", errorHandler = "kafkaErrorHandler")
  public void handleBookingStatusUpdate(@Payload BookingEventDto bookingEvent,
      @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
      @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
      @Header(KafkaHeaders.OFFSET) long offset, Acknowledgment ack) {

    log.info("예약 상태 업데이트 이벤트 수신: topic={}, partition={}, offset={}, event={}", topic, partition,
        offset, bookingEvent);

    validateBookingEvent(bookingEvent);
    processBookingEvent(bookingEvent);

    log.info("예약 상태 업데이트 이벤트 처리 완료: bookingId={}, status={}", bookingEvent.getBookingId(),
        bookingEvent.getStatus());

    ack.acknowledge();
  }

  private void validateBookingEvent(BookingEventDto bookingEvent) {
    if (bookingEvent == null) {
      throw new IllegalArgumentException("예약 이벤트가 null입니다.");
    }

    if (bookingEvent.getBookingId() == null) {
      throw new IllegalArgumentException("예약 ID가 null입니다.");
    }

    if (bookingEvent.getStatus() == null || bookingEvent.getStatus().trim().isEmpty()) {
      throw new IllegalArgumentException("예약 상태가 null이거나 비어있습니다.");
    }
  }

  public void processBookingEvent(BookingEventDto bookingEvent) {
    if (!"CANCEL_REQUESTED".equals(bookingEvent.getStatus())
        && !"CANCELED".equals(bookingEvent.getStatus())) {
      log.debug("예약 상태 무시 (취소 관련 아님): status={}, bookingId={}", bookingEvent.getStatus(),
          bookingEvent.getBookingId());
      return;
    }

    log.info("예약 취소 처리 시작: bookingId={}, status={}", bookingEvent.getBookingId(),
        bookingEvent.getStatus());

    paymentCancelService.cancelByBookingId(bookingEvent.getBookingId(), "예약 취소 요청");

    log.info("예약 취소 처리 완료: bookingId={}", bookingEvent.getBookingId());
  }
}
