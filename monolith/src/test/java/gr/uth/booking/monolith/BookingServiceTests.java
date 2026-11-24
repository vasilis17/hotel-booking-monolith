package gr.uth.booking.monolith;

import gr.uth.booking.monolith.model.*;
import gr.uth.booking.monolith.repository.*;
import gr.uth.booking.monolith.service.BookingService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingServiceTests {

    BookingRepository bookings =
            mock(BookingRepository.class);

    BookingService service = new BookingService(
            bookings,
            mock(RoomRepository.class),
            mock(UserRepository.class),
            mock(PaymentRepository.class)
    );

    Booking booking(BookingStatus status) {

        Booking b = new Booking();

        User u = new User();

        u.setEmail("owner@example.com");

        b.setUser(u);
        b.setStatus(status);
        b.setCheckInDate(LocalDate.now().plusDays(5));
        b.setTotalPrice(100);

        when(bookings.findLockedById(1L))
                .thenReturn(Optional.of(b));

        when(bookings.save(b))
                .thenReturn(b);

        return b;
    }

    @Test
    void rejectsOtherUser() {

        booking(BookingStatus.PENDING);

        assertThrows(
                org.springframework.security.access.AccessDeniedException.class,
                () -> service.cancelBooking(
                        1L,
                        "other@example.com")
        );
    }

    @Test
    void rejectsCheckedInCancellation() {

        booking(BookingStatus.CHECKED_IN);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.cancelBooking(
                        1L,
                        "owner@example.com")
        );
    }

    @Test
    void freeCancellationThroughStatus() {

        booking(BookingStatus.CONFIRMED);

        assertEquals(
                0.0,
                service.updateBookingStatus(
                        1L,
                        "CANCELLED",
                        "owner@example.com"
                ).getTotalPrice()
        );
    }
}