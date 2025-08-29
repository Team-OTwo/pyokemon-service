package com.pyokemon.booking.repository;


import java.util.List;
import java.util.Optional;

import com.pyokemon.booking.dto.response.BookingCountDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.pyokemon.booking.dto.response.ValidBookingDetail;
import com.pyokemon.booking.entity.Booking;

@Mapper
public interface BookingRepository {

  List<Long> findSeatIdsByEventScheduleId(@Param("eventScheduleId") Long eventScheduleId);

  List<Booking> findByAccountId(@Param("accountId") Long accountId);

  List<Booking> findAllByEventScheduleIdAndSeatId(@Param("eventScheduleId") Long eventScheduleId,
      @Param("seatId") Long seatId);

  Optional<Booking> findActiveBookingByEventScheduleIdAndAccountId(
      @Param("eventScheduleId") Long eventScheduleId, @Param("accountId") Long accountId);

  Optional<Booking> findByBookingId(@Param("bookingId") Long bookingId);

  List<Booking> findPendingBookings();

  List<ValidBookingDetail> findValidBookingsWithEventInfo(
      @Param("bookingIds") List<Long> bookingIds, @Param("accountId") Long accountId);

  void save(Booking booking);

  void update(Booking booking);

  void delete(@Param("bookingId") Long bookingId);

  Long updateStatus(@Param("eventScheduleId") Long eventScheduleId, @Param("status") String status);

  List<Booking> findAllByEventScheduleId(Long eventScheduleId);

  List<Booking> findByEventScheduleId(@Param("eventScheduleId") Long eventScheduleId);

  List<Booking> findByAccountIdOrderByDate(@Param("accountId") Long accountId,
                                           @Param("offset") Integer offset, @Param("size") Integer size);

  List<Booking> findByEventScheduleIdOrderByBookingId(@Param("eventScheduleId") Long eventScheduleId,
                                                      @Param("offset") Integer offset, @Param("size") Integer size);

  Long countByAccountId(@Param("accountId") Long accountId);

  Long countByEventScheduleId(@Param("eventScheduleId") Long eventScheduleId);

  List<BookingCountDto> findBookingCountsByScheduleIds(@Param("scheduleIds") List<Long> scheduleIds);

  Long countTotalSoldTicketsByScheduleIds(@Param("scheduleIds") List<Long> scheduleIds);

  // 예매순(booking_id DESC) 커서
  List<Booking> findByAccountWithCursor(@Param("accountId") long accountId,
                                        @Param("cursor") Long cursor, @Param("size") int size);

  /** 특정 스케줄 집합 — 예매순(booking_id DESC) 커서 */
  List<Booking> findByAccountAndSchedulesWithCursor(@Param("accountId") long accountId,
                                                    @Param("scheduleIds") List<Long> scheduleIds, @Param("cursor") Long cursor,
                                                    @Param("size") int size);
}
