package gr.uth.booking.monolith.controller;

import gr.uth.booking.monolith.jwt.JwtTokenProvider;
import gr.uth.booking.monolith.model.User;
import gr.uth.booking.monolith.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

record RegisterRequest(String name, String email, String password) {}
record LoginRequest(String email, String password) {}
record AuthResponse(String accessToken) {}

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(UserService userService, AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    // REGISTER
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            User savedUser = userService.registerUser(request.name(), request.email(), request.password());
            return new ResponseEntity<>("Ο χρήστης " + savedUser.getName() + " δημιουργήθηκε!", HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // LOGIN
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // Elegxos ton credentials meso AuthenticationManager
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );

            // Vazo authentication sto SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Dimiurgo JWT Token
            String token = jwtTokenProvider.generateToken(authentication);

            // TO epistrefo os JSON
            return new ResponseEntity<>(new AuthResponse(token), HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>("Λάθος email ή κωδικός!", HttpStatus.UNAUTHORIZED);
        }
    }
}