package com.imovel.api.services;

import com.imovel.api.error.ApiCode;
import com.imovel.api.exception.*;
import com.imovel.api.model.Location;
import com.imovel.api.model.Property;
import com.imovel.api.model.User;
import com.imovel.api.repository.PropertyRepository;
import com.imovel.api.repository.UserRepository;
import com.imovel.api.request.LocationDto;
import com.imovel.api.request.PropertyRequestDto;
import com.imovel.api.response.PropertyResponseDto;
import com.imovel.api.response.StandardResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private static final Long CURRENT_USER_ID = 1L;

    @Autowired
    public PropertyService(PropertyRepository propertyRepository, UserRepository userRepository) {
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
    }

    /**
     * Fetches the "current" user for operations.
     * FOR DEVELOPMENT: Fetches user with hardcoded ID.
     * IN PRODUCTION: This would be replaced by getting user from security context.
     *
     * @throws ResourceNotFoundException if the default user is not found
     */
    private User getCurrentUserForOperations() {
        return userRepository.findById(CURRENT_USER_ID)
                .orElseThrow(() -> new ResourceNotFoundException("User", CURRENT_USER_ID));
    }

    @Transactional
    public StandardResponse<PropertyResponseDto> createProperty(PropertyRequestDto propertyRequestDto) {
        User currentUser = getCurrentUserForOperations();

        Property property = mapToEntity(propertyRequestDto);
        property.setCreatedBy(currentUser);

        Property savedProperty = propertyRepository.save(property);
        return StandardResponse.success(mapToResponseDto(savedProperty));
    }

    @Transactional(readOnly = true)
    public StandardResponse<PropertyResponseDto> getPropertyById(Long id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property", id));
        return StandardResponse.success(mapToResponseDto(property));
    }

    @Transactional(readOnly = true)
    public StandardResponse<Page<PropertyResponseDto>> getAllProperties(Pageable pageable) {
        Page<Property> propertiesPage = propertyRepository.findAll(pageable);
        List<PropertyResponseDto> dtos = propertiesPage.getContent().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
        return StandardResponse.success(new PageImpl<>(dtos, pageable, propertiesPage.getTotalElements()));
    }

    @Transactional
    public StandardResponse<PropertyResponseDto> updateProperty(Long propertyId, PropertyRequestDto propertyRequestDto) {
        User currentUser = getCurrentUserForOperations();
        Property propertyToUpdate = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property", propertyId));

        if (!propertyToUpdate.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new AuthorizationException(ApiCode.PROPERTY_OPERATION_NOT_ALLOWED.getCode().toString(),
                    "User not authorized to update this property", HttpStatus.UNAUTHORIZED);
        }

        updatePropertyFields(propertyToUpdate, propertyRequestDto);
        Property updatedProperty = propertyRepository.save(propertyToUpdate);
        return StandardResponse.success(mapToResponseDto(updatedProperty));
    }

    @Transactional
    public StandardResponse<Void> deleteProperty(Long propertyId) {
        User currentUser = getCurrentUserForOperations();
        Property propertyToDelete = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property", propertyId));

        if (!propertyToDelete.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new AuthorizationException(ApiCode.PROPERTY_OPERATION_NOT_ALLOWED.getCode().toString(),
                    "User not authorized to delete this property",HttpStatus.UNAUTHORIZED);
        }

        propertyRepository.delete(propertyToDelete);
        return StandardResponse.success(null);
    }

    private void updatePropertyFields(Property property, PropertyRequestDto dto) {
        property.setTitle(dto.getTitle());
        property.setDescription(dto.getDescription());
        property.setPrice(dto.getPrice());
        property.setType(dto.getType());
        property.setCategory(dto.getCategory());
        property.setBedrooms(dto.getBedrooms());
        property.setBathrooms(dto.getBathrooms());
        property.setArea(dto.getArea());
        property.setStatus(dto.getStatus());

        if (dto.getLocation() != null) {
            Location location = property.getLocation();
            if (location == null) location = new Location();
            LocationDto locDto = dto.getLocation();
            location.setAddress(locDto.getAddress());
            location.setCity(locDto.getCity());
            location.setState(locDto.getState());
            location.setZipCode(locDto.getZipCode());
            location.setLatitude(locDto.getLatitude());
            location.setLongitude(locDto.getLongitude());
            property.setLocation(location);
        }

        if (dto.getAmenities() != null) {
            property.setAmenities(new HashSet<>(dto.getAmenities()));
        } else {
            property.setAmenities(new HashSet<>());
        }
    }

    private Property mapToEntity(PropertyRequestDto dto) {
        Property entity = new Property();
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setPrice(dto.getPrice());
        entity.setType(dto.getType());
        entity.setCategory(dto.getCategory());
        entity.setBedrooms(dto.getBedrooms());
        entity.setBathrooms(dto.getBathrooms());
        entity.setArea(dto.getArea());
        entity.setStatus(dto.getStatus());

        if (dto.getLocation() != null) {
            Location location = new Location();
            location.setAddress(dto.getLocation().getAddress());
            location.setCity(dto.getLocation().getCity());
            location.setState(dto.getLocation().getState());
            location.setZipCode(dto.getLocation().getZipCode());
            location.setLatitude(dto.getLocation().getLatitude());
            location.setLongitude(dto.getLocation().getLongitude());
            entity.setLocation(location);
        }

        if (dto.getAmenities() != null) {
            entity.setAmenities(new HashSet<>(dto.getAmenities()));
        } else {
            entity.setAmenities(new HashSet<>());
        }

        return entity;
    }

    private PropertyResponseDto mapToResponseDto(Property entity) {
        PropertyResponseDto dto = new PropertyResponseDto();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setPrice(entity.getPrice());
        dto.setType(entity.getType());
        dto.setCategory(entity.getCategory());
        dto.setBedrooms(entity.getBedrooms());
        dto.setBathrooms(entity.getBathrooms());
        dto.setArea(entity.getArea());
        dto.setStatus(entity.getStatus());

        if (entity.getLocation() != null) {
            LocationDto locDto = new LocationDto();
            locDto.setAddress(entity.getLocation().getAddress());
            locDto.setCity(entity.getLocation().getCity());
            locDto.setState(entity.getLocation().getState());
            locDto.setZipCode(entity.getLocation().getZipCode());
            locDto.setLatitude(entity.getLocation().getLatitude());
            locDto.setLongitude(entity.getLocation().getLongitude());
            dto.setLocation(locDto);
        }

        if (entity.getAmenities() != null) {
            dto.setAmenities(new HashSet<>(entity.getAmenities()));
        } else {
            dto.setAmenities(new HashSet<>());
        }

        if (entity.getCreatedBy() != null) {
            dto.setCreatedByEmail(entity.getCreatedBy().getEmail());
        }

        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}