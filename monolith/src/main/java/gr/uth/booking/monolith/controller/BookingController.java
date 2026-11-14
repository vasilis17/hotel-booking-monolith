package gr.uth.booking.monolith.controller;

import gr.uth.booking.monolith.model.Booking;
import gr.uth.booking.monolith.service.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

record BookingRequest(Long roomId, LocalDate checkIn, LocalDate checkOut) {}

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody BookingRequest request, Authentication authentication) {
        try {
            String email = authentication.getName();
            Booking booking = bookingService.createBookingByEmail(
                    email,
                    request.roomId(),
                    request.checkIn(),
                    request.checkOut()
            );
            return new ResponseEntity<>(booking, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/user")
    public ResponseEntity<List<Booking>> getUserBookings(org.springframework.security.core.Authentication authentication) {
        // Το backend βρίσκει αυτόματα ποιος είναι από το token
        return new ResponseEntity<>(bookingService.getBookingsByUserEmail(authentication.getName()), HttpStatus.OK);
    }
}