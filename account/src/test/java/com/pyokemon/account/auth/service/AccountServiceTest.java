package com.pyokemon.account.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.pyokemon.account.auth.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

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
import com.pyokemon.account.user.repository.UserRepository;
import com.pyokemon.account.user.repository.UserDeviceRepository;
import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.common.util.PasswordUtil;

import io.jsonwebtoken.Claims;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    @Mock private AccountRepository accountRepository;
    @Mock private UserRepository userRepository;
    @Mock private UserDeviceRepository userDeviceRepository;

    @Mock private PasswordUtil passwordUtil;
    @Mock private TokenGenerator tokenGenerator;

    @Mock private RedisTemplate<String, String> redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private AccountService accountService;

    private Account testAccount;

    @BeforeEach
    void setUp() {
        testAccount = new Account();
        testAccount.setAccountId(1L);
        testAccount.setLoginId("test@example.com");
        testAccount.setPassword("encodedPassword");
        testAccount.setRole("USER");
        testAccount.setStatus(AccountStatus.ACTIVE);
    }

    // ========== 로그인 ==========

    @Test
    @DisplayName("로그인 성공 테스트")
    void loginSuccess() {
        LoginRequestDto request = new LoginRequestDto();
        request.setLoginId("test@example.com");
        request.setPassword("password123");

        when(accountRepository.findByLoginIdAndStatus("test@example.com", AccountStatus.ACTIVE))
                .thenReturn(Optional.of(testAccount));
        when(passwordUtil.matches("password123", "encodedPassword")).thenReturn(true);
        when(tokenGenerator.generateAccessToken(1L, "USER")).thenReturn("access-token");
        when(tokenGenerator.generateRefreshToken(1L, "USER")).thenReturn("refresh-token");

        LoginResponseDto response = accountService.login(request);

        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals("USER", response.getRole());
        assertEquals(1L, response.getAccountId());

        verify(accountRepository).findByLoginIdAndStatus("test@example.com", AccountStatus.ACTIVE);
        verify(passwordUtil).matches("password123", "encodedPassword");
        verify(tokenGenerator).generateAccessToken(1L, "USER");
        verify(tokenGenerator).generateRefreshToken(1L, "USER");
    }

    @Test
    @DisplayName("로그인 실패 - 계정 없음")
    void loginFailAccountNotFound() {
        LoginRequestDto request = new LoginRequestDto();
        request.setLoginId("nonexistent@example.com");
        request.setPassword("password123");

        when(accountRepository.findByLoginId("nonexistent@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> accountService.login(request));

        verify(accountRepository).findByLoginId("nonexistent@example.com");
        verifyNoInteractions(passwordUtil);
        verifyNoInteractions(tokenGenerator);
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void loginFailPasswordMismatch() {
        LoginRequestDto request = new LoginRequestDto();
        request.setLoginId("test@example.com");
        request.setPassword("wrongPassword");

        when(accountRepository.findByLoginId("test@example.com"))
                .thenReturn(Optional.of(testAccount));
        when(passwordUtil.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        assertThrows(BusinessException.class, () -> accountService.login(request));

        verify(accountRepository).findByLoginId("test@example.com");
        verify(passwordUtil).matches("wrongPassword", "encodedPassword");
        verifyNoInteractions(tokenGenerator);
    }

    @Test
    @DisplayName("로그인 실패 - 삭제된 계정")
    void loginFailDeletedAccount() {
        testAccount.setStatus(AccountStatus.DELETED);
        LoginRequestDto request = new LoginRequestDto();
        request.setLoginId("test@example.com");
        request.setPassword("password123");

        when(accountRepository.findByLoginId("test@example.com"))
                .thenReturn(Optional.of(testAccount));

        assertThrows(BusinessException.class, () -> accountService.login(request));

        verify(accountRepository).findByLoginId("test@example.com");
        verifyNoInteractions(passwordUtil);
        verifyNoInteractions(tokenGenerator);
    }

    // ========== 로그아웃 (변경된 시그니처: token, accountId, deviceNumber) ==========

    @Test
    @DisplayName("로그아웃 성공 - USER 역할, 토큰 블랙리스트 추가 + 디바이스 로그아웃")
    void logoutSuccess_UserRole_DeviceLogout() {
        String tokenHeader = "Bearer valid-token";
        String accountId   = "1";
        String deviceNumber = null; // role == USER 이므로 디바이스 처리 분기 진입

        Claims claims = mock(Claims.class);
        when(tokenGenerator.parseToken("valid-token")).thenReturn(claims);
        when(claims.getExpiration()).thenReturn(new java.util.Date(System.currentTimeMillis() + 3600000));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // account -> user -> userDevice
        when(accountRepository.findByAccountId(1L)).thenReturn(Optional.of(testAccount));

        User user = new User();
        user.setUserId(10L);
        user.setAccountId(1L);
        when(userRepository.findByAccountId(1L)).thenReturn(Optional.of(user));

        UserDevice device = new UserDevice();
        device.setUserId(10L);
        device.setIsValid(true);
        device.setIsLogin(true);
        when(userDeviceRepository.findByUserIdAndIsValid(10L, true)).thenReturn(Optional.of(device));

        accountService.logout(tokenHeader, accountId, deviceNumber);

        verify(tokenGenerator).parseToken("valid-token");
        verify(redisTemplate).opsForValue();
        verify(valueOperations).set(eq("blacklist:valid-token"), eq("blacklisted"), anyLong(), eq(TimeUnit.SECONDS));

        verify(accountRepository).findByAccountId(1L);
        verify(userRepository).findByAccountId(1L);
        verify(userDeviceRepository).findByUserIdAndIsValid(10L, true);
        verify(userDeviceRepository).update(argThat(d -> d.getIsLogin() == Boolean.FALSE));
    }

    @Test
    @DisplayName("로그아웃 성공 - ADMIN 역할이지만 deviceNumber 제공 시 디바이스 분기 진입")
    void logoutSuccess_AdminRole_WithDeviceNumber() {
        testAccount.setRole("ADMIN");

        String tokenHeader = "Bearer valid-token";
        String accountId   = "1";
        String deviceNumber = "device-001"; // deviceNumber != null 로 인해 분기 진입

        Claims claims = mock(Claims.class);
        when(tokenGenerator.parseToken("valid-token")).thenReturn(claims);
        when(claims.getExpiration()).thenReturn(new java.util.Date(System.currentTimeMillis() + 3600000));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        when(accountRepository.findByAccountId(1L)).thenReturn(Optional.of(testAccount));

        User user = new User();
        user.setUserId(10L);
        user.setAccountId(1L);
        when(userRepository.findByAccountId(1L)).thenReturn(Optional.of(user));

        UserDevice device = new UserDevice();
        device.setUserId(10L);
        device.setIsValid(true);
        device.setIsLogin(true);
        when(userDeviceRepository.findByUserIdAndIsValid(10L, true)).thenReturn(Optional.of(device));

        accountService.logout(tokenHeader, accountId, deviceNumber);

        verify(userDeviceRepository).update(argThat(d -> d.getIsLogin() == Boolean.FALSE));
    }

    @Test
    @DisplayName("로그아웃 - 토큰 null이면 조기 반환 (레디스/리포지토리 미호출)")
    void logout_WithNullToken_ReturnsEarly() {
        accountService.logout(null, "1", null);

        verifyNoInteractions(tokenGenerator);
        verifyNoInteractions(redisTemplate);
        verifyNoInteractions(accountRepository);
        verifyNoInteractions(userRepository);
        verifyNoInteractions(userDeviceRepository);
    }

    @Test
    @DisplayName("로그아웃 - Bearer 접두사 없는 토큰도 허용")
    void logoutWithoutBearerPrefix() {
        String token = "valid-token"; // 접두사 없음
        String accountId = "1";

        Claims claims = mock(Claims.class);
        when(tokenGenerator.parseToken("valid-token")).thenReturn(claims);
        when(claims.getExpiration()).thenReturn(new java.util.Date(System.currentTimeMillis() + 3600000));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        when(accountRepository.findByAccountId(1L)).thenReturn(Optional.of(testAccount));

        User user = new User();
        user.setUserId(10L); user.setAccountId(1L);
        when(userRepository.findByAccountId(1L)).thenReturn(Optional.of(user));

        UserDevice device = new UserDevice();
        device.setUserId(10L); device.setIsValid(true); device.setIsLogin(true);
        when(userDeviceRepository.findByUserIdAndIsValid(10L, true)).thenReturn(Optional.of(device));

        accountService.logout(token, accountId, null);

        verify(tokenGenerator).parseToken("valid-token");
        verify(redisTemplate).opsForValue();
        verify(userDeviceRepository).update(any(UserDevice.class));
    }

    @Test
    @DisplayName("로그아웃 - 토큰 파싱 실패 시 레디스는 미호출, 디바이스 분기 위해 계정/유저/디바이스는 여전히 필요")
    void logoutTokenParseFailure() {
        String tokenHeader = "Bearer invalid-token";
        String accountId   = "1";

        when(tokenGenerator.parseToken("invalid-token"))
                .thenThrow(new RuntimeException("Invalid token"));

        // 파싱 실패 후에도 서비스는 계정/유저/디바이스 로직을 진행하므로 아래 모킹 필요
        when(accountRepository.findByAccountId(1L)).thenReturn(Optional.of(testAccount));

        User user = new User();
        user.setUserId(10L); user.setAccountId(1L);
        when(userRepository.findByAccountId(1L)).thenReturn(Optional.of(user));

        UserDevice device = new UserDevice();
        device.setUserId(10L); device.setIsValid(true); device.setIsLogin(true);
        when(userDeviceRepository.findByUserIdAndIsValid(10L, true)).thenReturn(Optional.of(device));

        assertDoesNotThrow(() -> accountService.logout(tokenHeader, accountId, null));

        verify(tokenGenerator).parseToken("invalid-token");
        verifyNoInteractions(valueOperations); // 레디스 블랙리스트 호출 없음
        verify(userDeviceRepository).update(any(UserDevice.class)); // 디바이스는 업데이트
    }

    @Test
    @DisplayName("로그아웃 실패 - 계정 없음")
    void logout_AccountNotFound() {
        String tokenHeader = "Bearer valid-token";
        String accountId   = "999";

        Claims claims = mock(Claims.class);
        when(tokenGenerator.parseToken("valid-token")).thenReturn(claims);
        when(claims.getExpiration()).thenReturn(new java.util.Date(System.currentTimeMillis() + 3600000));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        when(accountRepository.findByAccountId(999L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> accountService.logout(tokenHeader, accountId, null));

        verify(accountRepository).findByAccountId(999L);
        verifyNoInteractions(userRepository);
        verifyNoInteractions(userDeviceRepository);
    }

    @Test
    @DisplayName("로그아웃 실패 - USER 분기 진입 후 사용자 없음")
    void logout_UserMissing() {
        String tokenHeader = "Bearer valid-token";
        String accountId   = "1";

        Claims claims = mock(Claims.class);
        when(tokenGenerator.parseToken("valid-token")).thenReturn(claims);
        when(claims.getExpiration()).thenReturn(new java.util.Date(System.currentTimeMillis() + 3600000));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        when(accountRepository.findByAccountId(1L)).thenReturn(Optional.of(testAccount));
        when(userRepository.findByAccountId(1L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> accountService.logout(tokenHeader, accountId, null));

        verify(userRepository).findByAccountId(1L);
        verifyNoInteractions(userDeviceRepository);
    }

    @Test
    @DisplayName("로그아웃 실패 - 디바이스 없음")
    void logout_DeviceMissing() {
        String tokenHeader = "Bearer valid-token";
        String accountId   = "1";

        Claims claims = mock(Claims.class);
        when(tokenGenerator.parseToken("valid-token")).thenReturn(claims);
        when(claims.getExpiration()).thenReturn(new java.util.Date(System.currentTimeMillis() + 3600000));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        when(accountRepository.findByAccountId(1L)).thenReturn(Optional.of(testAccount));

        User user = new User(); user.setUserId(10L); user.setAccountId(1L);
        when(userRepository.findByAccountId(1L)).thenReturn(Optional.of(user));

        when(userDeviceRepository.findByUserIdAndIsValid(10L, true)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> accountService.logout(tokenHeader, accountId, null));

        verify(userDeviceRepository).findByUserIdAndIsValid(10L, true);
    }

    // ========== 토큰 갱신 ==========

    @Test
    @DisplayName("토큰 갱신 성공 테스트")
    void refreshTokenSuccess() {
        String refreshToken = "valid-refresh-token";
        Claims claims = mock(Claims.class);

        when(tokenGenerator.validateToken(refreshToken)).thenReturn(true);
        when(redisTemplate.hasKey("blacklist:" + refreshToken)).thenReturn(false);
        when(tokenGenerator.parseToken(refreshToken)).thenReturn(claims);
        when(claims.getSubject()).thenReturn("1");
        when(claims.get("role", String.class)).thenReturn("USER");
        when(accountRepository.findByAccountId(1L)).thenReturn(Optional.of(testAccount));
        when(tokenGenerator.generateAccessToken(1L, "USER")).thenReturn("new-access-token");

        TokenResponseDto response = accountService.refreshToken(refreshToken);

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
        String refreshToken = "invalid-refresh-token";

        when(tokenGenerator.validateToken(refreshToken)).thenReturn(false);

        assertThrows(BusinessException.class, () -> accountService.refreshToken(refreshToken));

        verify(tokenGenerator).validateToken(refreshToken);
        verifyNoInteractions(redisTemplate);
        verifyNoInteractions(accountRepository);
    }

    @Test
    @DisplayName("토큰 갱신 실패 - 블랙리스트된 토큰")
    void refreshTokenBlacklistedToken() {
        String refreshToken = "blacklisted-token";

        when(tokenGenerator.validateToken(refreshToken)).thenReturn(true);
        when(redisTemplate.hasKey("blacklist:" + refreshToken)).thenReturn(true);

        assertThrows(BusinessException.class, () -> accountService.refreshToken(refreshToken));

        verify(tokenGenerator).validateToken(refreshToken);
        verify(redisTemplate).hasKey("blacklist:" + refreshToken);
        verifyNoInteractions(accountRepository);
    }

    @Test
    @DisplayName("토큰 갱신 실패 - 계정 없음")
    void refreshTokenAccountNotFound() {
        String refreshToken = "valid-refresh-token";
        Claims claims = mock(Claims.class);

        when(tokenGenerator.validateToken(refreshToken)).thenReturn(true);
        when(redisTemplate.hasKey("blacklist:" + refreshToken)).thenReturn(false);
        when(tokenGenerator.parseToken(refreshToken)).thenReturn(claims);
        when(claims.getSubject()).thenReturn("999");
        when(claims.get("role", String.class)).thenReturn("USER");
        when(accountRepository.findByAccountId(999L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> accountService.refreshToken(refreshToken));

        verify(tokenGenerator).validateToken(refreshToken);
        verify(redisTemplate).hasKey("blacklist:" + refreshToken);
        verify(tokenGenerator).parseToken(refreshToken);
        verify(accountRepository).findByAccountId(999L);
    }

    @Test
    @DisplayName("토큰 갱신 실패 - 삭제된 계정")
    void refreshTokenDeletedAccount() {
        String refreshToken = "valid-refresh-token";
        Claims claims = mock(Claims.class);
        testAccount.setStatus(AccountStatus.DELETED);

        when(tokenGenerator.validateToken(refreshToken)).thenReturn(true);
        when(redisTemplate.hasKey("blacklist:" + refreshToken)).thenReturn(false);
        when(tokenGenerator.parseToken(refreshToken)).thenReturn(claims);
        when(claims.getSubject()).thenReturn("1");
        when(claims.get("role", String.class)).thenReturn("USER");
        when(accountRepository.findByAccountId(1L)).thenReturn(Optional.of(testAccount));

        assertThrows(BusinessException.class, () -> accountService.refreshToken(refreshToken));

        verify(tokenGenerator).validateToken(refreshToken);
        verify(redisTemplate).hasKey("blacklist:" + refreshToken);
        verify(tokenGenerator).parseToken(refreshToken);
        verify(accountRepository).findByAccountId(1L);
    }

    // ========== 비밀번호 변경 ==========

    @Test
    @DisplayName("비밀번호 변경 성공 테스트")
    void changePasswordSuccess() {
        Long accountId = 1L;
        UpdatePasswordRequestDto request = new UpdatePasswordRequestDto();
        request.setCurrentPassword("oldPassword");
        request.setNewPassword("newPassword123");

        when(accountRepository.findByAccountId(accountId)).thenReturn(Optional.of(testAccount));
        when(passwordUtil.matches("oldPassword", "encodedPassword")).thenReturn(true);
        when(passwordUtil.matches("newPassword123", "encodedPassword")).thenReturn(false);
        when(passwordUtil.encode("newPassword123")).thenReturn("newEncodedPassword");
        when(accountRepository.update(any(Account.class))).thenReturn(1);

        accountService.changePassword(accountId, request);

        verify(accountRepository).findByAccountId(accountId);
        verify(passwordUtil).matches("oldPassword", "encodedPassword");
        verify(passwordUtil).matches("newPassword123", "encodedPassword");
        verify(passwordUtil).encode("newPassword123");
        verify(accountRepository).update(any(Account.class));
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 계정 없음")
    void changePasswordAccountNotFound() {
        Long accountId = 999L;
        UpdatePasswordRequestDto request = new UpdatePasswordRequestDto();
        request.setCurrentPassword("oldPassword");
        request.setNewPassword("newPassword123");

        when(accountRepository.findByAccountId(accountId)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> accountService.changePassword(accountId, request));

        verify(accountRepository).findByAccountId(accountId);
        verifyNoInteractions(passwordUtil);
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 현재 비밀번호 불일치")
    void changePasswordCurrentPasswordMismatch() {
        Long accountId = 1L;
        UpdatePasswordRequestDto request = new UpdatePasswordRequestDto();
        request.setCurrentPassword("wrongPassword");
        request.setNewPassword("newPassword123");

        when(accountRepository.findByAccountId(accountId)).thenReturn(Optional.of(testAccount));
        when(passwordUtil.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        assertThrows(BusinessException.class, () -> accountService.changePassword(accountId, request));

        verify(accountRepository).findByAccountId(accountId);
        verify(passwordUtil).matches("wrongPassword", "encodedPassword");
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 새 비밀번호가 현재 비밀번호와 동일")
    void changePasswordNewPasswordSameAsCurrent() {
        Long accountId = 1L;
        UpdatePasswordRequestDto request = new UpdatePasswordRequestDto();
        request.setCurrentPassword("oldPassword");
        request.setNewPassword("oldPassword");

        when(accountRepository.findByAccountId(accountId)).thenReturn(Optional.of(testAccount));
        when(passwordUtil.matches("oldPassword", "encodedPassword")).thenReturn(true);

        assertThrows(BusinessException.class, () -> accountService.changePassword(accountId, request));

        verify(accountRepository).findByAccountId(accountId);
        verify(passwordUtil, times(2)).matches("oldPassword", "encodedPassword");
    }

    // ========== 계정 삭제 ==========

    @Test
    @DisplayName("계정 삭제 성공 테스트")
    void deleteAccountSuccess() {
        Long accountId = 1L;

        when(accountRepository.findByAccountId(accountId)).thenReturn(Optional.of(testAccount));
        when(accountRepository.updateStatus(accountId, AccountStatus.DELETED)).thenReturn(1);

        accountService.deleteAccount(accountId);

        verify(accountRepository).findByAccountId(accountId);
        verify(accountRepository).updateStatus(accountId, AccountStatus.DELETED);
    }

    @Test
    @DisplayName("계정 삭제 실패 - 계정 없음")
    void deleteAccountNotFound() {
        Long accountId = 999L;

        when(accountRepository.findByAccountId(accountId)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> accountService.deleteAccount(accountId));

        verify(accountRepository).findByAccountId(accountId);
    }
}
