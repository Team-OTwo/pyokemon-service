package com.pyokemon.payment.service;


import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.common.exception.code.PaymentErrorCodes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import com.pyokemon.payment.dto.PaymentConfirmRequestDto;
import com.pyokemon.payment.dto.PaymentConfirmResponseDto;
import com.pyokemon.payment.dto.PaymentKafkaDto;
import com.pyokemon.payment.producer.KafkaMessageProducer;
import com.pyokemon.payment.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.web.servlet.View;
import reactor.core.publisher.Mono;
@Slf4j
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
          .exchangeToMono(res -> {
            if (res.statusCode().isError()) {
              final HttpStatusCode s = res.statusCode();
              return res.bodyToMono(String.class).flatMap(body -> {
                log.error("Toss confirm error: {}", body);
                  return Mono.error(new BusinessException("Toss confirm failed", PaymentErrorCodes.TOSS_CONFIRM_FAILED));
              });
            }
            return res.bodyToMono(PaymentConfirmResponseDto.class);
          }).block();

      paymentRepository.updatePayment(request.getOrderId(), request.getPaymentKey(), "DONE",
          dto.getMethod()

      );
      var p = paymentRepository.selectByOrderId(request.getOrderId());
      PaymentKafkaDto kafkaDto =
          new PaymentKafkaDto(p.getPaymentId(), p.getBookingId(), p.getStatus());
      kafkaMessageProducer.sendPaymentConfirmed(kafkaDto);
      markedDone = true;
      return dto;

    } catch (Exception e) {
      if (!markedDone) {
        paymentRepository.updatePaymentFailed(request.getOrderId(), "FAILED", null);
        var p = paymentRepository.selectByOrderId(request.getOrderId());
        PaymentKafkaDto kafkaDto =
            new PaymentKafkaDto(p.getPaymentId(), p.getBookingId(), p.getStatus());
        kafkaMessageProducer.sendPaymentConfirmed(kafkaDto);
      }
      throw e;
    }

  }
  
  @Transactional
  public void fail(PaymentConfirmRequestDto request) {
    paymentRepository.updatePaymentFailed(request.getOrderId(), "FAILED", null);

    var p = paymentRepository.selectByOrderId(request.getOrderId());
    PaymentKafkaDto kafkaDto =
        new PaymentKafkaDto(p.getPaymentId(), p.getBookingId(), p.getStatus());
    kafkaMessageProducer.sendPaymentConfirmed(kafkaDto);
  }

}
