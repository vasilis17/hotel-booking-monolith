package gr.uth.booking.monolith.repository;

import gr.uth.booking.monolith.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select r from Room r where r.id = :id")
    Optional<Room> findLockedById(@org.springframework.data.repository.query.Param("id") Long id);

    Optional<Room> findByRoomNumber(String roomNumber);
}
