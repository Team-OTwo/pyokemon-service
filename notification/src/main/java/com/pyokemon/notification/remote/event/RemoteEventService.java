package com.pyokemon.notification.remote.event;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.pyokemon.notification.remote.event.dto.EventInfoResponseDto;

@FeignClient(name = "remote-event-service", url = "${event.service.base-url}")
public interface RemoteEventService {

  @GetMapping("/api/events/seat-detail")
  public EventInfoResponseDto getSeatDetails(@RequestParam Long eventScheduleId,
      @RequestParam Long seatId);
}
