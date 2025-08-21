package com.pyokemon.event.config;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pyokemon.common.config.CommonKafkaConfig;

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



//  @Bean
//  public ConsumerFactory<String, BookingEventDto> bookingEventDtoConsumerFactory() {
//    return CommonKafkaConfig.createConsumerFactory(bootstrapServers, applicationName,
//            BookingEventDto.class, "com.pyokemon.booking.dto.kafka", "com.pyokemon.payment.dto.kafka");
//  }
//
//  @Bean
//  public ConcurrentKafkaListenerContainerFactory<String, BookingEventDto> kafkaListenerContainerFactory() {
//    ConcurrentKafkaListenerContainerFactory<String, BookingEventDto> factory =
//            new ConcurrentKafkaListenerContainerFactory<>();
//    factory.setConsumerFactory(bookingEventDtoConsumerFactory());
//    factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
//    return factory;
//  }


  @Bean("kafkaTemplate")
  public KafkaTemplate<String, Object> kafkaTemplate() {
    return new KafkaTemplate<>(kafkaProducerFactory());
  }

}
