package com.pyokemon.account.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.pyokemon.account.auth.dto.request.LoginRequestDto;
import com.pyokemon.account.auth.dto.request.UpdatePasswordRequestDto;
import com.pyokemon.account.auth.dto.response.LoginResponseDto;
import com.pyokemon.account.auth.dto.response.TokenResponseDto;
import com.pyokemon.account.auth.entity.Account;
import com.pyokemon.account.auth.entity.AccountStatus;
import com.pyokemon.account.auth.repository.AccountRepository;
import com.pyokemon.account.auth.secret.jwt.TokenGenerator;
import com.pyokemon.common.exception.BusinessException;

import io.jsonwebtoken.Claims;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

  @Mock
  private AccountRepository accountRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private TokenGenerator tokenGenerator;

  @Mock
  private RedisTemplate<String, String> redisTemplate;

  @Mock
  private ValueOperations<String, String> valueOperations;

  @InjectMocks
  private AccountService accountService;

  private Account testAccount;

  @BeforeEach
  void setUp() {
    // 테스트용 계정 데이터 생성
    testAccount = new Account();
    testAccount.setAccountId(1L);
    testAccount.setLoginId("test@example.com");
    testAccount.setPassword("encodedPassword");
    testAccount.setRole("USER");
    testAccount.setStatus(AccountStatus.ACTIVE);
  }

  @Test
  @DisplayName("로그인 성공 테스트")
  void loginSuccess() {
    // given (테스트 준비)
    LoginRequestDto request = new LoginRequestDto();
    request.setLoginId("test@example.com");
    request.setPassword("password123");

    // Mock 설정
    when(accountRepository.findByLoginId("test@example.com")).thenReturn(Optional.of(testAccount));
    when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
    when(tokenGenerator.generateAccessToken(1L, "USER")).thenReturn("access-token");
    when(tokenGenerator.generateRefreshToken(1L, "USER")).thenReturn("refresh-token");

    // when (테스트 실행)
    LoginResponseDto response = accountService.login(request);

    // then (결과 검증)
    assertNotNull(response);
    assertEquals("access-token", response.getAccessToken());
    assertEquals("refresh-token", response.getRefreshToken());
    assertEquals("USER", response.getRole());
    assertEquals(1L, response.getAccountId());

    // Mock 메소드가 호출되었는지 검증
    verify(accountRepository).findByLoginId("test@example.com");
    verify(passwordEncoder).matches("password123", "encodedPassword");
    verify(tokenGenerator).generateAccessToken(1L, "USER");
    verify(tokenGenerator).generateRefreshToken(1L, "USER");
  }

  @Test
  @DisplayName("로그인 실패 - 계정 없음")
  void loginFailAccountNotFound() {
    // given
    LoginRequestDto request = new LoginRequestDto();
    request.setLoginId("nonexistent@example.com");
    request.setPassword("password123");

    when(accountRepository.findByLoginId("nonexistent@example.com")).thenReturn(Optional.empty());

    // when & then
    assertThrows(BusinessException.class, () -> accountService.login(request));

    verify(accountRepository).findByLoginId("nonexistent@example.com");
    verifyNoInteractions(passwordEncoder);
    verifyNoInteractions(tokenGenerator);
  }

  @Test
  @DisplayName("로그인 실패 - 비밀번호 불일치")
  void loginFailPasswordMismatch() {
    // given
    LoginRequestDto request = new LoginRequestDto();
    request.setLoginId("test@example.com");
    request.setPassword("wrongPassword");

    when(accountRepository.findByLoginId("test@example.com")).thenReturn(Optional.of(testAccount));
    when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

    // when & then
    assertThrows(BusinessException.class, () -> accountService.login(request));

    verify(accountRepository).findByLoginId("test@example.com");
    verify(passwordEncoder).matches("wrongPassword", "encodedPassword");
    verifyNoInteractions(tokenGenerator);
  }

  @Test
  @DisplayName("로그인 실패 - 삭제된 계정")
  void loginFailDeletedAccount() {
    // given
    testAccount.setStatus(AccountStatus.DELETED);
    LoginRequestDto request = new LoginRequestDto();
    request.setLoginId("test@example.com");
    request.setPassword("password123");

    when(accountRepository.findByLoginId("test@example.com")).thenReturn(Optional.of(testAccount));

    // when & then
    assertThrows(BusinessException.class, () -> accountService.login(request));

    verify(accountRepository).findByLoginId("test@example.com");
    verifyNoInteractions(passwordEncoder);
    verifyNoInteractions(tokenGenerator);
  }

  // ========== 로그아웃 테스트 ==========

  @Test
  @DisplayName("로그아웃 성공 테스트")
  void logoutSuccess() {
    // given
    String token = "Bearer valid-token";
    Claims claims = mock(Claims.class);

    when(tokenGenerator.parseToken("valid-token")).thenReturn(claims);
    when(claims.getExpiration())
        .thenReturn(new java.util.Date(System.currentTimeMillis() + 3600000)); // 1시간 후
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);

    // when
    accountService.logout(token);

    // then
    verify(tokenGenerator).parseToken("valid-token");
    verify(claims).getExpiration();
    verify(redisTemplate).opsForValue();
    verify(valueOperations).set(eq("blacklist:valid-token"), eq("blacklisted"), anyLong(),
        eq(java.util.concurrent.TimeUnit.SECONDS));
  }

  @Test
  @DisplayName("로그아웃 - Bearer 접두사 없는 토큰")
  void logoutWithoutBearerPrefix() {
    // given
    String token = "valid-token";
    Claims claims = mock(Claims.class);

    when(tokenGenerator.parseToken("valid-token")).thenReturn(claims);
    when(claims.getExpiration())
        .thenReturn(new java.util.Date(System.currentTimeMillis() + 3600000));
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);

    // when
    accountService.logout(token);

    // then
    verify(tokenGenerator).parseToken("valid-token");
    verify(redisTemplate).opsForValue();
  }

  @Test
  @DisplayName("로그아웃 - 토큰 파싱 실패")
  void logoutTokenParseFailure() {
    // given
    String token = "Bearer invalid-token";

    when(tokenGenerator.parseToken("invalid-token"))
        .thenThrow(new RuntimeException("Invalid token"));

    // when & then (예외가 발생하지 않아야 함)
    assertDoesNotThrow(() -> accountService.logout(token));

    verify(tokenGenerator).parseToken("invalid-token");
    verifyNoInteractions(redisTemplate);
  }

  // ========== 토큰 갱신 테스트 ==========

  @Test
  @DisplayName("토큰 갱신 성공 테스트")
  void refreshTokenSuccess() {
    // given
    String refreshToken = "valid-refresh-token";
    Claims claims = mock(Claims.class);

    when(tokenGenerator.validateToken(refreshToken)).thenReturn(true);
    when(redisTemplate.hasKey("blacklist:" + refreshToken)).thenReturn(false);
    when(tokenGenerator.parseToken(refreshToken)).thenReturn(claims);
    when(claims.getSubject()).thenReturn("1");
    when(claims.get("role", String.class)).thenReturn("USER");
    when(accountRepository.findByAccountId(1L)).thenReturn(Optional.of(testAccount));
    when(tokenGenerator.generateAccessToken(1L, "USER")).thenReturn("new-access-token");

    // when
    TokenResponseDto response = accountService.refreshToken(refreshToken);

    // then
    assertNotNull(response);
    assertEquals("new-access-token", response.getAccessToken());

    verify(tokenGenerator).validateToken(refreshToken);
    verify(redisTemplate).hasKey("blacklist:" + refreshToken);
    verify(tokenGenerator).parseToken(refreshToken);
    verify(accountRepository).findByAccountId(1L);
    verify(tokenGenerator).generateAccessToken(1L, "USER");
  }

  @Test
  @DisplayName("토큰 갱신 실패 - 유효하지 않은 토큰")
  void refreshTokenInvalidToken() {
    // given
    String refreshToken = "invalid-refresh-token";

    when(tokenGenerator.validateToken(refreshToken)).thenReturn(false);

    // when & then
    assertThrows(BusinessException.class, () -> accountService.refreshToken(refreshToken));

    verify(tokenGenerator).validateToken(refreshToken);
    verifyNoInteractions(redisTemplate);
    verifyNoInteractions(accountRepository);
  }

  @Test
  @DisplayName("토큰 갱신 실패 - 블랙리스트된 토큰")
  void refreshTokenBlacklistedToken() {
    // given
    String refreshToken = "blacklisted-token";

    when(tokenGenerator.validateToken(refreshToken)).thenReturn(true);
    when(redisTemplate.hasKey("blacklist:" + refreshToken)).thenReturn(true);

    // when & then
    assertThrows(BusinessException.class, () -> accountService.refreshToken(refreshToken));

    verify(tokenGenerator).validateToken(refreshToken);
    verify(redisTemplate).hasKey("blacklist:" + refreshToken);
    verifyNoInteractions(accountRepository);
  }

  @Test
  @DisplayName("토큰 갱신 실패 - 계정 없음")
  void refreshTokenAccountNotFound() {
    // given
    String refreshToken = "valid-refresh-token";
    Claims claims = mock(Claims.class);

    when(tokenGenerator.validateToken(refreshToken)).thenReturn(true);
    when(redisTemplate.hasKey("blacklist:" + refreshToken)).thenReturn(false);
    when(tokenGenerator.parseToken(refreshToken)).thenReturn(claims);
    when(claims.getSubject()).thenReturn("999");
    when(claims.get("role", String.class)).thenReturn("USER");
    when(accountRepository.findByAccountId(999L)).thenReturn(Optional.empty());

    // when & then
    assertThrows(BusinessException.class, () -> accountService.refreshToken(refreshToken));

    verify(tokenGenerator).validateToken(refreshToken);
    verify(redisTemplate).hasKey("blacklist:" + refreshToken);
    verify(tokenGenerator).parseToken(refreshToken);
    verify(accountRepository).findByAccountId(999L);
  }

  @Test
  @DisplayName("토큰 갱신 실패 - 삭제된 계정")
  void refreshTokenDeletedAccount() {
    // given
    String refreshToken = "valid-refresh-token";
    Claims claims = mock(Claims.class);
    testAccount.setStatus(AccountStatus.DELETED);

    when(tokenGenerator.validateToken(refreshToken)).thenReturn(true);
    when(redisTemplate.hasKey("blacklist:" + refreshToken)).thenReturn(false);
    when(tokenGenerator.parseToken(refreshToken)).thenReturn(claims);
    when(claims.getSubject()).thenReturn("1");
    when(claims.get("role", String.class)).thenReturn("USER");
    when(accountRepository.findByAccountId(1L)).thenReturn(Optional.of(testAccount));

    // when & then
    assertThrows(BusinessException.class, () -> accountService.refreshToken(refreshToken));

    verify(tokenGenerator).validateToken(refreshToken);
    verify(redisTemplate).hasKey("blacklist:" + refreshToken);
    verify(tokenGenerator).parseToken(refreshToken);
    verify(accountRepository).findByAccountId(1L);
  }

  // ========== 비밀번호 변경 테스트 ==========

  @Test
  @DisplayName("비밀번호 변경 성공 테스트")
  void changePasswordSuccess() {
    // given
    Long accountId = 1L;
    UpdatePasswordRequestDto request = new UpdatePasswordRequestDto();
    request.setCurrentPassword("oldPassword");
    request.setNewPassword("newPassword123");

    when(accountRepository.findByAccountId(accountId)).thenReturn(Optional.of(testAccount));
    when(passwordEncoder.matches("oldPassword", "encodedPassword")).thenReturn(true);
    when(passwordEncoder.matches("newPassword123", "encodedPassword")).thenReturn(false);
    when(passwordEncoder.encode("newPassword123")).thenReturn("newEncodedPassword");
    when(accountRepository.update(any(Account.class))).thenReturn(1);

    // when
    accountService.changePassword(accountId, request);

    // then
    verify(accountRepository).findByAccountId(accountId);
    verify(passwordEncoder).matches("oldPassword", "encodedPassword");
    verify(passwordEncoder).matches("newPassword123", "encodedPassword");
    verify(passwordEncoder).encode("newPassword123");
    verify(accountRepository).update(any(Account.class));
  }

  @Test
  @DisplayName("비밀번호 변경 실패 - 계정 없음")
  void changePasswordAccountNotFound() {
    // given
    Long accountId = 999L;
    UpdatePasswordRequestDto request = new UpdatePasswordRequestDto();
    request.setCurrentPassword("oldPassword");
    request.setNewPassword("newPassword123");

    when(accountRepository.findByAccountId(accountId)).thenReturn(Optional.empty());

    // when & then
    assertThrows(BusinessException.class, () -> accountService.changePassword(accountId, request));

    verify(accountRepository).findByAccountId(accountId);
    verifyNoInteractions(passwordEncoder);
  }

  @Test
  @DisplayName("비밀번호 변경 실패 - 현재 비밀번호 불일치")
  void changePasswordCurrentPasswordMismatch() {
    // given
    Long accountId = 1L;
    UpdatePasswordRequestDto request = new UpdatePasswordRequestDto();
    request.setCurrentPassword("wrongPassword");
    request.setNewPassword("newPassword123");

    when(accountRepository.findByAccountId(accountId)).thenReturn(Optional.of(testAccount));
    when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

    // when & then
    assertThrows(BusinessException.class, () -> accountService.changePassword(accountId, request));

    verify(accountRepository).findByAccountId(accountId);
    verify(passwordEncoder).matches("wrongPassword", "encodedPassword");
  }

  @Test
  @DisplayName("비밀번호 변경 실패 - 새 비밀번호가 현재 비밀번호와 동일")
  void changePasswordNewPasswordSameAsCurrent() {
    // given
    Long accountId = 1L;
    UpdatePasswordRequestDto request = new UpdatePasswordRequestDto();
    request.setCurrentPassword("oldPassword");
    request.setNewPassword("oldPassword");

    when(accountRepository.findByAccountId(accountId)).thenReturn(Optional.of(testAccount));
    when(passwordEncoder.matches("oldPassword", "encodedPassword")).thenReturn(true);

    // when & then
    assertThrows(BusinessException.class, () -> accountService.changePassword(accountId, request));

    verify(accountRepository).findByAccountId(accountId);
    verify(passwordEncoder, times(2)).matches("oldPassword", "encodedPassword");
  }

  // ========== 계정 삭제 테스트 ==========

  @Test
  @DisplayName("계정 삭제 성공 테스트")
  void deleteAccountSuccess() {
    // given
    Long accountId = 1L;

    when(accountRepository.findByAccountId(accountId)).thenReturn(Optional.of(testAccount));
    when(accountRepository.updateStatus(accountId, AccountStatus.DELETED)).thenReturn(1);

    // when
    accountService.deleteAccount(accountId);

    // then
    verify(accountRepository).findByAccountId(accountId);
    verify(accountRepository).updateStatus(accountId, AccountStatus.DELETED);
  }

  @Test
  @DisplayName("계정 삭제 실패 - 계정 없음")
  void deleteAccountNotFound() {
    // given
    Long accountId = 999L;

    when(accountRepository.findByAccountId(accountId)).thenReturn(Optional.empty());

    // when & then
    assertThrows(BusinessException.class, () -> accountService.deleteAccount(accountId));

    verify(accountRepository).findByAccountId(accountId);
  }
}
