// PermissionService.java
package com.imovel.api.services;

import com.imovel.api.error.ApiCode;
import com.imovel.api.model.Permissions;
import com.imovel.api.model.Role;
import com.imovel.api.model.User;
import com.imovel.api.model.enums.RoleReference;
import com.imovel.api.repository.PermissionRepository;
import com.imovel.api.repository.RoleRepository;
import com.imovel.api.repository.UserRepository;
import com.imovel.api.response.ApplicationResponse;
import com.imovel.api.security.Policies;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class PermissionService {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;

    public PermissionService(RoleRepository roleRepository,
                             PermissionRepository permissionRepository,
                             UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.userRepository = userRepository;
    }

    // Role management methods
    public ApplicationResponse<Role> createRole(String roleName, String description) {
        try {
            Role role = new Role(roleName, description);
            return ApplicationResponse.success(roleRepository.save(role), "Role created successfully");
        } catch (Exception e) {
            return ApplicationResponse.error(ApiCode.ROLE_CREATION_FAILED.getCode(),
                    "Failed to create role: " + e.getMessage(),
                    ApiCode.ROLE_CREATION_FAILED.getHttpStatus());
        }
    }

    public ApplicationResponse<Role> findRoleByName(String roleName) {
        Optional<Role> role = roleRepository.findByRoleName(roleName);
        return role.map(r -> ApplicationResponse.success(r))
                .orElseGet(() -> ApplicationResponse.error(ApiCode.ROLE_NOT_FOUND.getCode(),
                        "Role not found with name: " + roleName,
                        ApiCode.ROLE_NOT_FOUND.getHttpStatus()));
    }

    // Permission management methods
    public ApplicationResponse<Permissions> createPermission(String permissionName, String description) {
        try {
            Permissions permission = new Permissions(permissionName, description);
            return ApplicationResponse.success(permissionRepository.save(permission), "Permission created successfully");
        } catch (Exception e) {
            return ApplicationResponse.error(ApiCode.PERMISSION_CREATION_FAILED.getCode(),
                    "Failed to create permission: " + e.getMessage(),
                    ApiCode.PERMISSION_CREATION_FAILED.getHttpStatus());
        }
    }

    public ApplicationResponse<Permissions> findPermissionByName(String permissionName) {
        Optional<Permissions> permission = permissionRepository.findByPermissionName(permissionName);
        return permission.map(p -> ApplicationResponse.success(p))
                .orElseGet(() -> ApplicationResponse.error(ApiCode.PERMISSION_NOT_FOUND.getCode(),
                        "Permission not found with name: " + permissionName,
                        ApiCode.PERMISSION_NOT_FOUND.getHttpStatus()));
    }

    // Role-Permission assignment
    public ApplicationResponse<Permissions> addPermissionToRole(String roleName, String permissionName) {

        ApplicationResponse<Role> roleResponse = findRoleByName(roleName);
        ApplicationResponse<Permissions> permissionResponse = findPermissionByName(permissionName);

        if(!roleResponse.isSuccess()  && !permissionResponse.isSuccess())
        {
            return ApplicationResponse.error(ApiCode.PERMISSION_OR_ROLE_NOT_FOUND.getCode(), "failed to add permission to role", ApiCode.PERMISSION_OR_ROLE_NOT_FOUND.getHttpStatus());
        }
        Role role = roleResponse.getData();
        Permissions permission = permissionResponse.getData();
        role.getPermissions().add(permission);
        permission.getRoles().add(role);
        roleRepository.save(role);
        return ApplicationResponse.success(permission, "Permission added to role successfully");
    }

    public ApplicationResponse<Permissions> removePermissionFromRole(String roleName, String permissionName) {
        ApplicationResponse<Role> roleResponse = findRoleByName(roleName);
        ApplicationResponse<Permissions> permissionResponse = findPermissionByName(permissionName);

        if (!roleResponse.isSuccess() && !permissionResponse.isSuccess()) {
            return ApplicationResponse.error(ApiCode.PERMISSION_OR_ROLE_NOT_FOUND.getCode(), "failed to remove permission to role", ApiCode.PERMISSION_OR_ROLE_NOT_FOUND.getHttpStatus());
        }

        Role role = roleResponse.getData();
        Permissions permission = permissionResponse.getData();

        role.getPermissions().remove(permission);
        permission.getRoles().remove(role);
        roleRepository.save(role);
        return ApplicationResponse.success(permission, "Permission removed from role successfully");
    }

    // Initialize default roles and permissions
    public ApplicationResponse<Void> initializeDefaultRolesAndPermissions() {
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

            // Create default roles and assign permissions
            initializeAdminRole();
            initializeAgentRole();
            initializeTenantRole();

            return ApplicationResponse.success(null, "Default roles and permissions initialized successfully");
        } catch (Exception e) {
            return ApplicationResponse.error(ApiCode.INITIALIZATION_FAILED.getCode(),
                    "Failed to initialize default roles and permissions: " + e.getMessage(),
                    ApiCode.INITIALIZATION_FAILED.getHttpStatus());
        }
    }

    private void createPermissionIfNotExists(String permissionName, String description) {
        ApplicationResponse<Permissions> response = findPermissionByName(permissionName);
        if (!response.isSuccess()) {
            createPermission(permissionName, description);
        }
    }

    private ApplicationResponse<Void> initializeAdminRole() {
        ApplicationResponse<Role> response = findRoleByName(RoleReference.ADMIN.name());
        if (!response.isSuccess()) {
            ApplicationResponse<Role> createResponse = createRole(RoleReference.ADMIN.name(), "System Administrator with full access");
            if (!createResponse.isSuccess()) {
                return ApplicationResponse.error(createResponse.getError());
            }

            Role adminRole = createResponse.getData();

            // Assign all permissions to admin
            addPermissionToRole(RoleReference.ADMIN.name(), Policies.PROPERTY_CREATE);
            addPermissionToRole(RoleReference.ADMIN.name(), Policies.PROPERTY_READ);
            addPermissionToRole(RoleReference.ADMIN.name(), Policies.PROPERTY_UPDATE);
            addPermissionToRole(RoleReference.ADMIN.name(), Policies.PROPERTY_DELETE);
            addPermissionToRole(RoleReference.ADMIN.name(), Policies.PROPERTY_DEACTIVATE);
            addPermissionToRole(RoleReference.ADMIN.name(), Policies.PROPERTY_MANAGE_ALL);

            addPermissionToRole(RoleReference.ADMIN.name(), Policies.USER_CREATE);
            addPermissionToRole(RoleReference.ADMIN.name(), Policies.USER_READ);
            addPermissionToRole(RoleReference.ADMIN.name(), Policies.USER_UPDATE);
            addPermissionToRole(RoleReference.ADMIN.name(), Policies.USER_DELETE);
            addPermissionToRole(RoleReference.ADMIN.name(), Policies.USER_DEACTIVATE);

            addPermissionToRole(RoleReference.ADMIN.name(), Policies.FINANCIAL_READ);
            addPermissionToRole(RoleReference.ADMIN.name(), Policies.FINANCIAL_REPORT);

            addPermissionToRole(RoleReference.ADMIN.name(), Policies.SYSTEM_CONFIGURE);

            addPermissionToRole(RoleReference.ADMIN.name(), Policies.AGENT_APPROVE);

            addPermissionToRole(RoleReference.ADMIN.name(), Policies.DISPUTE_RESOLVE);
        }
        return ApplicationResponse.success(null);
    }

    private ApplicationResponse<Void> initializeAgentRole() {
        ApplicationResponse<Role> response = findRoleByName(RoleReference.AGENT.name());
        if (!response.isSuccess()) {
            ApplicationResponse<Role> createResponse = createRole(RoleReference.ADMIN.name(), "Real Estate Agent with property management access");
            if (!createResponse.isSuccess()) {
                return ApplicationResponse.error(createResponse.getError());
            }

            Role agentRole = createResponse.getData();

            // Assign agent permissions
            addPermissionToRole(RoleReference.AGENT.name(), Policies.PROPERTY_CREATE);
            addPermissionToRole(RoleReference.AGENT.name(), Policies.PROPERTY_READ);
            addPermissionToRole(RoleReference.AGENT.name(), Policies.PROPERTY_UPDATE);
            addPermissionToRole(RoleReference.AGENT.name(), Policies.PROPERTY_DELETE);
            addPermissionToRole(RoleReference.AGENT.name(), Policies.PROPERTY_DEACTIVATE);

            // Agent can only manage their own properties (handled in permission checks)
            addPermissionToRole(RoleReference.AGENT.name(), Policies.USER_UPDATE); // For their own profile
        }
        return ApplicationResponse.success(null);
    }

    private ApplicationResponse<Void> initializeTenantRole() {
        ApplicationResponse<Role> response = findRoleByName(RoleReference.TENANT.name());
        if (!response.isSuccess()) {
            ApplicationResponse<Role> createResponse = createRole(RoleReference.TENANT.name(), "Tenant with property viewing and application access");
            if (!createResponse.isSuccess()) {
                return ApplicationResponse.error(createResponse.getError());
            }

            Role tenantRole = createResponse.getData();

            // Assign tenant permissions
            addPermissionToRole(RoleReference.TENANT.name(), Policies.PROPERTY_READ);
            addPermissionToRole(RoleReference.TENANT.name(), Policies.TENANT_APPLICATION);
            addPermissionToRole(RoleReference.TENANT.name(), Policies.TENANT_WISHLIST);
            addPermissionToRole(RoleReference.TENANT.name(), Policies.TENANT_COMPARE);
            addPermissionToRole(RoleReference.TENANT.name(), Policies.USER_UPDATE); // For their own profile
        }
        return ApplicationResponse.success(null);
    }

    public ApplicationResponse<Set<Permissions>> getUserPermissions(User user) {
        try {
            Set<Permissions> permissions = new HashSet<>();

            for (Permissions permission : user.getRole().getPermissions()) {
                permissions.add(permission);
            }
            return ApplicationResponse.success(permissions);
        } catch (Exception e) {
            return ApplicationResponse.error(ApiCode.PERMISSION_RETRIEVAL_FAILED.getCode(),
                    "Failed to retrieve user permissions: " + e.getMessage(),
                    ApiCode.PERMISSION_RETRIEVAL_FAILED.getHttpStatus());
        }
    }
}