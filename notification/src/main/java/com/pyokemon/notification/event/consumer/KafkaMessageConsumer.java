package com.pyokemon.notification.event.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.stereotype.Service;

import com.pyokemon.common.kafka.KafkaTopicConstants;
import com.pyokemon.notification.event.consumer.message.notification.NotificationEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaMessageConsumer {

  @KafkaListener(topics = KafkaTopicConstants.NOTIFICATION_REQUEST,
      properties = {JsonDeserializer.VALUE_DEFAULT_TYPE
          + ":com.pyokemon.notification.event.consumer.message.notification.NotificationEvent"},
      groupId = "notification-service")
  void handleNotificationEvent(NotificationEvent notificationEvent, Acknowledgment ack) {
    log.info("Received notification event {}", notificationEvent);
  }
}
