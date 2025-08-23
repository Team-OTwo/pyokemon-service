package com.pyokemon.did.event.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.stereotype.Service;

import com.pyokemon.common.kafka.KafkaTopicConstants;
import com.pyokemon.did.event.consumer.message.booking.BookingEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaMessageConsumer {


  @KafkaListener(topics = KafkaTopicConstants.EVENT_STATUS_UPDATED,
      properties = {JsonDeserializer.VALUE_DEFAULT_TYPE
          + ":com.pyokemon.did.event.consumer.message.booking.BookingEvent"},
      groupId = "${spring.application.name}")
  void handleBookingEvent(BookingEvent event, Acknowledgment ack) {
    log.info("Received booking event: {}", event);

    try {
      switch (event.getStatus()) {
        // case "BOOKED" -> bookedEventProcessor.process(event);
        // case "CONFIRMED" -> confirmedEventProcessor.process(event);
        default -> log.warn("Unknown booking status: {}", event.getStatus());
      }
      ack.acknowledge();
    } catch (Exception e) {
      log.error("Error processing booking event: {}", event, e);
      // 에러 처리 로직
    }
  }
}
