package gr.uth.booking.monolith.repository;

import gr.uth.booking.monolith.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    // Gia na vrisko oles tis aksiologiseis tu domatiu
    List<Review> findByBookingRoomId(Long roomId);

    // Gia na elegxo an mia kratisi exei idi aksiologisi
    boolean existsByBookingId(Long bookingId);
}