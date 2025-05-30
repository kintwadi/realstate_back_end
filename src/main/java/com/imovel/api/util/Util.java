package com.imovel.api.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Util {
    private static ObjectMapper  objectMapper = new ObjectMapper();

    public static String toJSON(Object object) {
        try{
            return objectMapper.writeValueAsString(object);
        }catch (JsonProcessingException e)
        {
            System.out.println(e.getMessage());
        }
        return "{}";
    }
}
