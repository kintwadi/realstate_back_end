package com.imovel.api.controller;

import com.imovel.api.logger.ApiLogger;
import com.imovel.api.request.PropertyRequestDto;
import com.imovel.api.response.PropertyResponseDto;
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
    private static final String LOCATION = "PropertyController";

    @Autowired
    public PropertyController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @PostMapping
    public ResponseEntity<StandardResponse<PropertyResponseDto>> createProperty(
            @Valid @RequestBody PropertyRequestDto propertyRequestDto) {
        ApiLogger.info(LOCATION, "Received request to create property", propertyRequestDto);
        PropertyResponseDto createdProperty = propertyService.createProperty(propertyRequestDto);
        ApiLogger.info(LOCATION, "Successfully created property with ID: " + createdProperty.getId());
        return new ResponseEntity<>(
                StandardResponse.success(createdProperty, "Property created successfully."),
                HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StandardResponse<PropertyResponseDto>> getPropertyById(@PathVariable Long id) {
        ApiLogger.info(LOCATION, "Received request to get property by ID: " + id);
        PropertyResponseDto property = propertyService.getPropertyById(id);
        ApiLogger.info(LOCATION, "Successfully retrieved property with ID: " + id);
        return new ResponseEntity<>(
                StandardResponse.success(property, "Property retrieved successfully."),
                HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<StandardResponse<Page<PropertyResponseDto>>> getAllProperties(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort) {

        ApiLogger.info(LOCATION, "Received request to get all properties. Page: " + page + ", Size: " + size);
        String sortField = "createdAt";
        Sort.Direction direction = Sort.Direction.DESC;

        if (sort.length == 2) {
            sortField = sort[0];
            try {
                direction = Sort.Direction.fromString(sort[1]);
            } catch (IllegalArgumentException e) {
                ApiLogger.error(LOCATION, "Invalid sort direction provided: " + sort[1] + ". Using default DESC.", e);
            }
        } else if (sort.length == 1 && !sort[0].isEmpty()) {
            sortField = sort[0];
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        Page<PropertyResponseDto> propertiesPage = propertyService.getAllProperties(pageable);
        ApiLogger.info(LOCATION, "Successfully retrieved " + propertiesPage.getNumberOfElements() + " properties on page " + page);
        return new ResponseEntity<>(
                StandardResponse.success(propertiesPage, "Properties retrieved successfully."),
                HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StandardResponse<PropertyResponseDto>> updateProperty(
            @PathVariable Long id,
            @Valid @RequestBody PropertyRequestDto propertyRequestDto) {
        ApiLogger.info(LOCATION, "Received request to update property with ID: " + id, propertyRequestDto);
        PropertyResponseDto updatedProperty = propertyService.updateProperty(id, propertyRequestDto);
        ApiLogger.info(LOCATION, "Successfully updated property with ID: " + id);
        return new ResponseEntity<>(
                StandardResponse.success(updatedProperty, "Property updated successfully."),
                HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<StandardResponse<Void>> deleteProperty(@PathVariable Long id) {
        ApiLogger.info(LOCATION, "Received request to delete property with ID: " + id);
        propertyService.deleteProperty(id);
        ApiLogger.info(LOCATION, "Successfully deleted property with ID: " + id);
        return new ResponseEntity<>(
                StandardResponse.success("Property deleted successfully."),
                HttpStatus.OK);
    }
}
