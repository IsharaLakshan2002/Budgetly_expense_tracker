package com.example.budgetly_expense_tracker.sync;

import android.content.Context;
import android.util.Log;
import com.example.budgetly_expense_tracker.api.*;
import com.example.budgetly_expense_tracker.api.models.*;
import com.example.budgetly_expense_tracker.database.BudgetlyDBOpenHelper;
import com.example.budgetly_expense_tracker.models.*;
import java.util.*;
import retrofit2.*;

public class SyncService {
    private static final String TAG = "SyncService";
    private final BudgetlyDBOpenHelper db;
    private final OrdsApiService api;

    public SyncService(Context ctx) {
        this.db = new BudgetlyDBOpenHelper(ctx);
        this.api = RetrofitClient.getApiService();
    }

    // ========== COMPLETE SYNC ==========
// ========== COMPLETE SYNC ==========
    public void syncAll(String userId) {
        Log.d(TAG, "Starting full sync for user: " + userId);

        // CRITICAL: Push user FIRST to ensure they exist in Oracle
        pushUser(userId);

        // Small delay to ensure user is created before pushing dependent data
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Then push all other data
        pushExpenses(userId);
        pushBudgets(userId);
        pushSavings(userId);

        // Finally pull updates
        pullUsers();
        pullExpenses(userId);
        pullBudgets(userId);
        pullSavings(userId);
    }

