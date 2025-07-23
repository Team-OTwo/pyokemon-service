package com.pyokemon.event.repository;

import com.pyokemon.event.entity.Booking;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional; // findById 등을 위해

@Mapper
public interface BookingRepository {
    @Select("SELECT booking_id, event_schedule_id, user_id, payment_id, status, created_at, updated_at, seat_id " +
            "FROM tb_booking " +
            "WHERE event_schedule_id = #{eventScheduleId} AND status = #{status}")
    List<Booking> findByEventScheduleIdAndStatus(@Param("eventScheduleId") Long eventScheduleId, @Param("status") Booking.Booked status);

    @Select("SELECT booking_id, event_schedule_id, user_id, payment_id, status, created_at, updated_at, seat_id " +
            "FROM tb_booking " +
            "WHERE booking_id = #{id}")
    Optional<Booking> findById(@Param("id") Long id);
}