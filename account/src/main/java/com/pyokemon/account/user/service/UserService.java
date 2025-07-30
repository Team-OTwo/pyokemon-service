package com.pyokemon.account.user.service;

import com.pyokemon.account.user.entity.UserDevice;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pyokemon.account.auth.entity.Account;
import com.pyokemon.account.auth.repository.AccountRepository;
import com.pyokemon.account.user.dto.request.UpdateUserRequestDto;
import com.pyokemon.account.user.dto.request.RegisterDeviceRequestDto;
import com.pyokemon.account.user.dto.request.CreateUserRequestDto;
import com.pyokemon.account.user.dto.response.UserDetailDto;
import com.pyokemon.account.user.entity.User;
import com.pyokemon.account.user.repository.UserDeviceRepository;
import com.pyokemon.account.user.repository.UserRepository;
import com.pyokemon.common.exception.code.AccountErrorCodes;
import com.pyokemon.account.auth.entity.AccountStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final AccountRepository accountRepository;
  private final UserDeviceRepository userDeviceRepository;
  private final PasswordEncoder passwordEncoder;

  // 계정 생성
  @Transactional
  public UserDetailDto registerUser(CreateUserRequestDto request) {

    if (accountRepository.existsByLoginIdAndStatus(request.getLoginId(), AccountStatus.ACTIVE)) {
      throw new RuntimeException(AccountErrorCodes.LOGIN_ID_DUPLICATED);
    }

    Account account = Account.builder()
            .loginId(request.getLoginId())
            .password(passwordEncoder.encode(request.getPassword()))
            .role("USER")
            .status(AccountStatus.ACTIVE)
            .build();

    accountRepository.insert(account);

    User user = User.builder()
            .accountId(account.getAccountId())
            .name(request.getName())
            .phone(request.getPhone())
            .birth(request.getBirth())
            .build();

    userRepository.insert(user);

    return UserDetailDto.builder()
            .loginId(request.getLoginId())
            .name(request.getName())
            .phone(request.getPhone())
            .birth(request.getBirth())
            .build();
  }

  // 사용자 정보 조회
  @Transactional(readOnly = true)
  public UserDetailDto getUserProfile(Long userId) {
    User user = userRepository.findByUserId(userId)
        .orElseThrow(() -> new RuntimeException(AccountErrorCodes.ACCOUNT_NOT_FOUND));

    return UserDetailDto.builder()
            .name(user.getName())
            .phone(user.getPhone())
            .birth(user.getBirth())
            .build();
  }

  // 사용자 정보 수정
  @Transactional
  public UserDetailDto updateUserProfile(Long userId,
                                         UpdateUserRequestDto request) {
    User user = userRepository.findByUserId(userId)
        .orElseThrow(() -> new RuntimeException(AccountErrorCodes.ACCOUNT_NOT_FOUND));

    user.setName(request.getName());
    user.setPhone(request.getPhone());
    user.setBirth(request.getBirth());

    userRepository.update(user);

    return UserDetailDto.builder()
            .name(user.getName())
            .phone(user.getPhone())
            .birth(user.getBirth())
            .build();
  }

  @Transactional
  public void deleteUser(Long userId) {
    User user = userRepository.findByUserId(userId)
        .orElseThrow(() -> new RuntimeException(AccountErrorCodes.ACCOUNT_NOT_FOUND));

    
    accountRepository.updateStatus(user.getAccountId(), AccountStatus.DELETED);
  }

  @Transactional
  public void registerUserDevice(Long userId, RegisterDeviceRequestDto request) {
    User user = userRepository.findByUserId(userId)
        .orElseThrow(() -> new RuntimeException(AccountErrorCodes.ACCOUNT_NOT_FOUND));

    if (userDeviceRepository.existsByDeviceNumberAndIsValid(request.getDeviceNumber(), true)) {
      throw new RuntimeException(AccountErrorCodes.DEVICE_ALREADY_REGISTERED);
    }

    UserDevice userDevice = UserDevice.builder()
            .userId(user.getUserId())
            .deviceNumber(request.getDeviceNumber())
            .fcmToken(request.getFcmToken())
            .osType(request.getOsType())
            .isValid(true)
            .build();

    userDeviceRepository.insert(userDevice);
  }

  @Transactional
  public void deleteUserDevice(Long userId, String deviceNumber) {
    UserDevice userDevice = userDeviceRepository.findByUserIdAndDeviceNumberAndIsValid(userId, deviceNumber, true)
        .orElseThrow(() -> new RuntimeException(AccountErrorCodes.DEVICE_NOT_FOUND));
    
    userDevice.setIsValid(false);

    userDeviceRepository.update(userDevice);
  }
}
