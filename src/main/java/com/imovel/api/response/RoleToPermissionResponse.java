package com.imovel.api.response;

import com.imovel.api.model.Permissions;
import com.imovel.api.model.Role;
import java.util.Optional;

public class RoleToPermissionResponse {
    private Long roleId;
    private String roleName;
    private Long permissionId;
    private String permissionName;

    public RoleToPermissionResponse() {

    }

    public RoleToPermissionResponse(Long roleId, String roleName, Long permissionId, String permissionName) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.permissionId = permissionId;
        this.permissionName = permissionName;
    }

    public static Optional<RoleToPermissionResponse> parse(Role role, Permissions permission) {
        if (role == null || permission == null) {
            return Optional.empty();
        }
        return Optional.of(new RoleToPermissionResponse(
            role.getId(),
            role.getRoleName(),
            permission.getId(),
            permission.getPermissionName()
        ));
    }



    // Getters
    public Long getRoleId() {
        return roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public Long getPermissionId() {
        return permissionId;
    }

    public String getPermissionName() {
        return permissionName;
    }
}