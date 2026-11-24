package gr.uth.booking.monolith.service;

import gr.uth.booking.monolith.model.Room;
import gr.uth.booking.monolith.repository.RoomRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomService {

    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public Room createRoom(Room room) {
        if (room.getId() != null || room.getRoomNumber() == null || room.getRoomNumber().isBlank() || room.getType() == null || room.getType().isBlank() || !Double.isFinite(room.getPrice()) || room.getPrice() <= 0)
            throw new IllegalArgumentException("Μη έγκυρα στοιχεία δωματίου");
        // Elegxos an iparxei idi room me afton ton arithmho
        if (roomRepository.findByRoomNumber(room.getRoomNumber()).isPresent()) {
            throw new RuntimeException("Υπάρχει ήδη δωμάτιο με αριθμό " + room.getRoomNumber());
        }
        return roomRepository.save(room);
    }

    public void deleteRoom(Long id) {
        if (!roomRepository.existsById(id)) {
            throw new RuntimeException("Το δωμάτιο δεν βρέθηκε");
        }
        roomRepository.deleteById(id);
    }
}