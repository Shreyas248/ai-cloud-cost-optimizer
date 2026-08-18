package com.cloudoptimizer.backend.model;

public class Anomaly {

    private String service;

    private String month;

    private double actualCost;

    private double averageCost;

    private double percentageDeviation;

    private String severity;

    private String message;


    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public Anomaly(
            String service,
            String month,
            double actualCost,
            double averageCost,
            double percentageDeviation,
            String severity,
            String message
    ) {

        this.service = service;
        this.month = month;
        this.actualCost = actualCost;
        this.averageCost = averageCost;
        this.percentageDeviation = percentageDeviation;
        this.severity = severity;
        this.message = message;
    }


    // ==========================================
    // GETTERS
    // ==========================================

    public String getService() {
        return service;
    }


    public String getMonth() {
        return month;
    }


    public double getActualCost() {
        return actualCost;
    }


    public double getAverageCost() {
        return averageCost;
    }


    public double getPercentageDeviation() {
        return percentageDeviation;
    }


    public String getSeverity() {
        return severity;
    }


    public String getMessage() {
        return message;
    }

}

