package com.imovel.api.payment.repository;

import com.imovel.api.payment.model.Payment;
import com.imovel.api.payment.model.enums.PaymentGateway;
import com.imovel.api.payment.model.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    /**
     * Find payments by user ID
     */
    List<Payment> findByUserId(Long userId);
    
    /**
     * Find payments by user ID with pagination
     */
    Page<Payment> findByUserId(Long userId, Pageable pageable);
    
    /**
     * Find payments by status
     */
    List<Payment> findByStatus(PaymentStatus status);
    
    /**
     * Find payments by gateway
     */
    List<Payment> findByGateway(PaymentGateway gateway);
    
    /**
     * Find payment by gateway payment ID
     */
    Optional<Payment> findByGatewayPaymentId(String gatewayPaymentId);
    
    /**
     * Find payments by user ID and status
     */
    List<Payment> findByUserIdAndStatus(Long userId, PaymentStatus status);
    
    /**
     * Find payments by user ID and gateway
     */
    List<Payment> findByUserIdAndGateway(Long userId, PaymentGateway gateway);
    
    /**
     * Find payments within date range
     */
    @Query("SELECT p FROM Payment p WHERE p.createdAt BETWEEN :startDate AND :endDate")
    List<Payment> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                 @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find payments by user ID within date range
     */
    @Query("SELECT p FROM Payment p WHERE p.userId = :userId AND p.createdAt BETWEEN :startDate AND :endDate")
    List<Payment> findByUserIdAndDateRange(@Param("userId") Long userId,
                                          @Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);
    
    /**
     * Calculate total amount by user ID and status
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.userId = :userId AND p.status = :status")
    BigDecimal calculateTotalAmountByUserIdAndStatus(@Param("userId") Long userId, 
                                                    @Param("status") PaymentStatus status);
    
    /**
     * Calculate total amount by gateway and status
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.gateway = :gateway AND p.status = :status")
    BigDecimal calculateTotalAmountByGatewayAndStatus(@Param("gateway") PaymentGateway gateway,
                                                     @Param("status") PaymentStatus status);
    
    /**
     * Count payments by status
     */
    long countByStatus(PaymentStatus status);
    
    /**
     * Count payments by user ID and status
     */
    long countByUserIdAndStatus(Long userId, PaymentStatus status);
    
    /**
     * Find recent payments by user ID
     */
    @Query("SELECT p FROM Payment p WHERE p.userId = :userId ORDER BY p.createdAt DESC")
    List<Payment> findRecentPaymentsByUserId(@Param("userId") Long userId, Pageable pageable);
}