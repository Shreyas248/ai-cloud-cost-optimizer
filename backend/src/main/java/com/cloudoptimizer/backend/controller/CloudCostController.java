package com.cloudoptimizer.backend.controller;

import com.cloudoptimizer.backend.model.CloudCost;
import com.cloudoptimizer.backend.model.User;
import com.cloudoptimizer.backend.repository.UserRepository;
import com.cloudoptimizer.backend.service.CloudCostImportService;
import com.cloudoptimizer.backend.service.CloudCostService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/costs")
@CrossOrigin(origins = "http://localhost:5173")
public class CloudCostController {

    private final CloudCostService cloudCostService;
    private final CloudCostImportService cloudCostImportService;
    private final UserRepository userRepository;

    public CloudCostController(
            CloudCostService cloudCostService,
            CloudCostImportService cloudCostImportService,
            UserRepository userRepository
    ) {
        this.cloudCostService = cloudCostService;
        this.cloudCostImportService = cloudCostImportService;
        this.userRepository = userRepository;
    }

    // =========================================================
    // GET AUTHENTICATED USER
    // =========================================================

    private User getAuthenticatedUser(
            Authentication authentication
    ) {

        if (
                authentication == null ||
                !authentication.isAuthenticated()
        ) {

            throw new SecurityException(
                    "User is not authenticated"
            );
        }

        String email = null;

        Object principal =
                authentication.getPrincipal();

        // =====================================================
        // PRINCIPAL IS USER
        // =====================================================

        if (principal instanceof User user) {

            email = user.getEmail();
        }

        // =====================================================
        // PRINCIPAL IS STRING
        // =====================================================

        else if (principal instanceof String principalString) {

            email = principalString;
        }

        // =====================================================
        // FALLBACK
        // =====================================================

        if (
                email == null ||
                email.isBlank()
        ) {

            email = authentication.getName();
        }

        if (
                email == null ||
                email.isBlank()
        ) {

            throw new SecurityException(
                    "Authenticated user email is missing"
            );
        }

        email =
                email
                        .trim()
                        .toLowerCase();

        System.out.println("=================================");
        System.out.println("CLOUD COST AUTHENTICATED USER");
        System.out.println("Email = " + email);
        System.out.println(
                "Principal type = "
                        + principal.getClass().getName()
        );
        System.out.println("=================================");

        User user =
                userRepository
                        .findByEmail(email)
                        .orElse(null);

        if (user == null) {

            System.err.println("=================================");
            System.err.println(
                    "AUTHENTICATED USER NOT FOUND"
            );
            System.err.println(
                    "Email = " + email
            );
            System.err.println("=================================");

            throw new SecurityException(
                    "Authenticated user not found. "
                            + "Please login again."
            );
        }

        if (user.getId() == null) {

            throw new SecurityException(
                    "Authenticated user has no database ID"
            );
        }

        System.out.println(
                "User found successfully"
        );

        System.out.println(
                "User ID = "
                        + user.getId()
        );

        System.out.println(
                "User Email = "
                        + user.getEmail()
        );

        return user;
    }

    // =========================================================
    // GET ALL COSTS
    // =========================================================

    @GetMapping
    public ResponseEntity<?> getAllCosts(
            Authentication authentication
    ) {

        try {

            User user =
                    getAuthenticatedUser(authentication);

            return ResponseEntity.ok(
                    cloudCostService.getAllCosts(
                            user.getId()
                    )
            );

        } catch (SecurityException e) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            Map.of(
                                    "error",
                                    e.getMessage()
                            )
                    );
        }
    }

    // =========================================================
    // GET COSTS BY SERVICE
    // =========================================================

    @GetMapping("/service/{service}")
    public ResponseEntity<?> getByService(
            @PathVariable String service,
            Authentication authentication
    ) {

        try {

            User user =
                    getAuthenticatedUser(authentication);

            return ResponseEntity.ok(
                    cloudCostService.getByService(
                            user.getId(),
                            service
                    )
            );

        } catch (SecurityException e) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            Map.of(
                                    "error",
                                    e.getMessage()
                            )
                    );
        }
    }

    // =========================================================
    // GET COSTS BY MONTH
    // =========================================================

    @GetMapping("/month/{month}")
    public ResponseEntity<?> getByMonth(
            @PathVariable String month,
            Authentication authentication
    ) {

        try {

            User user =
                    getAuthenticatedUser(authentication);

            return ResponseEntity.ok(
                    cloudCostService.getByMonth(
                            user.getId(),
                            month
                    )
            );

        } catch (SecurityException e) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            Map.of(
                                    "error",
                                    e.getMessage()
                            )
                    );
        }
    }

    // =========================================================
    // IMPORT CSV
    // =========================================================

    @PostMapping("/import")
    public ResponseEntity<?> importCsv(
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {

        if (file == null || file.isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "error",
                                    "Please select a CSV file"
                            )
                    );
        }

        try {

            User user =
                    getAuthenticatedUser(authentication);

            int imported =
                    cloudCostImportService.importCsv(
                            file,
                            user
                    );

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "CSV imported successfully",

                            "recordsImported",
                            imported
                    )
            );

        } catch (SecurityException e) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            Map.of(
                                    "error",
                                    e.getMessage()
                            )
                    );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "error",
                                    e.getMessage() != null
                                            ? e.getMessage()
                                            : "CSV import failed"
                            )
                    );
        }
    }

    // =========================================================
    // COST SUMMARY
    // =========================================================

    @GetMapping("/summary")
    public ResponseEntity<?> getSummary(
            Authentication authentication
    ) {

        try {

            User user =
                    getAuthenticatedUser(authentication);

            return ResponseEntity.ok(
                    cloudCostService.getSummary(
                            user.getId()
                    )
            );

        } catch (SecurityException e) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            Map.of(
                                    "error",
                                    e.getMessage()
                            )
                    );
        }
    }

    // =========================================================
    // COST BY SERVICE
    // =========================================================

    @GetMapping("/by-service")
    public ResponseEntity<?> getCostByService(
            Authentication authentication
    ) {

        try {

            User user =
                    getAuthenticatedUser(authentication);

            return ResponseEntity.ok(
                    cloudCostService.getCostByService(
                            user.getId()
                    )
            );

        } catch (SecurityException e) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            Map.of(
                                    "error",
                                    e.getMessage()
                            )
                    );
        }
    }

    // =========================================================
    // COST BY MONTH
    // =========================================================

    @GetMapping("/by-month")
    public ResponseEntity<?> getCostByMonth(
            Authentication authentication
    ) {

        try {

            User user =
                    getAuthenticatedUser(authentication);

            return ResponseEntity.ok(
                    cloudCostService.getCostByMonth(
                            user.getId()
                    )
            );

        } catch (SecurityException e) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            Map.of(
                                    "error",
                                    e.getMessage()
                            )
                    );
        }
    }
}