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
public class SeatStatusService {

  private final RedisTemplate<String, String> redis;

  public SeatStatusService(@Qualifier("redisTemplate") RedisTemplate<String, String> redis) {
    this.redis = redis;
  }

  private static final int TOTAL_SEATS = 120;

  // Redis 키 패턴
  private static final String SEAT_STATUS_KEY_PATTERN = "seat:status:%d";
  private static final String SEAT_HOLD_KEY_PATTERN = "seat:hold:%d:%d";

  /**
   * 공연 스케줄에 대한 좌석 상태를 초기화합니다. 1~120번 좌석을 모두 빈 문자열("")로 설정합니다.
   * 
   * @param scheduleId 공연 스케줄 ID
   */
  public void initSeatStatuses(Long scheduleId) {
    String key = String.format(SEAT_STATUS_KEY_PATTERN, scheduleId);

    // 이미 초기화된 경우 스킵 (멱등성 보장)
    Long existingSize = redis.opsForHash().size(key);
    if (existingSize != null && existingSize > 0) {
      log.info("좌석 상태가 이미 초기화됨: scheduleId={}, existingSeats={}", scheduleId, existingSize);
      return;
    }

    // 1~120번 좌석을 빈 문자열로 초기화
    Map<String, String> initMap = new LinkedHashMap<>(TOTAL_SEATS);
    for (int i = 1; i <= TOTAL_SEATS; i++) {
      initMap.put(String.valueOf(i), ""); // 빈 문자열 = null 상태
    }

    // Redis에 한 번에 저장
    redis.opsForHash().putAll(key, initMap);

    log.info("좌석 상태 초기화 완료: scheduleId={}, seats={}", scheduleId, TOTAL_SEATS);
  }

  /**
   * 특정 좌석을 홀드 상태로 설정합니다.
   * 
   * @param scheduleId 공연 스케줄 ID
   * @param seatId 좌석 번호 (1~120)
   * @param userId 사용자 ID
   * @param ttlSeconds 홀드 유지 시간 (초)
   */
  public void holdSeat(Long scheduleId, Integer seatId, Long userId, long ttlSeconds) {
    if (seatId < 1 || seatId > TOTAL_SEATS) {
      throw new IllegalArgumentException("좌석 번호는 1~120 사이여야 합니다: " + seatId);
    }

    String holdKey = String.format(SEAT_HOLD_KEY_PATTERN, scheduleId, seatId);

    // 홀드 키에 사용자 ID 저장 및 TTL 설정
    redis.opsForValue().set(holdKey, String.valueOf(userId), ttlSeconds, TimeUnit.SECONDS);

    log.info("좌석 홀드 설정: scheduleId={}, seatId={}, userId={}, ttl={}초", scheduleId, seatId, userId,
        ttlSeconds);
  }

  /**
   * 특정 좌석의 홀드를 해제합니다.
   * 
   * @param scheduleId 공연 스케줄 ID
   * @param seatId 좌석 번호
   */
  public void releaseSeatHold(Long scheduleId, Integer seatId) {
    String holdKey = String.format(SEAT_HOLD_KEY_PATTERN, scheduleId, seatId);
    redis.delete(holdKey);

    log.info("좌석 홀드 해제: scheduleId={}, seatId={}", scheduleId, seatId);
  }

  /**
   * 좌석을 예약 확정 상태로 변경합니다.
   * 결제 성공 시 호출됩니다.
   * 
   * @param scheduleId 공연 스케줄 ID
   * @param seatId 좌석 번호
   */
  public void confirmSeat(Long scheduleId, Integer seatId) {
    String statusKey = String.format(SEAT_STATUS_KEY_PATTERN, scheduleId);
    String holdKey = String.format(SEAT_HOLD_KEY_PATTERN, scheduleId, seatId);

    // 홀드 키 삭제
    redis.delete(holdKey);

    // 상태를 BOOKED로 변경
    redis.opsForHash().put(statusKey, String.valueOf(seatId), "BOOKED");

    log.info("좌석 예약 확정: scheduleId={}, seatId={}", scheduleId, seatId);
  }

