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

  Optional<Booking> findByBookingId(@Param("bookingId") Long bookingId);
}
