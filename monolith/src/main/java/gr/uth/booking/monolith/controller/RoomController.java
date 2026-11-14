package gr.uth.booking.monolith.controller;

import gr.uth.booking.monolith.model.Room;
import gr.uth.booking.monolith.service.RoomService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    // GET: Ola ta domatia
    @GetMapping
    public ResponseEntity<List<Room>> getAllRooms() {
        return new ResponseEntity<>(roomService.getAllRooms(), HttpStatus.OK);
    }

    // POST: Dimiurgia domatiou
    @PostMapping
    public ResponseEntity<?> createRoom(@RequestBody Room room) {
        try {
            Room savedRoom = roomService.createRoom(room);
            return new ResponseEntity<>(savedRoom, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // DELETE: diagrafi domatiu
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRoom(@PathVariable Long id) {
        try {
            roomService.deleteRoom(id);
            return new ResponseEntity<>("Το δωμάτιο διαγράφηκε", HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}