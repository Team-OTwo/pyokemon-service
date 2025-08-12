package com.pyokemon.booking.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pyokemon.booking.dto.PaymentEventDto;
import com.pyokemon.booking.entity.Booking;
import com.pyokemon.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final BookingService bookingService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "payment-status-updated", groupId = "booking-service")
    public void handlePaymentStatusUpdate(String message) {
        try {
            PaymentEventDto paymentEvent = objectMapper.readValue(message, PaymentEventDto.class);
            Booking.Booked newStatus = mapPaymentStatusToBookingStatus(paymentEvent.getStatus());
                
            bookingService.updateBookingStatusAndPaymentId(paymentEvent.getBookingId(), newStatus, paymentEvent.getPaymentId());
        } catch (Exception e) {
            log.error("Error processing payment status update message: {}", message, e);
        }
    }

    private Booking.Booked mapPaymentStatusToBookingStatus(String paymentStatus) {
        return switch (paymentStatus.toUpperCase()) {
            case "DONE" -> Booking.Booked.BOOKED;
            case "CANCELED" -> Booking.Booked.CANCELED;
            case "FAILED" -> Booking.Booked.FAILED;
            default -> {
                log.warn("Unknown payment status: {}, defaulting to FAILED", paymentStatus);
                yield Booking.Booked.FAILED;
            }
        };
    }
}
