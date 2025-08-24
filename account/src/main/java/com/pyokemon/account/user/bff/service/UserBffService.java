package com.pyokemon.account.user.bff.service;

// import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pyokemon.account.auth.entity.Account;
import com.pyokemon.account.auth.entity.AccountStatus;
import com.pyokemon.account.auth.repository.AccountRepository;
import com.pyokemon.account.user.bff.dto.UserDto;
import com.pyokemon.account.user.bff.repository.UserBffRepository;
import com.pyokemon.account.user.dto.request.CreateUserRequestDto;
import com.pyokemon.account.user.dto.request.RegisterDeviceRequestDto;
import com.pyokemon.account.user.dto.request.UpdateUserRequestDto;
import com.pyokemon.account.user.dto.response.UserDetailDto;
import com.pyokemon.account.user.entity.User;
import com.pyokemon.account.user.entity.UserDevice;
import com.pyokemon.account.user.repository.UserDeviceRepository;
import com.pyokemon.account.user.repository.UserRepository;
import com.pyokemon.common.dto.IdsRequest;
import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.common.exception.code.AccountErrorCodes;
import com.pyokemon.common.util.PasswordUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserBffService {

  private final UserBffRepository userBffRepository;

  public UserDto getUser(Long accountId) {
    Optional<User> userOpt = userBffRepository.findByAccountId(accountId);

    if (userOpt.isEmpty()) {
      throw new BusinessException("사용자를 찾을 수 없습니다.", AccountErrorCodes.USER_NOT_FOUND);
    }

    User user = userOpt.get();

    return UserDto.builder().accountId(accountId).name(user.getName()).build();
  }

  public List<UserDto> getUsers(List<Long> accountIds) {
    if (accountIds == null || accountIds.isEmpty())
      return List.of();

    // DB 호출은 1번
    List<User> users = userBffRepository.findAllByAccountIdIn(accountIds);

    Map<Long, User> byId =
        users.stream().collect(Collectors.toMap(User::getAccountId, Function.identity()));

    // 요청 순서/중복 그대로 매핑, 누락 시 기존 정책대로 예외
    List<Long> missing =
        accountIds.stream().filter(id -> !byId.containsKey(id)).distinct().toList();
    if (!missing.isEmpty()) {
      throw new BusinessException("사용자를 찾을 수 없습니다. ids=" + missing,
          AccountErrorCodes.USER_NOT_FOUND);
    }

    return accountIds.stream().map(id -> {
      User u = byId.get(id);
      return UserDto.builder().accountId(id).name(u.getName()).build();
    }).toList();
  }
}
