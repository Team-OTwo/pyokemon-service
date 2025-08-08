package com.pyokemon.payment.service;

import com.pyokemon.payment.dto.PaymentConfirmRequestDto;
import com.pyokemon.payment.dto.PaymentConfirmResponseDto;
import com.pyokemon.payment.repository.PaymentRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TossPaymentServiceTest {

    @Mock
    PaymentRepository paymentRepository;

    @Mock
    WebClient webClient;

    @Mock
    WebClient.RequestBodyUriSpec uriSpec;

    @Mock
    WebClient.RequestBodySpec bodySpec;

    @Mock
    WebClient.RequestHeadersSpec<?> headersSpec;

    TossPaymentService tossPaymentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        tossPaymentService = new TossPaymentService(paymentRepository, webClient);
    }

    @Test
    void confirm_successfulPayment_updatesToDone() {
        // given
        PaymentConfirmRequestDto request = new PaymentConfirmRequestDto();
        request.setOrderId("ORDER123");
        request.setPaymentKey("PAY123");
        request.setAmount(1000);

        PaymentConfirmResponseDto responseDto = new PaymentConfirmResponseDto();
        responseDto.setMethod("카드");

        // mock WebClient 체인
        when(webClient.post()).thenReturn(uriSpec);
        when(uriSpec.uri("/payments/confirm")).thenReturn(bodySpec);
        when(bodySpec.bodyValue(request)).thenReturn(headersSpec);
        when(headersSpec.exchangeToMono(any())).thenAnswer(invocation -> {
            Function<ClientResponse, Mono<PaymentConfirmResponseDto>> func = invocation.getArgument(0);
            ClientResponse mockResponse = mock(ClientResponse.class);
            when(mockResponse.statusCode()).thenReturn(HttpStatus.OK);
            when(mockResponse.bodyToMono(PaymentConfirmResponseDto.class)).thenReturn(Mono.just(responseDto));
            return func.apply(mockResponse);
        });

        // when
        PaymentConfirmResponseDto result = tossPaymentService.confirm(request);

        // then
        assertNotNull(result);
        assertEquals("카드", result.getMethod());
        verify(paymentRepository).updatePayment("ORDER123", "PAY123", "DONE", "카드");
    }

    @Test
    void confirm_failedPayment_updatesToFailed() {
        // given
        PaymentConfirmRequestDto request = new PaymentConfirmRequestDto();
        request.setOrderId("ORDER456");
        request.setPaymentKey("FAILKEY");
        request.setAmount(2000);

        // mock WebClient 체인
        when(webClient.post()).thenReturn(uriSpec);
        when(uriSpec.uri("/payments/confirm")).thenReturn(bodySpec);
        when(bodySpec.bodyValue(request)).thenReturn(headersSpec);
        when(headersSpec.exchangeToMono(any())).thenAnswer(invocation -> {
            Function<ClientResponse, Mono<PaymentConfirmResponseDto>> func = invocation.getArgument(0);
            ClientResponse mockResponse = mock(ClientResponse.class);
            when(mockResponse.statusCode()).thenReturn(HttpStatus.BAD_REQUEST);
            when(mockResponse.bodyToMono(String.class)).thenReturn(Mono.just("에러 응답"));
            return func.apply(mockResponse);
        });

        // when
        PaymentConfirmResponseDto result = tossPaymentService.confirm(request);

        // then
        assertNull(result); // 예외 터졌지만 try-catch로 null 반환
        verify(paymentRepository).updatePaymentFailed("ORDER456", "FAILED", null);
    }

    @Test
    void fail_updatesToFailed() {
        // given
        PaymentConfirmRequestDto request = new PaymentConfirmRequestDto();
        request.setOrderId("ORDER789");

        // when
        tossPaymentService.fail(request);

        // then
        verify(paymentRepository).updatePaymentFailed("ORDER789", "FAILED", null);
    }
}
