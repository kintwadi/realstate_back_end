package com.imovel.api.booking.repository;

import com.imovel.api.booking.model.BookingGuest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingGuestRepository extends JpaRepository<BookingGuest, Long> {

    // Find all guests for a booking
    @Query("SELECT bg FROM BookingGuest bg WHERE bg.booking.id = :bookingId ORDER BY bg.isPrimaryGuest DESC, bg.createdAt")
    List<BookingGuest> findByBookingId(@Param("bookingId") Long bookingId);

    // Find primary guest for a booking
    @Query("SELECT bg FROM BookingGuest bg WHERE bg.booking.id = :bookingId AND bg.isPrimaryGuest = true")
    Optional<BookingGuest> findPrimaryGuestByBookingId(@Param("bookingId") Long bookingId);

    // Find additional guests for a booking (non-primary)
    @Query("SELECT bg FROM BookingGuest bg WHERE bg.booking.id = :bookingId AND bg.isPrimaryGuest = false ORDER BY bg.createdAt")
    List<BookingGuest> findAdditionalGuestsByBookingId(@Param("bookingId") Long bookingId);

    // Find guest by email
    @Query("SELECT bg FROM BookingGuest bg WHERE bg.email = :email ORDER BY bg.createdAt DESC")
    List<BookingGuest> findByEmail(@Param("email") String email);

    // Find guest by phone
    @Query("SELECT bg FROM BookingGuest bg WHERE bg.phone = :phone ORDER BY bg.createdAt DESC")
    List<BookingGuest> findByPhone(@Param("phone") String phone);

    // Find guests with special requests
    @Query("SELECT bg FROM BookingGuest bg WHERE bg.specialRequests IS NOT NULL AND bg.specialRequests != '' ORDER BY bg.createdAt DESC")
    List<BookingGuest> findGuestsWithSpecialRequests();

    // Find guests with dietary restrictions
    @Query("SELECT bg FROM BookingGuest bg WHERE bg.dietaryRestrictions IS NOT NULL AND bg.dietaryRestrictions != '' ORDER BY bg.createdAt DESC")
    List<BookingGuest> findGuestsWithDietaryRestrictions();

    // Find guests by age range
    @Query("SELECT bg FROM BookingGuest bg WHERE bg.age BETWEEN :minAge AND :maxAge ORDER BY bg.createdAt DESC")
    List<BookingGuest> findGuestsByAgeRange(@Param("minAge") Integer minAge, @Param("maxAge") Integer maxAge);

    // Find guests by property
    @Query("SELECT bg FROM BookingGuest bg WHERE bg.booking.property.id = :propertyId ORDER BY bg.createdAt DESC")
    List<BookingGuest> findByPropertyId(@Param("propertyId") Long propertyId);

    // Find guests by host
    @Query("SELECT bg FROM BookingGuest bg WHERE bg.booking.host.id = :hostId ORDER BY bg.createdAt DESC")
    List<BookingGuest> findByHostId(@Param("hostId") Long hostId);

    // Count guests for a booking
    @Query("SELECT COUNT(bg) FROM BookingGuest bg WHERE bg.booking.id = :bookingId")
    long countGuestsByBookingId(@Param("bookingId") Long bookingId);

    // Count adult guests for a booking (age >= 18)
    @Query("SELECT COUNT(bg) FROM BookingGuest bg WHERE bg.booking.id = :bookingId AND bg.age >= 18")
    long countAdultGuestsByBookingId(@Param("bookingId") Long bookingId);

    // Count child guests for a booking (age < 18)
    @Query("SELECT COUNT(bg) FROM BookingGuest bg WHERE bg.booking.id = :bookingId AND bg.age < 18")
    long countChildGuestsByBookingId(@Param("bookingId") Long bookingId);

    // Find guests with emergency contacts
    @Query("SELECT bg FROM BookingGuest bg WHERE bg.emergencyContactName IS NOT NULL AND bg.emergencyContactPhone IS NOT NULL ORDER BY bg.createdAt DESC")
    List<BookingGuest> findGuestsWithEmergencyContacts();

    // Find guests without emergency contacts
    @Query("SELECT bg FROM BookingGuest bg WHERE bg.emergencyContactName IS NULL OR bg.emergencyContactPhone IS NULL ORDER BY bg.createdAt DESC")
    List<BookingGuest> findGuestsWithoutEmergencyContacts();

    // Search guests by name (case-insensitive)
    @Query("SELECT bg FROM BookingGuest bg WHERE LOWER(bg.fullName) LIKE LOWER(CONCAT('%', :name, '%')) ORDER BY bg.createdAt DESC")
    List<BookingGuest> searchByName(@Param("name") String name);

    // Find frequent guests (guests who have made multiple bookings)
    @Query("SELECT bg.email, COUNT(DISTINCT bg.booking.id) as bookingCount FROM BookingGuest bg " +
           "WHERE bg.isPrimaryGuest = true GROUP BY bg.email HAVING COUNT(DISTINCT bg.booking.id) > 1 " +
           "ORDER BY bookingCount DESC")
    List<Object[]> findFrequentGuests();

    // Find guests by booking status
    @Query("SELECT bg FROM BookingGuest bg WHERE bg.booking.status = :status ORDER BY bg.createdAt DESC")
    List<BookingGuest> findGuestsByBookingStatus(@Param("status") String status);

    // Find primary guests for confirmed bookings
    @Query("SELECT bg FROM BookingGuest bg WHERE bg.isPrimaryGuest = true AND bg.booking.status = 'CONFIRMED' ORDER BY bg.createdAt DESC")
    List<BookingGuest> findPrimaryGuestsForConfirmedBookings();

    // Get guest statistics for a property
    @Query("SELECT COUNT(DISTINCT bg.email), AVG(bg.age), " +
           "COUNT(CASE WHEN bg.specialRequests IS NOT NULL AND bg.specialRequests != '' THEN 1 END), " +
           "COUNT(CASE WHEN bg.dietaryRestrictions IS NOT NULL AND bg.dietaryRestrictions != '' THEN 1 END) " +
           "FROM BookingGuest bg WHERE bg.booking.property.id = :propertyId")
    Object[] getGuestStatisticsForProperty(@Param("propertyId") Long propertyId);

    // Get guest statistics for a host
    @Query("SELECT COUNT(DISTINCT bg.email), AVG(bg.age), " +
           "COUNT(CASE WHEN bg.specialRequests IS NOT NULL AND bg.specialRequests != '' THEN 1 END), " +
           "COUNT(CASE WHEN bg.dietaryRestrictions IS NOT NULL AND bg.dietaryRestrictions != '' THEN 1 END) " +
           "FROM BookingGuest bg WHERE bg.booking.host.id = :hostId")
    Object[] getGuestStatisticsForHost(@Param("hostId") Long hostId);

    // Find guests with incomplete information
    @Query("SELECT bg FROM BookingGuest bg WHERE bg.email IS NULL OR bg.phone IS NULL OR bg.fullName IS NULL " +
           "OR bg.email = '' OR bg.phone = '' OR bg.fullName = '' ORDER BY bg.createdAt DESC")
    List<BookingGuest> findGuestsWithIncompleteInformation();

    // Check if email is already used for a specific booking
    @Query("SELECT COUNT(bg) > 0 FROM BookingGuest bg WHERE bg.booking.id = :bookingId AND bg.email = :email")
    boolean isEmailAlreadyUsedForBooking(@Param("bookingId") Long bookingId, @Param("email") String email);

    // Find duplicate guest entries (same email and booking)
    @Query("SELECT bg.email, bg.booking.id, COUNT(bg) FROM BookingGuest bg " +
           "GROUP BY bg.email, bg.booking.id HAVING COUNT(bg) > 1")
    List<Object[]> findDuplicateGuestEntries();

    // Find guests by name pattern and booking property
    @Query("SELECT bg FROM BookingGuest bg WHERE LOWER(bg.fullName) LIKE LOWER(CONCAT('%', :namePattern, '%')) " +
           "AND bg.booking.property.id = :propertyId ORDER BY bg.createdAt DESC")
    List<BookingGuest> findGuestsByNamePatternAndProperty(@Param("namePattern") String namePattern, 
                                                         @Param("propertyId") Long propertyId);

    // Get age distribution for guests
    @Query("SELECT " +
           "COUNT(CASE WHEN bg.age < 18 THEN 1 END) as children, " +
           "COUNT(CASE WHEN bg.age BETWEEN 18 AND 30 THEN 1 END) as youngAdults, " +
           "COUNT(CASE WHEN bg.age BETWEEN 31 AND 50 THEN 1 END) as middleAged, " +
           "COUNT(CASE WHEN bg.age > 50 THEN 1 END) as seniors " +
           "FROM BookingGuest bg")
    Object[] getAgeDistribution();

    // Find recent guests (within last N days)
    @Query("SELECT bg FROM BookingGuest bg WHERE bg.createdAt >= :since ORDER BY bg.createdAt DESC")
    List<BookingGuest> findRecentGuests(@Param("since") java.time.LocalDateTime since);
}
