// src/test/java/com/pyokemon/payment/service/PaymentServiceTest.java
package com.pyokemon.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.payment.dto.PaymentConfirmRequestDto;
import com.pyokemon.payment.dto.PaymentConfirmResponseDto;
import com.pyokemon.payment.dto.kafka.PaymentKafkaDto;
import com.pyokemon.payment.entity.Payment;
import com.pyokemon.payment.entity.Payment.PaymentStatus;
import com.pyokemon.payment.producer.KafkaMessageProducer;
import com.pyokemon.payment.repository.PaymentRepository;

import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

  @Mock
  private PaymentRepository paymentRepository;

  @Mock
  private WebClient tossWebClient;

  @Mock
  private KafkaMessageProducer kafkaMessageProducer;

  @InjectMocks
  private PaymentService paymentService;

  @Test
  @DisplayName("결제 확인 성공 테스트")
  void confirm_Success() {
    // given
    PaymentConfirmRequestDto request = new PaymentConfirmRequestDto();
    request.setOrderId("ORDER_123");
    request.setPaymentKey("PAYMENT_KEY_123");

    PaymentConfirmResponseDto response = new PaymentConfirmResponseDto();
    response.setMethod("카드");

    Payment persisted =
        Payment.builder().bookingId(100L).bookingId(200L).status(PaymentStatus.DONE).build();

    when(paymentRepository.findByOrderId("ORDER_123")).thenReturn(persisted);

    // when
    PaymentConfirmResponseDto result = paymentService.confirm(request);

    // then
    verify(paymentRepository).updatePayment(eq("ORDER_123"), eq("PAYMENT_KEY_123"), eq("DONE"),
        anyString());
    verify(paymentRepository).findByOrderId("ORDER_123");

    ArgumentCaptor<PaymentKafkaDto> captor = ArgumentCaptor.forClass(PaymentKafkaDto.class);
    verify(kafkaMessageProducer, times(1)).sendPaymentConfirmed(captor.capture());

    PaymentKafkaDto sent = captor.getValue();
    assertThat(sent.getPaymentId()).isEqualTo(100L);
    assertThat(sent.getBookingId()).isEqualTo(200L);
    assertThat(sent.getStatus()).isEqualTo("DONE");
  }

  @Test
  @DisplayName("결제 실패 처리 테스트")
  void fail_Success() {
    // given
    PaymentConfirmRequestDto request = new PaymentConfirmRequestDto();
    request.setOrderId("ORDER_999");

    Payment failedRow =
        Payment.builder().bookingId(999L).bookingId(888L).status(PaymentStatus.FAILED).build();

    when(paymentRepository.findByOrderId("ORDER_999")).thenReturn(failedRow);

    // when
    paymentService.fail(request);

    // then
    verify(paymentRepository).updatePaymentFailed(eq("ORDER_999"), eq("FAILED"), isNull());
    verify(paymentRepository).findByOrderId("ORDER_999");

    ArgumentCaptor<PaymentKafkaDto> captor = ArgumentCaptor.forClass(PaymentKafkaDto.class);
    verify(kafkaMessageProducer, times(1)).sendPaymentConfirmed(captor.capture());

    PaymentKafkaDto sent = captor.getValue();
    assertThat(sent.getPaymentId()).isEqualTo(999L);
    assertThat(sent.getBookingId()).isEqualTo(888L);
    assertThat(sent.getStatus()).isEqualTo("FAILED");
  }

  @Test
  @DisplayName("결제 확인 처리 - 성공 케이스")
  void processPaymentConfirm_Success() {
    // given
    PaymentConfirmRequestDto request = new PaymentConfirmRequestDto();
    request.setOrderId("ORDER_123");
    request.setPaymentKey("PAYMENT_KEY_123");

    Payment readyPayment =
        Payment.builder().bookingId(100L).bookingId(200L).status(PaymentStatus.READY).build();

    when(paymentRepository.findByOrderIdAndStatus("ORDER_123")).thenReturn(readyPayment);
    when(paymentRepository.findByOrderId("ORDER_123")).thenReturn(readyPayment);

    // when
    PaymentConfirmResponseDto result = paymentService.processPaymentConfirm(request);

    // then
    verify(paymentRepository).findByOrderIdAndStatus("ORDER_123");
    // confirm 메서드가 호출되는지 확인
    verify(paymentRepository).updatePayment(eq("ORDER_123"), eq("PAYMENT_KEY_123"), eq("DONE"),
        anyString());
  }

  @Test
  @DisplayName("결제 확인 처리 - 결제 정보 없음")
  void processPaymentConfirm_PaymentNotFound() {
    // given
    PaymentConfirmRequestDto request = new PaymentConfirmRequestDto();
    request.setOrderId("ORDER_NOT_FOUND");

    when(paymentRepository.findByOrderIdAndStatus("ORDER_NOT_FOUND")).thenReturn(null);

    // when & then
    assertThrows(BusinessException.class, () -> paymentService.processPaymentConfirm(request));
    verify(paymentRepository).findByOrderIdAndStatus("ORDER_NOT_FOUND");
  }
}
