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
          + ":com.pyokemon.did.event.consumer.message.booking.BookingEvent"})
  void handleBookingEvent(BookingEvent event, Acknowledgment ack) {
    log.info("Received booking event: {}", event);

    if ("BOOKED".equals(event.getStatus())) {
      // TODO: BOOKED 상태 처리 로직
      log.info("Processing BOOKED event for booking: {}", event.getBookingId());
    } else if ("CONFIRMED".equals(event.getStatus())) {
      // TODO: CONFIRMED 상태 처리 로직
      log.info("Processing CONFIRMED event for booking: {}", event.getBookingId());
    }

    ack.acknowledge();
  }
}
