package com.pyokemon.admin.service;

import com.pyokemon.admin.dto.AdminLoginDto;
import com.pyokemon.admin.entity.Admin;
import com.pyokemon.admin.exception.AdminException;
import com.pyokemon.admin.repository.AdminRepository;
import com.pyokemon.admin.secret.jwt.TokenGenerator;
import com.pyokemon.admin.secret.jwt.dto.TokenDto;
import com.pyokemon.admin.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {
    
    private final AdminRepository adminRepository;
    private final PasswordUtil passwordUtil;
    private final TokenGenerator tokenGenerator;
    
    public TokenDto.AccessRefreshToken login(AdminLoginDto loginDto) {

        if(loginDto.getAdminId() == null || loginDto.getAdminId().isEmpty()){
            throw new AdminException("아이디는 필수입니다.","LOGIN_ID_REQUIRED");
        }
        if(loginDto.getPassword() == null || loginDto.getPassword().isEmpty()){
            throw new AdminException("비밀번호는 필수입니다.", "PASSWORD_REQUIRED");
        }
        Optional<Admin> adminOpt = adminRepository.findByAdminId(loginDto.getAdminId());

        if (adminOpt.isEmpty()){
            throw AdminException.notFound();
        }

        Admin admin = adminOpt.get();

        if (!passwordUtil.matchPassword(loginDto.getPassword(), admin.getPassword())) {
            throw AdminException.passwordMismatch();
        }

        // 액세스 토큰과 리프레시 토큰 모두 발급
        return tokenGenerator.generateAccessRefreshToken(admin.getAdminId(), "web");
    }
    
    /**
     * 리프레시 토큰을 검증하고 새 액세스 토큰을 발급합니다.
     * 
     * @param refreshToken 리프레시 토큰
     * @return 새 액세스 토큰
     */
    public TokenDto.AccessToken refreshAccessToken(String refreshToken) {
        // 리프레시 토큰 검증
        String userId = tokenGenerator.validateJwtToken(refreshToken);
        
        if (userId == null) {
            throw AdminException.invalidToken();
        }
        
        // 사용자 존재 여부 확인
        Optional<Admin> adminOpt = adminRepository.findByAdminId(userId);
        if (adminOpt.isEmpty()) {
            throw AdminException.notFound();
        }
        
        // 새 액세스 토큰 발급
        return tokenGenerator.generateAccessToken(userId);
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
        if (adminRepository.findByAdminId(admin.getAdminId()).isPresent()) {
            throw AdminException.alreadyExists();
        }
        
        // 비밀번호 해싱
        admin.setPassword(passwordUtil.encodePassword(admin.getPassword()));
        
        // 관리자 저장
        int result = adminRepository.save(admin);
        if (result != 1) {
            throw new RuntimeException("저장 실패");
        }
        
        return admin;
    }
} 