package com.pyokemon.event.service;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class SeatStatusInitService {

  private final SeatStatusService seatStatusService;

  /**
   * 공연 스케줄 생성 시 좌석 상태를 Redis에 초기화합니다.
   * 
   * @param scheduleId 공연 스케줄 ID
   */
  public void initSeatStatuses(Long scheduleId) {
    log.info("좌석 상태 초기화 시작: scheduleId={}", scheduleId);
    
    try {
      // SeatStatusService를 통해 Redis에 좌석 상태 초기화
      seatStatusService.initSeatStatuses(scheduleId);
      log.info("좌석 상태 초기화 완료: scheduleId={}", scheduleId);
    } catch (Exception e) {
      log.error("좌석 상태 초기화 실패: scheduleId={}, error={}", scheduleId, e.getMessage(), e);
      throw new RuntimeException("좌석 상태 초기화에 실패했습니다: " + e.getMessage(), e);
    }
  }
}
