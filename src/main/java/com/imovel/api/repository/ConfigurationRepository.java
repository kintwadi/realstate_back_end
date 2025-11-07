package com.imovel.api.repository;

import com.imovel.api.model.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfigurationRepository extends JpaRepository<Configuration, Long> {

    boolean existsByKey(String key);
    Optional<Configuration> findByConfigKey(String key);

}
