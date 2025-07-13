package com.imovel.api.repository;

import com.imovel.api.model.Permissions;
import com.imovel.api.model.Role;
import com.imovel.api.model.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission,Long> {

    void deleteByRole_RoleId(Long roleId);
    void deleteByPermission_PermissionId(Long permissionId);

    boolean existsByRoleIdAndPermissionId(Long roleId, Long permissionId);

    @Query("SELECT rp FROM RolePermission rp WHERE rp.role.roleId = :roleId")
    List<RolePermission> findByRoleId(Long roleId);

    @Query("SELECT rp FROM RolePermission rp WHERE rp.permission.permissionId = :permissionId")
    List<RolePermission> findByPermissionId(Long permissionId);

    @Query("SELECT COUNT(rp) FROM RolePermission rp WHERE rp.permission.permissionId = :permissionId")
    long countByPermissionId(Long permissionId);
    Optional<RolePermission> findByRoleAndPermission(Role role, Permissions permission);

}