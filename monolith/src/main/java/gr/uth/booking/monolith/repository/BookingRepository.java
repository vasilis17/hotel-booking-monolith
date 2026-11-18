package gr.uth.booking.monolith.repository;

import gr.uth.booking.monolith.model.Booking;
import gr.uth.booking.monolith.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    // Elegxei an yparxei energos kratisi (oxi CANCELLED) poy epikaliptetai me tis nees imerominies
    List<Booking> findByRoomIdAndStatusNotAndCheckoutDateAfterAndCheckInDateBefore(
            Long roomId,
            BookingStatus status,
            LocalDate newCheckIn,
            LocalDate newCheckOut
    );
}