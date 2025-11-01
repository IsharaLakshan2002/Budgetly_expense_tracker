package com.example.budgetly_expense_tracker.sync;

import android.content.Context;
import android.util.Log;
import androidx.work.*;
import java.util.concurrent.TimeUnit;

public class SyncScheduler {
    private static final String TAG = "SyncScheduler";
    private static final String SYNC_WORK_NAME = "BudgetlyPeriodicSync";

    /**
     * Schedule periodic sync every 15 minutes (minimum allowed by WorkManager)
     * Syncs ALL data: users, expenses, budgets, and savings
     * Only runs when device has network connection
     */
    public static void schedulePeriodicSync(Context context) {
        // Constraints: Only run when connected to network
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        // Create periodic work request (15 minutes is minimum)
        PeriodicWorkRequest syncRequest = new PeriodicWorkRequest.Builder(
                BudgetlySyncWorker.class, // <-- Changed from ExpenseSyncWorker
                15, TimeUnit.MINUTES
        )
                .setConstraints(constraints)
                .setBackoffCriteria(
                        BackoffPolicy.LINEAR,
                        PeriodicWorkRequest.MIN_BACKOFF_MILLIS,
                        TimeUnit.MILLISECONDS
                )
                .build();

        // Enqueue unique work (replaces existing if already scheduled)
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                SYNC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
        );

        Log.d(TAG, "✓ Periodic sync scheduled (every 15 minutes) - syncs all data");
    }

    /**
     * Cancel all scheduled syncs
     */
    public static void cancelPeriodicSync(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(SYNC_WORK_NAME);
        Log.d(TAG, "Periodic sync cancelled");
    }

    /**
     * Trigger immediate one-time sync for all data
     */
    public static void syncNow(Context context, String userId) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest syncRequest = new OneTimeWorkRequest.Builder(BudgetlySyncWorker.class) // <-- Changed
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(context).enqueue(syncRequest);
        Log.d(TAG, "Immediate sync triggered (all data)");
    }
}
