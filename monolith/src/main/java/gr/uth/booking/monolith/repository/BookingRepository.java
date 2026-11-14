package gr.uth.booking.monolith.repository;

import gr.uth.booking.monolith.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    // Edo elegxetai an iparxei kratisi gia to domatio pou epikaliptetai me tis nees imerominies
    // logiki: Nea CheckInDate < palia CkeckOutDate KAI nea CkeckOutDate > palia CkeckInDate
    List<Booking> findByRoomIdAndCheckoutDateAfterAndCheckInDateBefore(Long roomId, LocalDate newCheckIn, LocalDate newCheckOut);
}
