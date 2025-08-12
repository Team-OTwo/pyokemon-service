package com.pyokemon.payment.service;


import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.pyokemon.payment.dto.PaymentConfirmRequestDto;
import com.pyokemon.payment.dto.PaymentConfirmResponseDto;
import com.pyokemon.payment.dto.PaymentKafkaDto;
import com.pyokemon.payment.producer.KafkaMessageProducer;
import com.pyokemon.payment.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class TossPaymentService {

  private final PaymentRepository paymentRepository;

  private final WebClient tossWebClient;
  private final KafkaMessageProducer kafkaMessageProducer;

  public PaymentConfirmResponseDto confirm(PaymentConfirmRequestDto request) {
    PaymentConfirmResponseDto dto = null;
    boolean markedDone = false;
    try {
      dto = tossWebClient.post().uri("/payments/confirm").bodyValue(request)
          .exchangeToMono(response -> {
            if (response.statusCode().isError()) {
              return response.bodyToMono(String.class)
                  .doOnNext(error -> System.out.println("에러: " + error))
                  .then(Mono.error(new RuntimeException("Toss confirm failed")));
            }
            return response.bodyToMono(PaymentConfirmResponseDto.class);
          }).block();

      paymentRepository.updatePayment(
              request.getOrderId(),
              request.getPaymentKey(),
              "DONE",
          dto.getMethod()

      );
      PaymentKafkaDto kafkaDto =
          new PaymentKafkaDto(dto.getPaymentId(),dto.getBookingId(), dto.getOrderId(), dto.getStatus());
      kafkaMessageProducer.sendPaymentConfirmed(kafkaDto);
      markedDone = true;

    } catch (Exception e) {
      if(!markedDone) {
        paymentRepository.updatePaymentFailed(request.getOrderId(), "FAILED", null);
      }
    }
    return dto;
  }

  public void fail(PaymentConfirmRequestDto request) {
    paymentRepository.updatePaymentFailed(request.getOrderId(), "FAILED", null);
  }

}