    // ========== USERS ==========
    private void pushUser(String userId) {
        Log.d(TAG, "Pushing user: " + userId);
        User user = db.getUserById(userId);

        if (user == null) {
            Log.e(TAG, "User not found in local DB: " + userId);
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("id", user.getId());
        body.put("name", user.getName());
        body.put("email", user.getEmail());
        body.put("password", user.getPassword());

        api.createUser(body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> resp) {
                if (resp.isSuccessful() || resp.code() == 409) {
                    // 409 means user already exists, which is fine
                    Log.d(TAG, "✓ User synced: " + user.getId());
                } else {
                    Log.e(TAG, "✗ Push user failed: " + resp.code());
                    try {
                        if (resp.errorBody() != null) {
                            Log.e(TAG, "Error: " + resp.errorBody().string());
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Push user error", t);
            }
        });
    }


    private void pullUsers() {
        api.getUsers().enqueue(new Callback<OrdsItemsWrapper<UserResponse>>() {
            @Override
            public void onResponse(Call<OrdsItemsWrapper<UserResponse>> call, Response<OrdsItemsWrapper<UserResponse>> resp) {
                if (resp.isSuccessful() && resp.body() != null) {
                    Log.d(TAG, "Pulled " + resp.body().getItems().size() + " users");
                    for (UserResponse u : resp.body().getItems()) {
                        // Optionally sync user data to local DB
                        // db.upsertUserFromServer(u);
                    }
                }
            }
            @Override
            public void onFailure(Call<OrdsItemsWrapper<UserResponse>> call, Throwable t) {
                Log.e(TAG, "Pull users error", t);
            }
        });
    }

    // ========== EXPENSES ==========
    private void pushExpenses(String userId) {
        List<Expense> unsynced = db.getUnsyncedExpenses(userId);
        Log.d(TAG, "Pushing " + unsynced.size() + " expenses...");

        for (Expense e : unsynced) {
            Map<String, Object> body = new HashMap<>();
            body.put("id", e.getId());
            body.put("user_id", e.getUserId());
            body.put("expense_date", e.getDate());
            body.put("category", e.getCategory());
            body.put("amount", e.getAmount());
            body.put("notes", e.getNotes());

            api.createExpense(body).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> resp) {
                    if (resp.isSuccessful()) {
                        db.markExpenseSynced(e.getId());
                        Log.d(TAG, "✓ Pushed expense " + e.getId());
                    } else {
                        Log.e(TAG, "✗ Push expense failed: " + resp.code());
                        try {
                            if (resp.errorBody() != null) {
                                Log.e(TAG, "Error: " + resp.errorBody().string());
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e(TAG, "Push expense error", t);
                }
            });
        }
    }

    private void pullExpenses(String userId) {
        api.getExpenses().enqueue(new Callback<OrdsItemsWrapper<ExpenseResponse>>() {
            @Override
            public void onResponse(Call<OrdsItemsWrapper<ExpenseResponse>> call, Response<OrdsItemsWrapper<ExpenseResponse>> resp) {
                if (resp.isSuccessful() && resp.body() != null) {
                    int count = 0;
                    for (ExpenseResponse e : resp.body().getItems()) {
                        if (e.getUserId().equals(userId)) {
                            db.upsertExpenseFromServer(e);
                            count++;
                        }
                    }
                    Log.d(TAG, "✓ Pulled " + count + " expenses for user");
                } else {
                    Log.e(TAG, "Pull expenses failed: " + resp.code());
                }
            }
            @Override
            public void onFailure(Call<OrdsItemsWrapper<ExpenseResponse>> call, Throwable t) {
                Log.e(TAG, "Pull expenses error", t);
            }
        });
    }

    // ========== BUDGETS ==========
    private void pushBudgets(String userId) {
        List<Budget> unsynced = db.getUnsyncedBudgets(userId);
        Log.d(TAG, "Pushing " + unsynced.size() + " budgets...");

        for (Budget b : unsynced) {
            Map<String, Object> body = new HashMap<>();
            body.put("id", b.getId());
            body.put("user_id", userId);
            body.put("month", b.getMonth());
            body.put("limit_amount", b.getLimitAmount());

            api.createBudget(body).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> resp) {
                    if (resp.isSuccessful()) {
                        db.markBudgetSynced(b.getId());
                        Log.d(TAG, "✓ Pushed budget " + b.getId());
                    } else {
                        Log.e(TAG, "✗ Push budget failed: " + resp.code());
                        try {
                            if (resp.errorBody() != null) {
                                Log.e(TAG, "Error: " + resp.errorBody().string());
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e(TAG, "Push budget error", t);
                }
            });
        }
    }

    private void pullBudgets(String userId) {
        api.getBudgets().enqueue(new Callback<OrdsItemsWrapper<BudgetResponse>>() {
            @Override
            public void onResponse(Call<OrdsItemsWrapper<BudgetResponse>> call, Response<OrdsItemsWrapper<BudgetResponse>> resp) {
                if (resp.isSuccessful() && resp.body() != null) {
                    int count = 0;
                    for (BudgetResponse b : resp.body().getItems()) {
                        if (b.getUserId().equals(userId)) {
                            db.upsertBudgetFromServer(b);
                            count++;
                        }
                    }
                    Log.d(TAG, "✓ Pulled " + count + " budgets for user");
                } else {
                    Log.e(TAG, "Pull budgets failed: " + resp.code());
                }
            }
            @Override
            public void onFailure(Call<OrdsItemsWrapper<BudgetResponse>> call, Throwable t) {
                Log.e(TAG, "Pull budgets error", t);
            }
        });
    }

    // ========== SAVINGS ==========
    private void pushSavings(String userId) {
        List<Saving> unsynced = db.getUnsyncedSavings(userId);
        Log.d(TAG, "Pushing " + unsynced.size() + " savings...");

        for (Saving s : unsynced) {
            Map<String, Object> body = new HashMap<>();
            body.put("id", s.getId());
            body.put("user_id", userId);
            body.put("goal_name", s.getGoalName());
            body.put("target_amount", s.getTargetAmount());
            body.put("current_amount", s.getCurrentAmount());

            api.createSaving(body).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> resp) {
                    if (resp.isSuccessful()) {
                        db.markSavingSynced(s.getId());
                        Log.d(TAG, "✓ Pushed saving " + s.getId());
                    } else {
                        Log.e(TAG, "✗ Push saving failed: " + resp.code());
                        try {
                            if (resp.errorBody() != null) {
                                Log.e(TAG, "Error: " + resp.errorBody().string());
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e(TAG, "Push saving error", t);
                }
            });
        }
    }

    private void pullSavings(String userId) {
        api.getSavings().enqueue(new Callback<OrdsItemsWrapper<SavingResponse>>() {
            @Override
            public void onResponse(Call<OrdsItemsWrapper<SavingResponse>> call, Response<OrdsItemsWrapper<SavingResponse>> resp) {
                if (resp.isSuccessful() && resp.body() != null) {
                    int count = 0;
                    for (SavingResponse s : resp.body().getItems()) {
                        if (s.getUserId().equals(userId)) {
                            db.upsertSavingFromServer(s);
                            count++;
                        }
                    }
                    Log.d(TAG, "✓ Pulled " + count + " savings for user");
                } else {
                    Log.e(TAG, "Pull savings failed: " + resp.code());
                }
            }
            @Override
            public void onFailure(Call<OrdsItemsWrapper<SavingResponse>> call, Throwable t) {
                Log.e(TAG, "Pull savings error", t);
            }
        });
    }
}
