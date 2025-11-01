package com.example.budgetly_expense_tracker.sync;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.example.budgetly_expense_tracker.SessionManager;

public class BudgetlySyncWorker extends Worker {
    private static final String TAG = "BudgetlySyncWorker";

    public BudgetlySyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Background sync started - syncing all data (users, expenses, budgets, savings)");

        // Get current user ID from session
        SessionManager sessionManager = new SessionManager(getApplicationContext());
        String userId = sessionManager.getUserId();

        if (userId == null) {
            Log.w(TAG, "No user logged in, skipping sync");
            return Result.success();
        }

        try {
            // Perform complete sync for ALL tables
            SyncService syncService = new SyncService(getApplicationContext());
            syncService.syncAll(userId);

            Log.d(TAG, "✓ Background sync completed successfully (all data synced)");
            return Result.success();

        } catch (Exception e) {
            Log.e(TAG, "✗ Background sync failed", e);
            return Result.retry(); // Retry on failure
        }
    }
}
