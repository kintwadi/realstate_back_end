// PermissionService.java
package com.imovel.api.services;

import com.imovel.api.model.Permission;
import com.imovel.api.model.Role;
import com.imovel.api.model.User;
import com.imovel.api.repository.PermissionRepository;
import com.imovel.api.repository.RoleRepository;
import com.imovel.api.repository.UserRepository;
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
    public Role createRole(String roleName, String description) {
        Role role = new Role(roleName, description);
        return roleRepository.save(role);
    }

    public Role findRoleByName(String roleName) {
        return roleRepository.findByRoleName(roleName).orElse(null);
    }

    // Permission management methods
    public Permission createPermission(String permissionName, String description) {
        Permission permission = new Permission(permissionName, description);
        return permissionRepository.save(permission);
    }

    public Permission findPermissionByName(String permissionName) {
        return permissionRepository.findByPermissionName(permissionName).orElse(null);
    }

    // Role-Permission assignment
    public void addPermissionToRole(String roleName, String permissionName) {
        Role role = findRoleByName(roleName);
        Permission permission = findPermissionByName(permissionName);
        
        if (role != null && permission != null) {
            role.getPermissions().add(permission);
            permission.getRoles().add(role);
            roleRepository.save(role);
        }
    }

    public void removePermissionFromRole(String roleName, String permissionName) {
        Role role = findRoleByName(roleName);
        Permission permission = findPermissionByName(permissionName);
        
        if (role != null && permission != null) {
            role.getPermissions().remove(permission);
            permission.getRoles().remove(role);
            roleRepository.save(role);
        }
    }

    // User-Role assignment
    public void addRoleToUser(User user, String roleName) {
        Role role = findRoleByName(roleName);
        if (role != null) {
            user.getRoles().add(role);
            role.getUsers().add(user);
            userRepository.save(user);
        }
    }

    public void removeRoleFromUser(User user, String roleName) {
        Role role = findRoleByName(roleName);
        if (role != null) {
            user.getRoles().remove(role);
            role.getUsers().remove(user);
            userRepository.save(user);
        }
    }

    // Initialize default roles and permissions
    public void initializeDefaultRolesAndPermissions() {
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
    }

    private void createPermissionIfNotExists(String permissionName, String description) {
        if (findPermissionByName(permissionName) == null) {
            createPermission(permissionName, description);
        }
    }

    private void initializeAdminRole() {
        Role adminRole = findRoleByName("ADMIN");
        if (adminRole == null) {
            adminRole = createRole("ADMIN", "System Administrator with full access");
            
            // Assign all permissions to admin
            addPermissionToRole("ADMIN", Policies.PROPERTY_CREATE);
            addPermissionToRole("ADMIN", Policies.PROPERTY_READ);
            addPermissionToRole("ADMIN", Policies.PROPERTY_UPDATE);
            addPermissionToRole("ADMIN", Policies.PROPERTY_DELETE);
            addPermissionToRole("ADMIN", Policies.PROPERTY_DEACTIVATE);
            addPermissionToRole("ADMIN", Policies.PROPERTY_MANAGE_ALL);
            
            addPermissionToRole("ADMIN", Policies.USER_CREATE);
            addPermissionToRole("ADMIN", Policies.USER_READ);
            addPermissionToRole("ADMIN", Policies.USER_UPDATE);
            addPermissionToRole("ADMIN", Policies.USER_DELETE);
            addPermissionToRole("ADMIN", Policies.USER_DEACTIVATE);
            
            addPermissionToRole("ADMIN", Policies.FINANCIAL_READ);
            addPermissionToRole("ADMIN", Policies.FINANCIAL_REPORT);
            
            addPermissionToRole("ADMIN", Policies.SYSTEM_CONFIGURE);
            
            addPermissionToRole("ADMIN", Policies.AGENT_APPROVE);
            
            addPermissionToRole("ADMIN", Policies.DISPUTE_RESOLVE);
        }
    }

    private void initializeAgentRole() {
        Role agentRole = findRoleByName("AGENT");
        if (agentRole == null) {
            agentRole = createRole("AGENT", "Real Estate Agent with property management access");
            
            // Assign agent permissions
            addPermissionToRole("AGENT", Policies.PROPERTY_CREATE);
            addPermissionToRole("AGENT", Policies.PROPERTY_READ);
            addPermissionToRole("AGENT", Policies.PROPERTY_UPDATE);
            addPermissionToRole("AGENT", Policies.PROPERTY_DELETE);
            addPermissionToRole("AGENT", Policies.PROPERTY_DEACTIVATE);
            
            // Agent can only manage their own properties (handled in permission checks)
            addPermissionToRole("AGENT", Policies.USER_UPDATE); // For their own profile
        }
    }

    private void initializeTenantRole() {
        Role tenantRole = findRoleByName("TENANT");
        if (tenantRole == null) {
            tenantRole = createRole("TENANT", "Tenant with property viewing and application access");
            
            // Assign tenant permissions
            addPermissionToRole("TENANT", Policies.PROPERTY_READ);
            addPermissionToRole("TENANT", Policies.TENANT_APPLICATION);
            addPermissionToRole("TENANT", Policies.TENANT_WISHLIST);
            addPermissionToRole("TENANT", Policies.TENANT_COMPARE);
            addPermissionToRole("TENANT", Policies.USER_UPDATE); // For their own profile
        }
    }

    public Set<String> getUserPermissions(User user) {
        Set<String> permissions = new HashSet<>();
        
        // Add permissions from roles
        for (Role role : user.getRoles()) {
            for (Permission permission : role.getPermissions()) {
                permissions.add(permission.getPermissionName());
            }
        }
        
        return permissions;
    }
}