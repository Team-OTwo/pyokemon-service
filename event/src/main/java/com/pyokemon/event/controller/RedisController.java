package com.pyokemon.event.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.pyokemon.event.service.RedisService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
@Slf4j
public class RedisController {

  private final RedisService redisService;

  /**
   * 특정 공연 스케줄의 모든 좌석 상태를 조회합니다.
   */
  @GetMapping("/{scheduleId}/status")
  public ResponseEntity<Map<String, String>> getAllSeatStatuses(@PathVariable Long scheduleId) {
    log.info("좌석 상태 조회 요청: scheduleId={}", scheduleId);

    try {
      Map<String, String> seatStatuses = redisService.getAllSeatStatuses(scheduleId);
      return ResponseEntity.ok(seatStatuses);
    } catch (Exception e) {
      log.error("좌석 상태 조회 실패: scheduleId={}, error={}", scheduleId, e.getMessage(), e);
      return ResponseEntity.internalServerError().build();
    }
  }

  /**
   * 특정 좌석의 상태를 조회합니다.
   */
  @GetMapping("/{scheduleId}/{seatId}/status")
  public ResponseEntity<String> getSeatStatus(@PathVariable Long scheduleId,
      @PathVariable Integer seatId) {
    log.info("좌석 상태 조회 요청: scheduleId={}, seatId={}", scheduleId, seatId);

    try {
      String status = redisService.getSeatStatus(scheduleId, seatId);
      return ResponseEntity.ok(status);
    } catch (Exception e) {
      log.error("좌석 상태 조회 실패: scheduleId={}, seatId={}, error={}", scheduleId, seatId,
          e.getMessage(), e);
      return ResponseEntity.internalServerError().build();
    }
  }

  /**
   * 좌석을 홀드 상태로 설정합니다.
   */
  @PostMapping("/{scheduleId}/{seatId}/hold")
  public ResponseEntity<String> holdSeat(
      @PathVariable Long scheduleId,
      @PathVariable Integer seatId,
      @RequestHeader("X-Auth-AccountId") Long userId) {
    // TTL 고정 300초
    long ttlSeconds = 300L;
    log.info("좌석 홀드 요청: scheduleId={}, seatId={}, userId={}, ttl={}초", scheduleId, seatId,
        userId, ttlSeconds);

    try {
      redisService.holdSeat(scheduleId, seatId, userId, ttlSeconds);
      return ResponseEntity.ok("좌석 홀드 성공");
    } catch (Exception e) {
      log.error("좌석 홀드 실패: scheduleId={}, seatId={}, error={}", scheduleId, seatId,
          e.getMessage(), e);
      return ResponseEntity.internalServerError().body("좌석 홀드 실패: " + e.getMessage());
    }
  }

  /**
   * 좌석 홀드를 해제합니다.
   */
  @DeleteMapping("/{scheduleId}/{seatId}/hold")
  public ResponseEntity<String> releaseSeatHold(@PathVariable Long scheduleId,
      @PathVariable Integer seatId) {
    log.info("좌석 홀드 해제 요청: scheduleId={}, seatId={}", scheduleId, seatId);

    try {
      redisService.releaseSeatHold(scheduleId, seatId);
      return ResponseEntity.ok("좌석 홀드 해제 성공");
    } catch (Exception e) {
      log.error("좌석 홀드 해제 실패: scheduleId={}, seatId={}, error={}", scheduleId, seatId,
          e.getMessage(), e);
      return ResponseEntity.internalServerError().body("좌석 홀드 해제 실패: " + e.getMessage());
    }
  }

  /**
   * 좌석을 예약 확정 상태로 변경합니다.
   */
  @PostMapping("/{scheduleId}/{seatId}/confirm")
  public ResponseEntity<String> confirmSeat(@PathVariable Long scheduleId,
      @PathVariable Integer seatId) {
    log.info("좌석 예약 확정 요청: scheduleId={}, seatId={}", scheduleId, seatId);

    try {
      redisService.confirmSeat(scheduleId, seatId);
      return ResponseEntity.ok("좌석 예약 확정 성공");
    } catch (Exception e) {
      log.error("좌석 예약 확정 실패: scheduleId={}, seatId={}, error={}", scheduleId, seatId,
          e.getMessage(), e);
      return ResponseEntity.internalServerError().body("좌석 예약 확정 실패: " + e.getMessage());
    }
  }

  /**
   * 좌석 예매를 취소하고 상태를 가용으로 변경합니다.
   */
  @PostMapping("/{scheduleId}/{seatId}/cancel")
  public ResponseEntity<String> cancelSeat(@PathVariable Long scheduleId,
      @PathVariable Integer seatId) {
    log.info("좌석 예매 취소 요청: scheduleId={}, seatId={}", scheduleId, seatId);

    try {
      redisService.cancelSeat(scheduleId, seatId);
      return ResponseEntity.ok("좌석 예매 취소 성공");
    } catch (Exception e) {
      log.error("좌석 예매 취소 실패: scheduleId={}, seatId={}, error={}", scheduleId, seatId,
          e.getMessage(), e);
      return ResponseEntity.internalServerError().body("좌석 예매 취소 실패: " + e.getMessage());
    }
  }

  /**
   * 공연 스케줄의 모든 좌석 상태를 삭제합니다.
   */
  @DeleteMapping("/{scheduleId}/status")
  public ResponseEntity<String> deleteSeatStatuses(@PathVariable Long scheduleId) {
    log.info("좌석 상태 삭제 요청: scheduleId={}", scheduleId);

    try {
      redisService.deleteSeatStatuses(scheduleId);
      return ResponseEntity.ok("좌석 상태 삭제 성공");
    } catch (Exception e) {
      log.error("좌석 상태 삭제 실패: scheduleId={}, error={}", scheduleId, e.getMessage(), e);
      return ResponseEntity.internalServerError().body("좌석 상태 삭제 실패: " + e.getMessage());
    }
  }
}
