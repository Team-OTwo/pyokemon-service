package com.pyokemon.common.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DLQListener {

  // Payment Status Updated DLQ
  @KafkaListener(
      topics = KafkaTopicConstants.PAYMENT_STATUS_UPDATED_DLQ,
      groupId = "${spring.application.name}-dlq")
  public void handlePaymentStatusUpdatedDLQ(
      @Payload DLQMessage<?> dlqMessage,
      @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
      @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
      @Header(KafkaHeaders.OFFSET) long offset,
      Acknowledgment ack) {
    
    processDLQMessage("PAYMENT_STATUS_UPDATED", dlqMessage, topic, partition, offset, ack);
  }

  // Booking Status Updated DLQ
  @KafkaListener(
      topics = KafkaTopicConstants.BOOKING_STATUS_UPDATED_DLQ,
      groupId = "${spring.application.name}-dlq")
  public void handleBookingStatusUpdatedDLQ(
      @Payload DLQMessage<?> dlqMessage,
      @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
      @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
      @Header(KafkaHeaders.OFFSET) long offset,
      Acknowledgment ack) {
    
    processDLQMessage("BOOKING_STATUS_UPDATED", dlqMessage, topic, partition, offset, ack);
  }

  // Event Status Updated DLQ
  @KafkaListener(
      topics = KafkaTopicConstants.EVENT_STATUS_UPDATED_DLQ,
      groupId = "${spring.application.name}-dlq")
  public void handleEventStatusUpdatedDLQ(
      @Payload DLQMessage<?> dlqMessage,
      @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
      @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
      @Header(KafkaHeaders.OFFSET) long offset,
      Acknowledgment ack) {
    
    processDLQMessage("EVENT_STATUS_UPDATED", dlqMessage, topic, partition, offset, ack);
  }

  private void processDLQMessage(String dlqType, DLQMessage<?> dlqMessage, String topic, 
                               int partition, long offset, Acknowledgment ack) {
    try {
      log.error("=== DLQ 메시지 수신 [{}] === topic={}, partition={}, offset={}", 
          dlqType, topic, partition, offset);
      log.error("원본 토픽: {}", dlqMessage.getOriginalTopic());
      log.error("원본 키: {}", dlqMessage.getOriginalKey());
      log.error("원본 메시지: {}", dlqMessage.getOriginalMessage());
      log.error("에러 메시지: {}", dlqMessage.getErrorMessage());
      log.error("타임스탬프: {}", dlqMessage.getTimestamp());
      log.error("=== DLQ 메시지 끝 [{}] ===", dlqType);
      
      ack.acknowledge();
      
    } catch (Exception e) {
      log.error("DLQ 메시지 처리 중 오류 발생 [{}]: {}", dlqType, e.getMessage(), e);
      ack.acknowledge();
    }
  }
}
