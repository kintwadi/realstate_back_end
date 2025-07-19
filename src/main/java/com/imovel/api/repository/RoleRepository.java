package com.imovel.api.repository;

import com.imovel.api.model.Permissions;
import com.imovel.api.model.Role;
import com.imovel.api.model.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleName(String roleName);

//    @Transactional
//    default void assignPermissionToRole(Role role, Permissions permission, RolePermissionRepository rolePermissionRepository) {
//        if (role == null || permission == null) {
//            throw new IllegalArgumentException("Role and Permission cannot be null");
//        }
//
//        // Check if the permission is already assigned
//        boolean alreadyAssigned = role.getRolePermissions().stream()
//                .anyMatch(rp -> rp.getPermission().equals(permission));
//
//        if (!alreadyAssigned) {
//            RolePermission rolePermission = new RolePermission(role /*, permission*/);
//            rolePermissionRepository.save(rolePermission);
//            role.addRolePermission(rolePermission);
//            permission.addRolePermission(rolePermission);
//            save(role);
//        }
//    }

//    @Transactional
//    default void removePermissionFromRole(Role role, Permissions permission, RolePermissionRepository rolePermissionRepository) {
//        if (role == null || permission == null) {
//            throw new IllegalArgumentException("Role and Permission cannot be null");
//        }
//
//        rolePermissionRepository.findByRoleAndPermission(role, permission)
//                .ifPresent(rolePermission -> {
//                    role.removeRolePermission(rolePermission);
//                    permission.removeRolePermission(rolePermission);
//                    rolePermissionRepository.delete(rolePermission);
//                    save(role);
//                });
//    }
}