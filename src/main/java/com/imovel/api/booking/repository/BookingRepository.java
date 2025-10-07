package com.imovel.api.booking.repository;

import com.imovel.api.booking.model.Booking;
import com.imovel.api.booking.model.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Find bookings by guest
    @Query("SELECT b FROM Booking b WHERE b.guest.id = :guestId ORDER BY b.createdAt DESC")
    List<Booking> findByGuestId(@Param("guestId") Long guestId);

    // Find bookings by host
    @Query("SELECT b FROM Booking b WHERE b.host.id = :hostId ORDER BY b.createdAt DESC")
    List<Booking> findByHostId(@Param("hostId") Long hostId);

    // Find bookings by property
    @Query("SELECT b FROM Booking b WHERE b.property.id = :propertyId ORDER BY b.checkInDate DESC")
    List<Booking> findByPropertyId(@Param("propertyId") Long propertyId);

    // Find bookings by status
    @Query("SELECT b FROM Booking b WHERE b.status = :status ORDER BY b.createdAt DESC")
    List<Booking> findByStatus(@Param("status") BookingStatus status);

    // Find active bookings for a property (confirmed or checked-in)
    @Query("SELECT b FROM Booking b WHERE b.property.id = :propertyId " +
           "AND b.status IN ('CONFIRMED', 'CHECKED_IN') " +
           "ORDER BY b.checkInDate")
    List<Booking> findActiveBookingsByPropertyId(@Param("propertyId") Long propertyId);

    // Check for overlapping bookings
    @Query("SELECT b FROM Booking b WHERE b.property.id = :propertyId " +
           "AND b.status IN ('CONFIRMED', 'CHECKED_IN') " +
           "AND ((b.checkInDate <= :checkOutDate AND b.checkOutDate >= :checkInDate))")
    List<Booking> findOverlappingBookings(@Param("propertyId") Long propertyId,
                                         @Param("checkInDate") LocalDate checkInDate,
                                         @Param("checkOutDate") LocalDate checkOutDate);

    // Check if property is available for specific dates
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.property.id = :propertyId " +
           "AND b.status IN ('CONFIRMED', 'CHECKED_IN') " +
           "AND ((b.checkInDate <= :checkOutDate AND b.checkOutDate >= :checkInDate))")
    long countOverlappingBookings(@Param("propertyId") Long propertyId,
                                 @Param("checkInDate") LocalDate checkInDate,
                                 @Param("checkOutDate") LocalDate checkOutDate);

    // Find bookings by date range
    @Query("SELECT b FROM Booking b WHERE " +
           "(:checkInDate IS NULL OR b.checkInDate >= :checkInDate) AND " +
           "(:checkOutDate IS NULL OR b.checkOutDate <= :checkOutDate) " +
           "ORDER BY b.checkInDate")
    List<Booking> findByDateRange(@Param("checkInDate") LocalDate checkInDate,
                                 @Param("checkOutDate") LocalDate checkOutDate);

    // Find upcoming bookings (check-in within next 7 days)
    @Query("SELECT b FROM Booking b WHERE b.status = 'CONFIRMED' " +
           "AND b.checkInDate BETWEEN :startDate AND :endDate " +
           "ORDER BY b.checkInDate")
    List<Booking> findUpcomingBookings(@Param("startDate") LocalDate startDate,
                                      @Param("endDate") LocalDate endDate);

    // Find bookings requiring check-out today
    @Query("SELECT b FROM Booking b WHERE b.status = 'CHECKED_IN' " +
           "AND b.checkOutDate = :date")
    List<Booking> findBookingsForCheckoutToday(@Param("date") LocalDate date);

    // Find expired pending bookings
    @Query("SELECT b FROM Booking b WHERE b.status = 'PENDING' " +
           "AND b.createdAt < :expiryTime")
    List<Booking> findExpiredPendingBookings(@Param("expiryTime") LocalDateTime expiryTime);

    // Revenue analytics queries
    @Query("SELECT SUM(b.totalAmount) FROM Booking b WHERE b.status = 'COMPLETED' " +
           "AND b.checkOutDate BETWEEN :startDate AND :endDate")
    Optional<Double> calculateRevenueByDateRange(@Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(b.totalAmount) FROM Booking b WHERE b.host.id = :hostId " +
           "AND b.status = 'COMPLETED' " +
           "AND b.checkOutDate BETWEEN :startDate AND :endDate")
    Optional<Double> calculateHostRevenueByDateRange(@Param("hostId") Long hostId,
                                                   @Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(b.totalAmount) FROM Booking b WHERE b.property.id = :propertyId " +
           "AND b.status = 'COMPLETED' " +
           "AND b.checkOutDate BETWEEN :startDate AND :endDate")
    Optional<Double> calculatePropertyRevenueByDateRange(@Param("propertyId") Long propertyId,
                                                       @Param("startDate") LocalDate startDate,
                                                       @Param("endDate") LocalDate endDate);

    // Booking statistics
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = :status")
    long countByStatus(@Param("status") BookingStatus status);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.host.id = :hostId AND b.status = :status")
    long countByHostIdAndStatus(@Param("hostId") Long hostId, @Param("status") BookingStatus status);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.property.id = :propertyId AND b.status = :status")
    long countByPropertyIdAndStatus(@Param("propertyId") Long propertyId, @Param("status") BookingStatus status);

    // Find bookings with specific guest count
    @Query("SELECT b FROM Booking b WHERE b.guestCount = :guestCount ORDER BY b.createdAt DESC")
    List<Booking> findByGuestCount(@Param("guestCount") Integer guestCount);

    // Find recent bookings
    @Query("SELECT b FROM Booking b WHERE b.createdAt >= :since ORDER BY b.createdAt DESC")
    List<Booking> findRecentBookings(@Param("since") LocalDateTime since);

    // Find bookings by guest and status
    @Query("SELECT b FROM Booking b WHERE b.guest.id = :guestId AND b.status = :status ORDER BY b.createdAt DESC")
    List<Booking> findByGuestIdAndStatus(@Param("guestId") Long guestId, @Param("status") BookingStatus status);

    // Find bookings by host and status
    @Query("SELECT b FROM Booking b WHERE b.host.id = :hostId AND b.status = :status ORDER BY b.createdAt DESC")
    List<Booking> findByHostIdAndStatus(@Param("hostId") Long hostId, @Param("status") BookingStatus status);

    // Check if guest has any active bookings
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.guest.id = :guestId " +
           "AND b.status IN ('CONFIRMED', 'CHECKED_IN')")
    long countActiveBookingsByGuestId(@Param("guestId") Long guestId);

    // Find bookings that can be cancelled (within cancellation policy)
    @Query("SELECT b FROM Booking b WHERE b.status IN ('PENDING', 'CONFIRMED') " +
           "AND b.checkInDate > :currentDate ORDER BY b.checkInDate")
    List<Booking> findCancellableBookings(@Param("currentDate") LocalDate currentDate);
}
