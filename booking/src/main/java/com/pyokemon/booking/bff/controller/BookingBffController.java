package com.pyokemon.booking.bff.controller;

import java.util.List;

import com.pyokemon.booking.bff.dto.*;
import org.springframework.http.ResponseEntity;
import com.pyokemon.booking.entity.Booking;
import com.pyokemon.common.dto.IdsRequest;
import org.springframework.web.bind.annotation.*;

import com.pyokemon.booking.bff.service.BookingBffService;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingBffController {

  private final BookingBffService bookingBffService;

  // @GetMapping("/event-schedules/{eventScheduleId}/bookings")
  // public List<BookingDto> getEventScheduleBookings(@PathVariable Long eventScheduleId) {
  // return bookingBffService.getEventScheduleBookings(eventScheduleId);
  // }

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

  @GetMapping("/event-schedules/{eventScheduleId}/bookings")
  public PageResponse<BookingDto> getBookingsOrderByDate(@PathVariable Long eventScheduleId,
      @RequestParam(required = false, defaultValue = "0") Integer page,
      @RequestParam(required = false, defaultValue = "10") Integer size) {
    return bookingBffService.getBookingsOrderByDate(eventScheduleId, page, size);
  }

  @GetMapping("/bff/{bookingId}")
  public BookingDto getBooking(@PathVariable Long bookingId) {
    return bookingBffService.getBooking(bookingId);
  }

  @GetMapping("/counts")
  public ResponseEntity<List<BookingCountDto>> getBookingCounts(
          @RequestParam("scheduleIds") List<Long> scheduleIds) {

    List<BookingCountDto> counts = bookingBffService.getBookingCountsByScheduleIds(scheduleIds);
    return ResponseEntity.ok(counts);
  }

  @GetMapping("/summary/sold-count")
  public ResponseEntity<TotalSoldTicketsResponseDto> getTotalSoldTickets(
          @RequestParam("scheduleIds") List<Long> scheduleIds) {

    TotalSoldTicketsResponseDto responseDto = bookingBffService.getTotalSoldTickets(scheduleIds);
    return ResponseEntity.ok(responseDto);
  }
  
  /** 1) 계정 전체(장르 없음) — 예매순 커서 */
  @GetMapping("/accounts/{accountId}/cursor")
  public CursorPageResponse<Booking> findByAccountCursor(@PathVariable long accountId,
                                                         @RequestParam(required = false) Long cursor, @RequestParam(defaultValue = "10") int size) {
    return bookingBffService.findByAccountCursor(accountId, cursor, size);
  }

  /** 2) 특정 스케줄 집합 — 예매순 커서 (IdsRequest: ids = eventScheduleIds) */
  @PostMapping("/accounts/{accountId}/by-schedules/cursor")
  public CursorPageResponse<Booking> findByAccountAndSchedulesCursor(@PathVariable long accountId,
                                                                     @RequestParam(required = false) Long cursor, @RequestParam(defaultValue = "10") int size,
                                                                     @RequestBody IdsRequest req) {
    return bookingBffService.findByAccountAndSchedulesCursor(accountId, req.getIds(), cursor, size);
  }

}
