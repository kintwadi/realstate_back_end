package com.imovel.api.security;

import com.imovel.api.model.Permissions;
import com.imovel.api.model.Property;
import com.imovel.api.model.User;
import com.imovel.api.model.enums.PropertyStatus;
import com.imovel.api.model.enums.RoleReference;
import com.imovel.api.response.ApplicationResponse;
import com.imovel.api.services.PermissionService;
import org.springframework.stereotype.Service;

import java.util.Set;
@Service
public class PermissionCheck {

    private final PermissionService permissionService;

    public PermissionCheck(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    // General permission check
    public boolean hasPermission(User user, String permissionName) {
        if (user == null) return false;
        
        // Admin has all permissions
        if (user.getRole().getRoleName().equals(RoleReference.ADMIN.name())) {
            return true;
        }
        
        ApplicationResponse<Set<Permissions>> response = permissionService.getUserPermissions(user);

        Set<Permissions> permissions = ( Set<Permissions>)response.getData();

        for (Permissions permission : permissions) {
            if(permission.getPermissionName().equals(permissionName)){
                return true;
            }
        }
        return false;
    }

    // Property-related permission checks
    public boolean canCreateProperty(User user) {
        return hasPermission(user, Policies.PROPERTY_CREATE);
    }

    public boolean canViewProperty(User user, Property property) {
        // Everyone can view active properties
        if (property.getStatus().name().equals(PropertyStatus.AVAILABLE.name())) {
            return true;
        }
        
        // For inactive properties, check ownership or admin rights
        return hasPermission(user, Policies.PROPERTY_MANAGE_ALL) || 
               (hasPermission(user, Policies.PROPERTY_READ) && isPropertyOwner(user, property));
    }

    public boolean canUpdateProperty(User user, Property property) {
        return hasPermission(user, Policies.PROPERTY_MANAGE_ALL) || 
               (hasPermission(user, Policies.PROPERTY_UPDATE) && isPropertyOwner(user, property));
    }

    public boolean canDeleteProperty(User user, Property property) {
        return hasPermission(user, Policies.PROPERTY_MANAGE_ALL) || 
               (hasPermission(user, Policies.PROPERTY_DELETE) && isPropertyOwner(user, property));
    }

    public boolean canDeactivateProperty(User user, Property property) {
        return hasPermission(user, Policies.PROPERTY_MANAGE_ALL) || 
               (hasPermission(user, Policies.PROPERTY_DEACTIVATE) && isPropertyOwner(user, property));
    }

    // User-related permission checks
    public boolean canCreateUser(User user) {
        return hasPermission(user, Policies.USER_CREATE);
    }

    public boolean canViewUser(User requester, User targetUser) {
        // Users can always view their own profile
        if (requester.getId().equals(targetUser.getId())) {
            return true;
        }
        return hasPermission(requester, Policies.USER_READ);
    }

    public boolean canUpdateUser(User requester, User targetUser) {
        // Users can always update their own profile
        if (requester.getId().equals(targetUser.getId())) {
            return true;
        }
        return hasPermission(requester, Policies.USER_UPDATE);
    }

    public boolean canDeleteUser(User user) {
        return hasPermission(user, Policies.USER_DELETE);
    }

    public boolean canDeactivateUser(User user) {
        return hasPermission(user, Policies.USER_DEACTIVATE);
    }

    // Financial permission checks
    public boolean canViewFinancialData(User user) {
        return hasPermission(user, Policies.FINANCIAL_READ);
    }

    public boolean canGenerateFinancialReports(User user) {
        return hasPermission(user, Policies.FINANCIAL_REPORT);
    }

    // System configuration permission checks
    public boolean canConfigureSystem(User user) {
        return hasPermission(user, Policies.SYSTEM_CONFIGURE);
    }

    // Agent registration permission checks
    public boolean canApproveAgent(User user) {
        return hasPermission(user, Policies.AGENT_APPROVE);
    }

    // Dispute resolution permission checks
    public boolean canResolveDisputes(User user) {
        return hasPermission(user, Policies.DISPUTE_RESOLVE);
    }

    // Tenant-specific permission checks
    public boolean canSubmitApplication(User user) {
        return hasPermission(user, Policies.TENANT_APPLICATION);
    }

    public boolean canManageWishlist(User user) {
        return hasPermission(user, Policies.TENANT_WISHLIST);
    }

    public boolean canCompareProperties(User user) {
        return hasPermission(user, Policies.TENANT_COMPARE);
    }

    // Helper method to check property ownership
    private boolean isPropertyOwner(User user, Property property) {
        if (user == null || property == null || property.getCreatedBy() == null) {
            return false;
        }
        return property.getCreatedBy().getId().equals(user.getId());
    }
}