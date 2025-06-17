package com.imovel.api.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.regex.Pattern;

public class Util {

    // Regex pattern for validating email format
    private static final Pattern EMAIL_REGEX_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

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
    public static boolean isEmailInvalid(final String email) {
        return !EMAIL_REGEX_PATTERN.matcher(email).matches();
    }
}
