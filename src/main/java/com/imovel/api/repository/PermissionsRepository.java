
// PermissionRepository.java
package com.imovel.api.repository;

import com.imovel.api.model.Permissions;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PermissionsRepository extends JpaRepository<Permissions, Long> {
    Optional<Permissions> findByPermissionName(String permissionName);
    boolean existsByPermissionName(String name);
}
