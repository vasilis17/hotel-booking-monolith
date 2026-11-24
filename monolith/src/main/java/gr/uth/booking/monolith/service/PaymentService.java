package gr.uth.booking.monolith.service;

import gr.uth.booking.monolith.model.*;
import gr.uth.booking.monolith.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Service
public class PaymentService {

    private final BookingRepository bookings;
    private final PaymentRepository payments;

    public PaymentService(
            BookingRepository bookings,
            PaymentRepository payments) {
        this.bookings = bookings;
        this.payments = payments;
    }

    public record PaymentResult(
            Long id,
            Long bookingId,
            BigDecimal amount,
            BigDecimal refundedAmount,
            PaymentStatus status,
            Instant paidAt,
            Instant refundedAt,
            BookingStatus bookingStatus,
            String currency) {}

    @Transactional
    public PaymentResult pay(Long bookingId, String email) {

        Booking booking = ownedBooking(bookingId, email);

        if (payments.findByBookingId(bookingId).isPresent())
            throw new IllegalArgumentException(
                    "Η κράτηση έχει ήδη πληρωθεί");

        if (booking.getStatus() != BookingStatus.PENDING)
            throw new IllegalArgumentException(
                    "Πληρωμή επιτρέπεται μόνο για PENDING κράτηση");

        if (!Double.isFinite(booking.getTotalPrice()))
            throw new IllegalArgumentException("Μη έγκυρο ποσό");

        BigDecimal amount = BigDecimal
                .valueOf(booking.getTotalPrice())
                .setScale(2, RoundingMode.HALF_UP);

        if (amount.signum() <= 0)
            throw new IllegalArgumentException("Μη έγκυρο ποσό");

        Payment payment = new Payment();

        payment.setBooking(booking);
        payment.setAmount(amount);
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(Instant.now());

        booking.setTotalPrice(amount.doubleValue());
        booking.setStatus(BookingStatus.CONFIRMED);

        bookings.save(booking);

        return result(payments.save(payment));
    }

    @Transactional
    public PaymentResult get(Long bookingId, String email) {

        ownedBooking(bookingId, email);

        return result(payments.findByBookingId(bookingId)
                .orElseThrow(() ->
                        new java.util.NoSuchElementException(
                                "Δεν βρέθηκε πληρωμή")));
    }

    private Booking ownedBooking(Long id, String email) {

        if (id == null)
            throw new IllegalArgumentException("Απαιτείται κράτηση");

        Booking b = bookings.findLockedById(id)
                .orElseThrow(() ->
                        new java.util.NoSuchElementException(
                                "Η κράτηση δεν βρέθηκε"));

        if (!b.getUser().getEmail().equals(email))
            throw new AccessDeniedException(
                    "Δεν επιτρέπεται η πρόσβαση");

        return b;
    }

    private PaymentResult result(Payment p) {

        return new PaymentResult(
                p.getId(),
                p.getBooking().getId(),
                p.getAmount(),
                p.getRefundedAmount(),
                p.getStatus(),
                p.getPaidAt(),
                p.getRefundedAt(),
                p.getBooking().getStatus(),
                "EUR"
        );
    }
}