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
        // Elegxos an iparxei xristis kai domatio
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("Ο χρήστης δεν βρέθηκε"));
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new RuntimeException("Το δωμάτιο δεν βρέθηκε"));

        // Elegxos imerominion (To checkIn prepei na 'nai prin to checkOut)
        if (checkIn.isAfter(checkOut) || checkIn.isEqual(checkOut)) {
            throw new RuntimeException("Οι ημερομηνίες είναι λανθασμένες");
        }

        // Elegxos diathesimotitas domatiu (An den iparxei idi kratisi
        List<Booking> existingBookings = bookingRepository.findByRoomIdAndCheckoutDateAfterAndCheckInDateBefore(roomId, checkIn, checkOut);

        if(!existingBookings.isEmpty()) {
            throw new RuntimeException("Το δωμάτιο είναι ήδη κλεισμένο για αυτές τις ημερομηνίες");
        }

        // Ipologismos kostus (Days*Price)
        long days = ChronoUnit.DAYS.between(checkIn, checkOut);
        double totalPrice = days * room.getPrice();

        // Dimiurgia kai apothikefsi kratisis
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setRoom(room);
        booking.setCheckInDate(checkIn);
        booking.setCheckoutDate(checkOut);
        booking.setTotalPrice(totalPrice);
        booking.setStatus(BookingStatus.CONFIRMED);// H PENDING
        return bookingRepository.save(booking);
    }

    public List<Booking> getUserBookings(Long userId) {
        return bookingRepository.findAll().stream().filter(b -> b.getUser().getId().equals(userId)).toList();
    }


}
