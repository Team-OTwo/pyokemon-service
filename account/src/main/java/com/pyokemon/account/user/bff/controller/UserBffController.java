package com.pyokemon.account.user.bff.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.pyokemon.account.common.web.context.GatewayRequestHeaderUtils;
import com.pyokemon.account.user.bff.dto.UserDto;
import com.pyokemon.account.user.bff.service.UserBffService;
import com.pyokemon.account.user.dto.request.CreateUserRequestDto;
import com.pyokemon.account.user.dto.request.RegisterDeviceRequestDto;
import com.pyokemon.account.user.dto.request.UpdateUserRequestDto;
import com.pyokemon.account.user.dto.response.UserDetailDto;
import com.pyokemon.account.user.service.UserService;
import com.pyokemon.common.dto.IdsRequest;
import com.pyokemon.common.dto.ResponseDto;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserBffController {

  private final UserBffService userBffService;

  @GetMapping("/{accountId}")
  public UserDto getUser(@PathVariable Long accountId) {
    return userBffService.getUser(accountId);
  }

  @PostMapping("/_batch")
  public List<UserDto> getUsers(@RequestBody IdsRequest request) {
    return userBffService.getUsers(request.getIds());
  }
}
