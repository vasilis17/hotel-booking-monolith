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

    public BookingService(BookingRepository bookingRepository, RoomRepository roomRepository, UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
    }

    public Booking createBookingByEmail(String email, Long roomId, LocalDate checkIn, LocalDate checkOut) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Ο χρήστης δεν βρέθηκε"));
        return createBooking(user.getId(), roomId, checkIn, checkOut);
    }

    public List<Booking> getBookingsByUserEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Ο χρήστης δεν βρέθηκε"));
        return getUserBookings(user.getId());
    }

    @Transactional
    public Booking createBooking(Long userId, Long roomId, LocalDate checkIn, LocalDate checkOut) {
        // Elegxos an yparxei xristis sto domatio
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("Ο χρήστης δεν βρέθηκε"));
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new RuntimeException("Το δωμάτιο δεν βρέθηκε"));

        // Elegxos gia imerominies (To checkin prepei na nai prin to checkout)
        if (checkIn.isAfter(checkOut) || checkIn.isEqual(checkOut)) {
            throw new RuntimeException("Οι ημερομηνίες είναι λανθασμένες (Το check-in πρέπει να είναι πριν το check-out)");
        }

        // Elegxos an to checkin einai sto parelthon
        if (checkIn.isBefore(LocalDate.now())) {
            throw new RuntimeException("Η ημερομηνία Check-in δεν μπορεί να είναι στο παρελθόν");
        }

        // Ipologismos dianikterefseon
        long days = ChronoUnit.DAYS.between(checkIn, checkOut);

        // Elaxisti diarkeia (Tulaxiston ena bradi)
        if (days < 1) {
            throw new RuntimeException("Η κράτηση πρέπει να είναι τουλάχιστον 1 διανυκτέρευση");
        }

        // Megisti diarkeia (To poli 30 meres)
        if (days > 30) {
            throw new RuntimeException("Η μέγιστη διάρκεια κράτησης είναι 30 διανυκτερεύσεις");
        }

        // Elegxos diathesimotitas domatiou
        List<Booking> existingBookings = bookingRepository.findByRoomIdAndStatusNotAndCheckoutDateAfterAndCheckInDateBefore(
                roomId,
                BookingStatus.CANCELLED,
                checkIn,
                checkOut
        );

        if(!existingBookings.isEmpty()) {
            throw new RuntimeException("Το δωμάτιο είναι ήδη κλεισμένο για αυτές τις ημερομηνίες");
        }

        // Ypologismos kostus
        double totalPrice = days * room.getPrice();

        // Dimiurgia + apothikefsi tis neas kratisis
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setRoom(room);
        booking.setCheckInDate(checkIn);
        booking.setCheckoutDate(checkOut);
        booking.setTotalPrice(totalPrice);

        // Afto tha allaksei se PENDING
        booking.setStatus(BookingStatus.PENDING);

        return bookingRepository.save(booking);
    }

    public List<Booking> getUserBookings(Long userId) {
        return bookingRepository.findAll().stream().filter(b -> b.getUser().getId().equals(userId)).toList();
    }

    @Transactional
    public Booking updateBookingStatus(Long bookingId, String newStatus) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Η κράτηση δεν βρέθηκε"));

        BookingStatus currentStatus = booking.getStatus();
        BookingStatus targetStatus;

        try {
            targetStatus = BookingStatus.valueOf(newStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Μη έγκυρη κατάσταση: " + newStatus);
        }

        // Kanones Metavasis
        if (currentStatus == BookingStatus.PENDING) {
            if (targetStatus != BookingStatus.CONFIRMED && targetStatus != BookingStatus.CANCELLED) {
                throw new RuntimeException("Από PENDING μπορείτε να πάτε μόνο σε CONFIRMED ή CANCELLED");
            }
        } else if (currentStatus == BookingStatus.CONFIRMED) {
            if (targetStatus != BookingStatus.CHECKED_IN && targetStatus != BookingStatus.CANCELLED) {
                throw new RuntimeException("Από CONFIRMED μπορείτε να πάτε μόνο σε CHECKED_IN ή CANCELLED");
            }
        } else if (currentStatus == BookingStatus.CHECKED_IN) {
            if (targetStatus != BookingStatus.COMPLETED) {
                throw new RuntimeException("Από CHECKED_IN μπορείτε να πάτε μόνο σε COMPLETED");
            }
        } else {
            throw new RuntimeException("Η κράτηση είναι " + currentStatus + " και δεν μπορεί να αλλάξει κατάσταση");
        }

        booking.setStatus(targetStatus);
        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Η κράτηση δεν βρέθηκε"));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new RuntimeException("Η κράτηση είναι ήδη ακυρωμένη");
        }
        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new RuntimeException("Δεν μπορείτε να ακυρώσετε μια ολοκληρωμένη κράτηση");
        }

        // Ipologismos diaforas oron mexri to telos ths dorean akirosis
        long hoursUntilCheckIn = java.time.temporal.ChronoUnit.HOURS.between(
                java.time.LocalDateTime.now(),
                booking.getCheckInDate().atStartOfDay()
        );

        // an einai ligotero apo 48 ores kratao to 50%
        if (hoursUntilCheckIn < 48) {
            booking.setTotalPrice(booking.getTotalPrice() * 0.5);
        }

        // Allazei to status se CANCELED se kathe periptosi
        booking.setStatus(BookingStatus.CANCELLED);

        return bookingRepository.save(booking);
    }

}
