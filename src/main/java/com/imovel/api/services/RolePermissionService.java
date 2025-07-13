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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RolePermissionService {
    private static final Logger logger = LoggerFactory.getLogger(RolePermissionService.class);

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
        logger.debug("Attempting to assign permission {} to role {}", permissionId, roleId);

        Optional<Role> roleOptional = roleRepository.findById(roleId);
        if (roleOptional.isEmpty()) {
            logger.error("Role not found with id: {}", roleId);
            return errorResponse(ApiCode.ROLE_NOT_FOUND, "Role not found with id: " + roleId);
        }

        Optional<Permissions> permissionOptional = permissionsRepository.findById(permissionId);
        if (permissionOptional.isEmpty()) {
            logger.error("Permission not found with id: {}", permissionId);
            return errorResponse(ApiCode.PERMISSION_NOT_FOUND, "Permission not found with id: " + permissionId);
        }

        if (rolePermissionRepository.existsByRoleIdAndPermissionId(roleId, permissionId)) {
            logger.warn("Permission {} already assigned to role {}", permissionId, roleId);
            return errorResponse(ApiCode.PERMISSION_ASSIGNMENT_FAILED, "Permission already assigned to this role");
        }

        try {
            Role role = roleOptional.get();
            Permissions permission = permissionOptional.get();

            RolePermission rolePermission = new RolePermission();
            rolePermission.setRole(role);
            rolePermission.setPermission(permission);
            rolePermissionRepository.save(rolePermission);

            logger.info("Successfully assigned permission {} to role {}", permissionId, roleId);

            return RoleToPermissionResponse.parse(role, permission)
                    .map(response -> successResponse(response, "Permission assigned successfully"))
                    .orElse(errorResponse(ApiCode.PERMISSION_ASSIGNMENT_FAILED, "Failed to create response"));
        } catch (Exception e) {
            logger.error("Failed to assign permission {} to role {}: {}", permissionId, roleId, e.getMessage(), e);
            return errorResponse(ApiCode.PERMISSION_ASSIGNMENT_FAILED, "Failed to assign permission");
        }
    }

    @Transactional
    public ApplicationResponse<RoleToPermissionResponse> assignPermissionToRoleByName(String roleName, String permissionName) {
        logger.debug("Attempting to assign permission '{}' to role '{}'", permissionName, roleName);

        Optional<Role> roleOptional = roleRepository.findByRoleName(roleName);
        if (roleOptional.isEmpty()) {
            logger.error("Role not found with name: {}", roleName);
            return errorResponse(ApiCode.ROLE_NOT_FOUND, "Role not found with name: " + roleName);
        }

        Optional<Permissions> permissionOptional = permissionsRepository.findByPermissionName(permissionName);
        if (permissionOptional.isEmpty()) {
            logger.error("Permission not found with name: {}", permissionName);
            return errorResponse(ApiCode.PERMISSION_NOT_FOUND, "Permission not found with name: " + permissionName);
        }

        Role role = roleOptional.get();
        Permissions permission = permissionOptional.get();

        if (rolePermissionRepository.existsByRoleIdAndPermissionId(role.getId(), permission.getId())) {
            logger.warn("Permission '{}' already assigned to role '{}'", permissionName, roleName);
            return errorResponse(ApiCode.PERMISSION_ASSIGNMENT_FAILED, "Permission already assigned to this role");
        }

        try {
            RolePermission rolePermission = new RolePermission();
            rolePermission.setRole(role);
            rolePermission.setPermission(permission);
            rolePermissionRepository.save(rolePermission);

            logger.info("Successfully assigned permission '{}' to role '{}'", permissionName, roleName);

            return RoleToPermissionResponse.parse(role, permission)
                    .map(response -> successResponse(response, "Permission assigned successfully"))
                    .orElse(errorResponse(ApiCode.PERMISSION_ASSIGNMENT_FAILED, "Failed to create response"));
        } catch (Exception e) {
            logger.error("Failed to assign permission '{}' to role '{}': {}", permissionName, roleName, e.getMessage(), e);
            return errorResponse(ApiCode.PERMISSION_ASSIGNMENT_FAILED, "Failed to assign permission");
        }
    }

    @Transactional
    public ApplicationResponse<Void> removePermissionFromRole(Long roleId, Long permissionId) {
        logger.debug("Attempting to remove permission {} from role {}", permissionId, roleId);

        if (!rolePermissionRepository.existsByRoleIdAndPermissionId(roleId, permissionId)) {
            logger.error("Permission {} not assigned to role {}", permissionId, roleId);
            return errorResponse(ApiCode.PERMISSION_OR_ROLE_NOT_FOUND, "Permission not assigned to this role");
        }

        Optional<Role> roleOptional = roleRepository.findById(roleId);
        if (roleOptional.isEmpty()) {
            logger.error("Role not found with id: {}", roleId);
            return errorResponse(ApiCode.ROLE_NOT_FOUND, "Role not found with id: " + roleId);
        }

        Optional<Permissions> permissionOptional = permissionsRepository.findById(permissionId);
        if (permissionOptional.isEmpty()) {
            logger.error("Permission not found with id: {}", permissionId);
            return errorResponse(ApiCode.PERMISSION_NOT_FOUND, "Permission not found with id: " + permissionId);
        }

        Role role = roleOptional.get();
        Permissions permission = permissionOptional.get();
        rolePermissionRepository.findByRoleAndPermission(role, permission)
                .ifPresent(rolePermissionRepository::delete);

        logger.info("Successfully removed permission {} from role {}", permissionId, roleId);

        return successResponse("Permission removed successfully");
    }

    @Transactional(readOnly = true)
    public ApplicationResponse<Set<RoleToPermissionResponse>> getPermissionsByRoleId(Long roleId) {
        logger.debug("Getting permissions for role {}", roleId);

        if (!roleRepository.existsById(roleId)) {
            logger.error("Role not found with id: {}", roleId);
            return errorResponse(ApiCode.ROLE_NOT_FOUND, "Role not found with id: " + roleId);
        }

        Set<RoleToPermissionResponse> permissions = rolePermissionRepository.findByRoleId(roleId).stream()
                .map(rp -> RoleToPermissionResponse.parse(rp.getRole(), rp.getPermission()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());

        logger.info("Found {} permissions for role {}", permissions.size(), roleId);

        return successResponse(permissions);
    }

    @Transactional(readOnly = true)
    public ApplicationResponse<Set<RoleToPermissionResponse>> getRolesByPermissionId(Long permissionId) {
        logger.debug("Getting roles for permission {}", permissionId);

        if (!permissionsRepository.existsById(permissionId)) {
            logger.error("Permission not found with id: {}", permissionId);
            return errorResponse(ApiCode.PERMISSION_NOT_FOUND, "Permission not found with id: " + permissionId);
        }

        Set<RoleToPermissionResponse> roles = rolePermissionRepository.findByPermissionId(permissionId).stream()
                .map(rp -> RoleToPermissionResponse.parse(rp.getRole(), rp.getPermission()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());

        logger.info("Found {} roles for permission {}", roles.size(), permissionId);

        return successResponse(roles);
    }

    @Transactional(readOnly = true)
    public ApplicationResponse<Boolean> hasPermission(Long roleId, Long permissionId) {
        logger.debug("Checking if role {} has permission {}", roleId, permissionId);

        try {
            boolean hasPermission = rolePermissionRepository.existsByRoleIdAndPermissionId(roleId, permissionId);

            logger.debug("Role {} {} permission {}", roleId, hasPermission ? "has" : "does not have", permissionId);

            return successResponse(hasPermission);
        } catch (Exception e) {
            logger.error("Failed to check permission {} for role {}: {}", permissionId, roleId, e.getMessage(), e);
            return errorResponse(ApiCode.PERMISSION_RETRIEVAL_FAILED, "Failed to check permission: " + e.getMessage());
        }
    }

    @Transactional
    public ApplicationResponse<RoleToPermissionResponse> updateRolePermissions(Long roleId, Long permissionId) {
        logger.debug("Updating role permissions for role {} and permission {}", roleId, permissionId);

        Optional<Role> roleOptional = roleRepository.findById(roleId);
        if (roleOptional.isEmpty()) {
            logger.error("Role not found with id: {}", roleId);
            return errorResponse(ApiCode.ROLE_NOT_FOUND, "Role not found with id: " + roleId);
        }

        Optional<Permissions> permissionsOptional = permissionsRepository.findById(permissionId);
        if (permissionsOptional.isEmpty()) {
            logger.error("Permission not found with id: {}", permissionId);
            return errorResponse(ApiCode.PERMISSION_NOT_FOUND, "Permission not found with id: " + permissionId);
        }

        Optional<RolePermission> rolePermission = rolePermissionRepository.findByRoleAndPermission(
                roleOptional.get(), permissionsOptional.get());

        if (rolePermission.isEmpty()) {
            logger.error("Role permission not found for role {} and permission {}", roleId, permissionId);
            return errorResponse(ApiCode.PERMISSION_OR_ROLE_NOT_FOUND, "Permission or Role not found with id");
        }

        rolePermissionRepository.save(rolePermission.get());

        logger.info("Successfully updated role permissions for role {} and permission {}", roleId, permissionId);

        return RoleToPermissionResponse.parse(rolePermission.get().getRole(), rolePermission.get().getPermission())
                .map(response -> successResponse(response, "Role permission updated successfully"))
                .orElse(errorResponse(ApiCode.PERMISSION_ASSIGNMENT_FAILED, "Failed to create response"));
    }

    @Transactional
    public ApplicationResponse<RoleToPermissionResponse> clearAllPermissionsFromRole(Long roleId) {
        logger.debug("Clearing all permissions from role {}", roleId);

        Optional<Role> roleOptional = roleRepository.findById(roleId);
        if (roleOptional.isEmpty()) {
            logger.error("Role not found with id: {}", roleId);
            return errorResponse(ApiCode.ROLE_NOT_FOUND, "Role not found with id: " + roleId);
        }

        List<RolePermission> rolePermissions = rolePermissionRepository.findByRoleId(roleId);
        if (rolePermissions.isEmpty()) {
            logger.error("No permissions found for role {}", roleId);
            return errorResponse(ApiCode.PERMISSION_OR_ROLE_NOT_FOUND, "Role permission not found with id: " + roleId);
        }

        rolePermissionRepository.delete(rolePermissions.get(0));

        logger.info("Successfully cleared all permissions from role {}", roleId);

        return RoleToPermissionResponse.parse(rolePermissions.get(0).getRole(), rolePermissions.get(0).getPermission())
                .map(response -> successResponse(response, "All permissions cleared from role"))
                .orElse(errorResponse(ApiCode.PERMISSION_ASSIGNMENT_FAILED, "Failed to create response"));
    }

    @Transactional(readOnly = true)
    public ApplicationResponse<Long> countRolesWithPermission(Long permissionId) {
        logger.debug("Counting roles with permission {}", permissionId);

        if (!permissionsRepository.existsById(permissionId)) {
            logger.error("Permission not found with id: {}", permissionId);
            return errorResponse(ApiCode.PERMISSION_NOT_FOUND, "Permission not found with id: " + permissionId);
        }

        long count = rolePermissionRepository.countByPermissionId(permissionId);

        logger.info("Found {} roles with permission {}", count, permissionId);

        return successResponse(count);
    }

    @Transactional
    public ApplicationResponse<RoleToPermissionResponse> assignDefaultRolePermissions() {
        logger.info("Assigning default role permissions");

        ApplicationResponse<RoleToPermissionResponse> adminResponse = assignAdminPermissions();
        if (!adminResponse.isSuccess()) {
            logger.error("Failed to assign admin permissions: {}", adminResponse.getMessage());
            return adminResponse;
        }

        ApplicationResponse<RoleToPermissionResponse> agentResponse = assignAgentPermissions();
        if (!agentResponse.isSuccess()) {
            logger.error("Failed to assign agent permissions: {}", agentResponse.getMessage());
            return agentResponse;
        }

        ApplicationResponse<RoleToPermissionResponse> tenantResponse = assignTenantPermissions();
        if (!tenantResponse.isSuccess()) {
            logger.error("Failed to assign tenant permissions: {}", tenantResponse.getMessage());
            return tenantResponse;
        }

        logger.info("Successfully assigned all default role permissions");
        return successResponse("Default role permissions assigned successfully");
    }

    private ApplicationResponse<RoleToPermissionResponse> assignAdminPermissions() {
        logger.debug("Assigning admin permissions");
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
        logger.debug("Assigning agent permissions");
        return assignPermissionsForRole(RoleReference.AGENT,
                Policies.PROPERTY_CREATE,
                Policies.PROPERTY_READ,
                Policies.PROPERTY_UPDATE,
                Policies.PROPERTY_DELETE,
                Policies.PROPERTY_DEACTIVATE,
                Policies.USER_UPDATE);
    }

    private ApplicationResponse<RoleToPermissionResponse> assignTenantPermissions() {
        logger.debug("Assigning tenant permissions");
        return assignPermissionsForRole(RoleReference.TENANT,
                Policies.PROPERTY_READ,
                Policies.TENANT_APPLICATION,
                Policies.TENANT_WISHLIST,
                Policies.TENANT_COMPARE,
                Policies.USER_UPDATE);
    }

    private ApplicationResponse<RoleToPermissionResponse> assignPermissionsForRole(RoleReference role, String... permissions) {
        logger.debug("Assigning {} permissions to role {}", permissions.length, role.name());

        Optional<Role> roleOptional = roleRepository.findByRoleName(role.name());
        if (roleOptional.isEmpty()) {
            logger.error("Role {} not found", role.name());
            return errorResponse(ApiCode.ROLE_NOT_FOUND, role.name() + " role not found");
        }

        for (String permission : permissions) {
            ApplicationResponse<RoleToPermissionResponse> response =
                    assignPermissionToRoleByName(role.name(), permission);
            if (!response.isSuccess()) {
                logger.error("Failed to assign permission {} to role {}: {}", permission, role.name(), response.getMessage());
                return errorResponse(ApiCode.PERMISSION_ASSIGNMENT_FAILED,
                        "Failed to assign permission: " + permission);
            }
        }

        logger.info("Successfully assigned {} permissions to role {}", permissions.length, role.name());

        return successResponse("Permissions assigned successfully to " + role.name());
    }

    public ApplicationResponse<Set<PermissionResponse>> getUserPermissions(User user) {
        try {
            logger.debug("Getting permissions for user {}", user.getId());

            if (user.getRole() == null) {
                logger.info("User {} has no role assigned", user.getId());
                return successResponse(Collections.emptySet());
            }

            Set<PermissionResponse> permissions = rolePermissionRepository.findByRoleId(user.getRole().getId()).stream()
                    .map(rp -> PermissionResponse.parse(rp.getPermission()))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toSet());

            logger.info("Found {} permissions for user {}", permissions.size(), user.getId());

            return successResponse(permissions);
        } catch (Exception e) {
            logger.error("Failed to retrieve permissions for user {}: {}", user.getId(), e.getMessage(), e);
            return errorResponse(ApiCode.PERMISSION_RETRIEVAL_FAILED,
                    "Failed to retrieve user permissions: " + e.getMessage());
        }
    }

    public ApplicationResponse<List<RoleToPermissionResponse>> findAll() {
        logger.debug("Finding all role permissions");

        List<RoleToPermissionResponse> roleToPermissions = rolePermissionRepository.findAll().stream()
                .map(rp -> RoleToPermissionResponse.parse(rp.getRole(), rp.getPermission()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        if (roleToPermissions.isEmpty()) {
            logger.error("No role permissions found");
            return errorResponse(ApiCode.PERMISSION_NOT_FOUND, "No role permissions assigned");
        }

        logger.info("Found {} role permissions", roleToPermissions.size());

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