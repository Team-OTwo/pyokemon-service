package com.pyokemon.payment.Listener;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pyokemon.payment.dto.kafka.BookingEventDto;
import com.pyokemon.payment.service.PaymentCancelService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingCancelListener {

  private final ObjectMapper objectMapper;
  private final PaymentCancelService paymentCancelService;

  @KafkaListener(topics = "booking-status-updated", groupId = "payment-service")
  public void onBookingEvent(String payload) {
    try {
      BookingEventDto evt = objectMapper.readValue(payload, BookingEventDto.class);

      if (!"CANCEL_REQUESTED".equals(evt.getStatus()) && !"CANCELED".equals(evt.getStatus())) {
        log.debug("Ignore booking status: {}", evt.getStatus());
        return;
      }

      paymentCancelService.cancelByBookingId(evt.getBookingId(), "예약 취소 요청");
      log.info("Handled cancel for booking {}", evt.getBookingId());

    } catch (Exception e) {
      log.error("Failed to handle booking event: {}", payload, e);
    }
  }
}
