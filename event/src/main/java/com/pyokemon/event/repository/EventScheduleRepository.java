package com.pyokemon.event.repository;

import com.pyokemon.event.entity.EventSchedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

@Mapper
public interface EventScheduleRepository {
    @Select("SELECT event_schedule_id, event_id, venue_id, event_date, ticket_open_at, created_at, updated_at " +
            "FROM tb_event_schedule " +
            "WHERE event_schedule_id = #{id}")
    Optional<EventSchedule> findById(@Param("id") Long id);

    List<EventSchedule> findAll();
}