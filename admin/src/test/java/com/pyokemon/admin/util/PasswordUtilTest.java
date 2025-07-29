package com.pyokemon.admin.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PasswordUtilTest {

  @Autowired
  private PasswordUtil passwordUtil;

  @Test
  @DisplayName("비밀번호 해싱 테스트")
  void testEncodePassword() {
    // given
    String rawPassword = "admin123";

    // when
    String encodedPassword = passwordUtil.encodePassword(rawPassword);

    // then
    assertNotNull(encodedPassword);
    assertNotEquals(rawPassword, encodedPassword);
    assertTrue(encodedPassword.startsWith("$2a$")); // BCrypt 해싱 알고리즘의 접두사 확인
  }

  @Test
  @DisplayName("비밀번호 검증 테스트 - 일치하는 경우")
  void testMatchPasswordSuccess() {
    // given
    String rawPassword = "admin123";
    String encodedPassword = passwordUtil.encodePassword(rawPassword);

    // when
    boolean isMatch = passwordUtil.matchPassword(rawPassword, encodedPassword);

    // then
    assertTrue(isMatch);
  }

  @Test
  @DisplayName("비밀번호 검증 테스트 - 일치하지 않는 경우")
  void testMatchPasswordFailure() {
    // given
    String rawPassword = "admin123";
    String wrongPassword = "wrong123";
    String encodedPassword = passwordUtil.encodePassword(rawPassword);

    // when
    boolean isMatch = passwordUtil.matchPassword(wrongPassword, encodedPassword);

    // then
    assertFalse(isMatch);
  }
}
