package com.example.userservice.controller;

import com.example.userservice.dto.InternalAuthRequest;
import com.example.userservice.dto.InternalAuthResponse;
import com.example.userservice.entity.User;
import com.example.userservice.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/internal")
public class InternalAuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String internalKey;

    public InternalAuthController(UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${auth.internal-key}") String internalKey) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.internalKey = internalKey;
    }

    @PostMapping("/verify-credentials")
    public ResponseEntity<InternalAuthResponse> verifyCredentials(
            @RequestHeader("X-Internal-Key") String requestKey,
            @Valid @RequestBody InternalAuthRequest request) {

        if (!internalKey.equals(requestKey)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user == null || !user.isActive()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String role = user.getRoles().stream()
                .findFirst()
                .map(r -> r.getName())
                .orElse("USER");

        return ResponseEntity.ok(new InternalAuthResponse(user.getId(), user.getEmail(), role));
    }
}
