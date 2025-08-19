package com.pyokemon.booking.listener;

import java.util.Map;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.pyokemon.booking.dto.kafka.PaymentEventDto;
import com.pyokemon.booking.entity.Booking;
import com.pyokemon.booking.service.BookingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

  private final BookingService bookingService;

  @KafkaListener(
      topics = "#{T(com.pyokemon.common.kafka.KafkaTopicConstants).PAYMENT_STATUS_UPDATED}",
      groupId = "${spring.application.name}")
  public void handlePaymentStatusUpdate(Map<String, Object> message) {
    try {
      PaymentEventDto paymentEvent = convertToPaymentEventDto(message);
      log.info("Converted to PaymentEventDto: {}", paymentEvent);
      
      processPaymentEvent(paymentEvent);
    } catch (Exception e) {
      log.error("Error processing payment status update message: {}", message, e);
    }
  }

  private PaymentEventDto convertToPaymentEventDto(Map<String, Object> message) {
    Long paymentId = getLongValue(message, "paymentId");
    Long bookingId = getLongValue(message, "bookingId");
    String status = (String) message.get("status");
    
    return new PaymentEventDto(paymentId, bookingId, status);
  }
  
  private Long getLongValue(Map<String, Object> map, String key) {
    Object value = map.get(key);
    if (value instanceof Integer) {
      return ((Integer) value).longValue();
    } else if (value instanceof Long) {
      return (Long) value;
    } else if (value instanceof String) {
      return Long.parseLong((String) value);
    }
    return null;
  }

  public void processPaymentEvent(PaymentEventDto paymentEvent) {
    Booking.Booked newStatus = mapPaymentStatusToBookingStatus(paymentEvent.getStatus());
    bookingService.updateBookingStatus(paymentEvent.getBookingId(), newStatus,
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
