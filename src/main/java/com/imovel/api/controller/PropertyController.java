package com.imovel.api.controller;

import com.imovel.api.request.PropertyRequestDto;
import com.imovel.api.request.PropertyResponseDto;
import com.imovel.api.response.StandardResponse;
import com.imovel.api.services.PropertyService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/properties")
public class PropertyController {

    private final PropertyService propertyService;

    @Autowired
    public PropertyController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @PostMapping
    public ResponseEntity<StandardResponse<PropertyResponseDto>> createProperty(
            @Valid @RequestBody PropertyRequestDto propertyRequestDto) {
        try {
            PropertyResponseDto createdProperty = propertyService.createProperty(propertyRequestDto);
            return new ResponseEntity<>(
                    new StandardResponse<>("Property created successfully.", null, createdProperty),
                    HttpStatus.CREATED);
        } catch (RuntimeException e) {
            String errorCode = "PROP_CREATE_ERROR";
            if (e.getMessage() != null && e.getMessage().contains("Default user with ID " + 1L + " not found")) {
                errorCode = "DEFAULT_USER_NOT_FOUND";
            }
            return new ResponseEntity<>(
                    new StandardResponse<>(e.getMessage(), errorCode, null),
                    HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<StandardResponse<PropertyResponseDto>> getPropertyById(@PathVariable Long id) {
        try {
            PropertyResponseDto property = propertyService.getPropertyById(id);
            return new ResponseEntity<>(
                    new StandardResponse<>("Property retrieved successfully.", null, property),
                    HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(
                    new StandardResponse<>(e.getMessage(), "PROP_NOT_FOUND", null),
                    HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping
    public ResponseEntity<StandardResponse<Page<PropertyResponseDto>>> getAllProperties(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort) {

        String sortField = "createdAt";
        Sort.Direction direction = Sort.Direction.DESC;

        if (sort.length == 2) {
            sortField = sort[0];
            try {
                direction = Sort.Direction.fromString(sort[1]);
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid sort direction provided: " + sort[1] + ". Using default DESC.");
            }
        } else if (sort.length == 1 && !sort[0].isEmpty()) {
            sortField = sort[0];
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        Page<PropertyResponseDto> propertiesPage = propertyService.getAllProperties(pageable);
        return new ResponseEntity<>(
                new StandardResponse<>("Properties retrieved successfully.", null, propertiesPage),
                HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StandardResponse<PropertyResponseDto>> updateProperty(
            @PathVariable Long id,
            @Valid @RequestBody PropertyRequestDto propertyRequestDto) {
        try {
            PropertyResponseDto updatedProperty = propertyService.updateProperty(id, propertyRequestDto);
            return new ResponseEntity<>(
                    new StandardResponse<>("Property updated successfully.", null, updatedProperty),
                    HttpStatus.OK);
        } catch (RuntimeException e) {
            HttpStatus status;
            String errorCode = "PROP_UPDATE_ERROR";
            if (e.getMessage() == null) {
                status = HttpStatus.INTERNAL_SERVER_ERROR;
            } else if (e.getMessage().toLowerCase().contains("not found")) {
                status = HttpStatus.NOT_FOUND;
                errorCode = "PROP_NOT_FOUND";
            } else if (e.getMessage().toLowerCase().contains("not authorized")) {
                status = HttpStatus.FORBIDDEN;
                errorCode = "PROP_UPDATE_UNAUTHORIZED";
            } else if (e.getMessage().contains("Default user with ID " + 1L + " not found")) {
                status = HttpStatus.INTERNAL_SERVER_ERROR;
                errorCode = "DEFAULT_USER_NOT_FOUND";
            } else {
                status = HttpStatus.BAD_REQUEST;
            }
            return new ResponseEntity<>(
                    new StandardResponse<>(e.getMessage(), errorCode, null),
                    status);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<StandardResponse<Void>> deleteProperty(@PathVariable Long id) {
        try {
            propertyService.deleteProperty(id);
            return new ResponseEntity<>(
                    new StandardResponse<>("Property deleted successfully.", null, null),
                    HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            HttpStatus status;
            String errorCode = "PROP_DELETE_ERROR";
            if (e.getMessage() == null) {
                status = HttpStatus.INTERNAL_SERVER_ERROR;
            } else if (e.getMessage().toLowerCase().contains("not found")) {
                status = HttpStatus.NOT_FOUND;
                errorCode = "PROP_NOT_FOUND";
            } else if (e.getMessage().toLowerCase().contains("not authorized")) {
                status = HttpStatus.FORBIDDEN;
                errorCode = "PROP_DELETE_UNAUTHORIZED";
            } else if (e.getMessage().contains("Default user with ID " + 1L + " not found")) {
                status = HttpStatus.INTERNAL_SERVER_ERROR;
                errorCode = "DEFAULT_USER_NOT_FOUND";
            } else {
                status = HttpStatus.INTERNAL_SERVER_ERROR;
            }
            return new ResponseEntity<>(
                    new StandardResponse<>(e.getMessage(), errorCode, null),
                    status);
        }
    }
}