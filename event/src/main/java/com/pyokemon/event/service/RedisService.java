package com.pyokemon.event.service;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RedisService {

  private final RedisTemplate<String, String> redis;

  public RedisService(@Qualifier("redisTemplate") RedisTemplate<String, String> redis) {
    this.redis = redis;
  }

  private static final int TOTAL_SEATS = 120;

  private static final String SEAT_STATUS_KEY_PATTERN = "seat:status:%d";
  private static final String SEAT_HOLD_KEY_PATTERN = "seat:hold:%d:%d";

  public void initSeatStatuses(Long scheduleId) {
    log.info("좌석 상태 초기화 시작: scheduleId={}", scheduleId);
    
    try {
      String key = String.format(SEAT_STATUS_KEY_PATTERN, scheduleId);

      Long existingSize = redis.opsForHash().size(key);
      if (existingSize != null && existingSize > 0) {
        log.info("좌석 상태가 이미 초기화됨: scheduleId={}, existingSeats={}", scheduleId, existingSize);
        return;
      }

      Map<String, String> initMap = new LinkedHashMap<>(TOTAL_SEATS);
      for (int i = 1; i <= TOTAL_SEATS; i++) {
        initMap.put(String.valueOf(i), "");
      }

      redis.opsForHash().putAll(key, initMap);

      log.info("좌석 상태 초기화 완료: scheduleId={}, seats={}", scheduleId, TOTAL_SEATS);
    } catch (Exception e) {
      log.error("좌석 상태 초기화 실패: scheduleId={}, error={}", scheduleId, e.getMessage(), e);
      throw new RuntimeException("좌석 상태 초기화에 실패했습니다: " + e.getMessage(), e);
    }
  }

  public void holdSeat(Long scheduleId, Integer seatId, Long userId, long ttlSeconds) {
    if (seatId < 1 || seatId > TOTAL_SEATS) {
      throw new IllegalArgumentException("좌석 번호는 1~120 사이여야 합니다: " + seatId);
    }

    String holdKey = String.format(SEAT_HOLD_KEY_PATTERN, scheduleId, seatId);

    redis.opsForValue().set(holdKey, String.valueOf(userId), ttlSeconds, TimeUnit.SECONDS);

    log.info("좌석 홀드 설정: scheduleId={}, seatId={}, userId={}, ttl={}초", scheduleId, seatId, userId,
        ttlSeconds);
  }

  public void releaseSeatHold(Long scheduleId, Integer seatId) {
    String holdKey = String.format(SEAT_HOLD_KEY_PATTERN, scheduleId, seatId);
    redis.delete(holdKey);

    log.info("좌석 홀드 해제: scheduleId={}, seatId={}", scheduleId, seatId);
  }

  public void confirmSeat(Long scheduleId, Integer seatId) {
    String statusKey = String.format(SEAT_STATUS_KEY_PATTERN, scheduleId);
    String holdKey = String.format(SEAT_HOLD_KEY_PATTERN, scheduleId, seatId);

    redis.delete(holdKey);

    redis.opsForHash().put(statusKey, String.valueOf(seatId), "BOOKED");

    log.info("좌석 예약 확정: scheduleId={}, seatId={}", scheduleId, seatId);
  }

  public void blockSeat(Long scheduleId, Integer seatId) {
    String statusKey = String.format(SEAT_STATUS_KEY_PATTERN, scheduleId);
    redis.opsForHash().put(statusKey, String.valueOf(seatId), "BLOCKED");

    log.info("좌석 블록: scheduleId={}, seatId={}", scheduleId, seatId);
  }

  public void clearSeatStatus(Long scheduleId, Integer seatId) {
    String statusKey = String.format(SEAT_STATUS_KEY_PATTERN, scheduleId);
    String holdKey = String.format(SEAT_HOLD_KEY_PATTERN, scheduleId, seatId);
    
    redis.delete(holdKey);
    
    redis.opsForHash().put(statusKey, String.valueOf(seatId), "");
    
    log.info("좌석 상태 초기화: scheduleId={}, seatId={}", scheduleId, seatId);
  }

  public void cancelSeat(Long scheduleId, Integer seatId) {
    clearSeatStatus(scheduleId, seatId);
    log.info("좌석 예매 취소: scheduleId={}, seatId={}", scheduleId, seatId);
  }

  public String getSeatStatus(Long scheduleId, Integer seatId) {
    String statusKey = String.format(SEAT_STATUS_KEY_PATTERN, scheduleId);
    String holdKey = String.format(SEAT_HOLD_KEY_PATTERN, scheduleId, seatId);

    String holdValue = redis.opsForValue().get(holdKey);
    if (holdValue != null) {
      return "HELD";
    }

    Object status = redis.opsForHash().get(statusKey, String.valueOf(seatId));
    return status != null ? (String) status : "";
  }

  public Map<String, String> getAllSeatStatuses(Long scheduleId) {
    String statusKey = String.format(SEAT_STATUS_KEY_PATTERN, scheduleId);
    Map<Object, Object> statusMap = redis.opsForHash().entries(statusKey);

    Map<String, String> result = new LinkedHashMap<>();

    for (int i = 1; i <= TOTAL_SEATS; i++) {
      String seatId = String.valueOf(i);
      String holdKey = String.format(SEAT_HOLD_KEY_PATTERN, scheduleId, i);

      String holdValue = redis.opsForValue().get(holdKey);
      if (holdValue != null) {
        result.put(seatId, "HELD");
      } else {
        Object status = statusMap.get(seatId);
        result.put(seatId, status != null ? (String) status : "");
      }
    }

    return result;
  }

  public void deleteSeatStatuses(Long scheduleId) {
    String statusKey = String.format(SEAT_STATUS_KEY_PATTERN, scheduleId);

    for (int i = 1; i <= TOTAL_SEATS; i++) {
      String holdKey = String.format(SEAT_HOLD_KEY_PATTERN, scheduleId, i);
      redis.delete(holdKey);
    }

    redis.delete(statusKey);

    log.info("좌석 상태 삭제 완료: scheduleId={}", scheduleId);
  }
}
