package com.imovel.api.controller;

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
        StandardResponse<PropertyResponseDto> response = propertyService.createProperty(propertyRequestDto);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StandardResponse<PropertyResponseDto>> getPropertyById(@PathVariable Long id) {
        StandardResponse<PropertyResponseDto> response = propertyService.getPropertyById(id);
        return ResponseEntity.ok(response);
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
                // Log warning and use default direction
                System.err.println("Invalid sort direction provided: " + sort[1] + ". Using default DESC.");
            }
        } else if (sort.length == 1 && !sort[0].isEmpty()) {
            sortField = sort[0];
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        StandardResponse<Page<PropertyResponseDto>> response = propertyService.getAllProperties(pageable);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StandardResponse<PropertyResponseDto>> updateProperty(
            @PathVariable Long id,
            @Valid @RequestBody PropertyRequestDto propertyRequestDto) {
        StandardResponse<PropertyResponseDto> response = propertyService.updateProperty(id, propertyRequestDto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<StandardResponse<Void>> deleteProperty(@PathVariable Long id) {
        StandardResponse<Void> response = propertyService.deleteProperty(id);
        return ResponseEntity.ok(response);
    }
}