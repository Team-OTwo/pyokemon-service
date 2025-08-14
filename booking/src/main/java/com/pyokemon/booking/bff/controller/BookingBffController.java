package com.pyokemon.booking.bff.controller;

import com.pyokemon.booking.bff.dto.BookingDto;
import com.pyokemon.booking.bff.service.BookingBffService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingBffController {

    private final BookingBffService bookingBffService;

    @GetMapping("/event-schedules/{eventScheduleId}/bookings")
    public List<BookingDto> getEventScheduleBookings(@PathVariable Long eventScheduleId) {
        return bookingBffService.getEventScheduleBookings(eventScheduleId);
    }

    @GetMapping("/accounts/{accountId}/bookings")
    public List<BookingDto> getAccountBookins(@PathVariable Long accountId) {
        return bookingBffService.getAccountIdBookings(accountId);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBooking(@PathVariable Long bookingId) {
        return bookingBffService.getBooking(bookingId);
    }

}
