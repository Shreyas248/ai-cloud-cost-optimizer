package com.cloudoptimizer.backend.dto;

public class Recommendation {

    private String service;
    private String priority;
    private String title;
    private String description;
    private double currentCost;
    private double estimatedSavings;
    private String action;

    public Recommendation() {
    }

    public Recommendation(
            String service,
            String priority,
            String title,
            String description,
            double currentCost,
            double estimatedSavings,
            String action
    ) {
        this.service = service;
        this.priority = priority;
        this.title = title;
        this.description = description;
        this.currentCost = currentCost;
        this.estimatedSavings = estimatedSavings;
        this.action = action;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getCurrentCost() {
        return currentCost;
    }

    public void setCurrentCost(double currentCost) {
        this.currentCost = currentCost;
    }

    public double getEstimatedSavings() {
        return estimatedSavings;
    }

    public void setEstimatedSavings(double estimatedSavings) {
        this.estimatedSavings = estimatedSavings;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}