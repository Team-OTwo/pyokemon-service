package com.pyokemon.booking.listener;

import org.springframework.kafka.annotation.KafkaListener;
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
      groupId = "${spring.application.name}")
  public void handlePaymentStatusUpdate(PaymentKafkaDto paymentEvent) {
    try {
      log.info("결제 상태 업데이트 이벤트 수신: {}", paymentEvent);
      processPaymentEvent(paymentEvent);
    } catch (Exception e) {
      log.error("결제 상태 업데이트 메시지 처리 중 오류 발생: {}", paymentEvent, e);
    }
  }

  public void processPaymentEvent(PaymentKafkaDto paymentEvent) {
    Booking.Booked newStatus = mapPaymentStatusToBookingStatus(paymentEvent.getStatus());
    bookingService.updateBookingStatusIfPending(paymentEvent.getBookingId(), newStatus,
        paymentEvent.getPaymentId());
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
