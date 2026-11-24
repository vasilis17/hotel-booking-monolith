package gr.uth.booking.monolith.service;

import gr.uth.booking.monolith.model.User;
import gr.uth.booking.monolith.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(String name, String email, String password) {
        if (name == null || name.isBlank() || name.length() > 255 || email == null || email.length() > 255 || !email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+") || password == null || password.length() < 8 || password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72)
            throw new IllegalArgumentException("Μη έγκυρα στοιχεία εγγραφής (κωδικός τουλάχιστον 8 χαρακτήρων, έως 72 bytes)");
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Το email χρησιμοποιείται ήδη!");
        }
        User newUser = new User();
        newUser.setName(name);
        newUser.setEmail(email);
        newUser.setPassword(passwordEncoder.encode(password));
        return userRepository.save(newUser);
    }

    public User login(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent() && passwordEncoder.matches(password, userOpt.get().getPassword())) {
            return userOpt.get();
        }
        throw new RuntimeException("Λάθος email ή κωδικός!");
    }

    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.emptyList()
        );
    }
}