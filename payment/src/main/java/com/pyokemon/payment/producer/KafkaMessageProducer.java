package com.pyokemon.payment.producer;

import org.springframework.stereotype.Service;

import com.pyokemon.common.kafka.KafkaMessageSender;
import com.pyokemon.common.kafka.KafkaTopicConstants;
import com.pyokemon.payment.dto.PaymentKafkaDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KafkaMessageProducer {

  private final KafkaMessageSender kafkaMessageSender;

  public void sendPaymentConfirmed(PaymentKafkaDto dto) {
    kafkaMessageSender.send(KafkaTopicConstants.PAYMENT_STATUS_UPDATED, dto.getPaymentId(), dto);
  }
}
