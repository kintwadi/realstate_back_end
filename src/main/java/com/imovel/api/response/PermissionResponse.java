package com.imovel.api.response;

import com.imovel.api.model.Permissions;
import com.imovel.api.model.RolePermission;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PermissionResponse {
    private Long permissionId;
    private String permissionName;
    private String description;
    private List<Long> roleIds;

    public PermissionResponse() {
    }

    public PermissionResponse(Long permissionId, String permissionName, String description, List<Long> roleIds) {
        this.permissionId = permissionId;
        this.permissionName = permissionName;
        this.description = description;
        this.roleIds = roleIds != null ? roleIds : Collections.emptyList();
    }

    // Getters and Setters
    public Long getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(Long permissionId) {
        this.permissionId = permissionId;
    }

    public String getPermissionName() {
        return permissionName;
    }

    public void setPermissionName(String permissionName) {
        this.permissionName = permissionName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Long> getRoleIds() {
        return roleIds != null ? roleIds : Collections.emptyList();
    }

    public void setRoleIds(List<Long> roleIds) {
        this.roleIds = roleIds != null ? roleIds : Collections.emptyList();
    }

    public static Optional<PermissionResponse> parse(Permissions permissions) {
        if (permissions == null) {
            return Optional.empty();
        }

        PermissionResponse response = new PermissionResponse();
        response.setPermissionId(permissions.getId());
        response.setPermissionName(permissions.getPermissionName());
        response.setDescription(permissions.getDescription());

        // Safely extract role IDs
        List<Long> roleIds = permissions.getRolePermissions() != null ?
                permissions.getRolePermissions().stream()
                        .map(RolePermission::getRole)
                        .filter(role -> role != null && role.getId() != null)
                        .map(role -> role.getId())
                        .collect(Collectors.toList()) :
                Collections.emptyList();

        response.setRoleIds(roleIds);

        return Optional.of(response);
    }

}
