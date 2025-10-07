package com.imovel.api.exception;

import com.imovel.api.error.ApiCode;
import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends ApiException {
    public ResourceNotFoundException(String resourceName, Long id) {
        super(ApiCode.RESOURCE_NOT_FOUND.getCode(),
              String.format("%s not found with id: %d", resourceName, id),
              HttpStatus.NOT_FOUND);
    }



    public ResourceNotFoundException(String resourceName, String identifier) {
        super(ApiCode.RESOURCE_NOT_FOUND.getCode(),
              String.format("%s not found with identifier: %s", resourceName, identifier),
              HttpStatus.NOT_FOUND);
    }
}
