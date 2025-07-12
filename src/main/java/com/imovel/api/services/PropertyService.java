package com.imovel.api.services;

import com.imovel.api.error.ApiCode;
import com.imovel.api.exception.AuthorizationException;
import com.imovel.api.exception.ResourceNotFoundException;
import com.imovel.api.logger.ApiLogger;
import com.imovel.api.model.Location;
import com.imovel.api.model.Property;
import com.imovel.api.model.User;
import com.imovel.api.model.embeddable.AccordionItem;
import com.imovel.api.model.embeddable.NearbyPlace;
import com.imovel.api.repository.PropertyRepository;
import com.imovel.api.repository.UserRepository;
import com.imovel.api.request.AccordionItemDto;
import com.imovel.api.request.LocationDto;
import com.imovel.api.request.NearbyPlaceDto;
import com.imovel.api.request.PropertyRequestDto;
import com.imovel.api.response.PropertyResponseDto;
import com.imovel.api.response.ApplicationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;

    @Autowired
    public PropertyService(PropertyRepository propertyRepository, UserRepository userRepository) {
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ApplicationResponse<PropertyResponseDto> createProperty(PropertyRequestDto propertyRequestDto, Long currentUserId) {
        final String TAG = "createProperty";
        ApiLogger.info(buildLogTag(TAG), "Attempting to create a new property.", propertyRequestDto);
        try {
            if (currentUserId == null) {
                throw new AuthorizationException(ApiCode.PERMISSION_DENIED.getCode(), "User ID could not be determined from token.", ApiCode.PERMISSION_DENIED.getHttpStatus());
            }
            User currentUser = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", currentUserId));

            Property property = mapToEntity(propertyRequestDto, new Property());
            property.setCreatedBy(currentUser);
            Property savedProperty = propertyRepository.save(property);
            ApiLogger.info(buildLogTag(TAG), "Successfully created property with ID: " + savedProperty.getId());
            return ApplicationResponse.success(mapToResponseDto(savedProperty), "Property created successfully.");
        } catch (Exception e) {
            ApiLogger.error(buildLogTag(TAG), "Error creating property.", e, propertyRequestDto);
            return ApplicationResponse.error(ApiCode.SYSTEM_ERROR.getCode(), e.getMessage(), ApiCode.SYSTEM_ERROR.getHttpStatus());
        }
    }

    @Transactional(readOnly = true)
    public ApplicationResponse<PropertyResponseDto> getPropertyById(Long id) {
        final String TAG = "getPropertyById";
        ApiLogger.info(buildLogTag(TAG), "Attempting to retrieve property with ID: " + id);
        try {
            Property property = propertyRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Property", id));
            ApiLogger.info(buildLogTag(TAG), "Successfully retrieved property with ID: " + id);
            return ApplicationResponse.success(mapToResponseDto(property), "Property retrieved successfully.");
        } catch (ResourceNotFoundException e) {
            ApiLogger.error(buildLogTag(TAG), e.getMessage());
            return ApplicationResponse.error(ApiCode.PROPERTY_NOT_FOUND.getCode(), e.getMessage(), ApiCode.PROPERTY_NOT_FOUND.getHttpStatus());
        } catch (Exception e) {
            ApiLogger.error(buildLogTag(TAG), "Error retrieving property with ID: " + id, e);
            return ApplicationResponse.error(ApiCode.SYSTEM_ERROR.getCode(), e.getMessage(), ApiCode.SYSTEM_ERROR.getHttpStatus());
        }
    }

    @Transactional(readOnly = true)
    public ApplicationResponse<Page<PropertyResponseDto>> getAllProperties(Pageable pageable) {
        final String TAG = "getAllProperties";
        ApiLogger.info(buildLogTag(TAG), "Attempting to retrieve all properties for page: " + pageable.getPageNumber());
        try {
            Page<Property> propertiesPage = propertyRepository.findAll(pageable);
            List<PropertyResponseDto> dtos = propertiesPage.getContent().stream()
                    .map(this::mapToResponseDto)
                    .collect(Collectors.toList());
            Page<PropertyResponseDto> responsePage = new PageImpl<>(dtos, pageable, propertiesPage.getTotalElements());
            ApiLogger.info(buildLogTag(TAG), "Successfully retrieved " + responsePage.getNumberOfElements() + " properties.");
            return ApplicationResponse.success(responsePage, "Properties retrieved successfully.");
        } catch (Exception e) {
            ApiLogger.error(buildLogTag(TAG), "Error retrieving properties.", e);
            return ApplicationResponse.error(ApiCode.SYSTEM_ERROR.getCode(), e.getMessage(), ApiCode.SYSTEM_ERROR.getHttpStatus());
        }
    }

    @Transactional
    public ApplicationResponse<PropertyResponseDto> updateProperty(Long propertyId, PropertyRequestDto propertyRequestDto, Long currentUserId) {
        final String TAG = "updateProperty";
        ApiLogger.info(buildLogTag(TAG), "Attempting to update property with ID: " + propertyId, propertyRequestDto);
        try {
            if (currentUserId == null) {
                throw new AuthorizationException(ApiCode.PERMISSION_DENIED.getCode(), "User ID could not be determined from token.", ApiCode.PERMISSION_DENIED.getHttpStatus());
            }

            Property propertyToUpdate = propertyRepository.findById(propertyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Property", propertyId));

            // Authorization check
            if (!propertyToUpdate.getCreatedBy().getId().equals(currentUserId)) {
                throw new AuthorizationException(
                        ApiCode.PERMISSION_DENIED.getCode(),
                        ApiCode.PERMISSION_DENIED.getMessage(),
                        ApiCode.PERMISSION_DENIED.getHttpStatus()
                );
            }

            mapToEntity(propertyRequestDto, propertyToUpdate);
            Property updatedProperty = propertyRepository.save(propertyToUpdate);
            ApiLogger.info(buildLogTag(TAG), "Successfully updated property with ID: " + propertyId);
            return ApplicationResponse.success(mapToResponseDto(updatedProperty), "Property updated successfully.");
        } catch (ResourceNotFoundException e) {
            ApiLogger.error(buildLogTag(TAG), e.getMessage());
            return ApplicationResponse.error(ApiCode.PROPERTY_NOT_FOUND.getCode(), e.getMessage(), ApiCode.PROPERTY_NOT_FOUND.getHttpStatus());
        } catch (AuthorizationException e) {
            ApiLogger.error(buildLogTag(TAG), e.getMessage());
            return ApplicationResponse.error(ApiCode.PERMISSION_DENIED.getCode(), e.getMessage(), ApiCode.PERMISSION_DENIED.getHttpStatus());
        } catch (Exception e) {
            ApiLogger.error(buildLogTag(TAG), "Error updating property with ID: " + propertyId, e);
            return ApplicationResponse.error(ApiCode.SYSTEM_ERROR.getCode(), e.getMessage(), ApiCode.SYSTEM_ERROR.getHttpStatus());
        }
    }

    @Transactional
    public ApplicationResponse<Void> deleteProperty(Long propertyId, Long currentUserId) {
        final String TAG = "deleteProperty";
        ApiLogger.info(buildLogTag(TAG), "Attempting to delete property with ID: " + propertyId);
        try {
            if (currentUserId == null) {
                throw new AuthorizationException(ApiCode.PERMISSION_DENIED.getCode(), "User ID could not be determined from token.", ApiCode.PERMISSION_DENIED.getHttpStatus());
            }

            Property propertyToDelete = propertyRepository.findById(propertyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Property", propertyId));

            // Authorization check
            if (!propertyToDelete.getCreatedBy().getId().equals(currentUserId)) {
                throw new AuthorizationException(
                        ApiCode.PERMISSION_DENIED.getCode(),
                        ApiCode.PERMISSION_DENIED.getMessage(),
                        ApiCode.PERMISSION_DENIED.getHttpStatus()
                );
            }
            propertyRepository.delete(propertyToDelete);
            ApiLogger.info(buildLogTag(TAG), "Successfully deleted property with ID: " + propertyId);
            return ApplicationResponse.success("Property deleted successfully.");
        } catch (ResourceNotFoundException e) {
            ApiLogger.error(buildLogTag(TAG), e.getMessage());
            return ApplicationResponse.error(ApiCode.PROPERTY_NOT_FOUND.getCode(), e.getMessage(), ApiCode.PROPERTY_NOT_FOUND.getHttpStatus());
        } catch (AuthorizationException e) {
            ApiLogger.error(buildLogTag(TAG), e.getMessage());
            return ApplicationResponse.error(ApiCode.PERMISSION_DENIED.getCode(), e.getMessage(), ApiCode.PERMISSION_DENIED.getHttpStatus());
        } catch (Exception e) {
            ApiLogger.error(buildLogTag(TAG), "Error deleting property with ID: " + propertyId, e);
            return ApplicationResponse.error(ApiCode.SYSTEM_ERROR.getCode(), e.getMessage(), ApiCode.SYSTEM_ERROR.getHttpStatus());
        }
    }

    private String buildLogTag(String method) {
        return "PropertyService" + "#" + method;
    }

    private Property mapToEntity(PropertyRequestDto dto, Property entity) {
        entity.setMainTitle(dto.getMainTitle());
        entity.setType(dto.getType());
        entity.setCategory(dto.getCategory());
        entity.setPrice(dto.getPrice());
        entity.setKeywords(dto.getKeywords());
        entity.setContactPhone(dto.getContactPhone());
        entity.setContactEmail(dto.getContactEmail());
        entity.setArea(dto.getArea());
        entity.setBedrooms(dto.getBedrooms());
        entity.setBathrooms(dto.getBathrooms());
        entity.setParkingSpots(dto.getParkingSpots());
        entity.setMaxAdultsAccommodation(dto.getMaxAdultsAccommodation());
        entity.setMaxChildrenAccommodation(dto.getMaxChildrenAccommodation());
        entity.setWebsite(dto.getWebsite());
        entity.setDescription(dto.getDescription());
        entity.setEnableAccordionWidget(dto.isEnableAccordionWidget());
        entity.setShowSimilarProperties(dto.isShowSimilarProperties());
        entity.setShowPriceChangeDynamics(dto.isShowPriceChangeDynamics());
        entity.setShowGoogleMaps(dto.isShowGoogleMaps());
        entity.setStatus(dto.getStatus());
        if (dto.getLocation() != null) {
            Location location = entity.getLocation();
            if (location == null) location = new Location();
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
        if (dto.getNearbyPlaces() != null) {
            entity.getNearbyPlaces().clear();
            entity.getNearbyPlaces().addAll(dto.getNearbyPlaces().stream()
                    .map(npDto -> new NearbyPlace(npDto.getPlaceType(), npDto.getName(), npDto.getDistance()))
                    .toList());
        } else {
            entity.getNearbyPlaces().clear();
        }
        if (dto.getAccordionItems() != null) {
            entity.getAccordionItems().clear();
            entity.getAccordionItems().addAll(dto.getAccordionItems().stream()
                    .map(aiDto -> new AccordionItem(aiDto.getTitle(), aiDto.getDetails()))
                    .toList());
        } else {
            entity.getAccordionItems().clear();
        }
        return entity;
    }

    private PropertyResponseDto mapToResponseDto(Property entity) {
        PropertyResponseDto dto = new PropertyResponseDto();
        dto.setId(entity.getId());
        dto.setMainTitle(entity.getMainTitle());
        dto.setType(entity.getType());
        dto.setCategory(entity.getCategory());
        dto.setPrice(entity.getPrice());
        dto.setKeywords(entity.getKeywords());
        dto.setContactPhone(entity.getContactPhone());
        dto.setContactEmail(entity.getContactEmail());
        dto.setArea(entity.getArea());
        dto.setBedrooms(entity.getBedrooms());
        dto.setBathrooms(entity.getBathrooms());
        dto.setParkingSpots(entity.getParkingSpots());
        dto.setMaxAdultsAccommodation(entity.getMaxAdultsAccommodation());
        dto.setMaxChildrenAccommodation(entity.getMaxChildrenAccommodation());
        dto.setWebsite(entity.getWebsite());
        dto.setDescription(entity.getDescription());
        dto.setEnableAccordionWidget(entity.isEnableAccordionWidget());
        dto.setShowSimilarProperties(entity.isShowSimilarProperties());
        dto.setShowPriceChangeDynamics(entity.isShowPriceChangeDynamics());
        dto.setShowGoogleMaps(entity.isShowGoogleMaps());
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
        if (entity.getNearbyPlaces() != null) {
            dto.setNearbyPlaces(entity.getNearbyPlaces().stream()
                    .map(np -> new NearbyPlaceDto(np.getPlaceType(), np.getName(), np.getDistance()))
                    .collect(Collectors.toList()));
        } else {
            dto.setNearbyPlaces(new ArrayList<>());
        }
        if (entity.getAccordionItems() != null) {
            dto.setAccordionItems(entity.getAccordionItems().stream()
                    .map(ai -> new AccordionItemDto(ai.getTitle(), ai.getDetails()))
                    .collect(Collectors.toList()));
        } else {
            dto.setAccordionItems(new ArrayList<>());
        }
        if (entity.getCreatedBy() != null) {
            dto.setCreatedByEmail(entity.getCreatedBy().getEmail());
        }
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

}
