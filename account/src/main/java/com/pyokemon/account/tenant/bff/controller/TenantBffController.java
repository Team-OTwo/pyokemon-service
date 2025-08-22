package com.pyokemon.account.tenant.bff.controller;

import com.pyokemon.account.tenant.bff.dto.TenantDto;
import com.pyokemon.account.tenant.bff.service.TenantBffService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bff/tenants")
@RequiredArgsConstructor
public class TenantBffController {

    private final TenantBffService tenantBffService;

    @GetMapping("/{tenantId}")
    public ResponseEntity<TenantDto> getTenantById(@PathVariable Long tenantId) {
        TenantDto responseDto = tenantBffService.findByTenantId(tenantId);
        return ResponseEntity.ok(responseDto);
    }
}
