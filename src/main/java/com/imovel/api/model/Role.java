package com.imovel.api.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Long id;

    @Column(name = "role_name", unique = true, nullable = false, length = 50)
    private String roleName;

    @Column(name = "description", length = 255)
    private String description;

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<User> users = new HashSet<>();

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RolePermission> rolePermissions = new HashSet<>();

    public Role() {
    }

    public Role(String roleName, String description) {
        if (roleName == null || roleName.isBlank()) {
            throw new IllegalArgumentException("Role name cannot be null or blank");
        }
        this.roleName = roleName;
        this.description = description;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        if (roleName == null || roleName.isBlank()) {
            throw new IllegalArgumentException("Role name cannot be null or blank");
        }
        this.roleName = roleName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<User> getUsers() {
        return new HashSet<>(users); // Return a copy for immutability
    }

    public void addUser(User user) {
        if (user != null) {
            users.add(user);
            user.setRole(this);
        }
    }

    public void removeUser(User user) {
        if (user != null) {
            users.remove(user);
            user.setRole(null);
        }
    }

    public Set<RolePermission> getRolePermissions() {
        return new HashSet<>(rolePermissions); // Return a copy for immutability
    }

    public void addRolePermission(RolePermission rolePermission) {
        if (rolePermission != null) {
            rolePermissions.add(rolePermission);
            rolePermission.setRole(this);
        }
    }

    public void removeRolePermission(RolePermission rolePermission) {
        if (rolePermission != null) {
            rolePermissions.remove(rolePermission);
            rolePermission.setRole(null);
        }
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Role)) return false;
        Role role = (Role) o;
        return roleName.equals(role.roleName);
    }

    @Override
    public int hashCode() {
        return roleName.hashCode();
    }

    @Override
    public String toString() {
        return "Role{" +
                "roleId=" + id +
                ", roleName='" + roleName + '\'' +
                '}';
    }
}
