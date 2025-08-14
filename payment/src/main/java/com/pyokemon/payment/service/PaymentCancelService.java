package com.pyokemon.payment.service;


import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.common.exception.code.PaymentErrorCodes;
import com.pyokemon.payment.dto.kafka.PaymentKafkaDto;
import com.pyokemon.payment.entity.Payment;
import com.pyokemon.payment.producer.KafkaMessageProducer;
import com.pyokemon.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentCancelService {

    private final PaymentRepository paymentRepository;
    private final KafkaMessageProducer kafkaMessageProducer;
    @Qualifier("tossWebClient")
    private final WebClient tossWebClient;


    @Transactional
    public void cancelByBookingId(Long bookingId, String reason) {
        Payment p = paymentRepository.selectLatestByBookingId(bookingId);
        if (p == null) {
            log.error("Payment {} not found.", p.getBookingId());
            throw new BusinessException(
                    "Payment not found.", PaymentErrorCodes.PAYMENT_NOT_FOUND
            );
        }

        if (p.getStatus() == Payment.PaymentStatus.CANCELED) return;

        Map<String, Object> body = new HashMap<>();
        body.put("cancelReason", reason);

        try {
            tossWebClient.post()
                    .uri("/payments/{paymentKey}/cancel", p.getPaymentKey())
                    .header("Idempotency-Key", "cancel-" + p.getPaymentId() + "-" + UUID.randomUUID())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, res -> res.bodyToMono(String.class).flatMap(msg -> {
                        log.warn("Toss cancel failed: {}", msg);
                        if (msg.contains("\"ALREADY_CANCELED_PAYMENT\"")) {
                            return Mono.empty();
                        }
                        return Mono.error(new BusinessException("Toss cancel failed: " + msg,
                                PaymentErrorCodes.TOSS_CONFIRM_FAILED));
                    }))
                    .toBodilessEntity()
                    .block();

        } catch (BusinessException e){
            throw e;
        } catch (Exception e) {
            log.error("Toss cancel error", e);
            throw new BusinessException("Toss cancel error", PaymentErrorCodes.TOSS_CONFIRM_FAILED);
        }
        paymentRepository.cancelPayment(
                p.getOrderId(),
                "CANCELED"

        );

        PaymentKafkaDto kafkaDto = new PaymentKafkaDto(p.getPaymentId(), p.getBookingId(), p.getStatus().name());
        kafkaMessageProducer.sendPaymentConfirmed(kafkaDto);

    }

}


