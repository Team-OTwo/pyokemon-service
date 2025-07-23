package com.pyokemon.user.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.pyokemon.common.dto.ResponseDto;
import com.pyokemon.user.api.dto.UserRegisterRequest;
import com.pyokemon.user.api.dto.UserResponse;
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
            @Valid @RequestBody UserRegisterRequest request) {
        return ResponseEntity.ok(
            ResponseDto.success(
                UserResponse.from(
                    userService.registerUser(request)
                )
            )
        );
    }
}
