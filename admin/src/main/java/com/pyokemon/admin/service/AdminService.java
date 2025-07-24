package com.pyokemon.admin.service;

import com.pyokemon.admin.dto.AdminLoginDto;
import com.pyokemon.admin.entity.Admin;
import com.pyokemon.admin.repository.AdminRepository;
import com.pyokemon.admin.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminService {
    
    private final AdminRepository adminRepository;
    private final PasswordUtil passwordUtil;
    
    public String login(AdminLoginDto loginDto) {
        Optional<Admin> adminOpt = adminRepository.findByUsername(loginDto.getUsername());
        
        if (adminOpt.isEmpty()) {
            throw new RuntimeException("존재하지 않는 사용자입니다.");
        }
        
        Admin admin = adminOpt.get();
        
        // BCrypt로 해싱된 비밀번호 검증
        if (!passwordUtil.matchPassword(loginDto.getPassword(), admin.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }
        
        // 실제로는 JWT 토큰을 생성해야 함
        return "admin_token_" + admin.getId();
    }

    
    public Optional<Admin> getAdminByUsername(String username) {
        return adminRepository.findByUsername(username);
    }
    
    /**
     * 새로운 관리자 계정을 생성합니다.
     * 비밀번호는 해싱되어 저장됩니다.
     * 
     * @param admin 생성할 관리자 정보
     * @return 생성된 관리자 정보
     */
    public Admin createAdmin(Admin admin) {
        // 중복 사용자명 확인
        if (adminRepository.findByUsername(admin.getUsername()).isPresent()) {
            throw new RuntimeException("이미 존재하는 사용자명입니다.");
        }
        
        // 비밀번호 해싱
        admin.setPassword(passwordUtil.encodePassword(admin.getPassword()));
        
        // 역할이 설정되지 않은 경우 기본값 설정
        if (admin.getRole() == null || admin.getRole().isEmpty()) {
            admin.setRole("ADMIN");
        }
        
        // 관리자 저장
        int result = adminRepository.save(admin);
        if (result != 1) {
            throw new RuntimeException("관리자 계정 생성에 실패했습니다.");
        }
        
        return admin;
    }
} 