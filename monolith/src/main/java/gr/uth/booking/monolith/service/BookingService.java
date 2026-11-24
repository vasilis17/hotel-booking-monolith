package gr.uth.booking.monolith.service;

import gr.uth.booking.monolith.model.Booking;
import gr.uth.booking.monolith.model.BookingStatus;
import gr.uth.booking.monolith.model.Room;
import gr.uth.booking.monolith.model.User;
import gr.uth.booking.monolith.repository.BookingRepository;
import gr.uth.booking.monolith.repository.RoomRepository;
import gr.uth.booking.monolith.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final gr.uth.booking.monolith.repository.PaymentRepository paymentRepository;

    public BookingService(
            BookingRepository bookingRepository,
            RoomRepository roomRepository,
            UserRepository userRepository,
            gr.uth.booking.monolith.repository.PaymentRepository paymentRepository) {

        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public Booking createBookingByEmail(
            String email,
            Long roomId,
            LocalDate checkIn,
            LocalDate checkOut) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Ο χρήστης δεν βρέθηκε"));

        return createBooking(
                user.getId(), roomId, checkIn, checkOut);
    }

    public List<Booking> getBookingsByUserEmail(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Ο χρήστης δεν βρέθηκε"));

        return getUserBookings(user.getId());
    }

    @Transactional
    public Booking createBooking(
            Long userId,
            Long roomId,
            LocalDate checkIn,
            LocalDate checkOut) {

        if (userId == null || roomId == null ||
                checkIn == null || checkOut == null) {

            throw new IllegalArgumentException(
                    "Απαιτούνται δωμάτιο και ημερομηνίες");
        }

        // Elegxos an yparxei xristis kai domatio
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("Ο χρήστης δεν βρέθηκε"));

        Room room = roomRepository.findLockedById(roomId)
                .orElseThrow(() ->
                        new RuntimeException("Το δωμάτιο δεν βρέθηκε"));

        if (!room.isAvailable())
            throw new IllegalArgumentException(
                    "Το δωμάτιο δεν είναι διαθέσιμο");

        // Elegxos gia imerominies
        if (checkIn.isAfter(checkOut) ||
                checkIn.isEqual(checkOut)) {

            throw new RuntimeException(
                    "Οι ημερομηνίες είναι λανθασμένες (Το check-in πρέπει να είναι πριν το check-out)");
        }

        // Elegxos an to checkin einai sto parelthon
        if (checkIn.isBefore(LocalDate.now())) {

            throw new RuntimeException(
                    "Η ημερομηνία Check-in δεν μπορεί να είναι στο παρελθόν");
        }

        // Ypologismos dianikterefseon
        long days = ChronoUnit.DAYS.between(checkIn, checkOut);

        // Elaxisti diarkeia
        if (days < 1) {
            throw new RuntimeException(
                    "Η κράτηση πρέπει να είναι τουλάχιστον 1 διανυκτέρευση");
        }

        // Megisti diarkeia
        if (days > 30) {
            throw new RuntimeException(
                    "Η μέγιστη διάρκεια κράτησης είναι 30 διανυκτερεύσεις");
        }

        // Elegxos diathesimotitas domatiou
        List<Booking> existingBookings =
                bookingRepository
                        .findByRoomIdAndStatusNotAndCheckoutDateAfterAndCheckInDateBefore(
                                roomId,
                                BookingStatus.CANCELLED,
                                checkIn,
                                checkOut
                        );

        if (!existingBookings.isEmpty()) {
            throw new RuntimeException(
                    "Το δωμάτιο είναι ήδη κλεισμένο για αυτές τις ημερομηνίες");
        }

        // Ypologismos kostous
        double totalPrice = days * room.getPrice();

        // Dimiourgia kai apothikefsi kratisis
        Booking booking = new Booking();

        booking.setUser(user);
        booking.setRoom(room);
        booking.setCheckInDate(checkIn);
        booking.setCheckoutDate(checkOut);
        booking.setTotalPrice(totalPrice);

        booking.setStatus(BookingStatus.PENDING);

        return bookingRepository.save(booking);
    }

    public List<Booking> getUserBookings(Long userId) {

        return bookingRepository.findByUserId(userId);
    }

    @Transactional
    public Booking updateBookingStatus(
            Long bookingId,
            String newStatus,
            String email) {

        Booking booking = bookingRepository
                .findLockedById(bookingId)
                .orElseThrow(() ->
                        new RuntimeException("Η κράτηση δεν βρέθηκε"));

        checkOwner(booking, email);

        if (newStatus == null)
            throw new IllegalArgumentException(
                    "Απαιτείται κατάσταση");

        BookingStatus currentStatus = booking.getStatus();
        BookingStatus targetStatus;

        try {
            targetStatus = BookingStatus.valueOf(
                    newStatus.toUpperCase(java.util.Locale.ROOT));

        } catch (IllegalArgumentException e) {

            throw new RuntimeException(
                    "Μη έγκυρη κατάσταση: " + newStatus);
        }

        // Kanones metavasis katastaseon
        if (currentStatus == BookingStatus.PENDING) {

            // H epivevaiosi ginetai mono meso pliromis
            if (targetStatus != BookingStatus.CANCELLED) {

                throw new RuntimeException(
                        "Η επιβεβαίωση απαιτεί πληρωμή μέσω /api/payments");
            }

        } else if (currentStatus == BookingStatus.CONFIRMED) {

            if (targetStatus != BookingStatus.CHECKED_IN &&
                    targetStatus != BookingStatus.CANCELLED) {

                throw new RuntimeException(
                        "Από CONFIRMED μπορείτε να πάτε μόνο σε CHECKED_IN ή CANCELLED");
            }

        } else if (currentStatus == BookingStatus.CHECKED_IN) {

            if (targetStatus != BookingStatus.COMPLETED) {

                throw new RuntimeException(
                        "Από CHECKED_IN μπορείτε να πάτε μόνο σε COMPLETED");
            }

        } else {

            throw new RuntimeException(
                    "Η κράτηση είναι " + currentStatus +
                            " και δεν μπορεί να αλλάξει κατάσταση");
        }

        if (targetStatus == BookingStatus.CANCELLED)
            return applyCancellation(booking);

        booking.setStatus(targetStatus);

        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking cancelBooking(Long bookingId, String email) {

        Booking booking = bookingRepository
                .findLockedById(bookingId)
                .orElseThrow(() ->
                        new RuntimeException("Η κράτηση δεν βρέθηκε"));

        checkOwner(booking, email);

        return applyCancellation(booking);
    }

    // Elegxos idioktisia kratisis
    private void checkOwner(Booking booking, String email) {

        if (!booking.getUser().getEmail().equals(email))

            throw new org.springframework.security.access.AccessDeniedException(
                    "Δεν επιτρέπεται η πρόσβαση");
    }

    private Booking applyCancellation(Booking booking) {

        if (booking.getStatus() == BookingStatus.CHECKED_IN)

            throw new IllegalArgumentException(
                    "Δεν επιτρέπεται ακύρωση μετά το check-in");

        if (booking.getStatus() == BookingStatus.CANCELLED) {

            throw new RuntimeException(
                    "Η κράτηση είναι ήδη ακυρωμένη");
        }

        if (booking.getStatus() == BookingStatus.COMPLETED) {

            throw new RuntimeException(
                    "Δεν μπορείτε να ακυρώσετε μια ολοκληρωμένη κράτηση");
        }

        // Ypologismos diaforas oron mexri to check-in
        long hoursUntilCheckIn =
                java.time.temporal.ChronoUnit.HOURS.between(
                        java.time.LocalDateTime.now(),
                        booking.getCheckInDate().atStartOfDay()
                );

        // Politiki akirosis
        if (hoursUntilCheckIn < 48) {

            booking.setTotalPrice(
                    booking.getTotalPrice() * 0.5);

        } else {

            booking.setTotalPrice(0.0);
        }

        // Epistrofi posou mono an yparxei
        // katagegrammeni pliromi
        paymentRepository.findByBookingId(booking.getId())
                .ifPresent(payment -> {

                    java.math.BigDecimal retained =
                            java.math.BigDecimal
                                    .valueOf(booking.getTotalPrice())
                                    .setScale(
                                            2,
                                            java.math.RoundingMode.HALF_UP
                                    );

                    java.math.BigDecimal refund =
                            payment.getAmount().subtract(retained);

                    payment.setRefundedAmount(refund);

                    payment.setRefundedAt(
                            java.time.Instant.now());

                    payment.setStatus(
                            retained.signum() == 0
                                    ? gr.uth.booking.monolith.model.PaymentStatus.REFUNDED
                                    : gr.uth.booking.monolith.model.PaymentStatus.PARTIALLY_REFUNDED
                    );

                    booking.setTotalPrice(
                            retained.doubleValue());

                    paymentRepository.save(payment);
                });

        booking.setStatus(BookingStatus.CANCELLED);

        return bookingRepository.save(booking);
    }
}