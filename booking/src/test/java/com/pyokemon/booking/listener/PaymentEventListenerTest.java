package com.pyokemon.booking.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pyokemon.booking.dto.PaymentEventDto;
import com.pyokemon.booking.entity.Booking;
import com.pyokemon.booking.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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
        String message = "{\"payment_id\":1,\"booking_id\":1,\"order_id\":\"ORDER123\",\"status\":\"DONE\"}";
        PaymentEventDto paymentEvent = new PaymentEventDto(1L, 1L, "ORDER123", "DONE");

        when(objectMapper.readValue(message, PaymentEventDto.class)).thenReturn(paymentEvent);

        paymentEventListener.handlePaymentStatusUpdate(message);

        verify(bookingService).updateBookingStatusAndPaymentId(eq(1L), eq(Booking.Booked.BOOKED), eq(1L));
    }

    @Test
    void handlePaymentStatusUpdate_Canceled_ShouldUpdateToCanceled() throws Exception {
        String message = "{\"payment_id\":2,\"booking_id\":1,\"order_id\":\"ORDER123\",\"status\":\"CANCELED\"}";
        PaymentEventDto paymentEvent = new PaymentEventDto(2L, 1L, "ORDER123", "CANCELED");

        when(objectMapper.readValue(message, PaymentEventDto.class)).thenReturn(paymentEvent);

        paymentEventListener.handlePaymentStatusUpdate(message);

        verify(bookingService).updateBookingStatusAndPaymentId(eq(1L), eq(Booking.Booked.CANCELED), eq(2L));
    }

    @Test
    void handlePaymentStatusUpdate_Failed_ShouldUpdateToFailed() throws Exception {
        String message = "{\"payment_id\":3,\"booking_id\":1,\"order_id\":\"ORDER123\",\"status\":\"FAILED\"}";
        PaymentEventDto paymentEvent = new PaymentEventDto(3L, 1L, "ORDER123", "FAILED");

        when(objectMapper.readValue(message, PaymentEventDto.class)).thenReturn(paymentEvent);

        paymentEventListener.handlePaymentStatusUpdate(message);

        verify(bookingService).updateBookingStatusAndPaymentId(eq(1L), eq(Booking.Booked.FAILED), eq(3L));
    }

    @Test
    void handlePaymentStatusUpdate_Ready_ShouldUpdateToFailed() throws Exception {
        String message = "{\"payment_id\":4,\"booking_id\":1,\"order_id\":\"ORDER123\",\"status\":\"READY\"}";
        PaymentEventDto paymentEvent = new PaymentEventDto(4L, 1L, "ORDER123", "READY");

        when(objectMapper.readValue(message, PaymentEventDto.class)).thenReturn(paymentEvent);

        paymentEventListener.handlePaymentStatusUpdate(message);

        verify(bookingService).updateBookingStatusAndPaymentId(eq(1L), eq(Booking.Booked.FAILED), eq(4L));
    }

    @Test
    void handlePaymentStatusUpdate_UnknownStatus_ShouldUpdateToFailed() throws Exception {
        String message = "{\"payment_id\":5,\"booking_id\":1,\"order_id\":\"ORDER123\",\"status\":\"UNKNOWN\"}";
        PaymentEventDto paymentEvent = new PaymentEventDto(5L, 1L, "ORDER123", "UNKNOWN");

        when(objectMapper.readValue(message, PaymentEventDto.class)).thenReturn(paymentEvent);

        paymentEventListener.handlePaymentStatusUpdate(message);

        verify(bookingService).updateBookingStatusAndPaymentId(eq(1L), eq(Booking.Booked.FAILED), eq(5L));
    }
}
