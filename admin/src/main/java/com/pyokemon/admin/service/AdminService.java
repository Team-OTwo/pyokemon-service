package com.pyokemon.admin.service;

import com.pyokemon.admin.dto.AdminLoginDto;
import com.pyokemon.admin.entity.Admin;
import com.pyokemon.admin.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminService {
    
    private final AdminRepository adminRepository;
    
    public String login(AdminLoginDto loginDto) {
        Optional<Admin> adminOpt = adminRepository.findByUsername(loginDto.getUsername());
        
        if (adminOpt.isEmpty()) {
            throw new RuntimeException("존재하지 않는 사용자입니다.");
        }
        
        Admin admin = adminOpt.get();
        
        // 실제로는 BCrypt로 암호화된 비밀번호를 비교해야 함
        if (!admin.getPassword().equals(loginDto.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }
        
        // 실제로는 JWT 토큰을 생성해야 함
        return "admin_token_" + admin.getId();
    }
    
    // 관리자 조회
    // public Optional<Admin> getAdminById(Long id) {
    //     return adminRepository.findById(id);
    // }
    
    public Optional<Admin> getAdminByUsername(String username) {
        return adminRepository.findByUsername(username);
    }
} 