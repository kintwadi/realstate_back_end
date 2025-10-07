package com.imovel.api.response;

import com.imovel.api.model.Role;
import com.imovel.api.model.RolePermission;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class RoleResponse {
    private Long roleId;
    private String roleName;
    private String description;
    private List<PermissionResponse> permissions;
    private Integer userCount;

    public RoleResponse() {
    }

    public RoleResponse(Long roleId, String roleName, String description, 
                       List<PermissionResponse> permissions, Integer userCount) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.description = description;
        this.permissions = permissions != null ? permissions : Collections.emptyList();
        this.userCount = userCount != null ? userCount : 0;
    }

    // Getters and Setters
    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<PermissionResponse> getPermissions() {
        return permissions != null ? permissions : Collections.emptyList();
    }

    public void setPermissions(List<PermissionResponse> permissions) {
        this.permissions = permissions != null ? permissions : Collections.emptyList();
    }

    public Integer getUserCount() {
        return userCount != null ? userCount : 0;
    }

    public void setUserCount(Integer userCount) {
        this.userCount = userCount != null ? userCount : 0;
    }

    // Parse methods
    public static Optional<RoleResponse> parse(Role role) {
        if (role == null) {
            return Optional.empty();
        }

        RoleResponse response = new RoleResponse();
        response.setRoleId(role.getId());
        response.setRoleName(role.getRoleName());
        response.setDescription(role.getDescription());
        response.setUserCount(role.getUsers().size());

        // Parse permissions if they are loaded
        if (role.getRolePermissions() != null) {
            List<PermissionResponse> permissionResponses = role.getRolePermissions().stream()
                    .map(RolePermission::getPermission)
                    .map(PermissionResponse::parse)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
            response.setPermissions(permissionResponses);
        }

        return Optional.of(response);
    }

    public static Optional<RoleResponse> parseSimple(Role role) {
        if (role == null) {
            return Optional.empty();
        }

        RoleResponse response = new RoleResponse();
        response.setRoleId(role.getId());
        response.setRoleName(role.getRoleName());
        response.setDescription(role.getDescription());
        response.setUserCount(role.getUsers().size());

        return Optional.of(response);
    }

    @Override
    public String toString() {
        return "RoleResponse{" +
                "roleId=" + roleId +
                ", roleName='" + roleName + '\'' +
                ", description='" + description + '\'' +
                ", permissions=" + permissions +
                ", userCount=" + userCount +
                '}';
    }
}
