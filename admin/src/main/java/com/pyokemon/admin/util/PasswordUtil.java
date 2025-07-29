package com.pyokemon.admin.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 비밀번호 암호화 및 검증을 위한 유틸리티 클래스
 */
@Component
public class PasswordUtil {

  private final PasswordEncoder passwordEncoder;

  public PasswordUtil() {
    this.passwordEncoder = new BCryptPasswordEncoder();
  }

  /**
   * 평문 비밀번호를 해싱하여 반환합니다.
   * 
   * @param rawPassword 평문 비밀번호
   * @return 해싱된 비밀번호
   */
  public String encodePassword(String rawPassword) {
    return passwordEncoder.encode(rawPassword);
  }

  /**
   * 평문 비밀번호와 해싱된 비밀번호가 일치하는지 검증합니다.
   * 
   * @param rawPassword 평문 비밀번호
   * @param encodedPassword 해싱된 비밀번호
   * @return 일치 여부
   */
  public boolean matchPassword(String rawPassword, String encodedPassword) {
    return passwordEncoder.matches(rawPassword, encodedPassword);
  }
}
