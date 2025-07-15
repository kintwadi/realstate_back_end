package com.imovel.api.controller;

import com.imovel.api.model.Role;
import com.imovel.api.model.User;
import com.imovel.api.response.ApplicationResponse;
import com.imovel.api.response.RoleResponse;
import com.imovel.api.services.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;

    @Autowired
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping
    public ApplicationResponse<Role> createRole(@RequestBody Role role) {
        return roleService.createRole(role);
    }

    @GetMapping
    public ApplicationResponse<List<RoleResponse>> findRoles() {

        return roleService.findAll();
    }


    @GetMapping("/{id}")
    public ApplicationResponse<Role> getRoleById(@PathVariable Long id) {
        return roleService.getRoleById(id);
    }

    @GetMapping("/name/{roleName}")
    public ApplicationResponse<Role> getRoleByName(@PathVariable String roleName) {
        return roleService.getRoleByName(roleName);
    }

    @PutMapping("/{id}")
    public ApplicationResponse<Role> updateRole(@PathVariable Long id, @RequestBody Role roleDetails) {
        return roleService.updateRole(id, roleDetails);
    }

    @DeleteMapping("/{id}")
    public ApplicationResponse<Void> deleteRole(@PathVariable Long id) {
        return roleService.deleteRole(id);
    }

    @PostMapping("/initialize")
    public ApplicationResponse<Void> initializeDefaultRoles() {
        return roleService.initializeDefaultRoles();
    }

    @PostMapping("/assign-to-user")
    public ApplicationResponse<Role> assignRoleToUser(
            @RequestBody User user,
            @RequestParam String roleName) {
        return roleService.addRoleToUser(user, roleName);
    }

    @DeleteMapping("/remove-from-user")
    public ApplicationResponse<Role> removeRoleFromUser(
            @RequestBody User user,
            @RequestParam String roleName) {
        return roleService.removeRoleFromUser(user, roleName);
    }
}