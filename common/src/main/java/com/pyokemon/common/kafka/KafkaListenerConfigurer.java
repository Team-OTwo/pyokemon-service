package com.pyokemon.common.kafka;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import lombok.RequiredArgsConstructor;

/**
 * Kafka Consumer 공통 설정 클래스 각 서비스 모듈에서 동일한 설정으로 Kafka Consumer를 사용할 수 있도록 합니다.
 */
@EnableKafka
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(value = "spring.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaListenerConfigurer {

  private final KafkaProperties kafkaProperties;

  /**
   * Kafka Consumer Factory 빈 생성
   * 
   * @param <K> 메시지 키 타입
   * @param <V> 메시지 값 타입
   * @return ConsumerFactory 인스턴스
   */
  @Bean
  @SuppressWarnings("removal")
  public <K, V> ConsumerFactory<K, V> consumerFactory() {
    Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties());
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
    props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);

    return new DefaultKafkaConsumerFactory<>(props);
  }

  /**
   * KafkaListenerContainerFactory 빈 생성
   * 
   * @param <K> 메시지 키 타입
   * @param <V> 메시지 값 타입
   * @return ConcurrentKafkaListenerContainerFactory 인스턴스
   */
  @Bean
  public <K, V> ConcurrentKafkaListenerContainerFactory<K, V> kafkaListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<K, V> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory());
    factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
    return factory;
  }
}
