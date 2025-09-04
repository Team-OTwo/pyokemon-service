package com.pyokemon.payment.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.common.exception.code.PaymentErrorCodes;
import com.pyokemon.payment.dto.PaymentConfirmRequestDto;
import com.pyokemon.payment.dto.PaymentConfirmResponseDto;
import com.pyokemon.payment.dto.PaymentDto;
import com.pyokemon.payment.dto.PaymentInitiateRequestDto;
import com.pyokemon.payment.dto.kafka.PaymentKafkaDto;
import com.pyokemon.payment.entity.Payment;
import com.pyokemon.payment.producer.KafkaMessageProducer;
import com.pyokemon.payment.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

  private final PaymentRepository paymentRepository;
  private final WebClient tossWebClient;
  private final KafkaMessageProducer kafkaMessageProducer;

  // 결제 예약
  public void reserve(PaymentInitiateRequestDto request, Long eventScheduleId) {
    PaymentDto dto = PaymentDto.builder().bookingId(request.getBookingId())
        .orderId(request.getOrderId()).amount(request.getAmount()).method(request.getMethod())
        .status("READY").eventScheduleId(eventScheduleId).build();

    paymentRepository.insertInitiatePayment(dto);
  }

  // 결제 확인 및 성공 처리
  public PaymentConfirmResponseDto confirm(PaymentConfirmRequestDto request) {
    PaymentConfirmResponseDto dto = null;
    boolean markedDone = false;
    try {
      dto = tossWebClient.post().uri("/payments/confirm").bodyValue(request).exchangeToMono(res -> {
        if (res.statusCode().isError()) {
          return res.bodyToMono(String.class).flatMap(body -> {
            log.error("Toss confirm error: {}", body);
            return Mono.error(new BusinessException("Toss confirm failed",
                PaymentErrorCodes.TOSS_CONFIRM_FAILED));
          });
        }
        return res.bodyToMono(PaymentConfirmResponseDto.class);
      }).block();

      paymentRepository.updatePayment(request.getOrderId(), request.getPaymentKey(), "DONE",
          dto.getMethod());

      var p = paymentRepository.findByOrderId(request.getOrderId());
      if (p == null) {
        log.error("Payment {} not found.", request.getOrderId());
        throw new BusinessException("Payment not found.", PaymentErrorCodes.PAYMENT_NOT_FOUND);
      }
      PaymentKafkaDto kafkaDto =
          new PaymentKafkaDto(p.getId(), p.getBookingId(), p.getStatus().name());
      kafkaMessageProducer.sendPaymentConfirmed(kafkaDto);
      markedDone = true;
      return dto;

    } catch (Exception e) {
      log.error("Confirm failed: type={}, msg={}", e.getClass().getName(), e.getMessage(), e);

      if (!markedDone) {
        paymentRepository.updatePaymentFailed(request.getOrderId(), "FAILED", null);
        var p = paymentRepository.findByOrderId(request.getOrderId());
        if (p == null) {
          log.error("Payment {} not found.", request.getOrderId());
          throw new BusinessException("Payment not found.", PaymentErrorCodes.PAYMENT_NOT_FOUND);
        }
        PaymentKafkaDto kafkaDto =
            new PaymentKafkaDto(p.getId(), p.getBookingId(), p.getStatus().name());
        kafkaMessageProducer.sendPaymentConfirmed(kafkaDto);
      }
      throw e;
    }
  }

  // 결제 실패 처리
  @Transactional
  public void fail(PaymentConfirmRequestDto request) {
    paymentRepository.updatePaymentFailed(request.getOrderId(), "FAILED", null);

    var p = paymentRepository.findByOrderId(request.getOrderId());
    if (p == null) {
      log.error("Payment {} not found.", request.getOrderId());
      throw new BusinessException("Payment not found.", PaymentErrorCodes.PAYMENT_NOT_FOUND);
    }
    PaymentKafkaDto kafkaDto =
        new PaymentKafkaDto(p.getId(), p.getBookingId(), p.getStatus().name());
    kafkaMessageProducer.sendPaymentConfirmed(kafkaDto);
  }

  // 결제 취소
  @Transactional
  public void cancelByBookingId(Long bookingId, String reason) {
    Payment p = paymentRepository.findLatestByBookingId(bookingId);
    if (p == null) {
      log.error("Payment {} not found.", bookingId);
      throw new BusinessException("Payment not found.", PaymentErrorCodes.PAYMENT_NOT_FOUND);
    }

    if (p.getStatus() == Payment.PaymentStatus.CANCELED)
      return;

    Map<String, Object> body = new HashMap<>();
    body.put("cancelReason", reason);

    try {
      tossWebClient.post().uri("/payments/{paymentKey}/cancel", p.getPaymentKey())
          .header("Idempotency-Key", "cancel-" + p.getId() + "-" + UUID.randomUUID())
          .contentType(MediaType.APPLICATION_JSON).bodyValue(body).retrieve()
          .onStatus(HttpStatusCode::isError, res -> res.bodyToMono(String.class).flatMap(msg -> {
            log.warn("Toss cancel failed: {}", msg);
            if (msg.contains("\"ALREADY_CANCELED_PAYMENT\"")) {
              return Mono.empty();
            }
            return Mono.error(new BusinessException("Toss cancel failed: " + msg,
                PaymentErrorCodes.TOSS_CONFIRM_FAILED));
          })).toBodilessEntity().block();

    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      log.error("Toss cancel error", e);
      throw new BusinessException("Toss cancel error", PaymentErrorCodes.TOSS_CONFIRM_FAILED);
    }
    paymentRepository.cancelPayment(p.getOrderId(), "CANCELED");

    PaymentKafkaDto kafkaDto =
        new PaymentKafkaDto(p.getId(), p.getBookingId(), p.getStatus().name());
    kafkaMessageProducer.sendPaymentConfirmed(kafkaDto);
  }

  // 결제 만료 처리
  public void expireByBookingId(Long bookingId, String reason) {
    Payment p = paymentRepository.findLatestByBookingId(bookingId);
    if (p == null) {
      log.error("Payment {} not found.", bookingId);
      throw new BusinessException("Payment not found.", PaymentErrorCodes.PAYMENT_NOT_FOUND);
    }

    if (p.getStatus() == Payment.PaymentStatus.EXPIRED)
      return;

    paymentRepository.cancelPayment(p.getOrderId(), "EXPIRED");
  }

  // 결제 확인 및 처리 (Controller 로직을 Service로 이동)
  public PaymentConfirmResponseDto processPaymentConfirm(PaymentConfirmRequestDto request) {
    Payment status = paymentRepository.findByOrderIdAndStatus(request.getOrderId());
    if (status == null || "CANCELED".equals(status.getStatus().name())
        || "FAILED".equals(status.getStatus().name())
        || "EXPIRED".equals(status.getStatus().name())) {
      throw new BusinessException("결제 정보를 찾을 수 없습니다.", PaymentErrorCodes.PAYMENT_NOT_FOUND);
    }

    if ("READY".equals(status.getStatus().name()) && request.getPaymentKey() != null) {
      return confirm(request);
    } else {
      fail(request);
      throw new BusinessException("결제 처리에 실패했습니다.", PaymentErrorCodes.PAYMENT_NOT_FOUND);
    }
  }
}
