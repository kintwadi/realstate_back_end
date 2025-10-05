package com.imovel.api.services;

import com.imovel.api.error.ApiCode;
import com.imovel.api.model.Role;
import com.imovel.api.model.User;
import com.imovel.api.model.enums.RoleReference;
import com.imovel.api.repository.RoleRepository;
import com.imovel.api.repository.UserRepository;
import com.imovel.api.response.ApplicationResponse;
import com.imovel.api.response.RoleResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.imovel.api.logger.ApiLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class RoleService {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository, UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ApplicationResponse<Role> createRole(Role role) {
        try {
            ApiLogger.debug("RoleService.createRole", "Attempting to create role", role.getRoleName());
            if (roleRepository.findByRoleName(role.getRoleName()).isPresent()) {
                ApiLogger.error("RoleService.createRole", "Role already exists", role.getRoleName());
                return ApplicationResponse.error(ApiCode.ROLE_ALREADY_EXISTS.getCode(),
                        ApiCode.ROLE_ALREADY_EXISTS.getMessage(),
                        ApiCode.ROLE_ALREADY_EXISTS.getHttpStatus());
            }

            Role savedRole = roleRepository.save(role);
            ApiLogger.info("RoleService.createRole", "Role created successfully", savedRole);
            return ApplicationResponse.success(savedRole, "Role created successfully");
        } catch (DataIntegrityViolationException e) {
            ApiLogger.error("RoleService.createRole", "Invalid role data", e, role);
            return ApplicationResponse.error(ApiCode.INVALID_ROLE_DATA.getCode(),
                    "Invalid role data: " + e.getMessage(),
                    ApiCode.INVALID_ROLE_DATA.getHttpStatus());
        } catch (Exception e) {
            ApiLogger.error("RoleService.createRole", "Failed to create role", e, role);
            return ApplicationResponse.error(ApiCode.ROLE_CREATION_FAILED.getCode(),
                    "Failed to create role: " + e.getMessage(),
                    ApiCode.ROLE_CREATION_FAILED.getHttpStatus());
        }
    }

    public ApplicationResponse<Role> getRoleById(Long id) {
        try {
            ApiLogger.debug("RoleService.getRoleById", "Attempting to get role by ID", id);
            Optional<Role> role = roleRepository.findById(id);
            if (role.isPresent()) {
                ApiLogger.info("RoleService.getRoleById", "Role retrieved successfully", role.get());
                return ApplicationResponse.success(role.get(), "Role retrieved successfully");
            } else {
                ApiLogger.error("RoleService.getRoleById", "Role not found", id);
                return ApplicationResponse.error(ApiCode.ROLE_NOT_FOUND.getCode(),
                        ApiCode.ROLE_NOT_FOUND.getMessage(),
                        ApiCode.ROLE_NOT_FOUND.getHttpStatus());
            }
        } catch (Exception e) {
            ApiLogger.error("RoleService.getRoleById", "Failed to retrieve role", e, id);
            return ApplicationResponse.error(ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to retrieve role: " + e.getMessage(),
                    ApiCode.SYSTEM_ERROR.getHttpStatus());
        }
    }

    public ApplicationResponse<Role> getRoleByName(String roleName) {
        try {
            ApiLogger.debug("RoleService.getRoleByName", "Attempting to get role by name", roleName);
            Optional<Role> role = roleRepository.findByRoleName(roleName);
            if (role.isPresent()) {
                ApiLogger.info("RoleService.getRoleByName", "Role retrieved successfully", role.get());
                return ApplicationResponse.success(role.get(), "Role retrieved successfully");
            } else {
                ApiLogger.error("RoleService.getRoleByName", "Role not found", roleName);
                return ApplicationResponse.error(ApiCode.ROLE_NOT_FOUND.getCode(),
                        ApiCode.ROLE_NOT_FOUND.getMessage(),
                        ApiCode.ROLE_NOT_FOUND.getHttpStatus());
            }
        } catch (Exception e) {
            ApiLogger.error("RoleService.getRoleByName", "Failed to retrieve role", e, roleName);
            return ApplicationResponse.error(ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to retrieve role: " + e.getMessage(),
                    ApiCode.SYSTEM_ERROR.getHttpStatus());
        }
    }

    public ApplicationResponse<List<Role>> getAllRoles() {
        try {
            ApiLogger.debug("RoleService.getAllRoles", "Attempting to get all roles");
            List<Role> roles = roleRepository.findAll();
            ApiLogger.info("RoleService.getAllRoles", "Roles retrieved successfully", roles.size());
            return ApplicationResponse.success(roles, "Roles retrieved successfully");
        } catch (Exception e) {
            ApiLogger.error("RoleService.getAllRoles", "Failed to retrieve roles", e);
            return ApplicationResponse.error(ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to retrieve roles: " + e.getMessage(),
                    ApiCode.SYSTEM_ERROR.getHttpStatus());
        }
    }

    @Transactional
    public ApplicationResponse<Role> updateRole(Long id, Role roleDetails) {
        try {
            ApiLogger.debug("RoleService.updateRole", "Attempting to update role", id);
            Optional<Role> optionalRole = roleRepository.findById(id);
            if (optionalRole.isPresent()) {
                Role existingRole = optionalRole.get();

                if (roleDetails.getRoleName() != null &&
                        !existingRole.getRoleName().equals(roleDetails.getRoleName())) {
                    Optional<Role> roleWithSameName = roleRepository.findByRoleName(roleDetails.getRoleName());
                    if (roleWithSameName.isPresent() && !roleWithSameName.get().getId().equals(id)) {
                        ApiLogger.error("RoleService.updateRole", "Role already exists", roleDetails.getRoleName());
                        return ApplicationResponse.error(ApiCode.ROLE_ALREADY_EXISTS.getCode(),
                                ApiCode.ROLE_ALREADY_EXISTS.getMessage(),
                                ApiCode.ROLE_ALREADY_EXISTS.getHttpStatus());
                    }
                }

                if (roleDetails.getRoleName() != null) {
                    existingRole.setRoleName(roleDetails.getRoleName());
                }
                if (roleDetails.getDescription() != null) {
                    existingRole.setDescription(roleDetails.getDescription());
                }

                Role updatedRole = roleRepository.save(existingRole);
                ApiLogger.info("RoleService.updateRole", "Role updated successfully", updatedRole);
                return ApplicationResponse.success(updatedRole, "Role updated successfully");
            } else {
                ApiLogger.error("RoleService.updateRole", "Role not found", id);
                return ApplicationResponse.error(ApiCode.ROLE_NOT_FOUND.getCode(),
                        ApiCode.ROLE_NOT_FOUND.getMessage(),
                        ApiCode.ROLE_NOT_FOUND.getHttpStatus());
            }
        } catch (Exception e) {
            ApiLogger.error("RoleService.updateRole", "Failed to update role", e, id);
            return ApplicationResponse.error(ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to update role: " + e.getMessage(),
                    ApiCode.SYSTEM_ERROR.getHttpStatus());
        }
    }

    @Transactional
    public ApplicationResponse<Void> deleteRole(Long id) {
        try {
            ApiLogger.debug("RoleService.deleteRole", "Attempting to delete role", id);
            Optional<Role> optionalRole = roleRepository.findById(id);
            if (optionalRole.isPresent()) {
                Role role = optionalRole.get();
                try {
                    roleRepository.delete(role);
                    ApiLogger.info("RoleService.deleteRole", "Role deleted successfully", id);
                    return ApplicationResponse.success("Role deleted successfully");
                } catch (DataIntegrityViolationException e) {
                    ApiLogger.error("RoleService.deleteRole", "Role in use", e, id);
                    return ApplicationResponse.error(ApiCode.ROLE_IN_USE.getCode(),
                            ApiCode.ROLE_IN_USE.getMessage(),
                            ApiCode.ROLE_IN_USE.getHttpStatus());
                }
            } else {
                ApiLogger.error("RoleService.deleteRole", "Role not found", id);
                return ApplicationResponse.error(ApiCode.ROLE_NOT_FOUND.getCode(),
                        ApiCode.ROLE_NOT_FOUND.getMessage(),
                        ApiCode.ROLE_NOT_FOUND.getHttpStatus());
            }
        } catch (Exception e) {
            ApiLogger.error("RoleService.deleteRole", "Failed to delete role", e, id);
            return ApplicationResponse.error(ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to delete role: " + e.getMessage(),
                    ApiCode.SYSTEM_ERROR.getHttpStatus());
        }
    }

    @Transactional
    public ApplicationResponse<Void> initializeDefaultRoles() {
        try {
            ApiLogger.debug("RoleService.initializeDefaultRoles", "Initializing default roles");
            for (RoleReference roleRef : RoleReference.values()) {
                String roleName = roleRef.name();
                if (!roleRepository.findByRoleName(roleName).isPresent()) {
                    Role role = new Role();
                    role.setRoleName(roleName);
                    role.setDescription("System default " + roleName.toLowerCase() + " role");
                    roleRepository.save(role);
                    ApiLogger.debug("RoleService.initializeDefaultRoles", "Created default role", roleName);
                }
            }
            ApiLogger.info("RoleService.initializeDefaultRoles", "Default roles initialized successfully");
            return ApplicationResponse.success("Default roles initialized successfully");
        } catch (Exception e) {
            ApiLogger.error("RoleService.initializeDefaultRoles", "Failed to initialize default roles", e);
            return ApplicationResponse.error(ApiCode.INITIALIZATION_FAILED.getCode(),
                    "Failed to initialize default roles: " + e.getMessage(),
                    ApiCode.INITIALIZATION_FAILED.getHttpStatus());
        }
    }

    @Transactional
    public ApplicationResponse<Role> addRoleToUser(User user, String roleName) {
        ApiLogger.debug("RoleService.addRoleToUser", "Attempting to add role to user",
                Map.of("userId", user.getId(), "roleName", roleName));
        ApplicationResponse<Role> roleResponse = getRoleByName(roleName);
        if (!roleResponse.isSuccess()) {
            ApiLogger.error("RoleService.addRoleToUser", "Role not found for user",
                    Map.of("userId", user.getId(), "roleName", roleName));
            return ApplicationResponse.error(ApiCode.ROLE_NOT_FOUND.getCode(),
                    "failed to add user to role",
                    ApiCode.ROLE_NOT_FOUND.getHttpStatus());
        }

        Role role = roleResponse.getData();
        user.setRole(role);
        role.getUsers().add(user);
        userRepository.save(user);
        ApiLogger.info("RoleService.addRoleToUser", "Role added to user successfully",
                Map.of("userId", user.getId(), "roleId", role.getId()));
        return ApplicationResponse.success(role, "Role added to user successfully");
    }

    @Transactional
    public ApplicationResponse<Role> removeRoleFromUser(User user, String roleName) {
        ApiLogger.debug("RoleService.removeRoleFromUser", "Attempting to remove role from user",
                Map.of("userId", user.getId(), "roleName", roleName));
        ApplicationResponse<Role> roleResponse = getRoleByName(roleName);
        if (!roleResponse.isSuccess()) {
            ApiLogger.error("RoleService.removeRoleFromUser", "Role not found for user",
                    Map.of("userId", user.getId(), "roleName", roleName));
            return ApplicationResponse.error(ApiCode.ROLE_NOT_FOUND.getCode(),
                    "failed to remove role from user",
                    HttpStatus.BAD_REQUEST);
        }

        Role role = roleResponse.getData();
        user.setRole(null);
        role.getUsers().remove(user);
        userRepository.save(user);
        ApiLogger.info("RoleService.removeRoleFromUser", "Role removed from user successfully",
                Map.of("userId", user.getId(), "roleId", role.getId()));
        return ApplicationResponse.success(role, "Role removed from user successfully");
    }

    public ApplicationResponse<List<RoleResponse>> findAll() {
        try {
            ApiLogger.debug("RoleService.findAll", "Attempting to find all roles");
            List<RoleResponse> roleResponseList = new ArrayList<>();
            List<Role> roles = roleRepository.findAll();

            if(roles.isEmpty()){
                ApiLogger.error("RoleService.findAll", "No roles found");
                return ApplicationResponse.error(ApiCode.ROLE_NOT_FOUND.getCode(),
                        "failed to retrieve list of roles",
                        ApiCode.ROLE_NOT_FOUND.getHttpStatus());
            }

            roles.forEach(role -> {
                roleResponseList.add(RoleResponse.parse(role).get());
            });

            ApiLogger.info("RoleService.findAll", "Roles found successfully", roleResponseList.size());
            return ApplicationResponse.success(roleResponseList,"list of roles found successfully");
        } catch (Exception e) {
            ApiLogger.error("RoleService.findAll", "Failed to retrieve roles", e);
            return ApplicationResponse.error(ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to retrieve roles: " + e.getMessage(),
                    ApiCode.SYSTEM_ERROR.getHttpStatus());
        }
    }
}
