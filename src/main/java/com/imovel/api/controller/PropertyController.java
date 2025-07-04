package com.imovel.api.controller;

import com.imovel.api.logger.ApiLogger;
import com.imovel.api.request.PropertyRequestDto;
import com.imovel.api.response.PropertyResponseDto;
import com.imovel.api.response.StandardResponse;
import com.imovel.api.security.token.JWTProvider;
import com.imovel.api.services.PropertyService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.StringUtils;


@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/properties")
public class PropertyController {

    private final PropertyService propertyService;
    private final JWTProvider jwtProvider;

    @Autowired
    public PropertyController(PropertyService propertyService, JWTProvider jwtProvider) {
        this.propertyService = propertyService;
        this.jwtProvider = jwtProvider;
    }

    private String buildLogTag(String method) {
        return "PropertyController" + "#" + method;
    }

    /**
     * Extracts the User ID from the JWT in the Authorization header.
     *
     * @param request The incoming HTTP request.
     * @return The Long value of the User ID, or null if not found.
     */
    private Long getUserIdFromToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                // The "userId" claim is added in TokenService.generateTokensForUser
                String userIdStr = jwtProvider.getClaim("userId", token);
                return Long.parseLong(userIdStr);
            } catch (Exception e) {
                ApiLogger.error(buildLogTag("getUserIdFromToken"), "Could not extract userId from token.", e);
                return null;
            }
        }
        return null;
    }

    @PostMapping
    public ResponseEntity<StandardResponse<PropertyResponseDto>> createProperty(
            @Valid @RequestBody PropertyRequestDto propertyRequestDto, HttpServletRequest request) {
        final String TAG = "createProperty";
        ApiLogger.info(buildLogTag(TAG), "Received request to create property.");

        Long currentUserId = getUserIdFromToken(request);

        StandardResponse<PropertyResponseDto> response = propertyService.createProperty(propertyRequestDto, currentUserId);
        HttpStatus status = response.isSuccess() ? HttpStatus.CREATED : response.getError().getStatus();
        return new ResponseEntity<>(response, status);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StandardResponse<PropertyResponseDto>> getPropertyById(@PathVariable Long id) {
        final String TAG = "getPropertyById";
        ApiLogger.info(buildLogTag(TAG), "Received request to get property by ID: " + id);
        StandardResponse<PropertyResponseDto> response = propertyService.getPropertyById(id);
        HttpStatus status = response.isSuccess() ? HttpStatus.OK : response.getError().getStatus();
        return new ResponseEntity<>(response, status);
    }

    @GetMapping
    public ResponseEntity<StandardResponse<Page<PropertyResponseDto>>> getAllProperties(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort) {

        final String TAG = "getAllProperties";
        ApiLogger.info(buildLogTag(TAG), "Received request to get all properties.");

        String sortField = "createdAt";
        Sort.Direction direction = Sort.Direction.DESC;

        try {
            if (sort.length == 2) {
                sortField = sort[0];
                direction = Sort.Direction.fromString(sort[1]);
            } else if (sort.length == 1 && !sort[0].isEmpty()) {
                sortField = sort[0];
            }
        } catch (IllegalArgumentException e) {
            ApiLogger.error(buildLogTag(TAG), "Invalid sort direction provided: " + (sort.length > 1 ? sort[1] : "") + ". Using default DESC.", e);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        StandardResponse<Page<PropertyResponseDto>> response = propertyService.getAllProperties(pageable);
        HttpStatus status = response.isSuccess() ? HttpStatus.OK : response.getError().getStatus();
        return new ResponseEntity<>(response, status);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StandardResponse<PropertyResponseDto>> updateProperty(
            @PathVariable Long id,
            @Valid @RequestBody PropertyRequestDto propertyRequestDto, HttpServletRequest request) {
        final String TAG = "updateProperty";
        ApiLogger.info(buildLogTag(TAG), "Received request to update property with ID: " + id);

        Long currentUserId = getUserIdFromToken(request);

        StandardResponse<PropertyResponseDto> response = propertyService.updateProperty(id, propertyRequestDto, currentUserId);
        HttpStatus status = response.isSuccess() ? HttpStatus.OK : response.getError().getStatus();
        return new ResponseEntity<>(response, status);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<StandardResponse<Void>> deleteProperty(@PathVariable Long id, HttpServletRequest request) {
        final String TAG = "deleteProperty";
        ApiLogger.info(buildLogTag(TAG), "Received request to delete property with ID: " + id);

        Long currentUserId = getUserIdFromToken(request);

        StandardResponse<Void> response = propertyService.deleteProperty(id, currentUserId);
        HttpStatus status = response.isSuccess() ? HttpStatus.OK : response.getError().getStatus();
        return new ResponseEntity<>(response, status);
    }
}