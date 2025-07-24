package com.pyokemon.admin.api.controller;

import com.pyokemon.admin.dto.AdminLoginDto;
import com.pyokemon.admin.dto.JwtResponseDto;
import com.pyokemon.admin.entity.Admin;
import com.pyokemon.admin.service.AdminService;
import com.pyokemon.admin.util.JwtUtil;
import com.pyokemon.admin.util.PasswordUtil;
import com.pyokemon.admin.web.context.GatewayRequestHeaderUtils;
import com.pyokemon.common.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminService adminService;
    private final JwtUtil jwtUtil;
    private final PasswordUtil passwordUtil;

     // 관리자 로그인 POST /api/admin/auth/login
    @PostMapping("/login")
    public ResponseEntity<ResponseDto<String>> login(@Valid @RequestBody AdminLoginDto loginDto) {
        Optional<Admin> adminOptional = adminService.getAdminByUsername(loginDto.getUsername());
        
        if (adminOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(ResponseDto.error("존재하지 않는 관리자입니다.", "ADMIN_NOT_FOUND"));
        }
        
        Admin admin = adminOptional.get();
        // 비밀번호 검증 - BCrypt 해싱 비교
        if (!passwordUtil.matchPassword(loginDto.getPassword(), admin.getPassword())) {
            return ResponseEntity.badRequest().body(ResponseDto.error("비밀번호가 일치하지 않습니다.", "INVALID_PASSWORD"));
        }

        String accessToken = jwtUtil.generateAccessToken(admin.getUsername(), "ADMIN");

        return ResponseEntity.ok(ResponseDto.success(accessToken, "로그인 성공"));
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

     // 토큰 검증 (테스트용) GET /api/admin/auth/verify
    @GetMapping("/verify")
    public ResponseEntity<ResponseDto<Map<String, Object>>> verifyToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(ResponseDto.error("유효하지 않은 Authorization 헤더입니다.", "INVALID_AUTH_HEADER"));
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.badRequest().body(ResponseDto.error("유효하지 않은 토큰입니다.", "INVALID_TOKEN"));
        }

        if (jwtUtil.isTokenExpired(token)) {
            return ResponseEntity.badRequest().body(ResponseDto.error("만료된 토큰입니다.", "EXPIRED_TOKEN"));
        }

        Map<String, Object> tokenInfo = new HashMap<>();
        tokenInfo.put("username", jwtUtil.getUsernameFromToken(token));
        tokenInfo.put("role", jwtUtil.getRoleFromToken(token));
        tokenInfo.put("expiration", jwtUtil.getExpirationDateFromToken(token));
        tokenInfo.put("isValid", true);

        return ResponseEntity.ok(ResponseDto.success(tokenInfo, "토큰 검증 성공"));
    }

    // 토큰 갱신 (테스트용) POST /api/admin/auth/refresh
    @PostMapping("/refresh")
    public ResponseEntity<ResponseDto<JwtResponseDto>> refreshToken(@RequestBody String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken)) {
            return ResponseEntity.badRequest().body(ResponseDto.error("유효하지 않은 리프레시 토큰입니다.", "INVALID_REFRESH_TOKEN"));
        }

        String username = jwtUtil.getUsernameFromToken(refreshToken);
        String newAccessToken = jwtUtil.generateAccessToken(username, "ADMIN");
        String newRefreshToken = jwtUtil.generateRefreshToken(username);

        JwtResponseDto jwtResponse = JwtResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(30L * 60L) // 30분
                .username(username)
                .role("ADMIN")
                .build();

        return ResponseEntity.ok(ResponseDto.success(jwtResponse, "토큰 갱신 성공"));
    }

     // 보호된 리소스 접근 (테스트용) GET /api/admin/auth/protected
    @GetMapping("/protected")
    public ResponseEntity<ResponseDto<String>> protectedResource(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(ResponseDto.error("인증이 필요합니다.", "UNAUTHORIZED"));
        }
        
        String token = authHeader.substring(7);
        
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.badRequest().body(ResponseDto.error("유효하지 않은 토큰입니다.", "INVALID_TOKEN"));
        }
        
        String username = jwtUtil.getUsernameFromToken(token);
        String role = jwtUtil.getRoleFromToken(token);
        
        return ResponseEntity.ok(ResponseDto.success(
            "안녕하세요, " + username + "님! (" + role + " 권한으로 접근)", 
            "보호된 리소스 접근 성공"
        ));
    }
} 