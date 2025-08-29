package com.pyokemon.booking.listener;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pyokemon.booking.dto.kafka.EventKafkaDto;
import com.pyokemon.booking.service.BookingService;
import com.pyokemon.common.kafka.KafkaTopicConstants;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventListener {

  private final BookingService bookingService;

  @KafkaListener(
      topics = "#{T(com.pyokemon.common.kafka.KafkaTopicConstants).EVENT_STATUS_UPDATED}",
      groupId = "${spring.application.name}",
      containerFactory = "eventkafkaListenerContainerFactory")
  public void handleEventStatusUpdate(EventKafkaDto evtdto) {
    try {
      log.info("이벤트 상태 업데이트 수신: {}", evtdto);
      bookingService.cancel(evtdto);
    } catch (Exception e) {
      log.error("이벤트 상태 업데이트 메시지 처리 중 오류 발생: {}", evtdto, e);
    }
  }

  @KafkaListener(
      topics = "#{T(com.pyokemon.common.kafka.KafkaTopicConstants).EVENT_SCHEDULE_2H_AHEAD}",
      groupId = "${spring.application.name}",
      containerFactory = "eventkafkaListenerContainerFactory")
  public void handleEventSchedule2hAhead(EventKafkaDto dto) {
    try {
      log.info("공연 시작 2시간 전 알림 수신: eventScheduleId={}", dto.getEventScheduleId());
      bookingService.publishConfirmedBookingsForEventSchedule(dto.getEventScheduleId());
    } catch (Exception e) {
      log.error("공연 시작 2시간 전 알림 메시지 처리 중 오류 발생: eventScheduleId={}", dto.getEventScheduleId(), e);
    }
  }
}
