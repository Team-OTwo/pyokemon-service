package com.pyokemon.common.util;

import java.util.UUID;

/**
 * UUID 생성을 위한 유틸리티 클래스
 */
public class UuidGenerator {

  private UuidGenerator() {
    // 인스턴스화 방지
  }

  /**
   * 랜덤 UUID 문자열 생성
   * 
   * @return UUID 문자열
   */
  public static String generateUuid() {
    return UUID.randomUUID().toString();
  }

  /**
   * 챌린지용 랜덤 문자열 생성
   * 
   * @return 챌린지 문자열
   */
  public static String generateChallenge() {
    return UUID.randomUUID().toString();
  }

  /**
   * 특정 접두사가 포함된 UUID 생성
   * 
   * @param prefix UUID 앞에 붙일 접두사
   * @return 접두사가 포함된 UUID 문자열
   */
  public static String generatePrefixedUuid(String prefix) {
    return prefix + "-" + UUID.randomUUID().toString();
  }
}
