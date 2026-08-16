package com.cloudoptimizer.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "cloud_costs")
public class CloudCost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String service;

    @Column(nullable = false)
    private String month;

    @Column(nullable = false)
    private Double cost;

    public CloudCost() {
    }

    public CloudCost(
            String service,
            String month,
            Double cost
    ) {
        this.service = service;
        this.month = month;
        this.cost = cost;
    }

    public Long getId() {
        return id;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public Double getCost() {
        return cost;
    }

    public void setCost(Double cost) {
        this.cost = cost;
    }
}