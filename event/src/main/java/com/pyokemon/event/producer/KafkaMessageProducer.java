package com.pyokemon.event.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.pyokemon.common.kafka.KafkaMessageSender;
import com.pyokemon.common.kafka.KafkaTopicConstants;
import com.pyokemon.event.dto.kafka.EventKafkaDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaMessageProducer {

  private final KafkaMessageSender kafkaMessageSender;
  private final KafkaTemplate<String, Object> kafkaTemplate;

  public void sendEventConfirmed(EventKafkaDto dto) {
    kafkaMessageSender.send(KafkaTopicConstants.EVENT_STATUS_UPDATED,
        String.valueOf(dto.getEventScheduleId()), dto);
  }

  public void sendTwoHoursBeforeEvent(Long eventScheduleId) {
    EventKafkaDto dto =
        EventKafkaDto.builder().eventScheduleId(eventScheduleId).status("2H_AHEAD").build();
    kafkaMessageSender.send(KafkaTopicConstants.EVENT_SCHEDULE_2H_AHEAD,
        String.valueOf(eventScheduleId), dto);
  }


}
