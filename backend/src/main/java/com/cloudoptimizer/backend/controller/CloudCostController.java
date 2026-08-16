package com.cloudoptimizer.backend.controller;

import com.cloudoptimizer.backend.model.CloudCost;
import com.cloudoptimizer.backend.service.CloudCostImportService;
import com.cloudoptimizer.backend.service.CloudCostService;

import org.springframework.http.ResponseEntity;
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

    public CloudCostController(
            CloudCostService cloudCostService,
            CloudCostImportService cloudCostImportService
    ) {
        this.cloudCostService = cloudCostService;
        this.cloudCostImportService = cloudCostImportService;
    }

    // ==========================================
    // GET ALL COSTS
    // ==========================================

    @GetMapping
    public ResponseEntity<List<CloudCost>> getAllCosts() {

        return ResponseEntity.ok(
                cloudCostService.getAllCosts()
        );
    }

    // ==========================================
    // GET COSTS BY SERVICE
    // ==========================================

    @GetMapping("/service/{service}")
    public ResponseEntity<List<CloudCost>> getByService(
            @PathVariable String service
    ) {

        return ResponseEntity.ok(
                cloudCostService.getByService(service)
        );
    }

    // ==========================================
    // GET COSTS BY MONTH
    // ==========================================

    @GetMapping("/month/{month}")
    public ResponseEntity<List<CloudCost>> getByMonth(
            @PathVariable String month
    ) {

        return ResponseEntity.ok(
                cloudCostService.getByMonth(month)
        );
    }

    // ==========================================
    // IMPORT CSV
    // ==========================================

    @PostMapping("/import")
    public ResponseEntity<?> importCsv(
            @RequestParam("file") MultipartFile file
    ) {

        if (file.isEmpty()) {

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

            int imported =
                    cloudCostImportService.importCsv(file);

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "CSV imported successfully",

                            "recordsImported",
                            imported
                    )
            );

        } catch (Exception e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "error",
                                    e.getMessage()
                            )
                    );
        }
    }

    // ==========================================
    // COST SUMMARY
    // ==========================================

    @GetMapping("/summary")
    public ResponseEntity<?> getSummary() {

        return ResponseEntity.ok(
                cloudCostService.getSummary()
        );
    }

    // ==========================================
    // COST BY SERVICE
    // ==========================================

    @GetMapping("/by-service")
    public ResponseEntity<?> getCostByService() {

        return ResponseEntity.ok(
                cloudCostService.getCostByService()
        );
    }

    // ==========================================
    // COST BY MONTH
    // ==========================================

    @GetMapping("/by-month")
    public ResponseEntity<?> getCostByMonth() {

        return ResponseEntity.ok(
                cloudCostService.getCostByMonth()
        );
    }
}