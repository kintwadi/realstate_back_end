package com.imovel.api.controller;

import com.imovel.api.model.Permissions;
import com.imovel.api.model.User;
import com.imovel.api.response.StandardResponse;
import com.imovel.api.services.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<StandardResponse<Permissions>> createPermission(
            @RequestParam String permissionName,
            @RequestParam String description) {
        return ResponseEntity.ok(permissionService.createPermission(permissionName, description));
    }

    @GetMapping("/{permissionName}")
    public ResponseEntity<StandardResponse<Permissions>> getPermissionByName(@PathVariable String permissionName) {
        return ResponseEntity.ok(permissionService.findPermissionByName(permissionName));
    }

    // Role-Permission Assignment Endpoints

    @PostMapping("/assign-permission-to-role")
    public ResponseEntity<StandardResponse<Permissions>> assignPermissionToRole(
            @RequestParam String roleName,
            @RequestParam String permissionName) {
        return ResponseEntity.ok(permissionService.addPermissionToRole(roleName, permissionName));
    }

    @DeleteMapping("/remove-permission-from-role")
    public ResponseEntity<StandardResponse<Permissions>> removePermissionFromRole(
            @RequestParam String roleName,
            @RequestParam String permissionName) {
        return ResponseEntity.ok(permissionService.removePermissionFromRole(roleName, permissionName));
    }

    // Initialization Endpoint

    @PostMapping("/initialize")
    public ResponseEntity<StandardResponse<Void>> initializeDefaults() {
        return ResponseEntity.ok(permissionService.initializeDefaultRolesAndPermissions());
    }

    // User Permissions Endpoint

    @GetMapping("/user-permissions")
    public ResponseEntity<StandardResponse<Set<Permissions>>> getUserPermissions(@RequestBody User user) {
        return ResponseEntity.ok(permissionService.getUserPermissions(user));
    }

}