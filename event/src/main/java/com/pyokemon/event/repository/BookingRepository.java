package com.pyokemon.event.repository;

import com.pyokemon.event.entity.Booking;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface BookingRepository {
    List<Booking> findByEventScheduleIdAndStatus(Long eventScheduleId, Booking.Booked status);
}
