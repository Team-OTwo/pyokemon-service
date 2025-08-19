package com.pyokemon.payment.Listener;

import java.util.Map;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.pyokemon.payment.dto.kafka.BookingEventDto;
import com.pyokemon.payment.service.PaymentCancelService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingCancelListener {

  private final PaymentCancelService paymentCancelService;

  @KafkaListener(topics = "#{T(com.pyokemon.common.kafka.KafkaTopicConstants).BOOKING_STATUS_UPDATED}",
          groupId = "${spring.application.name}")
  public void onBookingEvent(Map<String, Object> message) {
    try {
      log.info("Received booking status update message: {}", message);
      
      // Map을 BookingEventDto로 수동 변환
      BookingEventDto bookingEvent = convertToBookingEventDto(message);
      log.info("Converted to BookingEventDto: {}", bookingEvent);

      if (!"CANCEL_REQUESTED".equals(bookingEvent.getStatus()) && !"CANCELED".equals(bookingEvent.getStatus())) {
        log.debug("Ignore booking status: {}", bookingEvent.getStatus());
        return;
      }

      paymentCancelService.cancelByBookingId(bookingEvent.getBookingId(), "예약 취소 요청");
      log.info("Handled cancel for booking {}", bookingEvent.getBookingId());

    } catch (Exception e) {
      log.error("Failed to handle booking event message: {}", message, e);
    }
  }
  
  private BookingEventDto convertToBookingEventDto(Map<String, Object> message) {
    Long bookingId = getLongValue(message, "bookingId");
    Long eventScheduleId = getLongValue(message, "eventScheduleId");
    Long seatId = getLongValue(message, "seatId");
    Long accountId = getLongValue(message, "accountId");
    Long tenantId = getLongValue(message, "tenantId");
    String status = (String) message.get("status");
    
    BookingEventDto dto = new BookingEventDto();
    dto.setBookingId(bookingId);
    dto.setEventScheduleId(eventScheduleId);
    dto.setSeatId(seatId);
    dto.setAccountId(accountId);
    dto.setTenantId(tenantId);
    dto.setStatus(status);
    
    return dto;
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
}
