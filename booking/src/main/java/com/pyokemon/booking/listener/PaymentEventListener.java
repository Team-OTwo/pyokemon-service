package com.pyokemon.booking.listener;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pyokemon.booking.dto.kafka.PaymentKafkaDto;
import com.pyokemon.booking.entity.Booking;
import com.pyokemon.booking.service.BookingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

  private final BookingService bookingService;
  private final ObjectMapper objectMapper;

  @KafkaListener(
      topics = "#{T(com.pyokemon.common.kafka.KafkaTopicConstants).PAYMENT_STATUS_UPDATED}",
      groupId = "${spring.application.name}", errorHandler = "kafkaErrorHandler")
  public void handlePaymentStatusUpdate(@Payload PaymentKafkaDto paymentEvent,
      @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
      @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
      @Header(KafkaHeaders.OFFSET) long offset, Acknowledgment ack) {

    log.info("결제 상태 업데이트 이벤트 수신: topic={}, partition={}, offset={}, event={}", topic, partition,
        offset, paymentEvent);

    validatePaymentEvent(paymentEvent);
    processPaymentEvent(paymentEvent);

    log.info("결제 상태 업데이트 이벤트 처리 완료: paymentId={}, bookingId={}, status={}",
        paymentEvent.getPaymentId(), paymentEvent.getBookingId(), paymentEvent.getStatus());

    ack.acknowledge();
  }

  private void validatePaymentEvent(PaymentKafkaDto paymentEvent) {
    if (paymentEvent == null) {
      throw new IllegalArgumentException("결제 이벤트가 null입니다.");
    }

    if (paymentEvent.getPaymentId() == null) {
      throw new IllegalArgumentException("결제 ID가 null입니다.");
    }

    if (paymentEvent.getBookingId() == null) {
      throw new IllegalArgumentException("예약 ID가 null입니다.");
    }

    if (paymentEvent.getStatus() == null || paymentEvent.getStatus().trim().isEmpty()) {
      throw new IllegalArgumentException("결제 상태가 null이거나 비어있습니다.");
    }
  }

  public void processPaymentEvent(PaymentKafkaDto paymentEvent) {
    Booking.Booked newStatus = mapPaymentStatusToBookingStatus(paymentEvent.getStatus());
    bookingService.updateBookingStatusIfPending(paymentEvent.getBookingId(), newStatus,
        paymentEvent.getPaymentId());

    log.info("예약 상태 업데이트 완료: bookingId={}, paymentId={}, newStatus={}", paymentEvent.getBookingId(),
        paymentEvent.getPaymentId(), newStatus);
  }

  private Booking.Booked mapPaymentStatusToBookingStatus(String paymentStatus) {
    return switch (paymentStatus.toUpperCase()) {
      case "DONE" -> Booking.Booked.BOOKED;
      case "CANCELED" -> Booking.Booked.CANCELED;
      case "FAILED" -> Booking.Booked.FAILED;
      default -> {
        log.warn("Unknown payment status: {}, defaulting to FAILED", paymentStatus);
        yield Booking.Booked.FAILED;
      }
    };
  }
}
