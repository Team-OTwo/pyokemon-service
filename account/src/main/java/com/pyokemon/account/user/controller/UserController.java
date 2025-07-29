package com.pyokemon.account.user.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.pyokemon.account.common.annotation.UserOnly;
import com.pyokemon.account.user.dto.request.UpdateUserProfileRequestDto;
import com.pyokemon.account.user.dto.request.UserDeviceRequestDto;
import com.pyokemon.account.user.dto.request.UserRegisterRequestDto;
import com.pyokemon.account.user.dto.response.UserProfileResponseDto;
import com.pyokemon.account.user.service.UserService;
import com.pyokemon.common.dto.ResponseDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  // 사용자 계정 생성
  @PostMapping
  public ResponseEntity<ResponseDto<UserProfileResponseDto>> registerUser(
      @Valid @RequestBody UserRegisterRequestDto request) {
    UserProfileResponseDto response = userService.registerUser(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ResponseDto.success(response, "사용자 등록 성공"));
  }

  // 사용자 계정 상세 조회
  @UserOnly
  @GetMapping("/profile")
  public ResponseEntity<ResponseDto<UserProfileResponseDto>> getUserProfile() {
    // TODO: 구현 필요
    return ResponseEntity.ok(ResponseDto.success(null, "사용자 정보 조회 성공"));
  }

  // 사용자 정보 수정
  @UserOnly
  @PutMapping("/profile")
  public ResponseEntity<ResponseDto<UserProfileResponseDto>> updateUserProfile(
      @Valid @RequestBody UpdateUserProfileRequestDto request) {
    // TODO: 구현 필요
    return ResponseEntity.ok(ResponseDto.success(null, "사용자 정보 수정 성공"));
  }

  // 사용자 계정 삭제 (탈퇴)
  @UserOnly
  @DeleteMapping("/profile")
  public ResponseEntity<ResponseDto<Void>> deleteUser() {
    // TODO: 구현 필요
    return ResponseEntity.ok(ResponseDto.success("사용자 탈퇴 성공"));
  }

  // 사용자 기기 등록
  @UserOnly
  @PostMapping("/devices")
  public ResponseEntity<ResponseDto<Void>> registerUserDevice(
      @Valid @RequestBody UserDeviceRequestDto request) {
    // TODO: 구현 필요
    return ResponseEntity.ok(ResponseDto.success("기기 등록 성공"));
  }

  // 사용자 기기 삭제
  @UserOnly
  @DeleteMapping("/devices/{deviceId}")
  public ResponseEntity<ResponseDto<Void>> deleteUserDevice(@PathVariable String deviceId) {
    // TODO: 구현 필요
    return ResponseEntity.ok(ResponseDto.success("기기 삭제 성공"));
  }
}
