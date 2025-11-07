package com.imovel.api.services;

import com.imovel.api.error.ApiCode;
import com.imovel.api.model.Permissions;
import com.imovel.api.repository.PermissionsRepository;
import com.imovel.api.response.ApplicationResponse;
import com.imovel.api.response.PermissionResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class PermissionService {
    private final PermissionsRepository permissionRepository;

    public PermissionService(PermissionsRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    @Transactional
    public ApplicationResponse<PermissionResponse> createPermission(String permissionName, String description) {
        try {
            Permissions permission = new Permissions(permissionName, description);
            Optional<PermissionResponse> permissionResponse = PermissionResponse.parse(permissionRepository.save(permission));
            return ApplicationResponse.success(permissionResponse.get(), "Permission created successfully");
        } catch (Exception e) {
            return ApplicationResponse.error(ApiCode.PERMISSION_CREATION_FAILED.getCode(),
                    "Failed to create permission: " + e.getMessage(),
                    ApiCode.PERMISSION_CREATION_FAILED.getHttpStatus());
        }
    }

    public ApplicationResponse<PermissionResponse> findPermissionByName(String permissionName) {
        Optional<Permissions> permission = permissionRepository.findByPermissionName(permissionName);
        if (permission.isEmpty()) {
            return ApplicationResponse.error(ApiCode.PERMISSION_NOT_FOUND.getCode(),
                    "Permission not found with name: " + permissionName,
                    ApiCode.PERMISSION_NOT_FOUND.getHttpStatus());
        }
        return PermissionResponse.parse(permission.get())
                .map(p -> ApplicationResponse.success(p))
                .orElseGet(() -> ApplicationResponse.error(ApiCode.PARSE_ERROR.getCode(),
                        "Failed to parse permission",
                        ApiCode.PARSE_ERROR.getHttpStatus()));
    }

    public ApplicationResponse<List<PermissionResponse>> findAll() {
        List<PermissionResponse> permissionResponses = new ArrayList<>();
        List<Permissions> permissions = permissionRepository.findAll();
        if (permissions.isEmpty()) {
            return ApplicationResponse.error(ApiCode.PERMISSION_NOT_FOUND.getCode(),
                    "no permissions found",
                    ApiCode.PERMISSION_RETRIEVAL_FAILED.getHttpStatus());
        }
        permissions.forEach(permission -> {
            PermissionResponse.parse(permission).ifPresent(permissionResponses::add);
        });
        return ApplicationResponse.success(permissionResponses);
    }

    private void createPermissionIfNotExists(String permissionName, String description) {
        if (!permissionRepository.findByPermissionName(permissionName).isPresent()) {
            createPermission(permissionName, description);
        }
    }
}
