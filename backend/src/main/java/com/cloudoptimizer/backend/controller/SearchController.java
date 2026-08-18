package com.cloudoptimizer.backend.controller;

import com.cloudoptimizer.backend.dto.SearchRequest;
import com.cloudoptimizer.backend.service.RagService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class SearchController {

    private final RagService ragService;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public SearchController(
            RagService ragService
    ) {

        this.ragService =
                ragService;
    }


    // =========================================================
    // RAG SEARCH
    // =========================================================

    @PostMapping("/search")
    public ResponseEntity<Map<String, Object>> search(
            @RequestBody SearchRequest request,
            Authentication authentication
    ) {

        // =====================================================
        // VALIDATE REQUEST
        // =====================================================

        if (
                request == null ||
                request.getQuery() == null ||
                request.getQuery().isBlank()
        ) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "error",
                                    "Query cannot be empty"
                            )
                    );
        }


        // =====================================================
        // VALIDATE AUTHENTICATION
        // =====================================================

        if (
                authentication == null ||
                !authentication.isAuthenticated()
        ) {

            return ResponseEntity
                    .status(
                            HttpStatus.UNAUTHORIZED
                    )
                    .body(
                            Map.of(
                                    "error",
                                    "User is not authenticated"
                            )
                    );
        }


        try {

            // =================================================
            // GENERATE USER-SPECIFIC RAG RESPONSE
            // =================================================

            Map<String, Object> response =
                    ragService.generateAnswer(
                            request.getQuery().trim(),
                            authentication
                    );


            return ResponseEntity.ok(
                    response
            );


        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "error",
                                    e.getMessage()
                            )
                    );


        } catch (RuntimeException e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .body(
                            Map.of(
                                    "error",
                                    e.getMessage() != null
                                            ? e.getMessage()
                                            : "Failed to process search"
                            )
                    );
        }
    }
}