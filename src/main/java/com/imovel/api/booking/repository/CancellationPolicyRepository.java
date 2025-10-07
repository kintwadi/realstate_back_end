package com.imovel.api.booking.repository;

import com.imovel.api.booking.model.CancellationPolicy;
import com.imovel.api.booking.model.enums.CancellationPolicyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CancellationPolicyRepository extends JpaRepository<CancellationPolicy, Long> {

    // Find active cancellation policy for a property
    @Query("SELECT cp FROM CancellationPolicy cp WHERE cp.property.id = :propertyId AND cp.isActive = true")
    Optional<CancellationPolicy> findActiveByPropertyId(@Param("propertyId") Long propertyId);

    // Find all cancellation policies for a property
    @Query("SELECT cp FROM CancellationPolicy cp WHERE cp.property.id = :propertyId ORDER BY cp.isActive DESC, cp.createdAt DESC")
    List<CancellationPolicy> findByPropertyId(@Param("propertyId") Long propertyId);

    // Find cancellation policies by type
    @Query("SELECT cp FROM CancellationPolicy cp WHERE cp.policyType = :policyType AND cp.isActive = true ORDER BY cp.createdAt DESC")
    List<CancellationPolicy> findByPolicyType(@Param("policyType") CancellationPolicyType policyType);

    // Find cancellation policies by host
    @Query("SELECT cp FROM CancellationPolicy cp WHERE cp.property.createdBy.id = :hostId ORDER BY cp.isActive DESC, cp.createdAt DESC")
    List<CancellationPolicy> findByHostId(@Param("hostId") Long hostId);

    // Find active cancellation policies by host
    @Query("SELECT cp FROM CancellationPolicy cp WHERE cp.property.createdBy.id = :hostId AND cp.isActive = true ORDER BY cp.createdAt DESC")
    List<CancellationPolicy> findActiveByHostId(@Param("hostId") Long hostId);

    // Find properties with specific cancellation policy type
    @Query("SELECT DISTINCT cp.property.id FROM CancellationPolicy cp WHERE cp.policyType = :policyType AND cp.isActive = true")
    List<Long> findPropertyIdsByPolicyType(@Param("policyType") CancellationPolicyType policyType);

    // Find cancellation policies with refund percentage range
    @Query("SELECT cp FROM CancellationPolicy cp WHERE cp.refundPercentage BETWEEN :minPercentage AND :maxPercentage " +
           "AND cp.isActive = true ORDER BY cp.refundPercentage DESC")
    List<CancellationPolicy> findByRefundPercentageRange(@Param("minPercentage") BigDecimal minPercentage,
                                                         @Param("maxPercentage") BigDecimal maxPercentage);

    // Find cancellation policies with days before check-in range
    @Query("SELECT cp FROM CancellationPolicy cp WHERE cp.daysBeforeCheckin BETWEEN :minDays AND :maxDays " +
           "AND cp.isActive = true ORDER BY cp.daysBeforeCheckin")
    List<CancellationPolicy> findByDaysBeforeCheckinRange(@Param("minDays") Integer minDays,
                                                          @Param("maxDays") Integer maxDays);

    // Check if property has active cancellation policy
    @Query("SELECT COUNT(cp) > 0 FROM CancellationPolicy cp WHERE cp.property.id = :propertyId AND cp.isActive = true")
    boolean hasActiveCancellationPolicy(@Param("propertyId") Long propertyId);

    // Find most lenient cancellation policies (highest refund percentage)
    @Query("SELECT cp FROM CancellationPolicy cp WHERE cp.isActive = true ORDER BY cp.refundPercentage DESC, cp.daysBeforeCheckin ASC")
    List<CancellationPolicy> findMostLenientPolicies();

    // Find strictest cancellation policies (lowest refund percentage)
    @Query("SELECT cp FROM CancellationPolicy cp WHERE cp.isActive = true ORDER BY cp.refundPercentage ASC, cp.daysBeforeCheckin DESC")
    List<CancellationPolicy> findStrictestPolicies();

    // Get cancellation policy statistics
    @Query("SELECT cp.policyType, COUNT(cp), AVG(cp.refundPercentage), AVG(cp.daysBeforeCheckin) " +
           "FROM CancellationPolicy cp WHERE cp.isActive = true GROUP BY cp.policyType")
    List<Object[]> getCancellationPolicyStatistics();

    // Find cancellation policies that allow full refund
    @Query("SELECT cp FROM CancellationPolicy cp WHERE cp.refundPercentage = 100 AND cp.isActive = true ORDER BY cp.daysBeforeCheckin")
    List<CancellationPolicy> findFullRefundPolicies();

    // Find non-refundable cancellation policies
    @Query("SELECT cp FROM CancellationPolicy cp WHERE cp.refundPercentage = 0 AND cp.isActive = true ORDER BY cp.createdAt DESC")
    List<CancellationPolicy> findNonRefundablePolicies();

    // Find cancellation policies by description keyword
    @Query("SELECT cp FROM CancellationPolicy cp WHERE LOWER(cp.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "AND cp.isActive = true ORDER BY cp.createdAt DESC")
    List<CancellationPolicy> findByDescriptionKeyword(@Param("keyword") String keyword);

    // Count active cancellation policies by type
    @Query("SELECT cp.policyType, COUNT(cp) FROM CancellationPolicy cp WHERE cp.isActive = true GROUP BY cp.policyType")
    List<Object[]> countActivePoliciesByType();

    // Find properties without active cancellation policy
    @Query("SELECT p.id FROM Property p WHERE p.id NOT IN " +
           "(SELECT cp.property.id FROM CancellationPolicy cp WHERE cp.isActive = true)")
    List<Long> findPropertiesWithoutActiveCancellationPolicy();

    // Find cancellation policies that need review (very old or unusual terms)
    @Query("SELECT cp FROM CancellationPolicy cp WHERE cp.isActive = true AND " +
           "(cp.createdAt < :oldDate OR cp.refundPercentage > 100 OR cp.daysBeforeCheckin > 365) " +
           "ORDER BY cp.createdAt")
    List<CancellationPolicy> findPoliciesNeedingReview(@Param("oldDate") java.time.LocalDateTime oldDate);

    // Get average refund percentage by policy type
    @Query("SELECT cp.policyType, AVG(cp.refundPercentage) FROM CancellationPolicy cp " +
           "WHERE cp.isActive = true GROUP BY cp.policyType ORDER BY AVG(cp.refundPercentage) DESC")
    List<Object[]> getAverageRefundPercentageByType();

    // Find cancellation policies with custom terms (not standard policy types)
    @Query("SELECT cp FROM CancellationPolicy cp WHERE cp.isActive = true AND " +
           "(cp.description IS NOT NULL AND cp.description != '') " +
           "ORDER BY cp.createdAt DESC")
    List<CancellationPolicy> findPoliciesWithCustomTerms();

    // Find recently created cancellation policies
    @Query("SELECT cp FROM CancellationPolicy cp WHERE cp.createdAt >= :since ORDER BY cp.createdAt DESC")
    List<CancellationPolicy> findRecentlyCreatedPolicies(@Param("since") java.time.LocalDateTime since);

    // Find recently updated cancellation policies
    @Query("SELECT cp FROM CancellationPolicy cp WHERE cp.updatedAt >= :since ORDER BY cp.updatedAt DESC")
    List<CancellationPolicy> findRecentlyUpdatedPolicies(@Param("since") java.time.LocalDateTime since);

    // Find cancellation policies by host and type
    @Query("SELECT cp FROM CancellationPolicy cp WHERE cp.property.createdBy.id = :hostId " +
           "AND cp.policyType = :policyType AND cp.isActive = true ORDER BY cp.createdAt DESC")
    List<CancellationPolicy> findByHostIdAndPolicyType(@Param("hostId") Long hostId,
                                                       @Param("policyType") CancellationPolicyType policyType);

    // Check if host has consistent cancellation policy across properties
    @Query("SELECT COUNT(DISTINCT cp.policyType) FROM CancellationPolicy cp " +
           "WHERE cp.property.createdBy.id = :hostId AND cp.isActive = true")
    long countDistinctPolicyTypesForHost(@Param("hostId") Long hostId);

    // Find most popular cancellation policy type
    @Query("SELECT cp.policyType FROM CancellationPolicy cp WHERE cp.isActive = true " +
           "GROUP BY cp.policyType ORDER BY COUNT(cp) DESC")
    List<CancellationPolicyType> findMostPopularPolicyTypes();

    // Find cancellation policies that might be too generous (high refund, short notice)
    @Query("SELECT cp FROM CancellationPolicy cp WHERE cp.isActive = true " +
           "AND cp.refundPercentage > :minRefund AND cp.daysBeforeCheckin < :maxDays " +
           "ORDER BY cp.refundPercentage DESC, cp.daysBeforeCheckin ASC")
    List<CancellationPolicy> findGenerousPolicies(@Param("minRefund") BigDecimal minRefund,
                                                  @Param("maxDays") Integer maxDays);

    // Find cancellation policies that might be too strict (low refund, long notice)
    @Query("SELECT cp FROM CancellationPolicy cp WHERE cp.isActive = true " +
           "AND cp.refundPercentage < :maxRefund AND cp.daysBeforeCheckin > :minDays " +
           "ORDER BY cp.refundPercentage ASC, cp.daysBeforeCheckin DESC")
    List<CancellationPolicy> findStrictPolicies(@Param("maxRefund") BigDecimal maxRefund,
                                               @Param("minDays") Integer minDays);
}
