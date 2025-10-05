package com.imovel.api.security;


import com.imovel.api.model.AuthDetails;
import org.hibernate.annotations.Comment;
import org.springframework.stereotype.Component;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

@Component
public class PasswordManager {

    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 32;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";

    /**
     * Generates a random salt for password hashing
     * @return Base64 encoded salt
     */
    private String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Hashes a password with the given salt
     * @param password The plain text password to hash
     * @param salt The salt to use for hashing (Base64 encoded)
     * @return Base64 encoded hash
     * @throws RuntimeException if hashing fails
     */
    private String hashPassword(final String password, final String salt) {
        try {
            byte[] saltBytes = Base64.getDecoder().decode(salt);
            KeySpec spec = new PBEKeySpec(
                password.toCharArray(), 
                saltBytes, 
                ITERATIONS, 
                KEY_LENGTH
            );
            
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] hash = factory.generateSecret(spec).getEncoded();
            
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error while hashing password", e);
        }
    }

    /**
     * Verifies a password against stored hash and salt
     * @param password The password to verify
     * @param storedHash The stored hash (Base64 encoded)
     * @param storedSalt The stored salt (Base64 encoded)
     * @return true if password matches, false otherwise
     */
    public boolean verifyPassword(final String password, final String storedHash, final String storedSalt) {
        String computedHash = hashPassword(password, storedSalt);
        return computedHash.equals(storedHash);
    }

    /**
     * Creates AuthDetails for a new user registration
     * @param password The plain text password
     * @return AuthDetails with generated salt and hash
     */
    public AuthDetails createAuthDetails(final String password) {
        String salt = generateSalt();
        String hash = hashPassword(password, salt);
        
        AuthDetails authDetails = new AuthDetails();
        authDetails.setSalt(salt);
        authDetails.setHash(hash);
        
        return authDetails;
    }
}
