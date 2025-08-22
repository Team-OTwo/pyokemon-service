package com.pyokemon.common.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component("kafkaErrorHandler")
public class KafkaErrorHandler implements CommonErrorHandler {

  @Override
  public boolean handleOne(Exception thrownException, ConsumerRecord<?, ?> record,
      Consumer<?, ?> consumer, MessageListenerContainer container) {
    log.error("Kafka 메시지 처리 중 오류 발생: topic={}, partition={}, offset={}, key={}, error={}",
        record.topic(), record.partition(), record.offset(), record.key(),
        thrownException.getMessage(), thrownException);

    if (thrownException instanceof IllegalArgumentException) {
      log.warn("검증 실패로 인한 메시지 무시: {}", thrownException.getMessage());
      return true;
    }

    log.error("메시지 처리 실패로 재시도 예정: {}", thrownException.getMessage());
    return false;
  }

  @Override
  public void handleOtherException(Exception thrownException, Consumer<?, ?> consumer,
      MessageListenerContainer container, boolean batchListener) {
    log.error("Kafka 리스너 컨테이너 오류: {}", thrownException.getMessage(), thrownException);
  }
}
