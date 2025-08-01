package com.pyokemon.event.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.pyokemon.event.entity.Booking;

@Mapper
public interface BookingRepository {
  List<Booking> findByEventScheduleIdAndStatus(Long eventScheduleId, Booking.Booked status);
  void insert(Booking booking);
}
