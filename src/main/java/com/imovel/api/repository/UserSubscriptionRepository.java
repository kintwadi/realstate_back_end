package com.imovel.api.repository;

import com.imovel.api.model.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {
    
    /**
     * Find UserSubscription by user ID
     */
    Optional<UserSubscription> findByUserId(Long userId);
    
    /**
     * Find UserSubscription by subscription ID
     */
    Optional<UserSubscription> findBySubscriptionId(Long subscriptionId);
    
    /**
     * Check if user has an active subscription
     */
    boolean existsByUserId(Long userId);
    
    /**
     * Find UserSubscription with subscription details by user ID
     */
    @Query("SELECT us FROM UserSubscription us " +
           "LEFT JOIN FETCH us.basePlan " +
           "LEFT JOIN FETCH us.currentPlan " +
           "WHERE us.userId = :userId")
    Optional<UserSubscription> findByUserIdWithPlans(@Param("userId") Long userId);
    
    /**
     * Find all UserSubscriptions that have changed from their base plan
     */
    @Query("SELECT us FROM UserSubscription us " +
           "WHERE us.basePlan.id != us.currentPlan.id")
    java.util.List<UserSubscription> findAllWithPlanChanges();
    
    /**
     * Delete UserSubscription by user ID
     */
    void deleteByUserId(Long userId);
    
    /**
     * Delete UserSubscription by subscription ID
     */
    void deleteBySubscriptionId(Long subscriptionId);
}
