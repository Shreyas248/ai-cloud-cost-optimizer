package com.cloudoptimizer.backend.repository;

import com.cloudoptimizer.backend.model.CloudCost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CloudCostRepository
        extends JpaRepository<CloudCost, Long> {

    List<CloudCost> findByService(String service);

    List<CloudCost> findByMonth(String month);
}