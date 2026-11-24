package gr.uth.booking.monolith.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity // enimeronei to JPA oti afti i klasi einai pinakas sthn VD
@Table(name="users") // To onoma tu pinaka stin vasi
@Data // Lombok: ftiaxnei getters, setters, toString
@NoArgsConstructor // Lombok: ftiaxnei default constructor
@AllArgsConstructor // Lombok: ftiaxnei kataskevasti me ola ta pedia
public class User {
    @Id // To primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // gia aftomati afksisi tou primary key
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true) // to email prepei na einai monadiko
    private String email;

    @Column(nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    @lombok.ToString.Exclude
    private String password;

    // Tha prostheso kai rolo (px: admin, customer)

}
