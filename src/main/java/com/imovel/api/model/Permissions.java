package com.imovel.api.model;

import jakarta.persistence.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "permissions")
public class Permissions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "permission_id")
    private Long id;

    @Column(name = "permission_name", unique = true, nullable = false, length = 100)
    private String permissionName;

    @Column(name = "description", length = 255)
    private String description;

    @OneToMany(mappedBy = "permission", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RolePermission> rolePermissions = new HashSet<>();

    public Permissions() {
        // Default constructor required by JPA
    }

    public Permissions(String permissionName, String description) {
        if (permissionName == null || permissionName.isBlank()) {
            throw new IllegalArgumentException("Permission name cannot be null or blank");
        }
        this.permissionName = permissionName;
        this.description = description;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPermissionName() {
        return permissionName;
    }

    public void setPermissionName(String permissionName) {
        if (permissionName == null || permissionName.isBlank()) {
            throw new IllegalArgumentException("Permission name cannot be null or blank");
        }
        this.permissionName = permissionName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<RolePermission> getRolePermissions() {
        return Collections.unmodifiableSet(rolePermissions);
    }

    public void addRolePermission(RolePermission rolePermission) {
        if (rolePermission != null) {
            rolePermissions.add(rolePermission);
            rolePermission.setPermission(this);
        }
    }

    public void removeRolePermission(RolePermission rolePermission) {
        if (rolePermission != null) {
            rolePermissions.remove(rolePermission);
            rolePermission.setPermission(null);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Permissions)) return false;
        Permissions that = (Permissions) o;
        return permissionName.equals(that.permissionName);
    }

    @Override
    public int hashCode() {
        return permissionName.hashCode();
    }

}
