package com.cloudoptimizer.backend.repository;

import com.cloudoptimizer.backend.model.CloudCost;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CloudCostRepository
        extends JpaRepository<CloudCost, Long> {

    // ==========================================
    // USER COSTS
    // ==========================================

    List<CloudCost> findByUserId(
            Long userId
    );


    // ==========================================
    // SERVICE
    // ==========================================

    List<CloudCost> findByUserIdAndService(
            Long userId,
            String service
    );


    // ==========================================
    // MONTH
    // ==========================================

    List<CloudCost> findByUserIdAndMonth(
            Long userId,
            String month
    );


    // ==========================================
    // DELETE
    // ==========================================

    void deleteByUserIdAndServiceAndMonth(
            Long userId,
            String service,
            String month
    );
}