  /**
   * 좌석을 블록 상태로 설정합니다.
   * 
   * @param scheduleId 공연 스케줄 ID
   * @param seatId 좌석 번호
   */
  public void blockSeat(Long scheduleId, Integer seatId) {
    String statusKey = String.format(SEAT_STATUS_KEY_PATTERN, scheduleId);
    redis.opsForHash().put(statusKey, String.valueOf(seatId), "BLOCKED");

    log.info("좌석 블록: scheduleId={}, seatId={}", scheduleId, seatId);
  }

  /**
   * 좌석 상태를 가용(빈 문자열)로 되돌립니다.
   * 결제 취소/실패 또는 예약 취소 시 사용합니다.
   * 
   * @param scheduleId 공연 스케줄 ID
   * @param seatId 좌석 번호
   */
  public void clearSeatStatus(Long scheduleId, Integer seatId) {
    String statusKey = String.format(SEAT_STATUS_KEY_PATTERN, scheduleId);
    String holdKey = String.format(SEAT_HOLD_KEY_PATTERN, scheduleId, seatId);
    
    // 홀드 키가 있다면 삭제
    redis.delete(holdKey);
    
    // 상태를 빈 문자열로 초기화 (가용 상태)
    redis.opsForHash().put(statusKey, String.valueOf(seatId), "");
    
    log.info("좌석 상태 초기화: scheduleId={}, seatId={}", scheduleId, seatId);
  }

  /**
   * 예매 취소 시 좌석 상태를 가용으로 변경합니다.
   * 
   * @param scheduleId 공연 스케줄 ID
   * @param seatId 좌석 번호
   */
  public void cancelSeat(Long scheduleId, Integer seatId) {
    clearSeatStatus(scheduleId, seatId);
    log.info("좌석 예매 취소: scheduleId={}, seatId={}", scheduleId, seatId);
  }

  /**
   * 특정 좌석의 현재 상태를 조회합니다.
   * 
   * @param scheduleId 공연 스케줄 ID
   * @param seatId 좌석 번호
   * @return 좌석 상태 (빈 문자열="", "BOOKED", "BLOCKED")
   */
  public String getSeatStatus(Long scheduleId, Integer seatId) {
    String statusKey = String.format(SEAT_STATUS_KEY_PATTERN, scheduleId);
    String holdKey = String.format(SEAT_HOLD_KEY_PATTERN, scheduleId, seatId);

    // 홀드 상태 확인
    String holdValue = redis.opsForValue().get(holdKey);
    if (holdValue != null) {
      return "HELD"; // 홀드 중
    }

    // 기본 상태 확인
    Object status = redis.opsForHash().get(statusKey, String.valueOf(seatId));
    return status != null ? (String) status : "";
  }

  /**
   * 공연 스케줄의 모든 좌석 상태를 조회합니다.
   * 
   * @param scheduleId 공연 스케줄 ID
   * @return 좌석 번호별 상태 맵
   */
  public Map<String, String> getAllSeatStatuses(Long scheduleId) {
    String statusKey = String.format(SEAT_STATUS_KEY_PATTERN, scheduleId);
    Map<Object, Object> statusMap = redis.opsForHash().entries(statusKey);

    Map<String, String> result = new LinkedHashMap<>();

    // 1~120번 좌석에 대해 상태 확인
    for (int i = 1; i <= TOTAL_SEATS; i++) {
      String seatId = String.valueOf(i);
      String holdKey = String.format(SEAT_HOLD_KEY_PATTERN, scheduleId, i);

      // 홀드 상태 확인
      String holdValue = redis.opsForValue().get(holdKey);
      if (holdValue != null) {
        result.put(seatId, "HELD");
      } else {
        // 기본 상태
        Object status = statusMap.get(seatId);
        result.put(seatId, status != null ? (String) status : "");
      }
    }

    return result;
  }

  /**
   * 공연 스케줄의 좌석 상태를 삭제합니다.
   * 
   * @param scheduleId 공연 스케줄 ID
   */
  public void deleteSeatStatuses(Long scheduleId) {
    String statusKey = String.format(SEAT_STATUS_KEY_PATTERN, scheduleId);

    // 모든 홀드 키 삭제
    for (int i = 1; i <= TOTAL_SEATS; i++) {
      String holdKey = String.format(SEAT_HOLD_KEY_PATTERN, scheduleId, i);
      redis.delete(holdKey);
    }

    // 상태 키 삭제
    redis.delete(statusKey);

    log.info("좌석 상태 삭제 완료: scheduleId={}", scheduleId);
  }
}
