package com.cloudoptimizer.backend.service;

import com.cloudoptimizer.backend.model.CloudCost;
import com.cloudoptimizer.backend.repository.CloudCostRepository;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CloudCostService {

    private final CloudCostRepository repository;

    public CloudCostService(
            CloudCostRepository repository
    ) {
        this.repository = repository;
    }

    // =========================================================
    // GET ALL COSTS
    // =========================================================

    public List<CloudCost> getAllCosts(Long userId) {

        return repository.findByUserId(userId);
    }

    // =========================================================
    // GET BY SERVICE
    // =========================================================

    public List<CloudCost> getByService(
            Long userId,
            String service
    ) {

        return repository.findByUserIdAndService(
                userId,
                service
        );
    }

    // =========================================================
    // GET BY MONTH
    // =========================================================

    public List<CloudCost> getByMonth(
            Long userId,
            String month
    ) {

        return repository.findByUserIdAndMonth(
                userId,
                month
        );
    }

    // =========================================================
    // TOTAL COST
    // =========================================================

    public double getTotalCost(Long userId) {

        return repository.findByUserId(userId)
                .stream()
                .mapToDouble(CloudCost::getCost)
                .sum();
    }

    // =========================================================
    // COST BY SERVICE
    // =========================================================

    public List<Map<String, Object>> getCostByService(
            Long userId
    ) {

        List<CloudCost> costs =
                repository.findByUserId(userId);

        Map<String, Double> serviceTotals =
                new HashMap<>();

        for (CloudCost cost : costs) {

            serviceTotals.merge(
                    cost.getService(),
                    cost.getCost(),
                    Double::sum
            );
        }

        List<Map<String, Object>> result =
                new ArrayList<>();

        for (Map.Entry<String, Double> entry :
                serviceTotals.entrySet()) {

            result.add(
                    Map.of(
                            "service",
                            entry.getKey(),

                            "cost",
                            entry.getValue()
                    )
            );
        }

        return result;
    }

    // =========================================================
    // COST BY MONTH
    // =========================================================

    public List<Map<String, Object>> getCostByMonth(
            Long userId
    ) {

        List<CloudCost> costs =
                repository.findByUserId(userId);

        Map<String, Double> monthTotals =
                new HashMap<>();

        for (CloudCost cost : costs) {

            monthTotals.merge(
                    cost.getMonth(),
                    cost.getCost(),
                    Double::sum
            );
        }

        List<Map<String, Object>> result =
                new ArrayList<>();

        for (Map.Entry<String, Double> entry :
                monthTotals.entrySet()) {

            result.add(
                    Map.of(
                            "month",
                            entry.getKey(),

                            "cost",
                            entry.getValue()
                    )
            );
        }

        return result;
    }

    // =========================================================
    // SUMMARY
    // =========================================================

    public Map<String, Object> getSummary(
            Long userId
    ) {

        List<CloudCost> costs =
                repository.findByUserId(userId);

        double totalCost = 0;

        String highestCostService = null;

        double highestServiceCost = 0;

        Map<String, Double> serviceTotals =
                new HashMap<>();

        for (CloudCost cost : costs) {

            totalCost += cost.getCost();

            serviceTotals.merge(
                    cost.getService(),
                    cost.getCost(),
                    Double::sum
            );
        }

        for (Map.Entry<String, Double> entry :
                serviceTotals.entrySet()) {

            if (entry.getValue() >
                    highestServiceCost) {

                highestServiceCost =
                        entry.getValue();

                highestCostService =
                        entry.getKey();
            }
        }

        return Map.of(
                "totalCost",
                totalCost,

                "highestCostService",
                highestCostService == null
                        ? ""
                        : highestCostService,

                "highestServiceCost",
                highestServiceCost,

                "serviceCount",
                serviceTotals.size()
        );
    }
}