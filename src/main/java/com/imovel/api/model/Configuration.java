package com.imovel.api.model;

import jakarta.persistence.*;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@Entity
@Table(name = "Configuration")
public class Configuration {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "config_key", unique = true)
    private String configKey;
    @Column(name = "config_value")
    private String configValue;

    public Configuration()  {

    }

    public Configuration(String configKey, String configValue) {
        this.configKey = configKey;
        this.configValue = configValue;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getConfigKey() {
        return configKey;
    }
    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }
    public String getConfigValue() {
        return configValue;
    }
    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }

}
