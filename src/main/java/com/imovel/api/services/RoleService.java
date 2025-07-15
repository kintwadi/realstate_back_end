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

import java.util.ArrayList;
import java.util.List;
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
            if (roleRepository.findByRoleName(role.getRoleName()).isPresent()) {
                return ApplicationResponse.error(ApiCode.ROLE_ALREADY_EXISTS.getCode(),
                        ApiCode.ROLE_ALREADY_EXISTS.getMessage(),
                        ApiCode.ROLE_ALREADY_EXISTS.getHttpStatus());
            }

            Role savedRole = roleRepository.save(role);
            return ApplicationResponse.success(savedRole, "Role created successfully");
        } catch (DataIntegrityViolationException e) {
            return ApplicationResponse.error(ApiCode.INVALID_ROLE_DATA.getCode(),
                    "Invalid role data: " + e.getMessage(),
                    ApiCode.INVALID_ROLE_DATA.getHttpStatus());
        } catch (Exception e) {
            return ApplicationResponse.error(ApiCode.ROLE_CREATION_FAILED.getCode(),
                    "Failed to create role: " + e.getMessage(),
                    ApiCode.ROLE_CREATION_FAILED.getHttpStatus());
        }
    }

    public ApplicationResponse<Role> getRoleById(Long id) {
        try {
            Optional<Role> role = roleRepository.findById(id);
            return role.map(value -> ApplicationResponse.success(value, "Role retrieved successfully"))
                    .orElseGet(() -> ApplicationResponse.error(ApiCode.ROLE_NOT_FOUND.getCode(),
                            ApiCode.ROLE_NOT_FOUND.getMessage(),
                            ApiCode.ROLE_NOT_FOUND.getHttpStatus()));
        } catch (Exception e) {
            return ApplicationResponse.error(ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to retrieve role: " + e.getMessage(),
                    ApiCode.SYSTEM_ERROR.getHttpStatus());
        }
    }

    public ApplicationResponse<Role> getRoleByName(String roleName) {
        try {
            Optional<Role> role = roleRepository.findByRoleName(roleName);
            return role.map(value -> ApplicationResponse.success(value, "Role retrieved successfully"))
                    .orElseGet(() -> ApplicationResponse.error(ApiCode.ROLE_NOT_FOUND.getCode(),
                            ApiCode.ROLE_NOT_FOUND.getMessage(),
                            ApiCode.ROLE_NOT_FOUND.getHttpStatus()));
        } catch (Exception e) {
            return ApplicationResponse.error(ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to retrieve role: " + e.getMessage(),
                    ApiCode.SYSTEM_ERROR.getHttpStatus());
        }
    }

    public ApplicationResponse<List<Role>> getAllRoles() {
        try {
            List<Role> roles = roleRepository.findAll();
            return ApplicationResponse.success(roles, "Roles retrieved successfully");
        } catch (Exception e) {
            return ApplicationResponse.error(ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to retrieve roles: " + e.getMessage(),
                    ApiCode.SYSTEM_ERROR.getHttpStatus());
        }
    }

    @Transactional
    public ApplicationResponse<Role> updateRole(Long id, Role roleDetails) {
        try {
            Optional<Role> optionalRole = roleRepository.findById(id);
            if (optionalRole.isPresent()) {
                Role existingRole = optionalRole.get();

                if (roleDetails.getRoleName() != null &&
                        !existingRole.getRoleName().equals(roleDetails.getRoleName())) {
                    Optional<Role> roleWithSameName = roleRepository.findByRoleName(roleDetails.getRoleName());
                    if (roleWithSameName.isPresent() && !roleWithSameName.get().getId().equals(id)) {
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
                return ApplicationResponse.success(updatedRole, "Role updated successfully");
            } else {
                return ApplicationResponse.error(ApiCode.ROLE_NOT_FOUND.getCode(),
                        ApiCode.ROLE_NOT_FOUND.getMessage(),
                        ApiCode.ROLE_NOT_FOUND.getHttpStatus());
            }
        } catch (Exception e) {
            return ApplicationResponse.error(ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to update role: " + e.getMessage(),
                    ApiCode.SYSTEM_ERROR.getHttpStatus());
        }
    }

    @Transactional
    public ApplicationResponse<Void> deleteRole(Long id) {
        try {
            Optional<Role> optionalRole = roleRepository.findById(id);
            if (optionalRole.isPresent()) {
                Role role = optionalRole.get();
                try {
                    roleRepository.delete(role);
                    return ApplicationResponse.success("Role deleted successfully");
                } catch (DataIntegrityViolationException e) {
                    return ApplicationResponse.error(ApiCode.ROLE_IN_USE.getCode(),
                            ApiCode.ROLE_IN_USE.getMessage(),
                            ApiCode.ROLE_IN_USE.getHttpStatus());
                }
            } else {
                return ApplicationResponse.error(ApiCode.ROLE_NOT_FOUND.getCode(),
                        ApiCode.ROLE_NOT_FOUND.getMessage(),
                        ApiCode.ROLE_NOT_FOUND.getHttpStatus());
            }
        } catch (Exception e) {
            return ApplicationResponse.error(ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to delete role: " + e.getMessage(),
                    ApiCode.SYSTEM_ERROR.getHttpStatus());
        }
    }

    @Transactional
    public ApplicationResponse<Void> initializeDefaultRoles() {
        try {
            for (RoleReference roleRef : RoleReference.values()) {
                String roleName = roleRef.name();
                if (!roleRepository.findByRoleName(roleName).isPresent()) {
                    Role role = new Role();
                    role.setRoleName(roleName);
                    role.setDescription("System default " + roleName.toLowerCase() + " role");
                    roleRepository.save(role);
                }
            }
            return ApplicationResponse.success("Default roles initialized successfully");
        } catch (Exception e) {
            return ApplicationResponse.error(ApiCode.INITIALIZATION_FAILED.getCode(),
                    "Failed to initialize default roles: " + e.getMessage(),
                    ApiCode.INITIALIZATION_FAILED.getHttpStatus());
        }
    }

    @Transactional
    public ApplicationResponse<Role> addRoleToUser(User user, String roleName) {
        ApplicationResponse<Role> roleResponse = getRoleByName(roleName);
        if (!roleResponse.isSuccess()) {
            return ApplicationResponse.error(ApiCode.ROLE_NOT_FOUND.getCode(), 
                    "failed to add user to role", 
                    ApiCode.ROLE_NOT_FOUND.getHttpStatus());
        }
        
        Role role = roleResponse.getData();
        user.setRole(role);
        role.getUsers().add(user);
        userRepository.save(user);
        return ApplicationResponse.success(role, "Role added to user successfully");
    }

    @Transactional
    public ApplicationResponse<Role> removeRoleFromUser(User user, String roleName) {
        ApplicationResponse<Role> roleResponse = getRoleByName(roleName);
        if (!roleResponse.isSuccess()) {
            return ApplicationResponse.error(ApiCode.ROLE_NOT_FOUND.getCode(), 
                    "failed to remove role from user", 
                    HttpStatus.BAD_REQUEST);
        }

        Role role = roleResponse.getData();
        user.setRole(null);
        role.getUsers().remove(user);
        userRepository.save(user);
        return ApplicationResponse.success(role, "Role removed from user successfully");
    }

    public ApplicationResponse<List<RoleResponse>> findAll() {


        List<RoleResponse> roleResponseList = new ArrayList<>();
        List<Role>roles = roleRepository.findAll();

        System.out.println("roles: "+roles);
        if(roles.isEmpty()){

            return ApplicationResponse.error(ApiCode.ROLE_NOT_FOUND.getCode(),
                    "failed to retrieve list of roles",
                    ApiCode.ROLE_NOT_FOUND.getHttpStatus());
        }
        roles.forEach(role ->{

            roleResponseList.add( RoleResponse.parse(role).get());
        });
        return ApplicationResponse.success(roleResponseList,"list of  roles found successfully");
    }
}