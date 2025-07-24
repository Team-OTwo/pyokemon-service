package com.pyokemon.event.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.pyokemon.event.entity.EventSchedule;

@Repository
public interface EventScheduleRepository {
  // Add method for saving an event schedule
  Long save(EventSchedule eventSchedule);

  // Add method for finding event schedules by event ID
  List<EventSchedule> findByEventId(Long eventId);

  // Add method for finding an event schedule by ID
  Optional<EventSchedule> findById(Long eventScheduleId);
}
