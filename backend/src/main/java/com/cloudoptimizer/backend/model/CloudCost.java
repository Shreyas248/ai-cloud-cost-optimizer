package com.cloudoptimizer.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "cloud_costs")
public class CloudCost {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;


    // ==========================================
    // USER
    // ==========================================

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;


    // ==========================================
    // CLOUD COST DATA
    // ==========================================

    @Column(nullable = false)
    private String service;

    @Column(nullable = false)
    private String month;

    @Column(nullable = false)
    private Double cost;


    // ==========================================
    // CONSTRUCTORS
    // ==========================================

    public CloudCost() {
    }


    public CloudCost(
            User user,
            String service,
            String month,
            Double cost
    ) {

        this.user = user;
        this.service = service;
        this.month = month;
        this.cost = cost;

    }


    // ==========================================
    // GETTERS AND SETTERS
    // ==========================================

    public Long getId() {
        return id;
    }


    public User getUser() {
        return user;
    }


    public void setUser(
            User user
    ) {
        this.user = user;
    }


    public String getService() {
        return service;
    }


    public void setService(
            String service
    ) {
        this.service = service;
    }


    public String getMonth() {
        return month;
    }


    public void setMonth(
            String month
    ) {
        this.month = month;
    }


    public Double getCost() {
        return cost;
    }


    public void setCost(
            Double cost
    ) {
        this.cost = cost;
    }

}