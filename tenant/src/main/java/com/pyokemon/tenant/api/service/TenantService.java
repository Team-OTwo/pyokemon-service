package com.pyokemon.tenant.api.service;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.tenant.exception.TenantException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pyokemon.tenant.api.dto.request.CreateTenantRequestDto;
import com.pyokemon.tenant.api.dto.request.LoginRequestDto;
import com.pyokemon.tenant.api.dto.request.UpdatePasswordRequestDto;
import com.pyokemon.tenant.api.dto.request.UpdateProfileRequestDto;
import com.pyokemon.tenant.api.dto.response.TenantDetailResponseDto;
import com.pyokemon.tenant.api.dto.response.TenantListResponseDto;
import com.pyokemon.tenant.api.entity.Tenant;
import com.pyokemon.tenant.api.repository.TenantRepository;
import com.pyokemon.tenant.mapper.TenantMapper;
import com.pyokemon.tenant.util.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TenantService {

    private final TenantRepository tenantRepository;
    private final TenantMapper tenantMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;



}
