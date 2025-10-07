package com.imovel.api.controller;

import com.imovel.api.model.User;
import com.imovel.api.response.ApplicationResponse;
import com.imovel.api.response.PermissionResponse;
import com.imovel.api.services.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    @Autowired
    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @PostMapping
    public ApplicationResponse<PermissionResponse> createPermission(
            @RequestParam String permissionName,
            @RequestParam String description) {
        return permissionService.createPermission(permissionName, description);
    }

    @GetMapping("/{permissionName}")
    public ApplicationResponse<PermissionResponse> getPermissionByName(
            @PathVariable String permissionName) {
        return permissionService.findPermissionByName(permissionName);
    }

    @GetMapping
    public ApplicationResponse<List<PermissionResponse>> getAllPermissions() {
        return permissionService.findAll();
    }

    @PostMapping("/initialize")
    public ApplicationResponse<List<PermissionResponse>> initializeDefaultPermissions() {
        return permissionService.initializeDefaultPermissions();
    }
}
