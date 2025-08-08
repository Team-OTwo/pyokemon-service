package com.pyokemon.payment.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.pyokemon.payment.dto.PaymentConfirmRequestDto;
import com.pyokemon.payment.dto.PaymentConfirmResponseDto;
import com.pyokemon.payment.repository.PaymentRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class TossPaymentService {

    private final PaymentRepository paymentRepository;
    @Value("${toss.toss-secret-key}")
    private String secretKey;

    private WebClient webClient;

    @PostConstruct
    public void init() {
        String encoded = Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
        this.webClient = WebClient.builder()
                .baseUrl("https://api.tosspayments.com/v1")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + encoded)
                .build();
    }

    public PaymentConfirmResponseDto confirm(PaymentConfirmRequestDto request) {
        PaymentConfirmResponseDto dto = null;
        try {
            dto = webClient.post()
                    .uri("/payments/confirm")
                    .bodyValue(request)
                    .exchangeToMono(response -> {
                        if (response.statusCode().isError()) {
                            return response.bodyToMono(String.class)
                                    .doOnNext(error -> System.out.println("에러: " + error))
                                    .then(Mono.error(new RuntimeException("Toss confirm failed")));
                        }
                        return response.bodyToMono(PaymentConfirmResponseDto.class);
                    })
                    .block();

            paymentRepository.updatePayment(
                    request.getOrderId(),
                    request.getPaymentKey(),
                    "DONE",
                    dto.getMethod()

            );
        } catch (Exception e) {
            paymentRepository.updatePaymentFailed(
                    request.getOrderId(),
                    "FAILED",
                    null
            );
        }
        return dto;
    }

    public void fail(PaymentConfirmRequestDto request) {
        paymentRepository.updatePaymentFailed(
                request.getOrderId(),
                "FAILED",
                null
        );
    }

}
