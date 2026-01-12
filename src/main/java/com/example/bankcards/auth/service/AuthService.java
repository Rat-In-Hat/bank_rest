package com.example.bankcards.auth.service;

import com.example.bankcards.auth.dto.AuthRequest;
import com.example.bankcards.auth.dto.RegisterRequest;
import com.example.bankcards.auth.security.JwtService;
import com.example.bankcards.users.entity.User;
import com.example.bankcards.users.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public String register(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        String role = request.getRole() == null || request.getRole().isBlank()
                ? "ROLE_USER"
                : request.getRole();

        User user = new User(
                request.getUsername(),
                passwordEncoder.encode(request.getPassword()),
                role
        );
        userRepository.save(user);

        return jwtService.generateToken(user.getUsername(), user.getRole());
    }

    public String login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository
                .findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        return jwtService.generateToken(user.getUsername(), user.getRole());
    }
}
