package com.example.budgetly_expense_tracker.models;

public class Saving {
    private String id;
    private String goalName;
    private double targetAmount;
    private double currentAmount;
    private int syncStatus;

    // --- Getters and Setters ---
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getGoalName() {
        return goalName;
    }
    public void setGoalName(String goalName) {
        this.goalName = goalName;
    }
    public double getTargetAmount() {
        return targetAmount;
    }
    public void setTargetAmount(double targetAmount) {
        this.targetAmount = targetAmount;
    }
    public double getCurrentAmount() {
        return currentAmount;
    }
    public void setCurrentAmount(double currentAmount) {
        this.currentAmount = currentAmount;
    }

    // 2. Add Getter and Setter for syncStatus
    public int getSyncStatus() {
        return syncStatus;
    }
    public void setSyncStatus(int syncStatus) {
        this.syncStatus = syncStatus;
    }
}