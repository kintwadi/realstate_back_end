package com.imovel.api.booking.repository;

import com.imovel.api.booking.model.PropertyAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PropertyAvailabilityRepository extends JpaRepository<PropertyAvailability, Long> {

    // Find availability for a specific property and date
    @Query("SELECT pa FROM PropertyAvailability pa WHERE pa.property.id = :propertyId AND pa.date = :date")
    Optional<PropertyAvailability> findByPropertyIdAndDate(@Param("propertyId") Long propertyId, 
                                                          @Param("date") LocalDate date);

    // Find availability for a property within a date range
    @Query("SELECT pa FROM PropertyAvailability pa WHERE pa.property.id = :propertyId " +
           "AND pa.date BETWEEN :startDate AND :endDate ORDER BY pa.date")
    List<PropertyAvailability> findByPropertyIdAndDateRange(@Param("propertyId") Long propertyId,
                                                           @Param("startDate") LocalDate startDate,
                                                           @Param("endDate") LocalDate endDate);

    // Find available dates for a property within a date range
    @Query("SELECT pa FROM PropertyAvailability pa WHERE pa.property.id = :propertyId " +
           "AND pa.date BETWEEN :startDate AND :endDate " +
           "AND pa.isAvailable = true ORDER BY pa.date")
    List<PropertyAvailability> findAvailableDatesByPropertyIdAndDateRange(@Param("propertyId") Long propertyId,
                                                                         @Param("startDate") LocalDate startDate,
                                                                         @Param("endDate") LocalDate endDate);

    // Check if all dates in range are available
    @Query("SELECT COUNT(pa) FROM PropertyAvailability pa WHERE pa.property.id = :propertyId " +
           "AND pa.date BETWEEN :startDate AND :endDate " +
           "AND pa.isAvailable = false")
    long countUnavailableDatesInRange(@Param("propertyId") Long propertyId,
                                     @Param("startDate") LocalDate startDate,
                                     @Param("endDate") LocalDate endDate);

    // Find blocked dates for a property
    @Query("SELECT pa FROM PropertyAvailability pa WHERE pa.property.id = :propertyId " +
           "AND pa.isAvailable = false ORDER BY pa.date")
    List<PropertyAvailability> findBlockedDatesByPropertyId(@Param("propertyId") Long propertyId);

    // Find dates with custom pricing
    @Query("SELECT pa FROM PropertyAvailability pa WHERE pa.property.id = :propertyId " +
           "AND pa.price IS NOT NULL ORDER BY pa.date")
    List<PropertyAvailability> findDatesWithCustomPricing(@Param("propertyId") Long propertyId);

    // Get minimum stay requirements for date range
    @Query("SELECT MAX(pa.minStay) FROM PropertyAvailability pa WHERE pa.property.id = :propertyId " +
           "AND pa.date BETWEEN :startDate AND :endDate")
    Optional<Integer> getMaxMinimumStayInRange(@Param("propertyId") Long propertyId,
                                              @Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);

    // Get maximum stay limitations for date range
    @Query("SELECT MIN(pa.maxStay) FROM PropertyAvailability pa WHERE pa.property.id = :propertyId " +
           "AND pa.date BETWEEN :startDate AND :endDate")
    Optional<Integer> getMinMaximumStayInRange(@Param("propertyId") Long propertyId,
                                              @Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);

    // Calculate total price for date range
    @Query("SELECT SUM(COALESCE(pa.price, p.price)) FROM PropertyAvailability pa " +
           "JOIN pa.property p WHERE pa.property.id = :propertyId " +
           "AND pa.date BETWEEN :startDate AND :endDate " +
           "AND pa.isAvailable = true")
    Optional<BigDecimal> calculateTotalPriceForDateRange(@Param("propertyId") Long propertyId,
                                                        @Param("startDate") LocalDate startDate,
                                                        @Param("endDate") LocalDate endDate);

    // Find instant bookable dates
    @Query("SELECT pa FROM PropertyAvailability pa WHERE pa.property.id = :propertyId " +
           "AND pa.date BETWEEN :startDate AND :endDate " +
           "AND pa.isAvailable = true AND pa.isInstantBook = true ORDER BY pa.date")
    List<PropertyAvailability> findInstantBookableDates(@Param("propertyId") Long propertyId,
                                                       @Param("startDate") LocalDate startDate,
                                                       @Param("endDate") LocalDate endDate);

    // Bulk operations
    @Modifying
    @Query("UPDATE PropertyAvailability pa SET pa.isAvailable = :isAvailable " +
           "WHERE pa.property.id = :propertyId AND pa.date BETWEEN :startDate AND :endDate")
    int updateAvailabilityForDateRange(@Param("propertyId") Long propertyId,
                                      @Param("startDate") LocalDate startDate,
                                      @Param("endDate") LocalDate endDate,
                                      @Param("isAvailable") Boolean isAvailable);

    @Modifying
    @Query("UPDATE PropertyAvailability pa SET pa.price = :price " +
           "WHERE pa.property.id = :propertyId AND pa.date BETWEEN :startDate AND :endDate")
    int updatePricingForDateRange(@Param("propertyId") Long propertyId,
                                 @Param("startDate") LocalDate startDate,
                                 @Param("endDate") LocalDate endDate,
                                 @Param("price") BigDecimal price);

    @Modifying
    @Query("UPDATE PropertyAvailability pa SET pa.minStay = :minStay, pa.maxStay = :maxStay " +
           "WHERE pa.property.id = :propertyId AND pa.date BETWEEN :startDate AND :endDate")
    int updateStayRequirementsForDateRange(@Param("propertyId") Long propertyId,
                                          @Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate,
                                          @Param("minStay") Integer minStay,
                                          @Param("maxStay") Integer maxStay);

    // Delete old availability records
    @Modifying
    @Query("DELETE FROM PropertyAvailability pa WHERE pa.date < :cutoffDate")
    int deleteOldAvailabilityRecords(@Param("cutoffDate") LocalDate cutoffDate);

    // Find properties with availability in date range
    @Query("SELECT DISTINCT pa.property.id FROM PropertyAvailability pa " +
           "WHERE pa.date BETWEEN :startDate AND :endDate " +
           "AND pa.isAvailable = true")
    List<Long> findPropertiesWithAvailabilityInDateRange(@Param("startDate") LocalDate startDate,
                                                        @Param("endDate") LocalDate endDate);

    // Check if check-in is allowed on specific date
    @Query("SELECT pa.checkInAllowed FROM PropertyAvailability pa " +
           "WHERE pa.property.id = :propertyId AND pa.date = :date")
    Optional<Boolean> isCheckInAllowedOnDate(@Param("propertyId") Long propertyId, 
                                           @Param("date") LocalDate date);

    // Check if check-out is allowed on specific date
    @Query("SELECT pa.checkOutAllowed FROM PropertyAvailability pa " +
           "WHERE pa.property.id = :propertyId AND pa.date = :date")
    Optional<Boolean> isCheckOutAllowedOnDate(@Param("propertyId") Long propertyId, 
                                            @Param("date") LocalDate date);

    // Find gaps in availability calendar
    @Query("SELECT pa.date FROM PropertyAvailability pa WHERE pa.property.id = :propertyId " +
           "AND pa.date BETWEEN :startDate AND :endDate ORDER BY pa.date")
    List<LocalDate> findExistingAvailabilityDates(@Param("propertyId") Long propertyId,
                                                 @Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate);

    // Statistics queries
    @Query("SELECT COUNT(pa) FROM PropertyAvailability pa WHERE pa.property.id = :propertyId " +
           "AND pa.isAvailable = true AND pa.date >= :fromDate")
    long countAvailableDaysFromDate(@Param("propertyId") Long propertyId, 
                                   @Param("fromDate") LocalDate fromDate);

    @Query("SELECT AVG(COALESCE(pa.price, p.price)) FROM PropertyAvailability pa " +
           "JOIN pa.property p WHERE pa.property.id = :propertyId " +
           "AND pa.date BETWEEN :startDate AND :endDate")
    Optional<BigDecimal> getAveragePriceForDateRange(@Param("propertyId") Long propertyId,
                                                    @Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate);
}
