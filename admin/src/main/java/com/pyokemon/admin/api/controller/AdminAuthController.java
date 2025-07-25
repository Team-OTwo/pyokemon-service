package com.pyokemon.admin.api.controller;

import com.pyokemon.admin.dto.AdminLoginDto;
import com.pyokemon.admin.secret.jwt.dto.TokenDto;
import com.pyokemon.admin.service.AdminService;
import com.pyokemon.admin.util.PasswordUtil;
import com.pyokemon.common.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminService adminService;
    private final PasswordUtil passwordUtil;

     // 관리자 로그인 POST /api/admin/auth/login
    @PostMapping("/login")
    public ResponseEntity<ResponseDto<TokenDto.AccessRefreshToken>> login(@Valid @RequestBody AdminLoginDto loginDto) {
        TokenDto.AccessRefreshToken tokens = adminService.login(loginDto);
        return ResponseEntity.ok(ResponseDto.success(tokens, "로그인 성공"));
    }

    /**
     * 리프레시 토큰으로 새 액세스 토큰 발급
     * POST /api/admin/auth/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<ResponseDto<TokenDto.AccessToken>> refresh(@RequestHeader("Authorization") String authHeader) {
        // "Bearer " 접두사 제거
        String refreshToken = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        
        // 리프레시 토큰으로 새 액세스 토큰 발급
        TokenDto.AccessToken accessToken = adminService.refreshAccessToken(refreshToken);
        return ResponseEntity.ok(ResponseDto.success(accessToken, "토큰 갱신 성공"));
    }

    /**
     * 로그아웃
     * POST /api/admin/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<ResponseDto<Void>> logout(@RequestHeader("Authorization") String authHeader) {
        // "Bearer " 접두사 제거
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        
        // 로그아웃 처리 (토큰 무효화 등)
        // 실제 구현은 서비스에서 처리
        return ResponseEntity.ok(ResponseDto.success("로그아웃 성공"));
    }
} 