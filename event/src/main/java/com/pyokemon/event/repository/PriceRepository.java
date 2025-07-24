package com.pyokemon.event.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.pyokemon.event.entity.Price;

@Repository
public interface PriceRepository {
  // Add method for saving a price
  Long save(Price price);

  // Add method for finding prices by event schedule ID
  List<Price> findByEventScheduleId(Long eventScheduleId);

  // Add method for finding a price by ID
  Optional<Price> findById(Long priceId);
}
