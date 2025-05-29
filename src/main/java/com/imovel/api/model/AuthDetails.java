package com.imovel.api.model;

import jakarta.persistence.*;

@Entity
@Table(name = "auth_details",
       indexes = {@Index(name = "idx_auth_details_user_id", columnList = "user_id")})
public class AuthDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String salt;

    @Column(nullable = false)
    private String hash;

    @Column(name = "user_id", nullable = false)
    private Long userId; // Logical relationship with user table

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
