package com.pyokemon.common.kafka;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class KafkaMessageSender {

  private final KafkaTemplate<String, Object> defaultKafkaTemplate;
  private final Map<String, KafkaTemplate<?, ?>> kafkaTemplates;

  public KafkaMessageSender(
      @Qualifier("kafkaTemplate") KafkaTemplate<String, Object> defaultKafkaTemplate,
      Map<String, KafkaTemplate<?, ?>> kafkaTemplates) {
    this.defaultKafkaTemplate = defaultKafkaTemplate;
    this.kafkaTemplates = kafkaTemplates;
  }

  public <T> void send(String topic, String key, T message) {
    CompletableFuture<SendResult<String, Object>> future =
        defaultKafkaTemplate.send(topic, key, message);

    future.whenComplete((result, ex) -> {
      if (ex == null) {
        log.info("메시지 전송 성공 - topic: {}, key: {}, message: {}, partition: {}, offset: {}", topic,
            key, message, result.getRecordMetadata().partition(),
            result.getRecordMetadata().offset());
      } else {
        log.error("메시지 전송 실패 - topic: {}, key: {}, message: {}", topic, key, message, ex);
      }
    });
  }

  @SuppressWarnings("unchecked")
  public <K, V> void send(String templateName, String topic, K key, V message) {
    KafkaTemplate<K, V> template = (KafkaTemplate<K, V>) kafkaTemplates.get(templateName);
    if (template == null) {
      log.error("템플릿을 찾을 수 없음: {}", templateName);
      return;
    }

    CompletableFuture<SendResult<K, V>> future = template.send(topic, key, message);

    future.whenComplete((result, ex) -> {
      if (ex == null) {
        log.info("메시지 전송 성공 [{}] - topic: {}, key: {}, message: {}, partition: {}, offset: {}",
            templateName, topic, key, message, result.getRecordMetadata().partition(),
            result.getRecordMetadata().offset());
      } else {
        log.error("메시지 전송 실패 [{}] - topic: {}, key: {}, message: {}", templateName, topic, key,
            message, ex);
      }
    });
  }
}
