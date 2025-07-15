package com.imovel.api.services;

import com.imovel.api.error.ApiCode;
import com.imovel.api.model.Permissions;
import com.imovel.api.model.Role;
import com.imovel.api.model.RolePermission;
import com.imovel.api.model.User;
import com.imovel.api.model.enums.RoleReference;
import com.imovel.api.repository.PermissionsRepository;
import com.imovel.api.repository.RolePermissionRepository;
import com.imovel.api.repository.RoleRepository;
import com.imovel.api.response.ApplicationResponse;
import com.imovel.api.response.PermissionResponse;
import com.imovel.api.response.RoleToPermissionResponse;
import com.imovel.api.security.Policies;
import com.imovel.api.logger.ApiLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RolePermissionService {
    private static final String LOGGER_LOCATION = "RolePermissionService";

    private final RoleRepository roleRepository;
    private final PermissionsRepository permissionsRepository;
    private final RolePermissionRepository rolePermissionRepository;

    @Autowired
    public RolePermissionService(RoleRepository roleRepository,
                                 PermissionsRepository permissionsRepository,
                                 RolePermissionRepository rolePermissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionsRepository = permissionsRepository;
        this.rolePermissionRepository = rolePermissionRepository;
    }

    @Transactional
    public ApplicationResponse<RoleToPermissionResponse> assignPermissionToRole(Long roleId, Long permissionId) {
        ApiLogger.debug(LOGGER_LOCATION, "Attempting to assign permission to role",
                Map.of("roleId", roleId, "permissionId", permissionId));

        Optional<Role> roleOptional = roleRepository.findById(roleId);
        if (roleOptional.isEmpty()) {
            ApiLogger.error(LOGGER_LOCATION, "Role not found",
                    Map.of("roleId", roleId));
            return errorResponse(ApiCode.ROLE_NOT_FOUND, "Role not found with id: " + roleId);
        }

        Optional<Permissions> permissionOptional = permissionsRepository.findById(permissionId);
        if (permissionOptional.isEmpty()) {
            ApiLogger.error(LOGGER_LOCATION, "Permission not found",
                    Map.of("permissionId", permissionId));
            return errorResponse(ApiCode.PERMISSION_NOT_FOUND, "Permission not found with id: " + permissionId);
        }

        if (rolePermissionRepository.existsByRoleIdAndPermissionId(roleId, permissionId)) {
            ApiLogger.debug(LOGGER_LOCATION, "Permission already assigned to role",
                    Map.of("roleId", roleId, "permissionId", permissionId));
            return errorResponse(ApiCode.PERMISSION_ASSIGNMENT_FAILED, "Permission already assigned to this role");
        }

        try {
            Role role = roleOptional.get();
            Permissions permission = permissionOptional.get();

            RolePermission rolePermission = new RolePermission();
            rolePermission.setRole(role);
            rolePermission.setPermission(permission);
            rolePermissionRepository.save(rolePermission);

            ApiLogger.info(LOGGER_LOCATION, "Successfully assigned permission to role",
                    Map.of("roleId", roleId, "permissionId", permissionId));

            return RoleToPermissionResponse.parse(role, permission)
                    .map(response -> successResponse(response, "Permission assigned successfully"))
                    .orElse(errorResponse(ApiCode.PERMISSION_ASSIGNMENT_FAILED, "Failed to create response"));
        } catch (Exception e) {
            ApiLogger.error(LOGGER_LOCATION, "Failed to assign permission to role", e,
                    Map.of("roleId", roleId, "permissionId", permissionId));
            return errorResponse(ApiCode.PERMISSION_ASSIGNMENT_FAILED, "Failed to assign permission");
        }
    }

    @Transactional
    public ApplicationResponse<RoleToPermissionResponse> assignPermissionToRoleByName(String roleName, String permissionName) {
        ApiLogger.debug(LOGGER_LOCATION, "Attempting to assign permission to role by name",
                Map.of("roleName", roleName, "permissionName", permissionName));

        Optional<Role> roleOptional = roleRepository.findByRoleName(roleName);
        if (roleOptional.isEmpty()) {
            ApiLogger.error(LOGGER_LOCATION, "Role not found",
                    Map.of("roleName", roleName));
            return errorResponse(ApiCode.ROLE_NOT_FOUND, "Role not found with name: " + roleName);
        }

        Optional<Permissions> permissionOptional = permissionsRepository.findByPermissionName(permissionName);
        if (permissionOptional.isEmpty()) {
            ApiLogger.error(LOGGER_LOCATION, "Permission not found",
                    Map.of("permissionName", permissionName));
            return errorResponse(ApiCode.PERMISSION_NOT_FOUND, "Permission not found with name: " + permissionName);
        }

        Role role = roleOptional.get();
        Permissions permission = permissionOptional.get();

        if (rolePermissionRepository.existsByRoleIdAndPermissionId(role.getId(), permission.getId())) {
            ApiLogger.debug(LOGGER_LOCATION, "Permission already assigned to role",
                    Map.of("roleName", roleName, "permissionName", permissionName));
            return errorResponse(ApiCode.PERMISSION_ASSIGNMENT_FAILED, "Permission already assigned to this role");
        }

        try {
            RolePermission rolePermission = new RolePermission();
            rolePermission.setRole(role);
            rolePermission.setPermission(permission);
            rolePermissionRepository.save(rolePermission);

            ApiLogger.info(LOGGER_LOCATION, "Successfully assigned permission to role",
                    Map.of("roleName", roleName, "permissionName", permissionName));

            return RoleToPermissionResponse.parse(role, permission)
                    .map(response -> successResponse(response, "Permission assigned successfully"))
                    .orElse(errorResponse(ApiCode.PERMISSION_ASSIGNMENT_FAILED, "Failed to create response"));
        } catch (Exception e) {
            ApiLogger.error(LOGGER_LOCATION, "Failed to assign permission to role", e,
                    Map.of("roleName", roleName, "permissionName", permissionName));
            return errorResponse(ApiCode.PERMISSION_ASSIGNMENT_FAILED, "Failed to assign permission");
        }
    }

    @Transactional
    public ApplicationResponse<Void> removePermissionFromRole(Long roleId, Long permissionId) {
        ApiLogger.debug(LOGGER_LOCATION, "Attempting to remove permission from role",
                Map.of("roleId", roleId, "permissionId", permissionId));

        if (!rolePermissionRepository.existsByRoleIdAndPermissionId(roleId, permissionId)) {
            ApiLogger.error(LOGGER_LOCATION, "Permission not assigned to role",
                    Map.of("roleId", roleId, "permissionId", permissionId));
            return errorResponse(ApiCode.PERMISSION_OR_ROLE_NOT_FOUND, "Permission not assigned to this role");
        }

        Optional<Role> roleOptional = roleRepository.findById(roleId);
        if (roleOptional.isEmpty()) {
            ApiLogger.error(LOGGER_LOCATION, "Role not found",
                    Map.of("roleId", roleId));
            return errorResponse(ApiCode.ROLE_NOT_FOUND, "Role not found with id: " + roleId);
        }

        Optional<Permissions> permissionOptional = permissionsRepository.findById(permissionId);
        if (permissionOptional.isEmpty()) {
            ApiLogger.error(LOGGER_LOCATION, "Permission not found",
                    Map.of("permissionId", permissionId));
            return errorResponse(ApiCode.PERMISSION_NOT_FOUND, "Permission not found with id: " + permissionId);
        }

        Role role = roleOptional.get();
        Permissions permission = permissionOptional.get();
        rolePermissionRepository.findByRoleAndPermission(role, permission)
                .ifPresent(rolePermissionRepository::delete);

        ApiLogger.info(LOGGER_LOCATION, "Successfully removed permission from role",
                Map.of("roleId", roleId, "permissionId", permissionId));

        return successResponse("Permission removed successfully");
    }

    @Transactional(readOnly = true)
    public ApplicationResponse<Set<RoleToPermissionResponse>> getPermissionsByRoleId(Long roleId) {
        ApiLogger.debug(LOGGER_LOCATION, "Getting permissions for role",
                Map.of("roleId", roleId));

        if (!roleRepository.existsById(roleId)) {
            ApiLogger.error(LOGGER_LOCATION, "Role not found",
                    Map.of("roleId", roleId));
            return errorResponse(ApiCode.ROLE_NOT_FOUND, "Role not found with id: " + roleId);
        }

        Set<RoleToPermissionResponse> permissions = rolePermissionRepository.findByRoleId(roleId).stream()
                .map(rp -> RoleToPermissionResponse.parse(rp.getRole(), rp.getPermission()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());

        ApiLogger.info(LOGGER_LOCATION, "Found permissions for role",
                Map.of("roleId", roleId, "count", permissions.size()));

        return successResponse(permissions);
    }

    @Transactional(readOnly = true)
    public ApplicationResponse<Set<RoleToPermissionResponse>> getRolesByPermissionId(Long permissionId) {
        ApiLogger.debug(LOGGER_LOCATION, "Getting roles for permission",
                Map.of("permissionId", permissionId));

        if (!permissionsRepository.existsById(permissionId)) {
            ApiLogger.error(LOGGER_LOCATION, "Permission not found",
                    Map.of("permissionId", permissionId));
            return errorResponse(ApiCode.PERMISSION_NOT_FOUND, "Permission not found with id: " + permissionId);
        }

        Set<RoleToPermissionResponse> roles = rolePermissionRepository.findByPermissionId(permissionId).stream()
                .map(rp -> RoleToPermissionResponse.parse(rp.getRole(), rp.getPermission()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());

        ApiLogger.info(LOGGER_LOCATION, "Found roles for permission",
                Map.of("permissionId", permissionId, "count", roles.size()));

        return successResponse(roles);
    }

    @Transactional(readOnly = true)
    public ApplicationResponse<Boolean> hasPermission(Long roleId, Long permissionId) {
        ApiLogger.debug(LOGGER_LOCATION, "Checking if role has permission",
                Map.of("roleId", roleId, "permissionId", permissionId));

        try {
            boolean hasPermission = rolePermissionRepository.existsByRoleIdAndPermissionId(roleId, permissionId);

            ApiLogger.debug(LOGGER_LOCATION, "Permission check result",
                    Map.of("roleId", roleId, "permissionId", permissionId, "hasPermission", hasPermission));

            return successResponse(hasPermission);
        } catch (Exception e) {
            ApiLogger.error(LOGGER_LOCATION, "Failed to check permission for role", e,
                    Map.of("roleId", roleId, "permissionId", permissionId));
            return errorResponse(ApiCode.PERMISSION_RETRIEVAL_FAILED, "Failed to check permission: " + e.getMessage());
        }
    }

    @Transactional
    public ApplicationResponse<RoleToPermissionResponse> updateRolePermissions(Long roleId, Long permissionId) {
        ApiLogger.debug(LOGGER_LOCATION, "Updating role permissions",
                Map.of("roleId", roleId, "permissionId", permissionId));

        Optional<Role> roleOptional = roleRepository.findById(roleId);
        if (roleOptional.isEmpty()) {
            ApiLogger.error(LOGGER_LOCATION, "Role not found",
                    Map.of("roleId", roleId));
            return errorResponse(ApiCode.ROLE_NOT_FOUND, "Role not found with id: " + roleId);
        }

        Optional<Permissions> permissionsOptional = permissionsRepository.findById(permissionId);
        if (permissionsOptional.isEmpty()) {
            ApiLogger.error(LOGGER_LOCATION, "Permission not found",
                    Map.of("permissionId", permissionId));
            return errorResponse(ApiCode.PERMISSION_NOT_FOUND, "Permission not found with id: " + permissionId);
        }

        Optional<RolePermission> rolePermission = rolePermissionRepository.findByRoleAndPermission(
                roleOptional.get(), permissionsOptional.get());

        if (rolePermission.isEmpty()) {
            ApiLogger.error(LOGGER_LOCATION, "Role permission not found",
                    Map.of("roleId", roleId, "permissionId", permissionId));
            return errorResponse(ApiCode.PERMISSION_OR_ROLE_NOT_FOUND, "Permission or Role not found with id");
        }

        rolePermissionRepository.save(rolePermission.get());

        ApiLogger.info(LOGGER_LOCATION, "Successfully updated role permissions",
                Map.of("roleId", roleId, "permissionId", permissionId));

        return RoleToPermissionResponse.parse(rolePermission.get().getRole(), rolePermission.get().getPermission())
                .map(response -> successResponse(response, "Role permission updated successfully"))
                .orElse(errorResponse(ApiCode.PERMISSION_ASSIGNMENT_FAILED, "Failed to create response"));
    }

    @Transactional
    public ApplicationResponse<RoleToPermissionResponse> clearAllPermissionsFromRole(Long roleId) {
        ApiLogger.debug(LOGGER_LOCATION, "Clearing all permissions from role",
                Map.of("roleId", roleId));

        Optional<Role> roleOptional = roleRepository.findById(roleId);
        if (roleOptional.isEmpty()) {
            ApiLogger.error(LOGGER_LOCATION, "Role not found",
                    Map.of("roleId", roleId));
            return errorResponse(ApiCode.ROLE_NOT_FOUND, "Role not found with id: " + roleId);
        }

        List<RolePermission> rolePermissions = rolePermissionRepository.findByRoleId(roleId);
        if (rolePermissions.isEmpty()) {
            ApiLogger.error(LOGGER_LOCATION, "No permissions found for role",
                    Map.of("roleId", roleId));
            return errorResponse(ApiCode.PERMISSION_OR_ROLE_NOT_FOUND, "Role permission not found with id: " + roleId);
        }

        rolePermissionRepository.delete(rolePermissions.get(0));

        ApiLogger.info(LOGGER_LOCATION, "Successfully cleared all permissions from role",
                Map.of("roleId", roleId));

        return RoleToPermissionResponse.parse(rolePermissions.get(0).getRole(), rolePermissions.get(0).getPermission())
                .map(response -> successResponse(response, "All permissions cleared from role"))
                .orElse(errorResponse(ApiCode.PERMISSION_ASSIGNMENT_FAILED, "Failed to create response"));
    }

    @Transactional(readOnly = true)
    public ApplicationResponse<Long> countRolesWithPermission(Long permissionId) {
        ApiLogger.debug(LOGGER_LOCATION, "Counting roles with permission",
                Map.of("permissionId", permissionId));

        if (!permissionsRepository.existsById(permissionId)) {
            ApiLogger.error(LOGGER_LOCATION, "Permission not found",
                    Map.of("permissionId", permissionId));
            return errorResponse(ApiCode.PERMISSION_NOT_FOUND, "Permission not found with id: " + permissionId);
        }

        long count = rolePermissionRepository.countByPermissionId(permissionId);

        ApiLogger.info(LOGGER_LOCATION, "Found roles with permission",
                Map.of("permissionId", permissionId, "count", count));

        return successResponse(count);
    }

    @Transactional
    public ApplicationResponse<RoleToPermissionResponse> assignDefaultRolePermissions() {
        ApiLogger.info(LOGGER_LOCATION, "Assigning default role permissions");

        ApplicationResponse<RoleToPermissionResponse> adminResponse = assignAdminPermissions();
        if (!adminResponse.isSuccess()) {
            ApiLogger.error(LOGGER_LOCATION, "Failed to assign admin permissions",
                    Map.of("message", adminResponse.getMessage()));
            return adminResponse;
        }

        ApplicationResponse<RoleToPermissionResponse> agentResponse = assignAgentPermissions();
        if (!agentResponse.isSuccess()) {
            ApiLogger.error(LOGGER_LOCATION, "Failed to assign agent permissions",
                    Map.of("message", agentResponse.getMessage()));
            return agentResponse;
        }

        ApplicationResponse<RoleToPermissionResponse> tenantResponse = assignTenantPermissions();
        if (!tenantResponse.isSuccess()) {
            ApiLogger.error(LOGGER_LOCATION, "Failed to assign tenant permissions",
                    Map.of("message", tenantResponse.getMessage()));
            return tenantResponse;
        }

        ApiLogger.info(LOGGER_LOCATION, "Successfully assigned all default role permissions");
        return successResponse("Default role permissions assigned successfully");
    }

    private ApplicationResponse<RoleToPermissionResponse> assignAdminPermissions() {
        ApiLogger.debug(LOGGER_LOCATION, "Assigning admin permissions");
        return assignPermissionsForRole(RoleReference.ADMIN,
                Policies.PROPERTY_CREATE,
                Policies.PROPERTY_READ,
                Policies.PROPERTY_UPDATE,
                Policies.PROPERTY_DELETE,
                Policies.PROPERTY_DEACTIVATE,
                Policies.PROPERTY_MANAGE_ALL,
                Policies.USER_CREATE,
                Policies.USER_READ,
                Policies.USER_UPDATE,
                Policies.USER_DELETE,
                Policies.USER_DEACTIVATE,
                Policies.FINANCIAL_READ,
                Policies.FINANCIAL_REPORT,
                Policies.SYSTEM_CONFIGURE,
                Policies.AGENT_APPROVE,
                Policies.DISPUTE_RESOLVE);
    }

    private ApplicationResponse<RoleToPermissionResponse> assignAgentPermissions() {
        ApiLogger.debug(LOGGER_LOCATION, "Assigning agent permissions");
        return assignPermissionsForRole(RoleReference.AGENT,
                Policies.PROPERTY_CREATE,
                Policies.PROPERTY_READ,
                Policies.PROPERTY_UPDATE,
                Policies.PROPERTY_DELETE,
                Policies.PROPERTY_DEACTIVATE,
                Policies.USER_UPDATE);
    }

    private ApplicationResponse<RoleToPermissionResponse> assignTenantPermissions() {
        ApiLogger.debug(LOGGER_LOCATION, "Assigning tenant permissions");
        return assignPermissionsForRole(RoleReference.TENANT,
                Policies.PROPERTY_READ,
                Policies.TENANT_APPLICATION,
                Policies.TENANT_WISHLIST,
                Policies.TENANT_COMPARE,
                Policies.USER_UPDATE);
    }

    private ApplicationResponse<RoleToPermissionResponse> assignPermissionsForRole(RoleReference role, String... permissions) {
        ApiLogger.debug(LOGGER_LOCATION, "Assigning permissions to role",
                Map.of("role", role.name(), "permissionCount", permissions.length));

        Optional<Role> roleOptional = roleRepository.findByRoleName(role.name());
        if (roleOptional.isEmpty()) {
            ApiLogger.error(LOGGER_LOCATION, "Role not found",
                    Map.of("role", role.name()));
            return errorResponse(ApiCode.ROLE_NOT_FOUND, role.name() + " role not found");
        }

        for (String permission : permissions) {
            ApplicationResponse<RoleToPermissionResponse> response =
                    assignPermissionToRoleByName(role.name(), permission);
            if (!response.isSuccess()) {
                ApiLogger.error(LOGGER_LOCATION, "Failed to assign permission to role",
                        Map.of("role", role.name(), "permission", permission, "message", response.getMessage()));
                return errorResponse(ApiCode.PERMISSION_ASSIGNMENT_FAILED,
                        "Failed to assign permission: " + permission);
            }
        }

        ApiLogger.info(LOGGER_LOCATION, "Successfully assigned permissions to role",
                Map.of("role", role.name(), "permissionCount", permissions.length));

        return successResponse("Permissions assigned successfully to " + role.name());
    }

    public ApplicationResponse<Set<PermissionResponse>> getUserPermissions(User user) {
        try {
            ApiLogger.debug(LOGGER_LOCATION, "Getting permissions for user",
                    Map.of("userId", user.getId()));

            if (user.getRole() == null) {
                ApiLogger.info(LOGGER_LOCATION, "User has no role assigned",
                        Map.of("userId", user.getId()));
                return successResponse(Collections.emptySet());
            }

            Set<PermissionResponse> permissions = rolePermissionRepository.findByRoleId(user.getRole().getId()).stream()
                    .map(rp -> PermissionResponse.parse(rp.getPermission()))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toSet());

            ApiLogger.info(LOGGER_LOCATION, "Found permissions for user",
                    Map.of("userId", user.getId(), "count", permissions.size()));

            return successResponse(permissions);
        } catch (Exception e) {
            ApiLogger.error(LOGGER_LOCATION, "Failed to retrieve permissions for user", e,
                    Map.of("userId", user.getId()));
            return errorResponse(ApiCode.PERMISSION_RETRIEVAL_FAILED,
                    "Failed to retrieve user permissions: " + e.getMessage());
        }
    }

    public ApplicationResponse<List<RoleToPermissionResponse>> findAll() {

        ApiLogger.debug(LOGGER_LOCATION, "Finding all role permissions");

        List<RolePermission> rolePermissions = rolePermissionRepository.findAll();
        if(rolePermissions.isEmpty()){
            return errorResponse(ApiCode.PERMISSION_NOT_FOUND, "No role permissions assigned");
        }

        List<RoleToPermissionResponse> roleToPermissions = rolePermissions.stream()
                .map(rp -> RoleToPermissionResponse.parse(rp.getRole(), rp.getPermission()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        if (roleToPermissions.isEmpty()) {
            ApiLogger.error(LOGGER_LOCATION, "No role permissions found");
            return errorResponse(ApiCode.PERMISSION_NOT_FOUND, "No role permissions assigned");
        }

        ApiLogger.info(LOGGER_LOCATION, "Found role permissions",
                Map.of("count", roleToPermissions.size()));

        return successResponse(roleToPermissions, "All role permissions");
    }

    // Helper methods for consistent responses
    private <T> ApplicationResponse<T> successResponse(T data) {
        return ApplicationResponse.success(data);
    }

    private <T> ApplicationResponse<T> successResponse(T data, String message) {
        return ApplicationResponse.success(data, message);
    }

    private <T> ApplicationResponse<T> successResponse(String message) {
        return ApplicationResponse.success(message);
    }

    private <T> ApplicationResponse<T> errorResponse(ApiCode apiCode, String message) {
        return ApplicationResponse.error(apiCode.getCode(), message, apiCode.getHttpStatus());
    }
}