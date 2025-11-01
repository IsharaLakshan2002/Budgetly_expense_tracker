package com.example.budgetly_expense_tracker;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.budgetly_expense_tracker.adapter.SavingAdapter;
import com.example.budgetly_expense_tracker.database.BudgetlyDBOpenHelper;
import com.example.budgetly_expense_tracker.models.Saving;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

public class AddSavingsActivity extends AppCompatActivity implements SavingAdapter.OnSavingActionListener {

    private TextInputLayout textInputLayoutGoalName, textInputLayoutTargetAmount, textInputLayoutCurrentAmount;
    private TextInputEditText editTextGoalName, editTextTargetAmount, editTextCurrentAmount;
    private MaterialButton buttonSaveSaving;
    private RecyclerView recyclerViewSavings;
    private TextView textViewNoSavings;
    private MaterialToolbar toolbar;

    private BudgetlyDBOpenHelper dbHelper;
    private SavingAdapter savingAdapter;
    private List<Saving> savingList;

    private String currentUserId;
    private SessionManager sessionManager;

    // --- ADDED FOR EDIT MODE ---
    private boolean isEditMode = false;
    private Saving existingSaving = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_savings);

        dbHelper = new BudgetlyDBOpenHelper(this);
        savingList = new ArrayList<>();

        sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getUserId();

        if (currentUserId == null) {
            Toast.makeText(this, "Session expired. Please log in.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(AddSavingsActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        // Toolbar setup
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish()); // Go back

        // Initialize views
        textInputLayoutGoalName = findViewById(R.id.textInputLayoutGoalName);
        editTextGoalName = findViewById(R.id.editTextGoalName);
        textInputLayoutTargetAmount = findViewById(R.id.textInputLayoutTargetAmount);
        editTextTargetAmount = findViewById(R.id.editTextTargetAmount);
        textInputLayoutCurrentAmount = findViewById(R.id.textInputLayoutCurrentAmount);
        editTextCurrentAmount = findViewById(R.id.editTextCurrentAmount);
        buttonSaveSaving = findViewById(R.id.buttonSaveSaving);
        recyclerViewSavings = findViewById(R.id.recyclerViewSavings);
        textViewNoSavings = findViewById(R.id.textViewNoSavings);

        buttonSaveSaving.setOnClickListener(v -> saveSavingGoal());

        setupRecyclerView();
        loadSavings();
    }

    private void saveSavingGoal() {
        String goalName;
        if (isEditMode) {
            goalName = existingSaving.getGoalName();
        } else {
            goalName = editTextGoalName.getText().toString().trim();
        }

        String targetAmountStr = editTextTargetAmount.getText().toString().trim();
        String currentAmountStr = editTextCurrentAmount.getText().toString().trim();

        if (TextUtils.isEmpty(goalName)) {
            textInputLayoutGoalName.setError("Goal name is required");
            return;
        } else {
            textInputLayoutGoalName.setError(null);
        }

        if (TextUtils.isEmpty(targetAmountStr)) {
            textInputLayoutTargetAmount.setError("Target amount is required");
            return;
        } else {
            textInputLayoutTargetAmount.setError(null);
        }

        if (TextUtils.isEmpty(currentAmountStr)) {
            currentAmountStr = "0";
        }

        double targetAmount;
        double currentAmount;

        try {
            targetAmount = Double.parseDouble(targetAmountStr);
            if (targetAmount <= 0) {
                textInputLayoutTargetAmount.setError("Target must be greater than 0");
                return;
            } else {
                textInputLayoutTargetAmount.setError(null);
            }
        } catch (NumberFormatException e) {
            textInputLayoutTargetAmount.setError("Invalid amount");
            return;
        }

        try {
            currentAmount = Double.parseDouble(currentAmountStr);
            if (currentAmount < 0) {
                textInputLayoutCurrentAmount.setError("Cannot be negative");
                return;
            } else {
                textInputLayoutCurrentAmount.setError(null);
            }
        } catch (NumberFormatException e) {
            textInputLayoutCurrentAmount.setError("Invalid amount");
            return;
        }

        if (currentAmount > targetAmount) {
            textInputLayoutCurrentAmount.setError("Current amount cannot be greater than target");
            return;
        } else {
            textInputLayoutCurrentAmount.setError(null);
        }

        // --- MODIFIED SAVE/UPDATE LOGIC ---
        if (isEditMode) {
            // UPDATE logic
            existingSaving.setTargetAmount(targetAmount);
            existingSaving.setCurrentAmount(currentAmount);
            // Note: We don't change the goal name, as it's our unique key

            int rowsAffected = dbHelper.updateSaving(existingSaving, currentUserId);
            if (rowsAffected > 0) {
                Toast.makeText(this, "Goal updated!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to update goal.", Toast.LENGTH_LONG).show();
            }

        } else {
            // INSERT logic (original)
            String id = dbHelper.insertSaving(currentUserId, goalName, targetAmount, currentAmount);
            if (id != null && !id.isEmpty()) {
                Toast.makeText(this, "Savings goal saved!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to save goal. (Goal name might already exist)", Toast.LENGTH_LONG).show();
            }
        }

        // --- Clear form and reset to "Add" mode ---
        clearForm();
        loadSavings();
    }

    private void clearForm() {
        editTextGoalName.setText("");
        editTextTargetAmount.setText("");
        editTextCurrentAmount.setText("");

        // Clear errors
        textInputLayoutGoalName.setError(null);
        textInputLayoutTargetAmount.setError(null);
        textInputLayoutCurrentAmount.setError(null);

        // Reset state
        isEditMode = false;
        existingSaving = null;
        toolbar.setTitle("Set Savings Goal");
        buttonSaveSaving.setText("Save Savings Goal");
        buttonSaveSaving.setIconResource(R.drawable.ic_save);

        // Re-enable goal name field
        textInputLayoutGoalName.setEnabled(true);
        editTextGoalName.setEnabled(true);
    }

    private void setupRecyclerView() {
        recyclerViewSavings.setLayoutManager(new LinearLayoutManager(this));
        // Pass 'this' as the listener
        savingAdapter = new SavingAdapter(this, savingList, this);
        recyclerViewSavings.setAdapter(savingAdapter);
    }

    private void loadSavings() {
        savingList.clear();
        savingList.addAll(dbHelper.getSavingsByUser(currentUserId));
        savingAdapter.notifyDataSetChanged();

        if (savingList.isEmpty()) {
            recyclerViewSavings.setVisibility(View.GONE);
            textViewNoSavings.setVisibility(View.VISIBLE);
        } else {
            recyclerViewSavings.setVisibility(View.VISIBLE);
            textViewNoSavings.setVisibility(View.GONE);
        }
    }
    @Override
    public void onEditClick(Saving saving) {
        isEditMode = true;
        existingSaving = saving;

        editTextGoalName.setText(saving.getGoalName());
        editTextTargetAmount.setText(String.valueOf(saving.getTargetAmount()));
        editTextCurrentAmount.setText(String.valueOf(saving.getCurrentAmount()));

        // Update UI to "Edit" state
        toolbar.setTitle("Edit Savings Goal");
        buttonSaveSaving.setText("Update Goal");
        buttonSaveSaving.setIconResource(R.drawable.ic_edit); // Change icon

        textInputLayoutGoalName.setEnabled(false);
        editTextGoalName.setEnabled(false);

    }

    @Override
    public void onDeleteClick(Saving saving) {
        if (isEditMode) {
            Toast.makeText(this, "Please finish editing first", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete Goal")
                .setMessage("Are you sure you want to delete the goal: '" + saving.getGoalName() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    int rowsDeleted = dbHelper.softDeleteSaving(saving.getId(), currentUserId);

                    if (rowsDeleted > 0) {
                        Toast.makeText(AddSavingsActivity.this, "Goal deleted", Toast.LENGTH_SHORT).show();
                        loadSavings();
                    } else {
                        Toast.makeText(AddSavingsActivity.this, "Error: Could not delete goal.", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}