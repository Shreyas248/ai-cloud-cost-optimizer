package com.cloudoptimizer.backend.service;

import com.cloudoptimizer.backend.model.User;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {


    // ==========================================
    // GET CURRENT USER
    // ==========================================

    public User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        if (
                authentication == null ||
                !authentication.isAuthenticated()
        ) {

            throw new RuntimeException(
                    "User is not authenticated"
            );
        }


        Object principal =
                authentication.getPrincipal();


        if (
                principal instanceof User user
        ) {

            return user;
        }


        throw new RuntimeException(
                "Unable to identify authenticated user"
        );

    }

}