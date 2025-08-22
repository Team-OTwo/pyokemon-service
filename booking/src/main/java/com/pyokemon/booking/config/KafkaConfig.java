package com.pyokemon.booking.config;

import com.pyokemon.booking.dto.kafka.EventKafkaDto;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import com.pyokemon.booking.dto.kafka.PaymentKafkaDto;
import com.pyokemon.common.config.CommonKafkaConfig;

@Configuration
public class KafkaConfig {

  @Value("${spring.kafka.bootstrap-servers}")
  private String bootstrapServers;

  @Value("${spring.application.name}")
  private String applicationName;

  @Bean
  public ProducerFactory<String, Object> kafkaProducerFactory() {
    return CommonKafkaConfig.createProducerFactory(bootstrapServers);
  }

  @Bean
  public ConsumerFactory<String, PaymentKafkaDto> paymentKafkaDtoConsumerFactory() {
    return CommonKafkaConfig.createConsumerFactory(bootstrapServers, applicationName,
        PaymentKafkaDto.class, "com.pyokemon.booking.dto.kafka", "com.pyokemon.payment.dto.kafka");
  }

  @Bean
  public ConsumerFactory<String, EventKafkaDto> eventKafkaDtoConsumerFactory() {
    return CommonKafkaConfig.createConsumerFactory(bootstrapServers, applicationName,
            EventKafkaDto.class, "com.pyokemon.booking.dto.kafka", "com.pyokemon.event.dto.kafka");
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, PaymentKafkaDto> kafkaListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, PaymentKafkaDto> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(paymentKafkaDtoConsumerFactory());
    return factory;
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, EventKafkaDto> eventkafkaListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, EventKafkaDto> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(eventKafkaDtoConsumerFactory());
    return factory;
  }

  @Bean("kafkaTemplate")
  @Primary
  public KafkaTemplate<String, Object> kafkaTemplate() {
    return new KafkaTemplate<>(kafkaProducerFactory());
  }
}
