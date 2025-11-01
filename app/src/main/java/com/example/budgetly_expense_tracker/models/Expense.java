package com.example.budgetly_expense_tracker.models;

public class Expense {
    private String id;
    private String date;
    private String category;
    private double amount;
    private String notes;
    private int syncStatus;
    private String userId;

    // --- Getters and Setters ---
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }
    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }
    public double getAmount() {
        return amount;
    }
    public void setAmount(double amount) {
        this.amount = amount;
    }
    public String getNotes() {
        return notes;
    }
    public void setNotes(String notes) {
        this.notes = notes;
    }
    public int getSyncStatus() {
        return syncStatus;
    }
    public void setSyncStatus(int syncStatus) {
        this.syncStatus = syncStatus;
    }

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
}