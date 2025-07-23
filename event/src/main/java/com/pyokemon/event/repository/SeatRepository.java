package com.pyokemon.event.repository;

import com.pyokemon.event.entity.Seat;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

@Mapper
public interface SeatRepository {
    @Select("SELECT seat_id, venue_id, seat_class_id, `row`, col, created_at, updated_at " +
            "FROM tb_seat " +
            "WHERE venue_id = #{venueId}")
    List<Seat> findByVenueId(@Param("venueId") Long venueId);

    @Select("SELECT seat_id, venue_id, seat_class_id, `row`, col, created_at, updated_at " +
            "FROM tb_seat " +
            "WHERE seat_id = #{seatId}")
    Optional<Seat> findById(@Param("seatId") Long seatId);

    @Select("SELECT seat_id, venue_id, seat_class_id, `row`, col, created_at, updated_at " +
            "FROM tb_seat")
    List<Seat> findAll();
}