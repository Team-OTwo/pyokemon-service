package com.pyokemon.did.event.consumer;

import com.pyokemon.did.event.consumer.message.booking.BookingEventDto;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.stereotype.Service;

import com.pyokemon.common.kafka.KafkaTopicConstants;
import com.pyokemon.did.service.AcaPyConnectionService;
import com.pyokemon.did.service.IssuedVcService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaMessageConsumer {
  private final AcaPyConnectionService acaPyConnectionService;
  private final IssuedVcService issuedVcService;

  @KafkaListener(topics = KafkaTopicConstants.BOOKING_STATUS_UPDATED,
      groupId = "${spring.application.name}",
  containerFactory = "kafkaListenerContainerFactory")
  void handleBookingEvent(BookingEventDto event) {
    log.info("Received booking event: {}", event);

    try {
      if ("BOOKED".equals(event.getStatus())) {
        acaPyConnectionService.createAcaPyConnection(event.getTenantId(), event.getAccountId());
      }
      if ("CONFIRMED".equals(event.getStatus())) {
        issuedVcService.issueCredential(event);
      }

    } catch (Exception e) {
      log.error("Error processing booking event: {}", event, e);
      // 에러 처리 로직
    }

  }
}
