package com.pyokemon.user.api.controller;


import com.pyokemon.user.api.dto.UserLoginRequestDto;
import com.pyokemon.user.api.secret.jwt.dto.TokenDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.pyokemon.common.dto.ResponseDto;
import com.pyokemon.user.api.dto.UserRegisterRequestDto;
import com.pyokemon.user.api.dto.UserResponse;
import com.pyokemon.user.api.dto.UserUpdateRequestDto;
import com.pyokemon.user.api.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ResponseDto<UserResponse>> registerUser(
            @Valid @RequestBody UserRegisterRequestDto request) {
        return ResponseEntity.ok(
                ResponseDto.success(
                        UserResponse.from(
                                userService.registerUser(request))));
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseDto<TokenDto.AccessRefreshToken>> loginUser(
            @Valid @RequestBody UserLoginRequestDto request) {
        TokenDto.AccessRefreshToken tokens = userService.loginUser(request);
        return ResponseEntity.ok(ResponseDto.success(tokens, "로그인 성공"));
    }

    @PostMapping("/update")
    public ResponseEntity<ResponseDto<UserResponse>> updateUser(
            @Valid @RequestBody UserUpdateRequestDto request) {
        return ResponseEntity.ok(ResponseDto.success(UserResponse.from(userService.updateUser(request))));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ResponseDto<TokenDto.AccessToken>> refresh(@RequestHeader("Authorization") String authHeader) {
        // "Bearer " 접두사 제거
        String refreshToken = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;

        TokenDto.AccessToken accessToken = userService.refreshAccessToken(refreshToken);
        return ResponseEntity.ok(ResponseDto.success(accessToken, "토큰 갱신 성공"));
    }
}
