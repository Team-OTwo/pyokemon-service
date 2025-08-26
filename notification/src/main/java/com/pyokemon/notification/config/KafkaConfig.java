package com.pyokemon.notification.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;

import com.pyokemon.common.config.CommonKafkaConfig;
import com.pyokemon.notification.event.consumer.message.notification.NotificationEvent;

@Configuration
public class KafkaConfig {

  @Value("${spring.kafka.bootstrap-servers}")
  private String bootstrapServers;

  @Value("${spring.application.name}")
  private String applicationName;


  @Bean
  public ConsumerFactory<String, NotificationEvent> notificationKafkaDtoConsumerFactory() {
    return CommonKafkaConfig.createConsumerFactory(bootstrapServers, applicationName,
        NotificationEvent.class,
        "com.pyokemon.notification.event.consumer.message.notification.NotificationEvent");
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, NotificationEvent> kafkaListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, NotificationEvent> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(notificationKafkaDtoConsumerFactory());
    return factory;
  }

}
