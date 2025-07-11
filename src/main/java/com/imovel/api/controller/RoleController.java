package com.imovel.api.controller;

import com.imovel.api.model.Role;
import com.imovel.api.model.User;
import com.imovel.api.response.StandardResponse;
import com.imovel.api.services.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<StandardResponse<Role>> createRole(@RequestBody Role role) {
        StandardResponse<Role> response = roleService.createRole(role);
        return ResponseEntity.status(response.isSuccess() ? 201 : response.getError().getStatus().value())
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StandardResponse<Role>> getRoleById(@PathVariable Long id) {
        StandardResponse<Role> response = roleService.getRoleById(id);
        return ResponseEntity.status(response.isSuccess() ? 200 : response.getError().getStatus().value())
                .body(response);
    }

    @GetMapping("/name/{roleName}")
    public ResponseEntity<StandardResponse<Role>> getRoleByName(@PathVariable String roleName) {
        StandardResponse<Role> response = roleService.getRoleByName(roleName);
        return ResponseEntity.status(response.isSuccess() ? 200 : response.getError().getStatus().value())
                .body(response);
    }

    @GetMapping
    public ResponseEntity<StandardResponse<List<Role>>> getAllRoles() {
        StandardResponse<List<Role>> response = roleService.getAllRoles();
        return ResponseEntity.status(response.isSuccess() ? 200 : response.getError().getStatus().value())
                .body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StandardResponse<Role>> updateRole(@PathVariable Long id, @RequestBody Role roleDetails) {
        StandardResponse<Role> response = roleService.updateRole(id, roleDetails);
        return ResponseEntity.status(response.isSuccess() ? 200 : response.getError().getStatus().value())
                .body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<StandardResponse<Void>> deleteRole(@PathVariable Long id) {
        StandardResponse<Void> response = roleService.deleteRole(id);
        return ResponseEntity.status(response.isSuccess() ? 200 : response.getError().getStatus().value())
                .body(response);
    }

    @PostMapping("/initialize")
    public ResponseEntity<StandardResponse<Void>> initializeDefaultRoles() {
        StandardResponse<Void> response = roleService.initializeDefaultRoles();
        return ResponseEntity.status(response.isSuccess() ? 200 : response.getError().getStatus().value())
                .body(response);
    }


    @DeleteMapping("/remove-role-from-user")
    public ResponseEntity<StandardResponse<Role>> removeRoleFromUser(
            @RequestBody User user,
            @RequestParam String roleName) {
        return ResponseEntity.ok(roleService.removeRoleFromUser(user, roleName));
    }

    // User-Role Assignment Endpoints

    @PostMapping("/assign-role-to-user")
    public ResponseEntity<StandardResponse<Role>> assignRoleToUser(
            @RequestBody User user,
            @RequestParam String roleName) {
        return ResponseEntity.ok(roleService.addRoleToUser(user, roleName));
    }
}