package com.pyokemon.booking.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pyokemon.booking.dto.kafka.PaymentEventDto;
import com.pyokemon.booking.entity.Booking;
import com.pyokemon.booking.service.BookingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentEventListenerTest {

    @Mock
    private BookingService bookingService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private PaymentEventListener paymentEventListener;

    @Test
    void handlePaymentStatusUpdate_Done_ShouldUpdateToBooked() throws Exception {
        String message = "{\"payment_id\":1,\"booking_id\":1,\"status\":\"DONE\"}";
        PaymentEventDto paymentEvent = new PaymentEventDto(1L, 1L, "DONE");

        when(objectMapper.readValue(message, PaymentEventDto.class)).thenReturn(paymentEvent);

        paymentEventListener.handlePaymentStatusUpdate(message);

        verify(bookingService).updateBookingStatusAndPaymentId(eq(1L), eq(Booking.Booked.BOOKED), eq(1L));
    }

    @Test
    void handlePaymentStatusUpdate_Canceled_ShouldUpdateToCanceled() throws Exception {
        String message = "{\"payment_id\":2,\"booking_id\":1,\"status\":\"CANCELED\"}";
        PaymentEventDto paymentEvent = new PaymentEventDto(2L, 1L, "CANCELED");

        when(objectMapper.readValue(message, PaymentEventDto.class)).thenReturn(paymentEvent);

        paymentEventListener.handlePaymentStatusUpdate(message);

        verify(bookingService).updateBookingStatusAndPaymentId(eq(1L), eq(Booking.Booked.CANCELED), eq(2L));
    }

    @Test
    void handlePaymentStatusUpdate_Failed_ShouldUpdateToFailed() throws Exception {
        String message = "{\"payment_id\":3,\"booking_id\":1,\"status\":\"FAILED\"}";
        PaymentEventDto paymentEvent = new PaymentEventDto(3L, 1L, "FAILED");

        when(objectMapper.readValue(message, PaymentEventDto.class)).thenReturn(paymentEvent);

        paymentEventListener.handlePaymentStatusUpdate(message);

        verify(bookingService).updateBookingStatusAndPaymentId(eq(1L), eq(Booking.Booked.FAILED), eq(3L));
    }

    @Test
    void handlePaymentStatusUpdate_UnknownStatus_ShouldUpdateToFailed() throws Exception {
        String message = "{\"payment_id\":5,\"booking_id\":1,\"status\":\"UNKNOWN\"}";
        PaymentEventDto paymentEvent = new PaymentEventDto(5L, 1L, "UNKNOWN");

        when(objectMapper.readValue(message, PaymentEventDto.class)).thenReturn(paymentEvent);

        paymentEventListener.handlePaymentStatusUpdate(message);

        verify(bookingService).updateBookingStatusAndPaymentId(eq(1L), eq(Booking.Booked.FAILED), eq(5L));
    }
}
