package com.imovel.api.services;

import com.imovel.api.error.ApiCode;
import com.imovel.api.model.Permissions;
import com.imovel.api.model.User;
import com.imovel.api.repository.PermissionsRepository;
import com.imovel.api.response.ApplicationResponse;
import com.imovel.api.response.PermissionResponse;
import com.imovel.api.security.Policies;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class PermissionService {
    private final PermissionsRepository permissionRepository;

    public PermissionService(PermissionsRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    @Transactional
    public ApplicationResponse<PermissionResponse> createPermission(String permissionName, String description) {
        try {
            Permissions permission = new Permissions(permissionName, description);
            Optional<PermissionResponse> permissionResponse = PermissionResponse.parse(permissionRepository.save(permission));
            return ApplicationResponse.success(permissionResponse.get(), "Permission created successfully");
        } catch (Exception e) {
            return ApplicationResponse.error(ApiCode.PERMISSION_CREATION_FAILED.getCode(),
                    "Failed to create permission: " + e.getMessage(),
                    ApiCode.PERMISSION_CREATION_FAILED.getHttpStatus());
        }
    }

    public ApplicationResponse<PermissionResponse> findPermissionByName(String permissionName) {
        Optional<Permissions> permission = permissionRepository.findByPermissionName(permissionName);
        if (permission.isEmpty()) {
            return ApplicationResponse.error(ApiCode.PERMISSION_NOT_FOUND.getCode(),
                    "Permission not found with name: " + permissionName,
                    ApiCode.PERMISSION_NOT_FOUND.getHttpStatus());
        }
        return PermissionResponse.parse(permission.get())
                .map(p -> ApplicationResponse.success(p))
                .orElseGet(() -> ApplicationResponse.error(ApiCode.PARSE_ERROR.getCode(),
                        "Failed to parse permission",
                        ApiCode.PARSE_ERROR.getHttpStatus()));
    }

    @Transactional
    public ApplicationResponse<List<PermissionResponse>> initializeDefaultPermissions() {
        List<PermissionResponse> createdPermissions = new ArrayList<>();
        try {
            // Create permissions for property management
            createPermissionIfNotExists(Policies.PROPERTY_CREATE, "Create new property listings");
            createPermissionIfNotExists(Policies.PROPERTY_READ, "View property listings");
            createPermissionIfNotExists(Policies.PROPERTY_UPDATE, "Update property listings");
            createPermissionIfNotExists(Policies.PROPERTY_DELETE, "Delete property listings");
            createPermissionIfNotExists(Policies.PROPERTY_DEACTIVATE, "Deactivate property listings");
            createPermissionIfNotExists(Policies.PROPERTY_MANAGE_ALL, "Manage all properties in the system");

            // Create permissions for user management
            createPermissionIfNotExists(Policies.USER_CREATE, "Create new user accounts");
            createPermissionIfNotExists(Policies.USER_READ, "View user accounts");
            createPermissionIfNotExists(Policies.USER_UPDATE, "Update user accounts");
            createPermissionIfNotExists(Policies.USER_DELETE, "Delete user accounts");
            createPermissionIfNotExists(Policies.USER_DEACTIVATE, "Deactivate user accounts");

            // Create permissions for financial operations
            createPermissionIfNotExists(Policies.FINANCIAL_READ, "View financial transactions");
            createPermissionIfNotExists(Policies.FINANCIAL_REPORT, "Generate financial reports");

            // Create permissions for system configuration
            createPermissionIfNotExists(Policies.SYSTEM_CONFIGURE, "Configure system settings");

            // Create permissions for agent registration
            createPermissionIfNotExists(Policies.AGENT_APPROVE, "Approve agent registrations");

            // Create permissions for dispute resolution
            createPermissionIfNotExists(Policies.DISPUTE_RESOLVE, "Resolve disputes");

            // Create permissions for tenant operations
            createPermissionIfNotExists(Policies.TENANT_APPLICATION, "Submit rental applications");
            createPermissionIfNotExists(Policies.TENANT_WISHLIST, "Manage wishlist");
            createPermissionIfNotExists(Policies.TENANT_COMPARE, "Compare properties");

            List<Permissions> permissions = permissionRepository.findAll();
            permissions.forEach(permission -> {
                PermissionResponse.parse(permission).ifPresent(createdPermissions::add);
            });

            return ApplicationResponse.success(createdPermissions, "Default permissions initialized successfully");
        } catch (Exception e) {
            return ApplicationResponse.error(ApiCode.INITIALIZATION_FAILED.getCode(),
                    "Failed to initialize default permissions: " + e.getMessage(),
                    ApiCode.INITIALIZATION_FAILED.getHttpStatus());
        }
    }

    public ApplicationResponse<List<PermissionResponse>> findAll() {
        List<PermissionResponse> permissionResponses = new ArrayList<>();
        List<Permissions> permissions = permissionRepository.findAll();
        if (permissions.isEmpty()) {
            return ApplicationResponse.error(ApiCode.PERMISSION_NOT_FOUND.getCode(),
                    "no permissions found",
                    ApiCode.PERMISSION_RETRIEVAL_FAILED.getHttpStatus());
        }
        permissions.forEach(permission -> {
            PermissionResponse.parse(permission).ifPresent(permissionResponses::add);
        });
        return ApplicationResponse.success(permissionResponses);
    }

    private void createPermissionIfNotExists(String permissionName, String description) {
        if (!permissionRepository.findByPermissionName(permissionName).isPresent()) {
            createPermission(permissionName, description);
        }
    }
}
