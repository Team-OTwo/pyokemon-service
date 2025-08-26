package com.pyokemon.notification.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;

import com.pyokemon.common.config.CommonKafkaConfig;
import com.pyokemon.notification.event.consumer.message.booking.BookingEvent;

@Configuration
public class KafkaConfig {

  @Value("${spring.kafka.bootstrap-servers}")
  private String bootstrapServers;

  @Value("${spring.application.name}")
  private String applicationName;


  @Bean
  public ConsumerFactory<String, BookingEvent> notificationKafkaDtoConsumerFactory() {
    return CommonKafkaConfig.createConsumerFactory(bootstrapServers, applicationName,
        BookingEvent.class,
        "com.pyokemon.notification.event.consumer.message.notification.NotificationEvent");
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, BookingEvent> kafkaListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, BookingEvent> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(notificationKafkaDtoConsumerFactory());
    return factory;
  }

}
