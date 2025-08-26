package com.pyokemon.booking.bff.repository;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.pyokemon.booking.entity.Booking;

@Mapper
public interface BookingBffRepository {

  List<Booking> findByEventScheduleId(@Param("eventScheduleId") Long eventScheduleId);

  List<Booking> findByAccountId(@Param("accountId") Long accountId);

  List<Booking> findByAccountIdOrderByDate(@Param("accountId") Long accountId,
      @Param("offset") Integer offset, @Param("size") Integer size);

  List<Booking> findByEventScheduleIdOrderByDate(@Param("eventScheduleId") Long eventScheduleId,
      @Param("offset") Integer offset, @Param("size") Integer size);

  Long countByAccountId(@Param("accountId") Long accountId);

  Long countByEventScheduleId(@Param("eventScheduleId") Long eventScheduleId);

  Optional<Booking> findByBookingId(@Param("bookingId") Long bookingId);

  // 예매순(booking_id DESC) 커서
  List<Booking> findByAccountWithCursor(@Param("accountId") long accountId,
                                        @Param("cursor") Long cursor, @Param("size") int size);

  /** 특정 스케줄 집합 — 예매순(booking_id DESC) 커서 */
  List<Booking> findByAccountAndSchedulesWithCursor(@Param("accountId") long accountId,
                                                    @Param("scheduleIds") List<Long> scheduleIds, @Param("cursor") Long cursor,
                                                    @Param("size") int size);
}
