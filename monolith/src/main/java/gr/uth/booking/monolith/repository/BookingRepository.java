package gr.uth.booking.monolith.repository;

import gr.uth.booking.monolith.model.Booking;
import gr.uth.booking.monolith.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(Long userId);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select b from Booking b where b.id = :id")
    java.util.Optional<Booking> findLockedById(@org.springframework.data.repository.query.Param("id") Long id);

    // Elegxei an yparxei energos kratisi (oxi CANCELLED) poy epikaliptetai me tis nees imerominies
    List<Booking> findByRoomIdAndStatusNotAndCheckoutDateAfterAndCheckInDateBefore(
            Long roomId,
            BookingStatus status,
            LocalDate newCheckIn,
            LocalDate newCheckOut
    );
}