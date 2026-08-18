package com.cloudoptimizer.backend.service;

import com.cloudoptimizer.backend.model.Anomaly;
import com.cloudoptimizer.backend.model.CloudCost;
import com.cloudoptimizer.backend.repository.CloudCostRepository;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnomalyDetectionService {

    private final CloudCostRepository repository;

    public AnomalyDetectionService(
            CloudCostRepository repository
    ) {
        this.repository = repository;
    }

    // =========================================================
    // DETECT ANOMALIES
    // =========================================================

    public List<Anomaly> detectAnomalies() {

        List<CloudCost> costs =
                repository.findAll();

        List<Anomaly> anomalies =
                new ArrayList<>();

        if (costs.isEmpty()) {
            return anomalies;
        }

        // =====================================================
        // GROUP COSTS BY SERVICE
        // =====================================================

        Map<String, List<CloudCost>> costsByService =
                new HashMap<>();

        for (CloudCost cost : costs) {

            costsByService
                    .computeIfAbsent(
                            cost.getService(),
                            key -> new ArrayList<>()
                    )
                    .add(cost);
        }

        // =====================================================
        // ANALYZE EACH SERVICE
        // =====================================================

        for (
                Map.Entry<String, List<CloudCost>> entry
                : costsByService.entrySet()
        ) {

            String service =
                    entry.getKey();

            List<CloudCost> serviceCosts =
                    entry.getValue();

            // Need at least 2 months
            if (serviceCosts.size() < 2) {
                continue;
            }

            // =================================================
            // SORT RECORDS IN MONTH ORDER
            // =================================================

            serviceCosts.sort(
                    Comparator.comparingInt(
                            cost ->
                                    getMonthOrder(
                                            cost.getMonth()
                                    )
                    )
            );

            // =================================================
            // COMPARE WITH PREVIOUS MONTH
            // =================================================

            for (
                    int i = 1;
                    i < serviceCosts.size();
                    i++
            ) {

                CloudCost previousCost =
                        serviceCosts.get(i - 1);

                CloudCost currentCost =
                        serviceCosts.get(i);

                double previousAmount =
                        previousCost.getCost();

                double currentAmount =
                        currentCost.getCost();

                // Prevent division by zero
                if (previousAmount <= 0) {
                    continue;
                }

                // Percentage increase
                double percentageIncrease =
                        (
                                (currentAmount - previousAmount)
                                        / previousAmount
                        ) * 100;

                // =================================================
                // ANOMALY THRESHOLD
                //
                // Detect increases of 15% or more
                // =================================================

                if (percentageIncrease < 15) {
                    continue;
                }

                // =================================================
                // DETERMINE SEVERITY
                // =================================================

                String severity;

                if (percentageIncrease >= 75) {

                    severity = "CRITICAL";

                } else if (percentageIncrease >= 40) {

                    severity = "HIGH";

                } else {

                    severity = "MEDIUM";
                }

                // =================================================
                // CREATE MESSAGE
                // =================================================

                String message =
                        service
                                + " cost increased from ₹"
                                + String.format(
                                        "%,.2f",
                                        previousAmount
                                )
                                + " in "
                                + previousCost.getMonth()
                                + " to ₹"
                                + String.format(
                                        "%,.2f",
                                        currentAmount
                                )
                                + " in "
                                + currentCost.getMonth()
                                + ". This is a "
                                + String.format(
                                        "%.1f",
                                        percentageIncrease
                                )
                                + "% increase.";

                // =================================================
                // ADD ANOMALY
                // =================================================

                anomalies.add(
                        new Anomaly(
                                service,
                                currentCost.getMonth(),
                                currentAmount,
                                previousAmount,
                                percentageIncrease,
                                severity,
                                message
                        )
                );
            }
        }

        // =====================================================
        // SORT BY HIGHEST DEVIATION
        // =====================================================

        anomalies.sort(
                Comparator.comparingDouble(
                        Anomaly::getPercentageDeviation
                ).reversed()
        );

        return anomalies;
    }

    // =========================================================
    // MONTH ORDER
    // =========================================================

    private int getMonthOrder(
            String month
    ) {

        if (month == null) {
            return 0;
        }

        return switch (
                month.trim().toLowerCase()
        ) {

            case "january" -> 1;

            case "february" -> 2;

            case "march" -> 3;

            case "april" -> 4;

            case "may" -> 5;

            case "june" -> 6;

            case "july" -> 7;

            case "august" -> 8;

            case "september" -> 9;

            case "october" -> 10;

            case "november" -> 11;

            case "december" -> 12;

            default -> 0;
        };
    }
}

