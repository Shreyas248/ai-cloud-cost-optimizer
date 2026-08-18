package com.cloudoptimizer.backend.service;

import com.cloudoptimizer.backend.dto.AuthResponse;
import com.cloudoptimizer.backend.dto.LoginRequest;
import com.cloudoptimizer.backend.dto.RegisterRequest;
import com.cloudoptimizer.backend.model.User;
import com.cloudoptimizer.backend.repository.UserRepository;
import com.cloudoptimizer.backend.security.JwtService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;


    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {

        this.userRepository =
                userRepository;

        this.passwordEncoder =
                passwordEncoder;

        this.jwtService =
                jwtService;

    }


    // ==========================================
    // REGISTER USER
    // ==========================================

    public AuthResponse register(
            RegisterRequest request
    ) {

        String email =
                request.getEmail()
                        .trim()
                        .toLowerCase();

        if (
                userRepository
                        .existsByEmail(email)
        ) {

            throw new IllegalArgumentException(
                    "An account with this email already exists"
            );

        }


        User user =
                new User(
                        request.getName().trim(),
                        email,

                        passwordEncoder.encode(
                                request.getPassword()
                        )
                );


        User savedUser =
                userRepository.save(user);


        String token =
                jwtService.generateToken(
                        savedUser
                );


        return new AuthResponse(
                token,
                savedUser.getName(),
                savedUser.getEmail()
        );

    }


    // ==========================================
    // LOGIN USER
    // ==========================================

    public AuthResponse login(
            LoginRequest request
    ) {

        String email =
                request.getEmail()
                        .trim()
                        .toLowerCase();


        User user =
                userRepository
                        .findByEmail(email)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Invalid email or password"
                                        )
                        );


        boolean passwordMatches =
                passwordEncoder.matches(
                        request.getPassword(),
                        user.getPassword()
                );


        if (!passwordMatches) {

            throw new IllegalArgumentException(
                    "Invalid email or password"
            );

        }


        String token =
                jwtService.generateToken(
                        user
                );


        return new AuthResponse(
                token,
                user.getName(),
                user.getEmail()
        );

    }

}

