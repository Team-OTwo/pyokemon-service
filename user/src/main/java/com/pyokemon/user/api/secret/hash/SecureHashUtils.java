package com.pyokemon.user.api.secret.hash;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SecureHashUtils {

  // 입력받은 문자열을 해시값으로 변환하는 메서드 (SHA-256)
  public static String hash(String message) {
    try {
      // SHA-256 MessageDigest 인스턴스 생성
      MessageDigest digest = MessageDigest.getInstance("SHA-256");

      // 문자열을 바이트 배열로 변환 후 해시 계산
      byte[] hashBytes = digest.digest(message.getBytes(StandardCharsets.UTF_8));

      // 바이트 배열을 16진수 문자열로 변환
      StringBuilder hexString = new StringBuilder();
      for (byte b : hashBytes) {
        String hex = Integer.toHexString(0xff & b);
        if (hex.length() == 1) {
          hexString.append('0'); // 한 자리 수면 앞에 0 추가
        }
        hexString.append(hex);
      }

      return hexString.toString();

    } catch (NoSuchAlgorithmException e) {
      // SHA-256이 지원되지 않는 경우 (일반적으로 발생하지 않음)
      throw new RuntimeException("SHA-256 해시 알고리즘을 찾을 수 없습니다", e);
    }
  }

  // 입력 문자열을 해시해서 기존 해시값과 비교
  public static boolean matches(String message, String hashedMessage) {
    String hashed = hash(message);

    return hashed.equals(hashedMessage);
  }
}
