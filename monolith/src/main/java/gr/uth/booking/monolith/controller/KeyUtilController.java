package gr.uth.booking.monolith.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@RestController
@RequestMapping("/api")
public class KeyUtilController {

    @GetMapping("/create-key")
    public ResponseEntity<String> createKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("HmacSHA512"); // dimiurgia klidion me HmacSHA512
            SecretKey secretKey = keyGenerator.generateKey(); // paragogi tixeas akoluthias apo bytes
            byte[] keyBytes = secretKey.getEncoded();
            String keyInBase64 = Base64.getEncoder().encodeToString(keyBytes); // metatropi bytes se keimeno
            return ResponseEntity.ok(keyInBase64);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating secret key", e);
        }
    }
}