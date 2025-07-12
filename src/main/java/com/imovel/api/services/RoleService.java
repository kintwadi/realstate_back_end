package com.imovel.api.services;

import com.imovel.api.error.ApiCode;
import com.imovel.api.model.Role;
import com.imovel.api.model.User;
import com.imovel.api.model.enums.RoleReference;
import com.imovel.api.repository.RoleRepository;
import com.imovel.api.repository.UserRepository;
import com.imovel.api.response.ApplicationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository,UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    /**
     * Creates a new role
     * 
     * @param role The role to create
     * @return StandardResponse containing the created role
     */
    @Transactional
    public ApplicationResponse<Role> createRole(Role role) {
        try {
            // Check if role with same name already exists
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

    /**
     * Retrieves a role by ID
     * 
     * @param id The ID of the role to retrieve
     * @return StandardResponse containing the found role or error if not found
     */
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

    /**
     * Retrieves a role by name
     * 
     * @param roleName The name of the role to retrieve
     * @return StandardResponse containing the found role or error if not found
     */
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

    /**
     * Retrieves all roles
     * 
     * @return StandardResponse containing list of all roles
     */
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

    /**
     * Updates an existing role
     * 
     * @param id The ID of the role to update
     * @param roleDetails The new role details
     * @return StandardResponse containing the updated role
     */
    @Transactional
    public ApplicationResponse<Role> updateRole(Long id, Role roleDetails) {
        try {
            Optional<Role> optionalRole = roleRepository.findById(id);
            if (optionalRole.isPresent()) {
                Role existingRole = optionalRole.get();

                // Check if another role with the same name already exists
                if (roleDetails.getRoleName() != null &&
                        !existingRole.getRoleName().equals(roleDetails.getRoleName())) {
                    Optional<Role> roleWithSameName = roleRepository.findByRoleName(roleDetails.getRoleName());
                    if (roleWithSameName.isPresent() && roleWithSameName.get().getRoleId() == id) {
                        return ApplicationResponse.error(ApiCode.ROLE_ALREADY_EXISTS.getCode(),
                                ApiCode.ROLE_ALREADY_EXISTS.getMessage(),
                                ApiCode.ROLE_ALREADY_EXISTS.getHttpStatus());
                    }
                }

                // Update fields
                if (roleDetails.getRoleName() != null) {
                    existingRole.setRoleName(roleDetails.getRoleName());
                }
                if (roleDetails.getDescription() != null) {
                    existingRole.setDescription(roleDetails.getDescription());
                }
                if (roleDetails.getRoleId() != null) {
                    existingRole.setRoleId(roleDetails.getRoleId());
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

    /**
     * Deletes a role by ID
     * 
     * @param id The ID of the role to delete
     * @return StandardResponse with success message or error
     */
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

    /**
     * Initializes default roles if they don't exist
     * 
     * @return StandardResponse with success message
     */
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

    // User-Role assignment
    public ApplicationResponse<Role> addRoleToUser(User user, String roleName) {
        ApplicationResponse<Role> roleResponse = getRoleByName(roleName);

        if (!roleResponse.isSuccess())
        {
            return ApplicationResponse.error(ApiCode.ROLE_NOT_FOUND.getCode(), "failed to add user to role", ApiCode.ROLE_NOT_FOUND.getHttpStatus());
        }
        Role role = roleResponse.getData();
        user.setRole(role);
        role.getUsers().add(user);
        userRepository.save(user);

        return ApplicationResponse.success(role, "Role added to user successfully");
    }
    public ApplicationResponse<Role> removeRoleFromUser(User user, String roleName) {
        ApplicationResponse<Role> roleResponse = getRoleByName(roleName);
        if (!roleResponse.isSuccess()) {
            return ApplicationResponse.error(ApiCode.ROLE_NOT_FOUND.getCode(), "failed to remove role from user", HttpStatus.BAD_REQUEST);
        }

        Role role = roleResponse.getData();
        user.setRole(null);
        role.getUsers().remove(user);
        userRepository.save(user);

        return ApplicationResponse.success(role, "Role removed from user successfully");
    }

}