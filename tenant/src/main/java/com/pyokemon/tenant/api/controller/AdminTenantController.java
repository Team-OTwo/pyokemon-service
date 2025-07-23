package com.pyokemon.tenant.api.controller;

import com.pyokemon.tenant.api.service.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/")
@RequiredArgsConstructor
public class AdminTenantController {

    private final TenantService tenantService;
}
