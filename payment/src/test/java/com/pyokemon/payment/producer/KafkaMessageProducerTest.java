package com.pyokemon.payment.producer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pyokemon.common.kafka.KafkaMessageSender;
import com.pyokemon.common.kafka.KafkaTopicConstants;
import com.pyokemon.payment.dto.kafka.PaymentKafkaDto;

@ExtendWith(MockitoExtension.class)
class KafkaMessageProducerTest {

  @Mock
  KafkaMessageSender kafkaMessageSender;

  KafkaMessageProducer producer;

  @Captor
  ArgumentCaptor<String> topicCaptor;
  @Captor
  ArgumentCaptor<String> keyCaptor;
  @Captor
  ArgumentCaptor<PaymentKafkaDto> valueCaptor;

  @BeforeEach
  void setUp() {
    producer = new KafkaMessageProducer(kafkaMessageSender);
  }

  @Test
  void sendPaymentConfirmed_sendsDoneEventToExpectedTopic() {
    // given: 결제 완료(DONE) 이벤트 DTO
    PaymentKafkaDto dto = new PaymentKafkaDto(10L, 777L, "DONE");
    // builder가 있다면:
    // PaymentKafkaDto dto = PaymentKafkaDto.builder()
    // .paymentId(10L).bookingId(777L).status("DONE").build();

    // when
    producer.sendPaymentConfirmed(dto);

    // then
    verify(kafkaMessageSender, times(1)).send(eq(KafkaTopicConstants.PAYMENT_STATUS_UPDATED),
        eq(String.valueOf(10L)), eq(dto));

    verifyNoMoreInteractions(kafkaMessageSender);
  }
}
