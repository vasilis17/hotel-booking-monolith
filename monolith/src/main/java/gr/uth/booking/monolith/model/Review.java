package gr.uth.booking.monolith.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Sindeo thn aksiologisi me thn sigkekrimeni kratisi
    @OneToOne
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"user", "room", "review"})
    private Booking booking;

    @Column(nullable = false)
    private int rating; // Apo 1 eos 5

    @Column(length = 1000)
    private String comment;
}