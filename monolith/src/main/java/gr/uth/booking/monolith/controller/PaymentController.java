package gr.uth.booking.monolith.controller;

import gr.uth.booking.monolith.service.PaymentService;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService service;

    public PaymentController(PaymentService service) {
        this.service = service;
    }

    record PaymentRequest(Long bookingId) {}

    @PostMapping
    public ResponseEntity<?> pay(
            @RequestBody PaymentRequest request,
            Authentication auth) {

        try {
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(service.pay(
                            request.bookingId(),
                            auth.getName()));

        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).body(e.getMessage());

        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(e.getMessage());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<?> get(
            @PathVariable Long bookingId,
            Authentication auth) {

        try {
            return ResponseEntity.ok(
                    service.get(bookingId, auth.getName()));

        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).body(e.getMessage());

        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }
}