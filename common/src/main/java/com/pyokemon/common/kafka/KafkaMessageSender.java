package com.pyokemon.common.kafka;

import java.util.concurrent.CompletableFuture;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Kafka 메시지 전송을 위한 공통 클래스 모든 서비스에서 동일한 방식으로 Kafka 메시지를 전송할 수 있도록 지원합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaMessageSender {

  private final KafkaTemplate<Long, Object> kafkaTemplate;

  /**
   * 메시지 ID와 함께 Kafka 토픽으로 메시지를 전송합니다.
   * 
   * @param topic 메시지를 전송할 토픽 이름
   * @param id 메시지 ID (Kafka 메시지 키로 사용)
   * @param message 전송할 메시지 객체
   */
  public <T> void send(String topic, Long id, T message) {
    CompletableFuture<SendResult<Long, Object>> future = kafkaTemplate.send(topic, id, message);
    future.thenAccept(result -> {
      log.info("메시지 전송 성공: 토픽={}, 키={}, 메시지={}", topic, id, message);
    }).exceptionally(ex -> {
      log.error("메시지 전송 실패: 토픽={}, 키={}, 메시지={}, 예외={}", topic, id, message, ex.getMessage());
      return null;
    });
  }

  /**
   * Kafka 토픽으로 메시지를 전송합니다.
   * 
   * @param topic 메시지를 전송할 토픽 이름
   * @param message 전송할 메시지 객체
   */
  public <T> void send(String topic, T message) {
    CompletableFuture<SendResult<Long, Object>> future = kafkaTemplate.send(topic, message);
    future.thenAccept(result -> {
      log.info("메시지 전송 성공: 토픽={}, 메시지={}", topic, message);
    }).exceptionally(ex -> {
      log.error("메시지 전송 실패: 토픽={}, 메시지={}, 예외={}", topic, message, ex.getMessage());
      return null;
    });
  }
}
