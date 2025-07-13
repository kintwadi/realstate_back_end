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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RolePermissionService {
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
        Optional<Role> roleOptional = roleRepository.findById(roleId);
        if (!roleOptional.isPresent()) {
            return ApplicationResponse.error(ApiCode.ROLE_NOT_FOUND.getCode(),
                    "Role not found with id: " + roleId, ApiCode.ROLE_NOT_FOUND.getHttpStatus());
        }

        Optional<Permissions> permissionOptional = permissionsRepository.findById(permissionId);
        if (!permissionOptional.isPresent()) {
            return ApplicationResponse.error(ApiCode.PERMISSION_NOT_FOUND.getCode(),
                    "Permission not found with id: " + permissionId, ApiCode.PERMISSION_NOT_FOUND.getHttpStatus());
        }

        if (rolePermissionRepository.existsByRoleIdAndPermissionId(roleId, permissionId)) {
            return ApplicationResponse.error(ApiCode.PERMISSION_ASSIGNMENT_FAILED.getCode(),
                    "Permission already assigned to this role", ApiCode.PERMISSION_ASSIGNMENT_FAILED.getHttpStatus());
        }

        try{
            Role role = roleOptional.get();
            Permissions permission = permissionOptional.get();

            RolePermission rolePermission = new RolePermission();
            rolePermission.setRole(role);
            rolePermission.setPermission(permission);
            rolePermissionRepository.save(rolePermission);
            Optional<RoleToPermissionResponse> response = RoleToPermissionResponse.parse(role, permission);

            return  ApplicationResponse.success(response.get(), "Permission assigned successfully");

        }catch (Exception e){

           e.printStackTrace();

        }

        return ApplicationResponse.error(ApiCode.PERMISSION_ASSIGNMENT_FAILED.getCode(),
                "Failed to create response", HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @Transactional
    public ApplicationResponse<RoleToPermissionResponse> assignPermissionToRoleByName(String roleName, String permissionName) {
        Optional<Role> roleOptional = roleRepository.findByRoleName(roleName);
        if (roleOptional.isEmpty()) {
            return ApplicationResponse.error(ApiCode.ROLE_NOT_FOUND.getCode(),
                    "Role not found with name: " + roleName, ApiCode.ROLE_NOT_FOUND.getHttpStatus());
        }

        Optional<Permissions> permissionOptional = permissionsRepository.findByPermissionName(permissionName);
        if (permissionOptional.isEmpty()) {
            return ApplicationResponse.error(ApiCode.PERMISSION_NOT_FOUND.getCode(),
                    "Permission not found with name: " + permissionName, ApiCode.PERMISSION_NOT_FOUND.getHttpStatus());
        }

        Role role = roleOptional.get();
        Permissions permission = permissionOptional.get();

        if (rolePermissionRepository.existsByRoleIdAndPermissionId(role.getId(), permission.getId())) {
            return ApplicationResponse.error(ApiCode.PERMISSION_ASSIGNMENT_FAILED.getCode(),
                    "Permission already assigned to this role", ApiCode.PERMISSION_ASSIGNMENT_FAILED.getHttpStatus());
        }

        try{
            RolePermission rolePermission = new RolePermission();
            rolePermission.setRole(role);
            rolePermission.setPermission(permission);
            rolePermissionRepository.save(rolePermission);
        }catch (Exception e){
            e.printStackTrace();
        }
        return RoleToPermissionResponse.parse(role, permission)
                .map(response -> ApplicationResponse.success(response, "Permission assigned successfully"))
                .orElseGet(() -> ApplicationResponse.error(ApiCode.PERMISSION_ASSIGNMENT_FAILED.getCode(),
                        "Failed to create response", HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Transactional
    public ApplicationResponse<Void> removePermissionFromRole(Long roleId, Long permissionId) {

        if (!rolePermissionRepository.existsByRoleIdAndPermissionId(roleId, permissionId)) {
            return ApplicationResponse.error(ApiCode.PERMISSION_OR_ROLE_NOT_FOUND.getCode(),
                    "Permission not assigned to this role", ApiCode.PERMISSION_OR_ROLE_NOT_FOUND.getHttpStatus());
        }

        Optional<Role> roleOptional = roleRepository.findById(roleId);
        if (roleOptional.isEmpty()) {
            return ApplicationResponse.error(ApiCode.ROLE_NOT_FOUND.getCode(),
                    "Role not found with id: " + roleId, ApiCode.ROLE_NOT_FOUND.getHttpStatus());
        }

        Optional<Permissions> permissionOptional = permissionsRepository.findById(permissionId);
        if (permissionOptional.isEmpty()) {
            return ApplicationResponse.error(ApiCode.PERMISSION_NOT_FOUND.getCode(),
                    "Permission not found with id: " + permissionId, ApiCode.PERMISSION_NOT_FOUND.getHttpStatus());
        }

        Role role = roleOptional.get();
        Permissions permission = permissionOptional.get();
        Optional<RolePermission> byRoleAndPermission = rolePermissionRepository.findByRoleAndPermission(role, permission);
        if(byRoleAndPermission.isPresent())
            rolePermissionRepository.delete(byRoleAndPermission.get());

        return ApplicationResponse.success("Permission removed successfully");
    }

    @Transactional(readOnly = true)
    public ApplicationResponse<Set<RoleToPermissionResponse>> getPermissionsByRoleId(Long roleId) {
        if (!roleRepository.existsById(roleId)) {
            return ApplicationResponse.error(ApiCode.ROLE_NOT_FOUND.getCode(),
                    "Role not found with id: " + roleId, ApiCode.ROLE_NOT_FOUND.getHttpStatus());
        }

        Set<RoleToPermissionResponse> permissions = rolePermissionRepository.findByRoleId(roleId)
                .stream()
                .map(rp -> RoleToPermissionResponse.parse(rp.getRole(), rp.getPermission()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());

        return ApplicationResponse.success(permissions);
    }

    @Transactional(readOnly = true)
    public ApplicationResponse<Set<RoleToPermissionResponse>> getRolesByPermissionId(Long permissionId) {
        if (!permissionsRepository.existsById(permissionId)) {
            return ApplicationResponse.error(ApiCode.PERMISSION_NOT_FOUND.getCode(),
                    "Permission not found with id: " + permissionId, ApiCode.PERMISSION_NOT_FOUND.getHttpStatus());
        }

        Set<RoleToPermissionResponse> roles = rolePermissionRepository.findByPermissionId(permissionId)
                .stream()
                .map(rp -> RoleToPermissionResponse.parse(rp.getRole(), rp.getPermission()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());

        return ApplicationResponse.success(roles);
    }

    @Transactional(readOnly = true)
    public ApplicationResponse<Boolean> hasPermission(Long roleId, Long permissionId) {
        try {
            boolean hasPermission = rolePermissionRepository.existsByRoleIdAndPermissionId(roleId, permissionId);
            return ApplicationResponse.success(hasPermission);
        } catch (Exception e) {
            return ApplicationResponse.error(ApiCode.PERMISSION_RETRIEVAL_FAILED.getCode(),
                    "Failed to check permission: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public ApplicationResponse<RoleToPermissionResponse> updateRolePermissions(Long roleId, Long permissionId) {
        Optional<Role> roleOptional = roleRepository.findById(roleId);
        if (roleOptional.isEmpty()) {
            return ApplicationResponse.error(ApiCode.ROLE_NOT_FOUND.getCode(),
                    "Role not found with id: " + roleId, ApiCode.ROLE_NOT_FOUND.getHttpStatus());
        }

        Optional<Permissions> permissionsOptional = permissionsRepository.findById(permissionId);
        if (permissionsOptional.isEmpty()) {
            return ApplicationResponse.error(ApiCode.ROLE_NOT_FOUND.getCode(),
                    "Permission not found with id: " + permissionId, ApiCode.ROLE_NOT_FOUND.getHttpStatus());
        }
        Optional<RolePermission> rolePermission = rolePermissionRepository.findByRoleAndPermission(roleOptional.get(), permissionsOptional.get());
        if(!rolePermission.isPresent()){
            return ApplicationResponse.error(ApiCode.PERMISSION_OR_ROLE_NOT_FOUND.getCode(),
                    "Permission or Role not found with id", ApiCode.PERMISSION_OR_ROLE_NOT_FOUND.getHttpStatus());
        }
        rolePermissionRepository.save(rolePermission.get());
        Optional<RoleToPermissionResponse> roleToPermissionResponseOptional = RoleToPermissionResponse.parse(rolePermission.get().getRole(), rolePermission.get().getPermission());

        return ApplicationResponse.success(roleToPermissionResponseOptional.get(),"Role permission updated successfully");
    }

    @Transactional
    public ApplicationResponse<RoleToPermissionResponse> clearAllPermissionsFromRole(Long roleId) {
        Optional<Role> roleOptional = roleRepository.findById(roleId);
        if (roleOptional.isEmpty()) {
            return ApplicationResponse.error(ApiCode.ROLE_NOT_FOUND.getCode(),
                    "Role not found with id: " + roleId, ApiCode.ROLE_NOT_FOUND.getHttpStatus());
        }
        List<RolePermission> rolePermissions = rolePermissionRepository.findByRoleId(roleId);
        if (rolePermissions.isEmpty()) {
            return ApplicationResponse.error(ApiCode.PERMISSION_OR_ROLE_NOT_FOUND.getCode(),
                    "Role permission not found with id: " + roleId, ApiCode.PERMISSION_OR_ROLE_NOT_FOUND.getHttpStatus());
        }

        rolePermissionRepository.delete(rolePermissions.get(0));
        Optional<RoleToPermissionResponse> parse = RoleToPermissionResponse.parse(rolePermissions.get(0).getRole(), rolePermissions.get(0).getPermission());
        return ApplicationResponse.success(parse.get(),"All permissions cleared from role");
    }

    @Transactional(readOnly = true)
    public ApplicationResponse<Long> countRolesWithPermission(Long permissionId) {
        if (!permissionsRepository.existsById(permissionId)) {
            return ApplicationResponse.error(ApiCode.PERMISSION_NOT_FOUND.getCode(),
                    "Permission not found with id: " + permissionId, ApiCode.PERMISSION_NOT_FOUND.getHttpStatus());
        }

        long count = rolePermissionRepository.countByPermissionId(permissionId);
        return ApplicationResponse.success(count);
    }

    @Transactional
    public ApplicationResponse<RoleToPermissionResponse> assignDefaultRolePermissions() {
        ApplicationResponse<RoleToPermissionResponse> adminResponse = assignAdminPermissions();
        if (!adminResponse.isSuccess()) {
            return adminResponse;
        }

        ApplicationResponse<RoleToPermissionResponse>agentResponse = assignAgentPermissions();
        if (!agentResponse.isSuccess()) {
            return agentResponse;
        }

        ApplicationResponse<RoleToPermissionResponse> tenantResponse = assignTenantPermissions();
        if (!tenantResponse.isSuccess()) {
            return tenantResponse;
        }

        return ApplicationResponse.success("Default role permissions assigned successfully");
    }

    private ApplicationResponse<RoleToPermissionResponse> assignAdminPermissions() {
        Optional<Role> adminRoleOptional = roleRepository.findByRoleName(RoleReference.ADMIN.name());
        if (adminRoleOptional.isEmpty()) {
            return ApplicationResponse.error(ApiCode.ROLE_NOT_FOUND.getCode(),
                    "Admin role not found", ApiCode.ROLE_NOT_FOUND.getHttpStatus());
        }

        ApplicationResponse<RoleToPermissionResponse>  response = assignPermissionToRoleByName(RoleReference.ADMIN.name(), Policies.PROPERTY_CREATE);

        if (!response.isSuccess()) return ApplicationResponse.error(ApiCode.PERMISSION_ASSIGNMENT_FAILED.getCode(), ApiCode.PERMISSION_ASSIGNMENT_FAILED.getMessage(), ApiCode.PERMISSION_ASSIGNMENT_FAILED.getHttpStatus());

        response = assignPermissionToRoleByName(RoleReference.ADMIN.name(), Policies.PROPERTY_READ);
        if (!response.isSuccess()) return ApplicationResponse.error(ApiCode.PERMISSION_ASSIGNMENT_FAILED.getCode(), ApiCode.PERMISSION_ASSIGNMENT_FAILED.getMessage(), ApiCode.PERMISSION_ASSIGNMENT_FAILED.getHttpStatus());

        response = assignPermissionToRoleByName(RoleReference.ADMIN.name(), Policies.PROPERTY_UPDATE);
        if (!response.isSuccess()) return ApplicationResponse.error(ApiCode.PERMISSION_ASSIGNMENT_FAILED.getCode(), ApiCode.PERMISSION_ASSIGNMENT_FAILED.getMessage(), ApiCode.PERMISSION_ASSIGNMENT_FAILED.getHttpStatus());

        response = assignPermissionToRoleByName(RoleReference.ADMIN.name(), Policies.PROPERTY_DELETE);
        if (!response.isSuccess()) return ApplicationResponse.error(ApiCode.PERMISSION_ASSIGNMENT_FAILED.getCode(), ApiCode.PERMISSION_ASSIGNMENT_FAILED.getMessage(), ApiCode.PERMISSION_ASSIGNMENT_FAILED.getHttpStatus());

        response = assignPermissionToRoleByName(RoleReference.ADMIN.name(), Policies.PROPERTY_DEACTIVATE);
        if (!response.isSuccess()) return ApplicationResponse.error(ApiCode.PERMISSION_ASSIGNMENT_FAILED.getCode(), ApiCode.PERMISSION_ASSIGNMENT_FAILED.getMessage(), ApiCode.PERMISSION_ASSIGNMENT_FAILED.getHttpStatus());

        response = assignPermissionToRoleByName(RoleReference.ADMIN.name(), Policies.PROPERTY_MANAGE_ALL);
        if (!response.isSuccess()) return ApplicationResponse.error(ApiCode.PERMISSION_ASSIGNMENT_FAILED.getCode(), ApiCode.PERMISSION_ASSIGNMENT_FAILED.getMessage(), ApiCode.PERMISSION_ASSIGNMENT_FAILED.getHttpStatus());

        response = assignPermissionToRoleByName(RoleReference.ADMIN.name(), Policies.USER_CREATE);
        if (!response.isSuccess()) return ApplicationResponse.error(ApiCode.PERMISSION_ASSIGNMENT_FAILED.getCode(), ApiCode.PERMISSION_ASSIGNMENT_FAILED.getMessage(), ApiCode.PERMISSION_ASSIGNMENT_FAILED.getHttpStatus());

        response = assignPermissionToRoleByName(RoleReference.ADMIN.name(), Policies.USER_READ);
        if (!response.isSuccess()) return ApplicationResponse.error(ApiCode.PERMISSION_ASSIGNMENT_FAILED.getCode(), ApiCode.PERMISSION_ASSIGNMENT_FAILED.getMessage(), ApiCode.PERMISSION_ASSIGNMENT_FAILED.getHttpStatus());

        response = assignPermissionToRoleByName(RoleReference.ADMIN.name(), Policies.USER_UPDATE);
        if (!response.isSuccess()) return ApplicationResponse.error(ApiCode.PERMISSION_ASSIGNMENT_FAILED.getCode(), ApiCode.PERMISSION_ASSIGNMENT_FAILED.getMessage(), ApiCode.PERMISSION_ASSIGNMENT_FAILED.getHttpStatus());

        response = assignPermissionToRoleByName(RoleReference.ADMIN.name(), Policies.USER_DELETE);
        if (!response.isSuccess()) return ApplicationResponse.error(ApiCode.PERMISSION_ASSIGNMENT_FAILED.getCode(), ApiCode.PERMISSION_ASSIGNMENT_FAILED.getMessage(), ApiCode.PERMISSION_ASSIGNMENT_FAILED.getHttpStatus());

        response = assignPermissionToRoleByName(RoleReference.ADMIN.name(), Policies.USER_DEACTIVATE);
        if (!response.isSuccess()) return ApplicationResponse.error(ApiCode.PERMISSION_ASSIGNMENT_FAILED.getCode(), ApiCode.PERMISSION_ASSIGNMENT_FAILED.getMessage(), ApiCode.PERMISSION_ASSIGNMENT_FAILED.getHttpStatus());

        response = assignPermissionToRoleByName(RoleReference.ADMIN.name(), Policies.FINANCIAL_READ);
        if (!response.isSuccess()) return ApplicationResponse.error(ApiCode.PERMISSION_ASSIGNMENT_FAILED.getCode(), ApiCode.PERMISSION_ASSIGNMENT_FAILED.getMessage(), ApiCode.PERMISSION_ASSIGNMENT_FAILED.getHttpStatus());

        response = assignPermissionToRoleByName(RoleReference.ADMIN.name(), Policies.FINANCIAL_REPORT);
        if (!response.isSuccess()) return ApplicationResponse.error(ApiCode.PERMISSION_ASSIGNMENT_FAILED.getCode(), ApiCode.PERMISSION_ASSIGNMENT_FAILED.getMessage(), ApiCode.PERMISSION_ASSIGNMENT_FAILED.getHttpStatus());

        response = assignPermissionToRoleByName(RoleReference.ADMIN.name(), Policies.SYSTEM_CONFIGURE);
        if (!response.isSuccess()) return ApplicationResponse.error(ApiCode.PERMISSION_ASSIGNMENT_FAILED.getCode(), ApiCode.PERMISSION_ASSIGNMENT_FAILED.getMessage(), ApiCode.PERMISSION_ASSIGNMENT_FAILED.getHttpStatus());

        response = assignPermissionToRoleByName(RoleReference.ADMIN.name(), Policies.AGENT_APPROVE);
        if (!response.isSuccess()) return ApplicationResponse.error(ApiCode.PERMISSION_ASSIGNMENT_FAILED.getCode(), ApiCode.PERMISSION_ASSIGNMENT_FAILED.getMessage(), ApiCode.PERMISSION_ASSIGNMENT_FAILED.getHttpStatus());

        response = assignPermissionToRoleByName(RoleReference.ADMIN.name(), Policies.DISPUTE_RESOLVE);
        if (!response.isSuccess()) return ApplicationResponse.error(ApiCode.PERMISSION_ASSIGNMENT_FAILED.getCode(), ApiCode.PERMISSION_ASSIGNMENT_FAILED.getMessage(), ApiCode.PERMISSION_ASSIGNMENT_FAILED.getHttpStatus());

        return ApplicationResponse.success(response.getData());
    }

    private ApplicationResponse<RoleToPermissionResponse>  assignAgentPermissions() {
        Optional<Role> agentRoleOptional = roleRepository.findByRoleName(RoleReference.AGENT.name());
        if (agentRoleOptional.isEmpty()) {
            return ApplicationResponse.error(ApiCode.ROLE_NOT_FOUND.getCode(),
                    "Agent role not found", ApiCode.ROLE_NOT_FOUND.getHttpStatus());
        }

        ApplicationResponse<RoleToPermissionResponse>      response = assignPermissionToRoleByName(RoleReference.AGENT.name(), Policies.PROPERTY_CREATE);

        if (!response.isSuccess()) return ApplicationResponse.error(ApiCode.PERMISSION_ASSIGNMENT_FAILED.getCode(), ApiCode.PERMISSION_ASSIGNMENT_FAILED.getMessage(), ApiCode.PERMISSION_ASSIGNMENT_FAILED.getHttpStatus());

        response = assignPermissionToRoleByName(RoleReference.AGENT.name(), Policies.PROPERTY_READ);
        if (!response.isSuccess()) return ApplicationResponse.error(ApiCode.PERMISSION_ASSIGNMENT_FAILED.getCode(), ApiCode.PERMISSION_ASSIGNMENT_FAILED.getMessage(), ApiCode.PERMISSION_ASSIGNMENT_FAILED.getHttpStatus());

        response = assignPermissionToRoleByName(RoleReference.AGENT.name(), Policies.PROPERTY_UPDATE);
        if (!response.isSuccess()) return ApplicationResponse.error(ApiCode.PERMISSION_ASSIGNMENT_FAILED.getCode(), ApiCode.PERMISSION_ASSIGNMENT_FAILED.getMessage(), ApiCode.PERMISSION_ASSIGNMENT_FAILED.getHttpStatus());

        response = assignPermissionToRoleByName(RoleReference.AGENT.name(), Policies.PROPERTY_DELETE);
        if (!response.isSuccess()) return ApplicationResponse.error(ApiCode.PERMISSION_ASSIGNMENT_FAILED.getCode(), ApiCode.PERMISSION_ASSIGNMENT_FAILED.getMessage(), ApiCode.PERMISSION_ASSIGNMENT_FAILED.getHttpStatus());

        response = assignPermissionToRoleByName(RoleReference.AGENT.name(), Policies.PROPERTY_DEACTIVATE);
        if (!response.isSuccess()) return ApplicationResponse.error(ApiCode.PERMISSION_ASSIGNMENT_FAILED.getCode(), ApiCode.PERMISSION_ASSIGNMENT_FAILED.getMessage(), ApiCode.PERMISSION_ASSIGNMENT_FAILED.getHttpStatus());

        response = assignPermissionToRoleByName(RoleReference.AGENT.name(), Policies.USER_UPDATE);
        if (!response.isSuccess()) return ApplicationResponse.error(ApiCode.PERMISSION_ASSIGNMENT_FAILED.getCode(), ApiCode.PERMISSION_ASSIGNMENT_FAILED.getMessage(), ApiCode.PERMISSION_ASSIGNMENT_FAILED.getHttpStatus());

        return ApplicationResponse.success(response.getData());
    }

    private ApplicationResponse<RoleToPermissionResponse> assignTenantPermissions() {
        Optional<Role> tenantRoleOptional = roleRepository.findByRoleName(RoleReference.TENANT.name());
        if (tenantRoleOptional.isEmpty()) {
            return ApplicationResponse.error(ApiCode.ROLE_NOT_FOUND.getCode(),
                    "Tenant role not found", ApiCode.ROLE_NOT_FOUND.getHttpStatus());
        }

        ApplicationResponse<RoleToPermissionResponse> response = assignPermissionToRoleByName(RoleReference.TENANT.name(), Policies.PROPERTY_READ);

        if (!response.isSuccess()) return ApplicationResponse.error(ApiCode.PERMISSION_ASSIGNMENT_FAILED.getCode(), ApiCode.PERMISSION_ASSIGNMENT_FAILED.getMessage(), ApiCode.PERMISSION_ASSIGNMENT_FAILED.getHttpStatus());

        response = assignPermissionToRoleByName(RoleReference.TENANT.name(), Policies.TENANT_APPLICATION);
        if (!response.isSuccess()) return ApplicationResponse.error(ApiCode.PERMISSION_ASSIGNMENT_FAILED.getCode(), ApiCode.PERMISSION_ASSIGNMENT_FAILED.getMessage(), ApiCode.PERMISSION_ASSIGNMENT_FAILED.getHttpStatus());

        response = assignPermissionToRoleByName(RoleReference.TENANT.name(), Policies.TENANT_WISHLIST);
        if (!response.isSuccess()) return ApplicationResponse.error(ApiCode.PERMISSION_ASSIGNMENT_FAILED.getCode(), ApiCode.PERMISSION_ASSIGNMENT_FAILED.getMessage(), ApiCode.PERMISSION_ASSIGNMENT_FAILED.getHttpStatus());

        response = assignPermissionToRoleByName(RoleReference.TENANT.name(), Policies.TENANT_COMPARE);
        if (!response.isSuccess()) return ApplicationResponse.error(ApiCode.PERMISSION_ASSIGNMENT_FAILED.getCode(), ApiCode.PERMISSION_ASSIGNMENT_FAILED.getMessage(), ApiCode.PERMISSION_ASSIGNMENT_FAILED.getHttpStatus());

        response = assignPermissionToRoleByName(RoleReference.TENANT.name(), Policies.USER_UPDATE);
        if (!response.isSuccess()) return ApplicationResponse.error(ApiCode.PERMISSION_ASSIGNMENT_FAILED.getCode(), ApiCode.PERMISSION_ASSIGNMENT_FAILED.getMessage(), ApiCode.PERMISSION_ASSIGNMENT_FAILED.getHttpStatus());

        return ApplicationResponse.success(response.getData());
    }

    public ApplicationResponse<Set<PermissionResponse>> getUserPermissions(User user) {
        try {
            Set<PermissionResponse> permissions = new HashSet<>();

            if (user.getRole() == null) {
                return ApplicationResponse.success(permissions);
            }

            // Get all permissions assigned to the user's role
            List<RolePermission> rolePermissions = rolePermissionRepository.findByRoleId(user.getRole().getId());

            for (RolePermission rp : rolePermissions) {
                PermissionResponse.parse(rp.getPermission()).ifPresent(permissions::add);
            }

            return ApplicationResponse.success(permissions);
        } catch (Exception e) {
            return ApplicationResponse.error(ApiCode.PERMISSION_RETRIEVAL_FAILED.getCode(),
                    "Failed to retrieve user permissions: " + e.getMessage(),
                    ApiCode.PERMISSION_RETRIEVAL_FAILED.getHttpStatus());
        }
    }

    public ApplicationResponse<List<RoleToPermissionResponse>>findAll(){

        List<RolePermission> all = rolePermissionRepository.findAll();
        if(all.isEmpty()){
            return ApplicationResponse.error(ApiCode.PERMISSION_NOT_FOUND.getCode(),
                    "no role permissions assigned : ",
                    ApiCode.PERMISSION_NOT_FOUND.getHttpStatus());
        }
        List<RoleToPermissionResponse> roleToPermissions = new ArrayList<>();
        all.forEach(rolePermission -> {
            roleToPermissions.add(RoleToPermissionResponse.parse(rolePermission.getRole(),rolePermission.getPermission()).get());
        });

        return ApplicationResponse.success(roleToPermissions, "all role permissions ");
    }
}