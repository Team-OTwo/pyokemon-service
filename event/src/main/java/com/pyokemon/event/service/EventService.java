package com.pyokemon.event.service;

import com.pyokemon.event.dto.EventRegisterDto;
import com.pyokemon.event.dto.EventResponseDto;

public interface EventService {
  EventResponseDto registerEvent(EventRegisterDto eventRegisterDto);
}
