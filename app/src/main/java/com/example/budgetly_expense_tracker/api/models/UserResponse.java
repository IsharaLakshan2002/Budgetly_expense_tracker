package com.example.budgetly_expense_tracker.api.models;

import com.google.gson.annotations.SerializedName;

public class UserResponse {
    @SerializedName("id") private String id;
    @SerializedName("name") private String name;
    @SerializedName("email") private String email;
    @SerializedName("sync_status") private int syncStatus;
    @SerializedName("is_deleted") private int isDeleted;
    @SerializedName("last_modified") private String lastModified;

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public int getSyncStatus() { return syncStatus; }
    public int getIsDeleted() { return isDeleted; }
    public String getLastModified() { return lastModified; }
}
