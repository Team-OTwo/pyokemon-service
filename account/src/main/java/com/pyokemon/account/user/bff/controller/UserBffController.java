package com.pyokemon.account.user.bff.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.pyokemon.account.user.bff.dto.UserDto;
import com.pyokemon.account.user.bff.service.UserBffService;
import com.pyokemon.common.dto.IdsRequest;

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
