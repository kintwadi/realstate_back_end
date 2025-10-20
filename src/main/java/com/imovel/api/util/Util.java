package com.imovel.api.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Base64;
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

    /**
     * Convert byte array to Base64 string
     * @param imageBytes Byte array of the image
     * @return Base64 encoded string
     */
    public static String convertBytesToBase64(byte[] imageBytes) {
        if (imageBytes == null || imageBytes.length == 0) {
            return null;
        }
        return Base64.getEncoder().encodeToString(imageBytes);
    }

    /**
     * Convert byte array to Base64 with MIME type
     * @param imageBytes Byte array of the image
     * @param mimeType MIME type of the image
     * @return Base64 encoded string with MIME type
     */
    public static String convertBytesToBase64WithMime(byte[] imageBytes, String mimeType) {
        String base64 = convertBytesToBase64(imageBytes);
        return base64 != null ? "data:" + mimeType + ";base64," + base64 : null;
    }
}
