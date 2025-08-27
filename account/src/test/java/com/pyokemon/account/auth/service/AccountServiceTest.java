package com.pyokemon.account.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.pyokemon.account.auth.constants.AuthConstants;
import com.pyokemon.account.auth.dto.request.LoginRequestDto;
import com.pyokemon.account.auth.dto.request.UpdatePasswordRequestDto;
import com.pyokemon.account.auth.dto.response.LoginResponseDto;
import com.pyokemon.account.auth.dto.response.TokenResponseDto;
import com.pyokemon.account.auth.entity.Account;
import com.pyokemon.account.auth.entity.AccountStatus;
import com.pyokemon.account.auth.repository.AccountRepository;
import com.pyokemon.account.auth.secret.jwt.TokenGenerator;
import com.pyokemon.account.user.entity.User;
import com.pyokemon.account.user.entity.UserDevice;
import com.pyokemon.account.user.repository.UserDeviceRepository;
import com.pyokemon.account.user.repository.UserRepository;
import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.common.util.PasswordUtil;

import io.jsonwebtoken.Claims;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

  @Mock
  private AccountRepository accountRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private UserDeviceRepository userDeviceRepository;

  @Mock
  private PasswordUtil passwordUtil;

  @Mock
  private TokenGenerator tokenGenerator;

  @Mock
  private RedisTemplate<String, String> redisTemplate;

  @Mock
  private ValueOperations<String, String> valueOperations;

  @InjectMocks
  private AccountService accountService;

  private Account testAccount;
  private User testUser;
  private UserDevice testUserDevice;

  @BeforeEach
  void setUp() {
    // 테스트용 계정 데이터 생성
    testAccount = new Account();
    testAccount.setAccountId(1L);
    testAccount.setLoginId("test@example.com");
    testAccount.setPassword("encodedPassword");
    testAccount.setRole("USER");
    testAccount.setStatus(AccountStatus.ACTIVE);

    // 테스트용 사용자 데이터 생성
    testUser = new User();
    testUser.setUserId(1L);
    testUser.setAccountId(1L);
    testUser.setName("테스트 사용자");
    testUser.setIsVerified(true);

    // 테스트용 디바이스 데이터 생성
    testUserDevice = new UserDevice();
    testUserDevice.setUserDeviceId(1L);
    testUserDevice.setUserId(1L);
    testUserDevice.setDeviceNumber("device123");
    testUserDevice.setIsValid(true);
    testUserDevice.setIsLogin(false);
  }

  @Test
  @DisplayName("로그인 성공 테스트 - USER 역할")
  void loginSuccess_UserRole() {
    // given
    LoginRequestDto request = new LoginRequestDto();
    request.setLoginId("test@example.com");
    request.setPassword("password123");

    when(accountRepository.findByLoginIdAndStatus("test@example.com", AccountStatus.ACTIVE))
        .thenReturn(Optional.of(testAccount));
    when(passwordUtil.matches("password123", "encodedPassword")).thenReturn(true);
    when(tokenGenerator.generateAccessToken(1L, "USER")).thenReturn("access-token");
    when(tokenGenerator.generateRefreshToken(1L, "USER")).thenReturn("refresh-token");
    when(userRepository.findByAccountId(1L)).thenReturn(Optional.of(testUser));

    // when
    LoginResponseDto response = accountService.login(request);

    // then
    assertNotNull(response);
    assertEquals("access-token", response.getAccessToken());
    assertEquals("refresh-token", response.getRefreshToken());
    assertEquals("USER", response.getRole());
    assertEquals(1L, response.getAccountId());
    assertEquals("테스트 사용자", response.getUserName());
    assertTrue(response.getIsVerified());

    verify(accountRepository).findByLoginIdAndStatus("test@example.com", AccountStatus.ACTIVE);
    verify(passwordUtil).matches("password123", "encodedPassword");
    verify(userRepository).findByAccountId(1L);
    verify(tokenGenerator).generateAccessToken(1L, "USER");
    verify(tokenGenerator).generateRefreshToken(1L, "USER");
  }

  @Test
  @DisplayName("로그인 성공 테스트 - ADMIN 역할")
  void loginSuccess_AdminRole() {
    // given
    testAccount.setRole("ADMIN");
    LoginRequestDto request = new LoginRequestDto();
    request.setLoginId("admin@example.com");
    request.setPassword("password123");

    when(accountRepository.findByLoginIdAndStatus("admin@example.com", AccountStatus.ACTIVE))
        .thenReturn(Optional.of(testAccount));
    when(passwordUtil.matches("password123", "encodedPassword")).thenReturn(true);
    when(tokenGenerator.generateAccessToken(1L, "ADMIN")).thenReturn("access-token");
    when(tokenGenerator.generateRefreshToken(1L, "ADMIN")).thenReturn("refresh-token");

    // when
    LoginResponseDto response = accountService.login(request);

    // then
    assertNotNull(response);
    assertEquals("access-token", response.getAccessToken());
    assertEquals("refresh-token", response.getRefreshToken());
    assertEquals("ADMIN", response.getRole());
    assertEquals(1L, response.getAccountId());
    assertNull(response.getUserName());
    assertNull(response.getIsVerified());

    verify(accountRepository).findByLoginIdAndStatus("admin@example.com", AccountStatus.ACTIVE);
    verify(passwordUtil).matches("password123", "encodedPassword");
    verify(tokenGenerator).generateAccessToken(1L, "ADMIN");
    verify(tokenGenerator).generateRefreshToken(1L, "ADMIN");
    verifyNoInteractions(userRepository);
  }

  @Test
  @DisplayName("로그인 실패 - 계정 없음")
  void loginFailure_AccountNotFound() {
    // given
    LoginRequestDto request = new LoginRequestDto();
    request.setLoginId("nonexistent@example.com");
    request.setPassword("password123");

    when(accountRepository.findByLoginIdAndStatus("nonexistent@example.com", AccountStatus.ACTIVE))
        .thenReturn(Optional.empty());

    // when & then
    BusinessException exception = assertThrows(BusinessException.class, () -> {
      accountService.login(request);
    });

    assertEquals("계정을 찾을 수 없습니다.", exception.getMessage());
    verify(accountRepository).findByLoginIdAndStatus("nonexistent@example.com",
        AccountStatus.ACTIVE);
    verifyNoInteractions(passwordUtil, tokenGenerator, userRepository);
  }

  @Test
  @DisplayName("로그인 실패 - 비밀번호 불일치")
  void loginFailure_InvalidPassword() {
    // given
    LoginRequestDto request = new LoginRequestDto();
    request.setLoginId("test@example.com");
    request.setPassword("wrongpassword");

    when(accountRepository.findByLoginIdAndStatus("test@example.com", AccountStatus.ACTIVE))
        .thenReturn(Optional.of(testAccount));
    when(passwordUtil.matches("wrongpassword", "encodedPassword")).thenReturn(false);

    // when & then
    BusinessException exception = assertThrows(BusinessException.class, () -> {
      accountService.login(request);
    });

    assertEquals("로그인 ID 또는 비밀번호가 올바르지 않습니다.", exception.getMessage());
    verify(accountRepository).findByLoginIdAndStatus("test@example.com", AccountStatus.ACTIVE);
    verify(passwordUtil).matches("wrongpassword", "encodedPassword");
    verifyNoInteractions(tokenGenerator, userRepository);
  }

  @Test
  @DisplayName("토큰 갱신 성공 테스트")
  void refreshTokenSuccess() {
    // given
    String refreshToken = "valid-refresh-token";
    Claims claims = mock(Claims.class);

    when(tokenGenerator.validateToken(refreshToken)).thenReturn(true);
    when(redisTemplate.hasKey(AuthConstants.BLACKLIST_PREFIX + refreshToken)).thenReturn(false);
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
    verify(redisTemplate).hasKey(AuthConstants.BLACKLIST_PREFIX + refreshToken);
    verify(tokenGenerator).parseToken(refreshToken);
    verify(accountRepository).findByAccountId(1L);
    verify(tokenGenerator).generateAccessToken(1L, "USER");
  }

  @Test
  @DisplayName("토큰 갱신 실패 - 유효하지 않은 토큰")
  void refreshTokenFailure_InvalidToken() {
    // given
    String refreshToken = "invalid-refresh-token";
    when(tokenGenerator.validateToken(refreshToken)).thenReturn(false);

    // when & then
    BusinessException exception = assertThrows(BusinessException.class, () -> {
      accountService.refreshToken(refreshToken);
    });

    assertEquals("유효하지 않은 리프레시 토큰입니다.", exception.getMessage());
    verify(tokenGenerator).validateToken(refreshToken);
    verifyNoInteractions(redisTemplate, accountRepository);
  }

  @Test
  @DisplayName("토큰 갱신 실패 - 블랙리스트된 토큰")
  void refreshTokenFailure_BlacklistedToken() {
    // given
    String refreshToken = "blacklisted-token";
    when(tokenGenerator.validateToken(refreshToken)).thenReturn(true);
    when(redisTemplate.hasKey(AuthConstants.BLACKLIST_PREFIX + refreshToken)).thenReturn(true);

    // when & then
    BusinessException exception = assertThrows(BusinessException.class, () -> {
      accountService.refreshToken(refreshToken);
    });

    assertEquals("로그아웃된 토큰입니다.", exception.getMessage());
    verify(tokenGenerator).validateToken(refreshToken);
    verify(redisTemplate).hasKey(AuthConstants.BLACKLIST_PREFIX + refreshToken);
    verifyNoInteractions(accountRepository);
  }

  @Test
  @DisplayName("토큰 갱신 실패 - 삭제된 계정")
  void refreshTokenFailure_DeletedAccount() {
    // given
    String refreshToken = "valid-refresh-token";
    Claims claims = mock(Claims.class);
    testAccount.setStatus(AccountStatus.DELETED);

    when(tokenGenerator.validateToken(refreshToken)).thenReturn(true);
    when(redisTemplate.hasKey(AuthConstants.BLACKLIST_PREFIX + refreshToken)).thenReturn(false);
    when(tokenGenerator.parseToken(refreshToken)).thenReturn(claims);
    when(claims.getSubject()).thenReturn("1");
    when(claims.get("role", String.class)).thenReturn("USER");
    when(accountRepository.findByAccountId(1L)).thenReturn(Optional.of(testAccount));

    // when & then
    BusinessException exception = assertThrows(BusinessException.class, () -> {
      accountService.refreshToken(refreshToken);
    });

    assertEquals("삭제된 계정입니다.", exception.getMessage());
    verify(tokenGenerator).validateToken(refreshToken);
    verify(redisTemplate).hasKey(AuthConstants.BLACKLIST_PREFIX + refreshToken);
    verify(tokenGenerator).parseToken(refreshToken);
    verify(accountRepository).findByAccountId(1L);
  }

  // ========== 로그아웃 테스트 ==========

  @Test
  @DisplayName("로그아웃 성공 - 토큰 블랙리스트 추가")
  void logoutSuccess_blacklistToken() {
    // given
    String token = "valid-token";
    String fullToken = "Bearer " + token;
    Claims claims = mock(Claims.class);
    Date expiration = new Date(System.currentTimeMillis() + 3600000); // 1 hour expiry

    when(tokenGenerator.parseToken(token)).thenReturn(claims);
    when(claims.getExpiration()).thenReturn(expiration);
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);

    // when
    accountService.logout(fullToken, null);

    // then
    verify(redisTemplate.opsForValue()).set(
        eq(AuthConstants.BLACKLIST_PREFIX + token),
        eq("blacklisted"),
        anyLong(),
        eq(TimeUnit.SECONDS));
    verifyNoInteractions(userRepository, userDeviceRepository);
  }

  @Test
  @DisplayName("로그아웃 성공 - 디바이스 로그아웃 처리")
  void logoutSuccess_withDeviceLogout() {
    // given
    String token = "valid-token";
    String fullToken = "Bearer " + token;
    String deviceNumber = "device123";
    Claims claims = mock(Claims.class);
    Date expiration = new Date(System.currentTimeMillis() + 3600000);
    testUserDevice.setIsLogin(true);

    when(tokenGenerator.parseToken(token)).thenReturn(claims);
    when(claims.getExpiration()).thenReturn(expiration);
    when(claims.getSubject()).thenReturn("1");
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(userRepository.findByAccountId(1L)).thenReturn(Optional.of(testUser));
    when(userDeviceRepository.findByUserIdAndDeviceNumberAndIsValid(testUser.getUserId(), deviceNumber, true))
        .thenReturn(Optional.of(testUserDevice));

    // when
    accountService.logout(fullToken, deviceNumber);

    // then
    verify(redisTemplate.opsForValue()).set(
        eq(AuthConstants.BLACKLIST_PREFIX + token),
        anyString(),
        anyLong(),
        eq(TimeUnit.SECONDS));
    verify(userRepository).findByAccountId(1L);
    verify(userDeviceRepository).findByUserIdAndDeviceNumberAndIsValid(testUser.getUserId(), deviceNumber, true);
    verify(userDeviceRepository).update(testUserDevice);
    assertFalse(testUserDevice.getIsLogin());
  }

  @Test
  @DisplayName("로그아웃 - 이미 만료된 토큰")
  void logout_expiredToken() {
    // given
    String token = "expired-token";
    String fullToken = "Bearer " + token;
    Claims claims = mock(Claims.class);
    Date expiration = new Date(System.currentTimeMillis() - 1000); // Expired 1 sec ago

    when(tokenGenerator.parseToken(token)).thenReturn(claims);
    when(claims.getExpiration()).thenReturn(expiration);

    // when
    accountService.logout(fullToken, "device123");

    // then
    verify(redisTemplate, never()).opsForValue();
    verifyNoInteractions(userRepository, userDeviceRepository);
  }

  @Test
  @DisplayName("로그아웃 - 토큰 없음")
  void logout_noToken() {
    // when
    accountService.logout(null, "device123");

    // then
    verifyNoInteractions(tokenGenerator, redisTemplate, userRepository, userDeviceRepository);
  }

  @Test
  @DisplayName("로그아웃 - 유효하지 않은 토큰 파싱 예외")
  void logout_tokenParseException() {
    // given
    String token = "invalid-token";
    String fullToken = "Bearer " + token;
    when(tokenGenerator.parseToken(token)).thenThrow(new RuntimeException("Invalid token"));

    // when
    accountService.logout(fullToken, "device123");

    // then
    verify(redisTemplate, never()).opsForValue();
    verifyNoInteractions(userRepository, userDeviceRepository);
  }

  @Test
    @DisplayName("계정 삭제 성공 테스트")
    void deleteAccountSuccess() {
        // given
        when(accountRepository.findByAccountId(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.updateStatus(1L, AccountStatus.DELETED)).thenReturn(1);

        // when
        assertDoesNotThrow(() -> {
            accountService.deleteAccount(1L);
        });

        // then
        verify(accountRepository).findByAccountId(1L);
        verify(accountRepository).updateStatus(1L, AccountStatus.DELETED);
    }
}
