package gr.uth.booking.monolith.controller;

import gr.uth.booking.monolith.model.Review;
import gr.uth.booking.monolith.service.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

record ReviewRequest(Long bookingId, int rating, String comment) {}

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<?> createReview(@RequestBody ReviewRequest request) {
        try {
            Review review = reviewService.createReview(request.bookingId(), request.rating(), request.comment());
            return new ResponseEntity<>(review, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<Review>> getRoomReviews(@PathVariable Long roomId) {
        return new ResponseEntity<>(reviewService.getReviewsForRoom(roomId), HttpStatus.OK);
    }
}