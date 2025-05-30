package com.imovel.api.security.keystore;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import java.security.KeyStore;
import java.util.Objects;
import java.util.Optional;

/**
 * Manages loading and retrieving keys from various keystore sources.
 * Supports loading from classpath resources, system properties, and default Java cacerts.
 */
public final class KeyStoreManager {
    private static final String KEY_STORE_PATH_P12 = "tokens.p12";
    private static final String ENV_KEYSTORE_PASS = "ENV_KEYSTORE_PASS";
    private static final String DEFAULT_KEYSTORE_PASS = "DEFAULT_KEYSTORE_PASS";
    private static final String ACCESS_TOKEN_ALIAS = "ENV_ACCESS_TOKEN_ALIAS";
    private static final String ACCESS_TOKEN_PASS = "ENV_ACCESS_TOKEN_PASS";
    private static final String REFRESH_TOKEN_ALIAS = "ENV_REFRESH_TOKEN_ALIAS";
    private static final String REFRESH_TOKEN_PASS = "ENV_REFRESH_TOKEN_PASS";

    /**
     * Loads the keystore from a classpath resource.
     *
     * @return Optional containing the loaded KeyStore, or empty if loading fails
     */
    private Optional<KeyStore> loadKeyStoreFromResource() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(KEY_STORE_PATH_P12)) {
            if (inputStream == null) {
                System.err.printf("Keystore resource not found: %s%n", KEY_STORE_PATH_P12);
                return Optional.empty();
            }

            char[] password = getRequiredEnv(ENV_KEYSTORE_PASS).toCharArray();
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(inputStream, password);
            return Optional.of(keyStore);
        } catch (Exception e) {
            System.err.printf("Unable to load keystore from classpath: %s%n", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Loads the default Java cacerts keystore.
     *
     * @return The loaded KeyStore instance
     * @throws Exception if there's an error loading the keystore
     */
    public KeyStore loadDefaultKeyStore() throws Exception {
        Path cacertsPath = Paths.get(
                System.getProperty("java.home"),
                "lib", "security", "cacerts"
        );

        char[] password = getRequiredEnv(DEFAULT_KEYSTORE_PASS).toCharArray();

        try (FileInputStream fis = new FileInputStream(cacertsPath.toFile())) {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(fis, password);
            return keyStore;
        }
    }

    /**
     * Attempts to load a KeyStore from multiple sources in order of preference.
     *
     * @return The loaded KeyStore instance
     * @throws Exception if all loading attempts fail
     */
    private KeyStore loadKeyStore() throws Exception {
        // Try classpath resource first
        Optional<KeyStore> keyStore = loadKeyStoreFromResource();
        if (keyStore.isPresent()) {
            return keyStore.get();
        }

        // Try system properties next
        String keyStoreFile = System.getProperty("javax.net.ssl.keyStore");
        String keyStorePassword = System.getProperty("javax.net.ssl.keyStorePassword");

        if (keyStoreFile != null && keyStorePassword != null) {
            try (FileInputStream stream = new FileInputStream(keyStoreFile)) {
                KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
                ks.load(stream, keyStorePassword.toCharArray());
                return ks;
            } catch (Exception e) {
                System.err.printf(
                        "Unable to load keystore from javax.net.ssl.keyStore: %s%n",
                        e.getMessage()
                );
            }
        }

        // Fall back to default cacerts
        return loadDefaultKeyStore();
    }

    /**
     * Retrieves the access token key from the keystore.
     *
     * @return Optional containing the access token key, or empty if retrieval fails
     */
    public Optional<Key> retrieveAccessTokenKey() {
        try {
            String alias = getRequiredEnv(ACCESS_TOKEN_ALIAS);
            char[] password = getRequiredEnv(ACCESS_TOKEN_PASS).toCharArray();
            return Optional.of(loadKeyStore().getKey(alias, password));
        } catch (Exception e) {
            System.err.printf("Failed to retrieve access token key: %s%n", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Retrieves the refresh token key from the keystore.
     *
     * @return Optional containing the refresh token key, or empty if retrieval fails
     */
    public Optional<Key> retrieveRefreshTokenKey() {
        try {
            String alias = getRequiredEnv(REFRESH_TOKEN_ALIAS);
            char[] password = getRequiredEnv(REFRESH_TOKEN_PASS).toCharArray();
            return Optional.of(loadKeyStore().getKey(alias, password));
        } catch (Exception e) {
            System.err.printf("Failed to retrieve refresh token key: %s%n", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Gets a required environment variable or throws an exception if not found.
     *
     * @param name the environment variable name
     * @return the environment variable value
     * @throws IllegalStateException if the variable is not set
     */
    private String getRequiredEnv(String name) {
        return Objects.requireNonNull(
                System.getenv(name),
                () -> "Environment variable " + name + " is required but not set"
        );
    }
}