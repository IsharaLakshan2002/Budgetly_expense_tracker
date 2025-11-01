package com.example.budgetly_expense_tracker.api.models;

import com.google.gson.annotations.SerializedName;

public class SavingResponse {
    @SerializedName("id") private String id;
    @SerializedName("user_id") private String userId;
    @SerializedName("goal_name") private String goalName;
    @SerializedName("target_amount") private double targetAmount;
    @SerializedName("current_amount") private double currentAmount;
    @SerializedName("sync_status") private int syncStatus;
    @SerializedName("is_deleted") private int isDeleted;
    @SerializedName("last_modified") private String lastModified;

    // Getters and setters
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getGoalName() { return goalName; }
    public double getTargetAmount() { return targetAmount; }
    public double getCurrentAmount() { return currentAmount; }
    public int getSyncStatus() { return syncStatus; }
    public int getIsDeleted() { return isDeleted; }
    public String getLastModified() { return lastModified; }
}
