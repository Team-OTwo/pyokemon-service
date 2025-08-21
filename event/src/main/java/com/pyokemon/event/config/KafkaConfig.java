package com.pyokemon.event.config;

import com.pyokemon.common.config.CommonKafkaConfig;
import com.pyokemon.event.dto.kafka.BookingEventDto;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class KafkaConfig {

  @Value("${spring.kafka.bootstrap-servers}")
  private String bootstrapServers;

  @Value("${spring.application.name}")
  private String applicationName;

  @Autowired
  private ObjectMapper objectMapper;

  @Bean
  public ProducerFactory<String, Object> kafkaProducerFactory() {
    return CommonKafkaConfig.createProducerFactory(bootstrapServers);
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<Long, Object> bookingEventListenerContainerFactory() {
    return CommonKafkaConfig.longObjectKafkaListenerContainerFactory(bootstrapServers, applicationName);
  }

  @Bean("kafkaTemplate")
  public KafkaTemplate<String, Object> kafkaTemplate() {
    return new KafkaTemplate<>(kafkaProducerFactory());
  }

}
