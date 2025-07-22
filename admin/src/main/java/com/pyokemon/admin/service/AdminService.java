package com.pyokemon.admin.service;

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
    
    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }
    
    public Optional<Admin> getAdminById(Long id) {
        return adminRepository.findById(id);
    }
    
    public Optional<Admin> getAdminByUsername(String username) {
        return adminRepository.findByUsername(username);
    }
    
    public Admin createAdmin(Admin admin) {
        return adminRepository.save(admin);
    }
    
    public void deleteAdmin(Long id) {
        adminRepository.deleteById(id);
    }
} 