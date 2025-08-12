package com.pyokemon.payment.service;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.web.reactive.function.client.WebClient;

import com.pyokemon.payment.dto.PaymentConfirmRequestDto;
import com.pyokemon.payment.dto.PaymentConfirmResponseDto;
import com.pyokemon.payment.dto.PaymentKafkaDto;
import com.pyokemon.payment.producer.KafkaMessageProducer;
import com.pyokemon.payment.repository.PaymentRepository;

import reactor.core.publisher.Mono;

class TossPaymentServiceTest {

  @Mock
  private PaymentRepository paymentRepository;

  @Mock
  private KafkaMessageProducer kafkaMessageProducer;

  @Mock
  private WebClient tossWebClient;

  @Mock
  private WebClient.RequestBodyUriSpec requestBodyUriSpec;

  @Mock
  private WebClient.RequestBodySpec requestBodySpec;

  @Mock
  private WebClient.RequestHeadersSpec requestHeadersSpec;

  @Mock
  private WebClient.ResponseSpec responseSpec;

  @InjectMocks
  private TossPaymentService tossPaymentService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void confirm_success() {
    // given
    PaymentConfirmRequestDto request = new PaymentConfirmRequestDto();
    request.setOrderId("order-123");
    request.setPaymentKey("pay-key-123");

    PaymentConfirmResponseDto responseDto = new PaymentConfirmResponseDto();
    responseDto.setMethod("카드");
    responseDto.setPaymentId(100L);
    responseDto.setStatus("DONE");

    // WebClient mock 체인
    when(tossWebClient.post()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.uri("/payments/confirm")).thenReturn(requestBodySpec);
    when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.exchangeToMono(any())).thenAnswer(invocation -> {
      var func = invocation.getArgument(0);
      return (Mono<PaymentConfirmResponseDto>) Mono.just(responseDto);
    });

    // when
    PaymentConfirmResponseDto result = tossPaymentService.confirm(request);

    // then
    verify(paymentRepository).updatePayment(eq("order-123"), eq("pay-key-123"), eq("DONE"),
        eq("카드"));
    verify(kafkaMessageProducer).sendPaymentConfirmed(any(PaymentKafkaDto.class));
    verifyNoMoreInteractions(paymentRepository, kafkaMessageProducer);

    // 결과 값 검증
    assert result != null;
    assert result.getMethod().equals("카드");
  }

  @Test
  void confirm_fail() {
    // given
    PaymentConfirmRequestDto request = new PaymentConfirmRequestDto();
    request.setOrderId("order-123");
    request.setPaymentKey("pay-key-123");

    // WebClient mock에서 예외 발생
    when(tossWebClient.post()).thenThrow(new RuntimeException("Toss API Error"));

    // when
    tossPaymentService.confirm(request);

    // then
    verify(paymentRepository).updatePaymentFailed(eq("order-123"), eq("FAILED"), isNull());
    verifyNoInteractions(kafkaMessageProducer);
  }
}
