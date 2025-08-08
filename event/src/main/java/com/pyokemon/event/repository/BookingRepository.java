package com.pyokemon.event.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.pyokemon.event.entity.Booking;

@Mapper
public interface BookingRepository {
  List<Booking> findByEventScheduleIdAndStatus(Long eventScheduleId, Booking.Booked status);

  List<Booking> findByEventScheduleIdAndAccountId(Long eventScheduleId, Long accountId);

  void insert(Booking booking);

  List<Booking> findByStatus(Booking.Booked status);

  void delete(Booking booking);
}
