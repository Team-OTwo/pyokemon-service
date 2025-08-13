package com.pyokemon.common.config;

import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

/**
 * 공통 Kafka 설정 클래스 각 서비스 모듈에서 공통으로 사용할 수 있는 Kafka 관련 설정을 제공합니다.
 */
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(value = "spring.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaCommonConfig {

  private final ObjectMapper objectMapper;
  private final KafkaProperties kafkaProperties;

  /**
   * Kafka Producer Factory 빈 생성
   * 
   * @param <K> 메시지 키 타입
   * @param <V> 메시지 값 타입
   * @return ProducerFactory 인스턴스
   */
  @Bean
  @SuppressWarnings("removal")
  public <K, V> ProducerFactory<K, V> producerFactory() {
    Map<String, Object> producerProperties = kafkaProperties.buildProducerProperties();

    // JSON 직렬화에 ObjectMapper 사용
    producerProperties.put(JsonSerializer.TYPE_MAPPINGS, "true");

    return new DefaultKafkaProducerFactory<>(producerProperties);
  }

  /**
   * Long 키와 Object 값을 위한 KafkaTemplate 빈 생성
   * 
   * @param <V> 메시지 값 타입
   * @return KafkaTemplate 인스턴스
   */
  @Bean
  public <V> KafkaTemplate<Long, V> kafkaTemplate() {
    return new KafkaTemplate<>(producerFactory());
  }
}
