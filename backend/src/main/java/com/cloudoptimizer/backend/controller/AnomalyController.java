package com.cloudoptimizer.backend.controller;

import com.cloudoptimizer.backend.model.Anomaly;
import com.cloudoptimizer.backend.service.AnomalyDetectionService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/anomalies")
@CrossOrigin(origins = "http://localhost:5173")
public class AnomalyController {

    private final AnomalyDetectionService anomalyDetectionService;


    public AnomalyController(
            AnomalyDetectionService anomalyDetectionService
    ) {

        this.anomalyDetectionService =
                anomalyDetectionService;

    }


    // =========================================================
    // DETECT ANOMALIES
    // =========================================================

    @GetMapping
    public ResponseEntity<?> getAnomalies() {

        List<Anomaly> anomalies =
                anomalyDetectionService
                        .detectAnomalies();


        return ResponseEntity.ok(
                Map.of(

                        "count",
                        anomalies.size(),

                        "anomalies",
                        anomalies

                )
        );

    }

}

