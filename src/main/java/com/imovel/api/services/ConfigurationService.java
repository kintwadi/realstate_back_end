package com.imovel.api.services;

import com.imovel.api.model.Configuration;
import com.imovel.api.repository.ConfigurationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service class for managing application configurations.
 * Provides CRUD operations and default configuration setup.
 */
@Service
public class ConfigurationService {

    private final ConfigurationRepository configurationRepository;

    // Configuration keys for default values
    public static final String ACCESS_EXPIRATION_KEY = "ACCESS_EXPIRATION_MS";
    public static final String REFRESH_EXPIRATION_KEY = "REFRESH_EXPIRATION_MS";
    private static final String DEFAULT_ACCESS_TOKEN_EXPIRATION_VALUE = "900000";
    private static final String DEFAULT_REFRESH_EXPIRATION_VALUE = "604800000";
    public static final String MAX_REFRESH_TOKEN_PER_USER_KEY  = "MAX_REFRESH_TOKEN_PER_USER";
    private static final String MAX_REFRESH_TOKEN_PER_USER_VALUE = "5";
    public static final String REFRESH_CLEAN_UP_INTERVAL_KEY  = "REFRESH_CLEAN_UP_INTERVAL";
    private static final String REFRESH_CLEAN_UP_INTERVAL_VALUE = "86400000";//24H

    @Autowired
    public ConfigurationService(ConfigurationRepository configurationRepository) {
        this.configurationRepository = configurationRepository;
    }

    /**
     * Saves a new configuration to the database.
     *
     * @param configuration The configuration to be saved
     * @return The saved configuration
     */
    @Transactional
    public Configuration add(final Configuration configuration) {
        return configurationRepository.save(configuration);
    }

    /**
     * Retrieves a configuration by its key.
     *
     * @param key The configuration key to search for
     * @return Optional containing the configuration if found
     */
    public Optional<Configuration> findByConfigKey(final String key) {
        return configurationRepository.findByConfigKey(key);
    }

    /**
     * Retrieves all configurations from the database.
     *
     * @return List of all configurations
     */
    public List<Configuration> findAll() {
        return configurationRepository.findAll();
    }

    /**
     * Sets default configurations if they don't already exist.
     * Creates default entries for access and refresh token expiration times.
     */
    public void setDefaultConfigurations() {
        // Check if default configurations exist
        final boolean accessExpirationMissing = !findByConfigKey(ACCESS_EXPIRATION_KEY).isPresent();
        final boolean refreshExpirationMissing = !findByConfigKey(REFRESH_EXPIRATION_KEY).isPresent();
        final boolean maxRefreshTokenPerUser = !findByConfigKey(String.valueOf(MAX_REFRESH_TOKEN_PER_USER_KEY)).isPresent();
        final boolean refreshTokenCleanupInterval = !findByConfigKey(String.valueOf(REFRESH_CLEAN_UP_INTERVAL_KEY)).isPresent();

        if (accessExpirationMissing || refreshExpirationMissing) {
            // Save default configurations if they're missing
            if (accessExpirationMissing) {
                configurationRepository.save(new Configuration(ACCESS_EXPIRATION_KEY, DEFAULT_ACCESS_TOKEN_EXPIRATION_VALUE));
            }
            if (refreshExpirationMissing) {
                configurationRepository.save(new Configuration(REFRESH_EXPIRATION_KEY, DEFAULT_REFRESH_EXPIRATION_VALUE));
        }
        }
        if (maxRefreshTokenPerUser)
        {
            configurationRepository.save(new Configuration(MAX_REFRESH_TOKEN_PER_USER_KEY, MAX_REFRESH_TOKEN_PER_USER_VALUE));
        }
        if (refreshTokenCleanupInterval)
        {
            configurationRepository.save(new Configuration(REFRESH_CLEAN_UP_INTERVAL_KEY, REFRESH_CLEAN_UP_INTERVAL_VALUE));
        }
    }


}
