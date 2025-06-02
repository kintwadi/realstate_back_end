package com.imovel.api.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends ApplicationException {
    public ResourceNotFoundException(String resourceName, Long id) {
        super("RESOURCE_NOT_FOUND", 
              String.format("%s not found with id: %d", resourceName, id),
              HttpStatus.NOT_FOUND);
    }
    
    public ResourceNotFoundException(String resourceName, String identifier) {
        super("RESOURCE_NOT_FOUND",
              String.format("%s not found with identifier: %s", resourceName, identifier),
              HttpStatus.NOT_FOUND);
    }
}