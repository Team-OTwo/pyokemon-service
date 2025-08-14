package com.pyokemon.account.user.bff.controller;

import com.pyokemon.account.user.bff.dto.UserDto;
import com.pyokemon.account.user.bff.service.UserBffService;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.pyokemon.account.common.web.context.GatewayRequestHeaderUtils;
import com.pyokemon.account.user.dto.request.CreateUserRequestDto;
import com.pyokemon.account.user.dto.request.RegisterDeviceRequestDto;
import com.pyokemon.account.user.dto.request.UpdateUserRequestDto;
import com.pyokemon.account.user.dto.response.UserDetailDto;
import com.pyokemon.account.user.service.UserService;
import com.pyokemon.common.dto.ResponseDto;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserBffController {

    private final UserBffService userBffService;

    @GetMapping("/{accountId}")
    public UserDto getUser(@PathVariable Long accountId) {
        return userBffService.getUser(accountId);
    }

}
