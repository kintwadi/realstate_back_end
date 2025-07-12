package com.imovel.api.controller;

import com.imovel.api.model.Permissions;
import com.imovel.api.model.User;
import com.imovel.api.response.ApplicationResponse;
import com.imovel.api.services.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    @Autowired
    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }
    // Permission Management Endpoints

    @PostMapping("/create")
    public ApplicationResponse<Permissions> createPermission(
            @RequestParam String permissionName,
            @RequestParam String description) {
        return permissionService.createPermission(permissionName, description);
    }

    @GetMapping("/{permissionName}")
    public ApplicationResponse<Permissions> getPermissionByName(@PathVariable String permissionName) {
        return permissionService.findPermissionByName(permissionName);
    }

    // Role-Permission Assignment Endpoints

    @PostMapping("/assign-permission-to-role")
    public ApplicationResponse<Permissions> assignPermissionToRole(
            @RequestParam String roleName,
            @RequestParam String permissionName) {
        return permissionService.addPermissionToRole(roleName, permissionName);
    }

    @DeleteMapping("/remove-permission-from-role")
    public ApplicationResponse<Permissions> removePermissionFromRole(
            @RequestParam String roleName,
            @RequestParam String permissionName) {
        return permissionService.removePermissionFromRole(roleName, permissionName);
    }

    // Initialization Endpoint

    @PostMapping("/initialize")
    public ApplicationResponse<Void> initializeDefaults() {
        return permissionService.initializeDefaultRolesAndPermissions();
    }

    // User Permissions Endpoint

    @GetMapping("/user-permissions")
    public ApplicationResponse<Set<Permissions>> getUserPermissions(@RequestBody User user) {
        return permissionService.getUserPermissions(user);
    }
}