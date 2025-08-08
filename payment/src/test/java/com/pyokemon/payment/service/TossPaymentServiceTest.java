package com.pyokemon.payment.service;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import com.pyokemon.payment.dto.PaymentConfirmRequestDto;
import com.pyokemon.payment.dto.PaymentConfirmResponseDto;
import com.pyokemon.payment.repository.PaymentRepository;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import reactor.core.publisher.Mono;

class TossPaymentServiceTest {

  private MockWebServer mockWebServer;
  private TossPaymentService tossPaymentService;
  private PaymentRepository paymentRepository;

  @BeforeEach
  void setUp() throws Exception {
    mockWebServer = new MockWebServer();
    mockWebServer.start();

    WebClient webClient = WebClient.builder().baseUrl(mockWebServer.url("/").toString())
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).build();

    paymentRepository = mock(PaymentRepository.class);
    tossPaymentService = new TossPaymentService(paymentRepository, webClient);
  }

  @AfterEach
  void tearDown() throws Exception {
    mockWebServer.shutdown();
  }

  @Test
  void confirm_successfulPayment_updatesToDone() {
    // given
    String jsonResponse = "{\"method\":\"카드\"}";

    mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(jsonResponse)
        .addHeader("Content-Type", "application/json"));

    PaymentConfirmRequestDto request = new PaymentConfirmRequestDto();
    request.setOrderId("ORDER123");
    request.setPaymentKey("KEY123");
    request.setAmount(1000);

    // when
    PaymentConfirmResponseDto response = tossPaymentService.confirm(request);

    // then
    verify(paymentRepository).updatePayment(eq("ORDER123"), eq("KEY123"), eq("DONE"), eq("카드"));
    Assertions.assertNotNull(response);
    Assertions.assertEquals("카드", response.getMethod());
  }

  @Test
  void confirm_failedPayment_updatesToFailed() {
    // given
    mockWebServer.enqueue(new MockResponse().setResponseCode(400).setBody("{\"message\":\"error\"}")
        .addHeader("Content-Type", "application/json"));

    PaymentConfirmRequestDto request = new PaymentConfirmRequestDto();
    request.setOrderId("ORDER_FAIL");
    request.setPaymentKey("KEY_FAIL");
    request.setAmount(1000);

    // when
    PaymentConfirmResponseDto response = tossPaymentService.confirm(request);

    // then
    verify(paymentRepository).updatePaymentFailed("ORDER_FAIL", "FAILED", null);
    Assertions.assertNull(response);
  }
}
