package com.pyokemon.admin.api.controller;

import com.pyokemon.admin.dto.AdminLoginDto;
import com.pyokemon.admin.entity.Admin;
import com.pyokemon.admin.service.AdminService;
import com.pyokemon.common.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminService adminService;

    @PostMapping("/login")
    public ResponseEntity<ResponseDto<Admin>> login(@Valid @RequestBody AdminLoginDto loginDto) {
        Optional<Admin> adminOptional = adminService.getAdminByUsername(loginDto.getUsername());
        
        if (adminOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(ResponseDto.error("존재하지 않는 관리자입니다.", "ADMIN_NOT_FOUND"));
        }
        
        Admin admin = adminOptional.get();
        // 실제 환경에서는 비밀번호 검증 로직이 필요합니다.
        // 여기서는 간단히 구현합니다.
        if (!admin.getPassword().equals(loginDto.getPassword())) {
            return ResponseEntity.badRequest().body(ResponseDto.error("비밀번호가 일치하지 않습니다.", "INVALID_PASSWORD"));
        }
        
        // 실제 환경에서는 토큰 생성 및 반환이 필요합니다.
        return ResponseEntity.ok(ResponseDto.success(admin, "로그인 성공"));
        // to do: 토큰 생성 및 반환 해야함
    }
} 