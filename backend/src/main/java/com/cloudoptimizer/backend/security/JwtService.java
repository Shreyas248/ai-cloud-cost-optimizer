package com.cloudoptimizer.backend.security;

import com.cloudoptimizer.backend.model.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;


    // =========================================================
    // GENERATE TOKEN
    // =========================================================

    public String generateToken(User user) {

        if (user == null) {

            throw new IllegalArgumentException(
                    "User cannot be null"
            );
        }

        if (user.getEmail() == null) {

            throw new IllegalArgumentException(
                    "User email cannot be null"
            );
        }

        return Jwts.builder()

                .subject(
                        user.getEmail()
                )

                .claim(
                        "name",
                        user.getName()
                )

                .issuedAt(
                        new Date()
                )

                .expiration(
                        new Date(
                                System.currentTimeMillis()
                                        + jwtExpiration
                        )
                )

                .signWith(
                        getSigningKey()
                )

                .compact();
    }


    // =========================================================
    // EXTRACT EMAIL
    // =========================================================

    public String extractEmail(String token) {

        if (
                token == null ||
                token.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "JWT token cannot be empty"
            );
        }

        return extractAllClaims(token)
                .getSubject();
    }


    // =========================================================
    // VALIDATE TOKEN
    // =========================================================

    public boolean isTokenValid(
            String token,
            User user
    ) {

        try {

            if (
                    token == null ||
                    token.isBlank() ||
                    user == null ||
                    user.getEmail() == null
            ) {

                return false;
            }


            String email =
                    extractEmail(token);


            return email != null
                    && email.equals(
                            user.getEmail()
                    )
                    && !isTokenExpired(token);


        } catch (Exception e) {

            System.err.println(
                    "JWT validation error: "
                            + e.getClass().getSimpleName()
                            + " - "
                            + e.getMessage()
            );

            return false;
        }
    }


    // =========================================================
    // CHECK EXPIRATION
    // =========================================================

    private boolean isTokenExpired(
            String token
    ) {

        Date expiration =
                extractAllClaims(token)
                        .getExpiration();


        if (expiration == null) {

            return true;
        }


        return expiration.before(
                new Date()
        );
    }


    // =========================================================
    // EXTRACT ALL CLAIMS
    // =========================================================

    private Claims extractAllClaims(
            String token
    ) {

        return Jwts.parser()

                .verifyWith(
                        getSigningKey()
                )

                .build()

                .parseSignedClaims(
                        token
                )

                .getPayload();
    }


    // =========================================================
    // SIGNING KEY
    // =========================================================

    private SecretKey getSigningKey() {

        if (
                jwtSecret == null ||
                jwtSecret.isBlank()
        ) {

            throw new IllegalStateException(
                    "jwt.secret is not configured"
            );
        }


        byte[] keyBytes =
                jwtSecret.getBytes(
                        StandardCharsets.UTF_8
                );


        /*
         * HS512 requires a strong key.
         *
         * Use a secret of at least 64 bytes.
         */

        if (keyBytes.length < 64) {

            throw new IllegalStateException(
                    "jwt.secret must be at least 64 bytes for HS512"
            );
        }


        return Keys.hmacShaKeyFor(
                keyBytes
        );
    }
}