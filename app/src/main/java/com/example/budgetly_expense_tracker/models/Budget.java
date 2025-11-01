package com.example.budgetly_expense_tracker.models;

public class Budget {
    private String id;
    private String month; // e.g., "2025-10"
    private double limitAmount;

    // 1. Add the syncStatus field
    private int syncStatus;

    // --- Getters and Setters ---
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getMonth() {
        return month;
    }
    public void setMonth(String month) {
        this.month = month;
    }
    public double getLimitAmount() {
        return limitAmount;
    }
    public void setLimitAmount(double limitAmount) {
        this.limitAmount = limitAmount;
    }

    // 2. Add Getter and Setter for syncStatus
    public int getSyncStatus() {
        return syncStatus;
    }
    public void setSyncStatus(int syncStatus) {
        this.syncStatus = syncStatus;
    }
}