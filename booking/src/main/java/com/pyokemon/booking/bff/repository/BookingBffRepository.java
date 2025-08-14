package com.pyokemon.booking.bff.repository;

import com.pyokemon.booking.entity.Booking;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface BookingBffRepository {

  List<Booking> findByEventScheduleId (@Param("eventScheduleId") Long eventScheduleId);

  List<Booking> findByAccountId (@Param("accountId") Long accountId);

  Optional<Booking> findByBookingId (@Param("bookingId") Long bookingId);
}
