package com.imovel.api.repository;

import com.imovel.api.model.AuthDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthDetailRepository extends JpaRepository<AuthDetails, Long> {

    /**
     * Find AuthDetails by user ID
     * @param userId The ID of the user
     * @return AuthDetails associated with the user
     */
    Optional<AuthDetails> findByUserId(Long userId);

    /**
     * Check if AuthDetails exists for a given user ID
     * @param userId The ID of the user
     * @return true if exists, false otherwise
     */
    boolean existsByUserId(Long userId);

    /**
     * Delete AuthDetails by user ID
     * @param userId The ID of the user
     */
    void deleteByUserId(Long userId);
}