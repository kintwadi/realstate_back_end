package com.imovel.api.controller;

import com.imovel.api.model.User;
import com.imovel.api.request.RolePermissionRequest;
import com.imovel.api.response.ApplicationResponse;
import com.imovel.api.response.PermissionResponse;
import com.imovel.api.response.RoleToPermissionResponse;
import com.imovel.api.services.RolePermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/role-permissions")
public class RoleToPermissionController {

    private final RolePermissionService rolePermissionService;

    @Autowired
    public RoleToPermissionController(RolePermissionService rolePermissionService) {
        this.rolePermissionService = rolePermissionService;
    }

    @PostMapping("/assign")
    public ApplicationResponse<RoleToPermissionResponse>assignPermissionToRole(
            @RequestBody RolePermissionRequest request)
    {

        ApplicationResponse<RoleToPermissionResponse> response = rolePermissionService.assignPermissionToRole(request.getRoleId(), request.getPermissionId());
        return response;

    }

    @GetMapping
    public ApplicationResponse<List<RoleToPermissionResponse>> findAll() {

        return rolePermissionService.findAll();
    }

    @PostMapping("/assign-by-name")
    public ApplicationResponse<RoleToPermissionResponse> assignPermissionToRoleByName(
            @RequestParam String roleName,
            @RequestParam String permissionName) {
        return rolePermissionService.assignPermissionToRoleByName(roleName, permissionName);
    }

    @DeleteMapping("/remove")
    public ApplicationResponse<Void> removePermissionFromRole(@RequestBody RolePermissionRequest request) {
        return rolePermissionService.removePermissionFromRole(request.getRoleId(), request.getPermissionId());
    }

    @GetMapping("/role/{roleId}")
    public ApplicationResponse<Set<RoleToPermissionResponse>> getPermissionsByRoleId(
            @PathVariable Long roleId) {
        return rolePermissionService.getPermissionsByRoleId(roleId);
    }

    @GetMapping("/permission/{permissionId}")
    public ApplicationResponse<Set<RoleToPermissionResponse>> getRolesByPermissionId(
            @PathVariable Long permissionId) {
        return rolePermissionService.getRolesByPermissionId(permissionId);
    }

    @GetMapping("/check")
    public ApplicationResponse<Boolean> hasPermission(@RequestBody RolePermissionRequest request) {
        return rolePermissionService.hasPermission(request.getRoleId(), request.getPermissionId());
    }

    //to be  tested
    @PutMapping("/update")
    public ApplicationResponse<RoleToPermissionResponse> updateRolePermissions(@RequestBody RolePermissionRequest request) {
        return rolePermissionService.updateRolePermissions(request.getRoleId(), request.getPermissionId());
    }
    //to be  tested
    @DeleteMapping("/remove/{roleId}")
    public ApplicationResponse<RoleToPermissionResponse> clearAllPermissionsFromRole( @PathVariable Long roleId) {
        return rolePermissionService.clearAllPermissionsFromRole(roleId);
    }

    //to be  tested
    @GetMapping("/count/{permissionId}")
    public ApplicationResponse<Long> countRolesWithPermission(
            @PathVariable Long permissionId) {
        return rolePermissionService.countRolesWithPermission(permissionId);
    }

    @PostMapping("/initialize-defaults")
    public ApplicationResponse<RoleToPermissionResponse> assignDefaultRolePermissions() {
        return rolePermissionService.assignDefaultRolePermissions();
    }
    @GetMapping("/user-permissions")
    public ApplicationResponse<Set<PermissionResponse>> getUserPermissions(
            @RequestBody User user) {
        return rolePermissionService.getUserPermissions(user);
    }
}
