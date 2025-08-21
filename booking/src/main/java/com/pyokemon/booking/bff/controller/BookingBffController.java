package com.pyokemon.booking.bff.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.pyokemon.booking.bff.dto.BookingDto;
import com.pyokemon.booking.bff.dto.PageResponse;
import com.pyokemon.booking.bff.service.BookingBffService;

import lombok.RequiredArgsConstructor;


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
  public List<BookingDto> getAccountBookings(@PathVariable Long accountId) {
    return bookingBffService.getAccountIdBookings(accountId);
  }

  @GetMapping("/accounts/{accountId}/bookings/order")
  public PageResponse<BookingDto> getAccountBookingsOrderByDate(@PathVariable Long accountId,
      @RequestParam(required = false, defaultValue = "0") Integer page,
      @RequestParam(required = false, defaultValue = "10") Integer size) {
    return bookingBffService.getAccountIdBookingsOrderByDate(accountId, page, size);
  }

  @GetMapping("/bff/{bookingId}")
  public BookingDto getBooking(@PathVariable Long bookingId) {
    return bookingBffService.getBooking(bookingId);
  }

}
