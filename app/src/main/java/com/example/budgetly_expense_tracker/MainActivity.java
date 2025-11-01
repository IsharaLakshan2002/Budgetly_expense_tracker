package com.example.budgetly_expense_tracker;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import com.example.budgetly_expense_tracker.sync.SyncScheduler;
import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);

        // Check if user is logged in. If not, redirect to Login.
        if (sessionManager.getUserId() == null) {
            goToLogin();
            return;
        }

        MaterialButton btnAddExpense = findViewById(R.id.btnAddExpense);
        MaterialButton btnViewExpenses = findViewById(R.id.btnViewExpenses);
        MaterialButton btnAddBudget = findViewById(R.id.btnAddBudget);
        MaterialButton btnAddSavings = findViewById(R.id.btnAddSavings);
        MaterialButton btnLogout = findViewById(R.id.btnLogout);
        MaterialButton btnSync = findViewById(R.id.btnSync);

        btnAddExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AddExpenseActivity.class));
            }
        });

        btnViewExpenses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ViewExpensesActivity.class));
            }
        });

        btnAddBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AddBudgetActivity.class));
            }
        });

        btnAddSavings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AddSavingsActivity.class));
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Cancel periodic sync on logout
                SyncScheduler.cancelPeriodicSync(MainActivity.this);

                // Clear session
                sessionManager.clearSession();
                Toast.makeText(MainActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
                goToLogin();
            }
        });

        // --- SYNC BUTTON LOGIC ---
        btnSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userId = sessionManager.getUserId();

                if (userId == null) {
                    Toast.makeText(MainActivity.this, "Please login first", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Disable button during sync
                btnSync.setEnabled(false);

                // Rotate animation
                v.animate().rotation(360f).setDuration(1000).start();

                Toast.makeText(MainActivity.this, "Syncing with server...", Toast.LENGTH_SHORT).show();

                // Trigger immediate sync using WorkManager
                SyncScheduler.syncNow(MainActivity.this, userId);

                // Re-enable after animation
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        v.setRotation(0f);
                        btnSync.setEnabled(true);
                        Toast.makeText(MainActivity.this, "✓ Sync triggered!", Toast.LENGTH_SHORT).show();
                    }
                }, 1500);
            }
        });
    }

    private void goToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
