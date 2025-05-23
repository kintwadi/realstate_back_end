package com.imovel.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standard API response structure for all endpoints
 * @param <T> Type of the data payload
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StandardResponse<T> {
    private String errorText;
    private String errorCode;
    private T data;
}