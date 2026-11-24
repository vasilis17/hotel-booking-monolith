package gr.uth.booking.monolith.service;

import gr.uth.booking.monolith.model.Booking;
import gr.uth.booking.monolith.model.BookingStatus;
import gr.uth.booking.monolith.model.Review;
import gr.uth.booking.monolith.repository.BookingRepository;
import gr.uth.booking.monolith.repository.ReviewRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;

    public ReviewService(ReviewRepository reviewRepository, BookingRepository bookingRepository) {
        this.reviewRepository = reviewRepository;
        this.bookingRepository = bookingRepository;
    }

    @jakarta.transaction.Transactional
    public Review createReview(Long bookingId, int rating, String comment, String email) {
        if (bookingId == null || (comment != null && comment.length() > 1000))
            throw new IllegalArgumentException("Μη έγκυρη κράτηση ή σχόλιο άνω των 1000 χαρακτήρων");
        // Elegxos gia to an iparxei h kratisi
        Booking booking = bookingRepository.findLockedById(bookingId)
                .orElseThrow(() -> new RuntimeException("Η κράτηση δεν βρέθηκε"));

        if (!booking.getUser().getEmail().equals(email))
            throw new org.springframework.security.access.AccessDeniedException("Δεν επιτρέπεται η πρόσβαση");
        // Elegxos gia to an h kratisi exei oloklirothei
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new RuntimeException("Μπορείτε να αξιολογήσετε μόνο ολοκληρωμένες κρατήσεις");
        }

        // Elegxos an i kratisi exei idi aksiologithei
        if (reviewRepository.existsByBookingId(bookingId)) {
            throw new RuntimeException("Έχετε ήδη αξιολογήσει αυτή την κράτηση");
        }

        // Elegxos egkirotitas ths vathmologias
        if (rating < 1 || rating > 5) {
            throw new RuntimeException("Η βαθμολογία πρέπει να είναι από 1 έως 5");
        }

        // Dimiurgia aksiologisis kai apothikefsi
        Review review = new Review();
        review.setBooking(booking);
        review.setRating(rating);
        review.setComment(comment);

        return reviewRepository.save(review);
    }

    public List<Review> getReviewsForRoom(Long roomId) {
        return reviewRepository.findByBookingRoomId(roomId);
    }
}