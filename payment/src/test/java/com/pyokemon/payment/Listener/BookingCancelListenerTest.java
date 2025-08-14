package com.pyokemon.payment.Listener;

import com.pyokemon.payment.service.PaymentCancelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingCancelListenerTest {

    @Mock
    PaymentCancelService paymentCancelService;

    ObjectMapper objectMapper;
    BookingCancelListener listener;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        listener = new BookingCancelListener(objectMapper, paymentCancelService);
    }

    @Test
    void onBookingEvent_callsService_whenStatusCancelRequested() {
        // given
        String payload = "{\"bookingId\":123,\"status\":\"CANCEL_REQUESTED\"}";

        // when
        listener.onBookingEvent(payload);

        // then
        verify(paymentCancelService, times(1))
                .cancelByBookingId(123L, "예약 취소 요청");
        verifyNoMoreInteractions(paymentCancelService);
    }

    @Test
    void onBookingEvent_callsService_whenStatusCanceled() {
        // given
        String payload = "{\"bookingId\":456,\"status\":\"CANCELED\"}";

        // when
        listener.onBookingEvent(payload);

        // then
        verify(paymentCancelService, times(1))
                .cancelByBookingId(456L, "예약 취소 요청");
        verifyNoMoreInteractions(paymentCancelService);
    }

    @Test
    void onBookingEvent_ignoresOtherStatuses() {
        // given
        String payload = "{\"bookingId\":789,\"status\":\"CONFIRMED\"}";

        // when
        listener.onBookingEvent(payload);

        // then
        verifyNoInteractions(paymentCancelService);
    }

    @Test
    void onBookingEvent_handlesBadJsonGracefully() {
        // given
        String bad = "NOT_JSON";

        // when & then: 예외 터지지 않고 내부에서 로그만 찍고 끝나야 함
        assertDoesNotThrow(() -> listener.onBookingEvent(bad));
        verifyNoInteractions(paymentCancelService);
    }
}
