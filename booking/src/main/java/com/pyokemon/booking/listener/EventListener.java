package com.pyokemon.booking.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pyokemon.booking.dto.kafka.EventKafkaDto;
import com.pyokemon.booking.dto.kafka.PaymentKafkaDto;
import com.pyokemon.booking.entity.Booking;
import com.pyokemon.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventListener {

  private final BookingService bookingService;
  private final ObjectMapper objectMapper;

  @KafkaListener(
      topics = "#{T(com.pyokemon.common.kafka.KafkaTopicConstants).EVENT_STATUS_UPDATED}",
      groupId = "${spring.application.name}", containerFactory = "eventkafkaListenerContainerFactory")
  public void handleEventStatusUpdate(EventKafkaDto evtdto) {
    try {
      log.info("이벤트 상태 업데이트 수신: {}", evtdto);
      bookingService.cancel(evtdto);
    } catch (Exception e) {
      log.error("이벤트 상태 업데이트 메시지 처리 중 오류 발생: {}", evtdto, e);
    }
  }

}