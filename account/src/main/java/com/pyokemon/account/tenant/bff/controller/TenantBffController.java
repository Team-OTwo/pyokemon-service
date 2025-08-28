package com.pyokemon.account.tenant.bff.controller;

import com.pyokemon.common.dto.IdsRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.pyokemon.account.tenant.bff.dto.TenantDto;
import com.pyokemon.account.tenant.bff.service.TenantBffService;

import lombok.RequiredArgsConstructor;

import java.util.List;

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

  @PostMapping("/_batch")
  public List<TenantDto> findTenantsBatch(@RequestBody IdsRequest req) {
    return tenantBffService.findTenantsBatch(req.getIds());
  }
}
