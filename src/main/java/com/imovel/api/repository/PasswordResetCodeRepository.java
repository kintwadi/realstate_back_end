// src/main/java/com/imovel/api/repository/PasswordResetCodeRepository.java
package com.imovel.api.repository;

import com.imovel.api.model.PasswordResetCode;
import com.imovel.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetCodeRepository extends JpaRepository<PasswordResetCode, Long> {

    Optional<PasswordResetCode> findTopByUserAndConsumedFalseOrderByCreatedAtDesc(User user);

    Optional<PasswordResetCode> findTopByUserAndCodeAndConsumedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
            User user, String code, LocalDateTime now);
}
