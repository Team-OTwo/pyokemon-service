package com.pyokemon.booking.listener;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import com.pyokemon.booking.dto.kafka.PaymentEventDto;
import com.pyokemon.booking.entity.Booking;
import com.pyokemon.booking.service.BookingService;
import com.pyokemon.common.kafka.KafkaTopicConstants;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

  private final BookingService bookingService;

  @KafkaListener(topics = "#{T(com.pyokemon.common.kafka.KafkaTopicConstants).PAYMENT_STATUS_UPDATED}", 
               groupId = "${spring.application.name}")
  public void handlePaymentStatusUpdate(PaymentEventDto paymentEvent, Acknowledgment ack) {
    try {
      log.info("Received payment event: {}", paymentEvent);
      
      Booking.Booked newStatus = mapPaymentStatusToBookingStatus(paymentEvent.getStatus());
      bookingService.updateBookingStatus(paymentEvent.getBookingId(), newStatus,
          paymentEvent.getPaymentId());
          
      ack.acknowledge();
    } catch (Exception e) {
      log.error("Error processing payment status update: paymentId={}, bookingId={}", 
          paymentEvent.getPaymentId(), paymentEvent.getBookingId(), e);
    }
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
