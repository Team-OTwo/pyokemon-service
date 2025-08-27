package com.pyokemon.booking.bff.repository;

import java.util.List;
import java.util.Optional;

import com.pyokemon.booking.bff.dto.BookingCountDto;
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

  List<BookingCountDto> findBookingCountsByScheduleIds(@Param("scheduleIds") List<Long> scheduleIds);

  Long countTotalSoldTicketsByScheduleIds(@Param("scheduleIds") List<Long> scheduleIds);
}
