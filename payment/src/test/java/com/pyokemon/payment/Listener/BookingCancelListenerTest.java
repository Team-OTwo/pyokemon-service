package com.pyokemon.payment.Listener;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pyokemon.payment.service.PaymentCancelService;

@ExtendWith(MockitoExtension.class)
class BookingCancelListenerTest {

  @Mock
  PaymentCancelService paymentCancelService;

  ObjectMapper objectMapper;
  BookingCancelListener listener;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    listener = new BookingCancelListener(paymentCancelService);
  }

  @Test
  void onBookingEvent_callsService_whenStatusCancelRequested() throws JsonProcessingException {
    // given
    Map<String, Object> message = new HashMap<>();
    message.put("bookingId", 123);
    message.put("status", "CANCEL_REQUESTED");

    // when
    listener.onBookingEvent(message);

    // then
    verify(paymentCancelService, times(1)).cancelByBookingId(123L, "예약 취소 요청");
    verifyNoMoreInteractions(paymentCancelService);
  }

  @Test
  void onBookingEvent_callsService_whenStatusCanceled() throws JsonProcessingException {
    // given
    Map<String, Object> message = new HashMap<>();
    message.put("bookingId", 456);
    message.put("status", "CANCELED");

    // when
    listener.onBookingEvent(message);

    // then
    verify(paymentCancelService, times(1)).cancelByBookingId(456L, "예약 취소 요청");
    verifyNoMoreInteractions(paymentCancelService);
  }

  @Test
  void onBookingEvent_ignoresOtherStatuses() throws JsonProcessingException {
    // given
    Map<String, Object> message = new HashMap<>();
    message.put("bookingId", 789);
    message.put("status", "CONFIRMED");

    // when
    listener.onBookingEvent(message);

    // then
    verifyNoInteractions(paymentCancelService);
  }

  @Test
  void onBookingEvent_handlesBadMessageGracefully() {
    // given
    Map<String, Object> badMessage = new HashMap<>();
    // bookingId나 status 필드가 없는 메시지

    // when & then: 예외 터지지 않고 내부에서 로그만 찍고 끝나야 함
    assertDoesNotThrow(() -> listener.onBookingEvent(badMessage));
    verifyNoInteractions(paymentCancelService);
  }
}
