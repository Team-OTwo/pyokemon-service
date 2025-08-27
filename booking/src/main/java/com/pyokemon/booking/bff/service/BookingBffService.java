package com.pyokemon.booking.bff.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.pyokemon.booking.bff.dto.*;
import org.springframework.stereotype.Service;

import com.pyokemon.booking.bff.repository.BookingBffRepository;
import com.pyokemon.booking.entity.Booking;
import com.pyokemon.common.exception.BusinessException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookingBffService {

  private final BookingBffRepository bookingBffRepository;

  public List<BookingDto> getEventScheduleBookings(Long eventScheduleId) {
    List<Booking> bookings = bookingBffRepository.findByEventScheduleId(eventScheduleId);

    if (bookings.isEmpty()) {
      return null;
    }

    return bookings.stream().map(this::toDto) // Booking -> BookingDto
        .toList(); // Java 16+면 이거 사용 가능
  }

  public List<BookingDto> getAccountIdBookings(Long accountId) {
    List<Booking> bookings = bookingBffRepository.findByAccountId(accountId);

    if (bookings.isEmpty()) {
      return null;
    }

    return bookings.stream().map(this::toDto) // Booking -> BookingDto
        .toList();
  }

  public PageResponse<BookingDto> getAccountIdBookingsOrderByDate(Long accountId, Integer page,
      Integer size) {
    List<Booking> bookings =
        bookingBffRepository.findByAccountIdOrderByDate(accountId, page * size, size);
    Long totalCount = bookingBffRepository.countByAccountId(accountId);

    if (bookings.isEmpty()) {
      return null;
    }

    List<BookingDto> dtoList = bookings.stream().map(this::toDto) // Booking -> BookingDto
        .toList();


    return new PageResponse<>(dtoList, page, totalCount);
  }

  public PageResponse<BookingDto> getBookingsOrderByDate(Long eventScheduleId, Integer page,
      Integer size) {
    List<Booking> bookings =
        bookingBffRepository.findByEventScheduleIdOrderByBookingId(eventScheduleId, page * size, size);
    Long totalCount = bookingBffRepository.countByEventScheduleId(eventScheduleId);

    if (bookings.isEmpty()) {
      return null;
    }

    List<BookingDto> dtoList = bookings.stream().map(this::toDto) // Booking -> BookingDto
        .toList();


    return new PageResponse<>(dtoList, page, totalCount);
  }

  public BookingDto getBooking(Long bookingId) {
    Optional<Booking> bookingOpt = bookingBffRepository.findByBookingId(bookingId);

    if (bookingOpt.isEmpty()) {
      throw new BusinessException("해당 예약은 존재하지 않습니다.", "BOOKING_NOT_FOUND");
    }

    Booking booking = bookingOpt.get();

    return toDto(booking);
  }

  public CursorPageResponse<Booking> findByAccountCursor(long accountId, Long cursor, int size) {
    List<Booking> bookings =
            bookingBffRepository.findByAccountWithCursor(accountId, cursor, size + 1);

    boolean hasMore = bookings.size() > size;

    if (hasMore) {
      bookings = bookings.subList(0, size);
    }

    Long nextCursor = hasMore ? bookings.getLast().getBookingId() : null;

    return new CursorPageResponse<>(bookings, nextCursor, hasMore);
  }

  public CursorPageResponse<Booking> findByAccountAndSchedulesCursor(long accountId,
                                                                     List<Long> scheduleIds, Long cursor, int size) {
    List<Booking> bookings = bookingBffRepository.findByAccountAndSchedulesWithCursor(accountId,
            scheduleIds, cursor, size + 1);

    boolean hasMore = bookings.size() > size;

    if (hasMore) {
      bookings = bookings.subList(0, size);
    }

    Long nextCursor = hasMore ? bookings.getLast().getBookingId() : null;

    return new CursorPageResponse<>(bookings, nextCursor, hasMore);

  }

  private BookingDto toDto(Booking b) {
    return BookingDto.builder().bookingId(b.getBookingId()).eventScheduleId(b.getEventScheduleId())
        .seatId(b.getSeatId()).accountId(b.getAccountId()).paymentId(b.getPaymentId())
        .status(b.getStatus()).updatedAt(b.getUpdatedAt()).tenantId(b.getTenantId()).build();
  }

  public List<BookingCountDto> getBookingCountsByScheduleIds(List<Long> scheduleIds) {
    if (scheduleIds == null || scheduleIds.isEmpty()) {
      return Collections.emptyList();
    }
    return bookingBffRepository.findBookingCountsByScheduleIds(scheduleIds);
  }

  public TotalSoldTicketsResponseDto getTotalSoldTickets(List<Long> scheduleIds) {
    if (scheduleIds == null || scheduleIds.isEmpty()) {
      return new TotalSoldTicketsResponseDto(0L);
    }
    Long count = bookingBffRepository.countTotalSoldTicketsByScheduleIds(scheduleIds);
    return new TotalSoldTicketsResponseDto(count);
  }
}
