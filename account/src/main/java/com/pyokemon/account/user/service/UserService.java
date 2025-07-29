package com.pyokemon.account.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pyokemon.account.auth.entity.Account;
import com.pyokemon.account.auth.repository.AccountRepository;
import com.pyokemon.account.user.dto.request.UpdateUserProfileRequestDto;
import com.pyokemon.account.user.dto.request.UserDeviceRequestDto;
import com.pyokemon.account.user.dto.request.UserRegisterRequestDto;
import com.pyokemon.account.user.dto.response.UserProfileResponseDto;
import com.pyokemon.account.user.entity.User;
import com.pyokemon.account.user.entity.UserDevice;
import com.pyokemon.account.user.repository.UserDeviceRepository;
import com.pyokemon.account.user.repository.UserRepository;
import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.common.exception.code.AccountErrorCodes;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final AccountRepository accountRepository;
  private final UserDeviceRepository userDeviceRepository;
  private final PasswordEncoder passwordEncoder;

  @Transactional
  public UserProfileResponseDto registerUser(UserRegisterRequestDto request) {
    // TODO: 구현 필요
    return UserProfileResponseDto.builder().userId(1L).accountId(1L).loginId(request.getLoginId())
        .name(request.getName()).phone(request.getPhone()).birth(request.getBirth()).build();
  }

  @Transactional(readOnly = true)
  public UserProfileResponseDto getUserProfile(Long userId) {
    // TODO: 구현 필요
    return UserProfileResponseDto.builder().userId(userId).accountId(1L).loginId("dummy-login-id")
        .name("dummy-name").phone("dummy-phone").birth(java.time.LocalDate.now()).build();
  }

  @Transactional
  public UserProfileResponseDto updateUserProfile(Long userId,
      UpdateUserProfileRequestDto request) {
    // TODO: 구현 필요
    return UserProfileResponseDto.builder().userId(userId).accountId(1L).loginId("dummy-login-id")
        .name(request.getName()).phone(request.getPhone()).birth(request.getBirth()).build();
  }

  @Transactional
  public void deleteUser(Long userId) {
    // TODO: 구현 필요
  }

  @Transactional
  public void registerUserDevice(Long userId, UserDeviceRequestDto request) {
    // TODO: 구현 필요
  }

  @Transactional
  public void deleteUserDevice(Long userId, String deviceNumber) {
    // TODO: 구현 필요
  }
}
