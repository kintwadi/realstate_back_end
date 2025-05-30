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
    private static final String ACCESS_EXPIRATION_KEY = "ACCESS_EXPIRATION_MS";
    private static final String REFRESH_EXPIRATION_KEY = "REFRESH_EXPIRATION_MS";
    private static final String DEFAULT_ACCESS_TOKEN_EXPIRATION = "900000";
    private static final String DEFAULT_REFRESH_EXPIRATION = "604800000";

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

        if (accessExpirationMissing || refreshExpirationMissing) {
            // Save default configurations if they're missing
            if (accessExpirationMissing) {
                configurationRepository.save(new Configuration(ACCESS_EXPIRATION_KEY, DEFAULT_ACCESS_TOKEN_EXPIRATION));
            }
            if (refreshExpirationMissing) {
                configurationRepository.save(new Configuration(REFRESH_EXPIRATION_KEY, DEFAULT_REFRESH_EXPIRATION));
            }
        }
    }
}