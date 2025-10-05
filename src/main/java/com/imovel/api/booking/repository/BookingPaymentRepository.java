package com.imovel.api.booking.repository;

import com.imovel.api.booking.model.BookingPayment;
import com.imovel.api.booking.model.enums.PaymentStatus;
import com.imovel.api.booking.model.enums.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingPaymentRepository extends JpaRepository<BookingPayment, Long> {

    // Find payments by booking
    @Query("SELECT bp FROM BookingPayment bp WHERE bp.booking.id = :bookingId ORDER BY bp.createdAt")
    List<BookingPayment> findByBookingId(@Param("bookingId") Long bookingId);

    // Find payments by booking and status
    @Query("SELECT bp FROM BookingPayment bp WHERE bp.booking.id = :bookingId " +
           "AND bp.paymentStatus = :status ORDER BY bp.createdAt")
    List<BookingPayment> findByBookingIdAndStatus(@Param("bookingId") Long bookingId, 
                                                 @Param("status") PaymentStatus status);

    // Find payments by booking and type
    @Query("SELECT bp FROM BookingPayment bp WHERE bp.booking.id = :bookingId " +
           "AND bp.paymentType = :paymentType ORDER BY bp.createdAt")
    List<BookingPayment> findByBookingIdAndPaymentType(@Param("bookingId") Long bookingId, 
                                                      @Param("paymentType") PaymentType paymentType);

    // Find payment by transaction ID
    @Query("SELECT bp FROM BookingPayment bp WHERE bp.transactionId = :transactionId")
    Optional<BookingPayment> findByTransactionId(@Param("transactionId") String transactionId);

    // Find payment by gateway transaction ID
    @Query("SELECT bp FROM BookingPayment bp WHERE bp.gatewayPaymentId = :gatewayTransactionId")
    Optional<BookingPayment> findByGatewayTransactionId(@Param("gatewayTransactionId") String gatewayTransactionId);

    // Calculate total paid amount for booking
    @Query("SELECT COALESCE(SUM(bp.amount), 0) FROM BookingPayment bp " +
           "WHERE bp.booking.id = :bookingId AND bp.paymentStatus = 'COMPLETED'")
    BigDecimal calculateTotalPaidAmount(@Param("bookingId") Long bookingId);

    // Calculate total refunded amount for booking
    @Query("SELECT COALESCE(SUM(bp.refundAmount), 0) FROM BookingPayment bp " +
           "WHERE bp.booking.id = :bookingId AND bp.refundAmount IS NOT NULL")
    BigDecimal calculateTotalRefundedAmount(@Param("bookingId") Long bookingId);

    // Find pending payments
    @Query("SELECT bp FROM BookingPayment bp WHERE bp.paymentStatus = 'PENDING' " +
           "ORDER BY bp.createdAt")
    List<BookingPayment> findPendingPayments();

    // Find failed payments
    @Query("SELECT bp FROM BookingPayment bp WHERE bp.paymentStatus = 'FAILED' " +
           "ORDER BY bp.createdAt DESC")
    List<BookingPayment> findFailedPayments();

    // Find payments requiring refund
    @Query("SELECT bp FROM BookingPayment bp WHERE bp.paymentStatus = 'REFUND_REQUESTED' " +
           "ORDER BY bp.createdAt")
    List<BookingPayment> findPaymentsRequiringRefund();

    // Find payments by guest
    @Query("SELECT bp FROM BookingPayment bp WHERE bp.booking.guest.id = :guestId " +
           "ORDER BY bp.createdAt DESC")
    List<BookingPayment> findByGuestId(@Param("guestId") Long guestId);

    // Find payments by host
    @Query("SELECT bp FROM BookingPayment bp WHERE bp.booking.host.id = :hostId " +
           "ORDER BY bp.createdAt DESC")
    List<BookingPayment> findByHostId(@Param("hostId") Long hostId);

    // Find payments by property
    @Query("SELECT bp FROM BookingPayment bp WHERE bp.booking.property.id = :propertyId " +
           "ORDER BY bp.createdAt DESC")
    List<BookingPayment> findByPropertyId(@Param("propertyId") Long propertyId);

    // Find payments within date range
    @Query("SELECT bp FROM BookingPayment bp WHERE bp.paymentDate BETWEEN :startDate AND :endDate " +
           "ORDER BY bp.paymentDate DESC")
    List<BookingPayment> findByPaymentDateRange(@Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);

    // Find successful payments within date range
    @Query("SELECT bp FROM BookingPayment bp WHERE bp.paymentStatus = 'COMPLETED' " +
           "AND bp.paymentDate BETWEEN :startDate AND :endDate " +
           "ORDER BY bp.paymentDate DESC")
    List<BookingPayment> findSuccessfulPaymentsByDateRange(@Param("startDate") LocalDateTime startDate,
                                                          @Param("endDate") LocalDateTime endDate);

    // Calculate revenue for host within date range
    @Query("SELECT COALESCE(SUM(bp.amount), 0) FROM BookingPayment bp " +
           "WHERE bp.booking.host.id = :hostId AND bp.paymentStatus = 'COMPLETED' " +
           "AND bp.paymentDate BETWEEN :startDate AND :endDate")
    BigDecimal calculateHostRevenue(@Param("hostId") Long hostId,
                                   @Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);

    // Calculate revenue for property within date range
    @Query("SELECT COALESCE(SUM(bp.amount), 0) FROM BookingPayment bp " +
           "WHERE bp.booking.property.id = :propertyId AND bp.paymentStatus = 'COMPLETED' " +
           "AND bp.paymentDate BETWEEN :startDate AND :endDate")
    BigDecimal calculatePropertyRevenue(@Param("propertyId") Long propertyId,
                                       @Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);

    // Calculate total platform revenue within date range
    @Query("SELECT COALESCE(SUM(bp.amount), 0) FROM BookingPayment bp " +
           "WHERE bp.paymentStatus = 'COMPLETED' " +
           "AND bp.paymentDate BETWEEN :startDate AND :endDate")
    BigDecimal calculatePlatformRevenue(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);

    // Find payments by payment method
    @Query("SELECT bp FROM BookingPayment bp WHERE bp.paymentMethod = :paymentMethod " +
           "ORDER BY bp.createdAt DESC")
    List<BookingPayment> findByPaymentMethod(@Param("paymentMethod") String paymentMethod);

    // Find overdue payments (pending for more than specified hours)
    @Query("SELECT bp FROM BookingPayment bp WHERE bp.paymentStatus = 'PENDING' " +
           "AND bp.createdAt < :cutoffTime ORDER BY bp.createdAt")
    List<BookingPayment> findOverduePayments(@Param("cutoffTime") LocalDateTime cutoffTime);

    // Check if booking has any successful payments
    @Query("SELECT COUNT(bp) > 0 FROM BookingPayment bp " +
           "WHERE bp.booking.id = :bookingId AND bp.paymentStatus = 'COMPLETED'")
    boolean hasSuccessfulPayments(@Param("bookingId") Long bookingId);

    // Check if booking is fully paid
    @Query("SELECT CASE WHEN COALESCE(SUM(bp.amount), 0) >= b.totalAmount THEN true ELSE false END " +
           "FROM Booking b LEFT JOIN b.payments bp " +
           "WHERE b.id = :bookingId AND (bp IS NULL OR bp.paymentStatus = 'COMPLETED')")
    boolean isBookingFullyPaid(@Param("bookingId") Long bookingId);

    // Get payment statistics for host
    @Query("SELECT COUNT(bp.id), COALESCE(SUM(bp.amount), 0.0), " +
           "SUM(CASE WHEN bp.paymentStatus = 'COMPLETED' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN bp.paymentStatus = 'FAILED' THEN 1 ELSE 0 END) " +
           "FROM BookingPayment bp JOIN bp.booking b WHERE b.host.id = :hostId")
    Object[] getPaymentStatisticsForHost(@Param("hostId") Long hostId);

    // Get payment statistics for property
    @Query("SELECT COUNT(bp.id), COALESCE(SUM(bp.amount), 0.0), " +
           "SUM(CASE WHEN bp.paymentStatus = 'COMPLETED' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN bp.paymentStatus = 'FAILED' THEN 1 ELSE 0 END) " +
           "FROM BookingPayment bp JOIN bp.booking b WHERE b.property.id = :propertyId")
    Object[] getPaymentStatisticsForProperty(@Param("propertyId") Long propertyId);

    // Find recent payments for dashboard
    @Query("SELECT bp FROM BookingPayment bp WHERE bp.createdAt >= :since " +
           "ORDER BY bp.createdAt DESC")
    List<BookingPayment> findRecentPayments(@Param("since") LocalDateTime since);

    // Find payments requiring attention (failed or refund requested)
    @Query("SELECT bp FROM BookingPayment bp WHERE bp.paymentStatus IN ('FAILED', 'REFUND_REQUESTED') " +
           "ORDER BY bp.updatedAt DESC")
    List<BookingPayment> findPaymentsRequiringAttention();

    // Count payments by status
    @Query("SELECT bp.paymentStatus, COUNT(bp) FROM BookingPayment bp GROUP BY bp.paymentStatus")
    List<Object[]> countPaymentsByStatus();

    // Count payments by type
    @Query("SELECT bp.paymentType, COUNT(bp) FROM BookingPayment bp GROUP BY bp.paymentType")
    List<Object[]> countPaymentsByType();

    // Find duplicate payments (same booking, amount, and recent time)
    @Query("SELECT bp FROM BookingPayment bp WHERE bp.booking.id = :bookingId " +
           "AND bp.amount = :amount AND bp.createdAt >= :recentTime " +
           "ORDER BY bp.createdAt DESC")
    List<BookingPayment> findPotentialDuplicatePayments(@Param("bookingId") Long bookingId,
                                                       @Param("amount") BigDecimal amount,
                                                       @Param("recentTime") LocalDateTime recentTime);
}
