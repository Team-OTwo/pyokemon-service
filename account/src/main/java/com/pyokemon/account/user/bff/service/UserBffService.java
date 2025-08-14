package com.pyokemon.account.user.bff.service;

// import org.springframework.security.crypto.password.PasswordEncoder;

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
import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.common.exception.code.AccountErrorCodes;
import com.pyokemon.common.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserBffService {

  private final UserBffRepository userBffRepository;

  public UserDto getUser(Long accountId){
    Optional<User> userOpt = userBffRepository.findByAccountId(accountId);

    if(userOpt.isEmpty()){
      throw new BusinessException("없음 ㅋㅋ", "ㅋㅋㄹㅃㅃ");
    }

    User user = userOpt.get();

    return UserDto.builder()
            .accountId(accountId)
            .name(user.getName())
            .build();
  }
}
