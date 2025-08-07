package com.pyokemon.account.user.controller;

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

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  // 사용자 계정 생성
  @PostMapping
  public ResponseEntity<ResponseDto<UserDetailDto>> registerUser(
      @Valid @RequestBody CreateUserRequestDto request) {
    UserDetailDto response = userService.registerUser(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ResponseDto.success(response, "사용자 등록 성공"));
  }

  // 사용자 계정 상세 조회 (사용자 본인만)
  @GetMapping("/profile")
  public ResponseEntity<ResponseDto<UserDetailDto>> getUserProfile() {
    String currentUserAccountId = GatewayRequestHeaderUtils.getUserIdOrThrowException();
    Long userId = Long.parseLong(currentUserAccountId);
    UserDetailDto response = userService.getUserProfile(userId);
    return ResponseEntity.ok(ResponseDto.success(response, "사용자 정보 조회 성공"));
  }

  // 사용자 정보 수정 (사용자 본인만)
  @PutMapping("/profile")
  public ResponseEntity<ResponseDto<UserDetailDto>> updateUserProfile(
      @Valid @RequestBody UpdateUserRequestDto request) {
    String currentUserAccountId = GatewayRequestHeaderUtils.getUserIdOrThrowException();
    Long userId = Long.parseLong(currentUserAccountId);
    UserDetailDto response = userService.updateUserProfile(userId, request);
    return ResponseEntity.ok(ResponseDto.success(response, "사용자 정보 수정 성공"));
  }

  // 사용자 계정 삭제 (탈퇴) (사용자 본인만)
  @DeleteMapping("/profile")
  public ResponseEntity<ResponseDto<Void>> deleteUser() {
    String currentUserAccountId = GatewayRequestHeaderUtils.getUserIdOrThrowException();
    Long userId = Long.parseLong(currentUserAccountId);
    userService.deleteUser(userId);
    return ResponseEntity.ok(ResponseDto.success("사용자 탈퇴 성공"));
  }

  // 사용자 기기 등록 (사용자 본인만)
  @PostMapping("/devices")
  public ResponseEntity<ResponseDto<Void>> registerUserDevice(
      @Valid @RequestBody RegisterDeviceRequestDto request) {
    String currentUserAccountId = GatewayRequestHeaderUtils.getUserIdOrThrowException();
    Long userId = Long.parseLong(currentUserAccountId);
    userService.registerUserDevice(userId, request);
    return ResponseEntity.ok(ResponseDto.success("기기 등록 성공"));
  }

  // 사용자 기기 삭제 (사용자 본인만)
  @DeleteMapping("/devices/{deviceId}")
  public ResponseEntity<ResponseDto<Void>> deleteUserDevice(@PathVariable String deviceId) {
    String currentUserAccountId = GatewayRequestHeaderUtils.getUserIdOrThrowException();
    Long userId = Long.parseLong(currentUserAccountId);
    userService.deleteUserDevice(userId, deviceId);
    return ResponseEntity.ok(ResponseDto.success("기기 삭제 성공"));
  }
}
