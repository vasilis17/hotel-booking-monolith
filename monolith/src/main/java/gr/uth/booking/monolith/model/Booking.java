package gr.uth.booking.monolith.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "bookings")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Sysxetisi me ton xristi: Polles kratiseis mporoun na anikoun se enan xristi
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Sysxetisi me to domatio: Polles kratiseis mporun na aforun ena domatio
    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable = false)
    private LocalDate checkInDate;

    @Column(nullable = false)
    private LocalDate checkoutDate;

    @Column(nullable = false)
    private double totalPrice;

    @Enumerated(EnumType.STRING) // Apothikevei to status os keimeno
    @Column(nullable = false)
    private BookingStatus status;

}
