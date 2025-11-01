package com.example.budgetly_expense_tracker;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.example.budgetly_expense_tracker.database.BudgetlyDBOpenHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.example.budgetly_expense_tracker.adapter.BudgetAdapter;
import com.example.budgetly_expense_tracker.models.Budget;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddBudgetActivity extends AppCompatActivity implements BudgetAdapter.OnBudgetActionListener {

    private TextInputLayout textInputLayoutMonth, textInputLayoutBudgetLimit;
    private TextInputEditText editTextMonth, editTextBudgetLimit;
    private MaterialButton buttonSaveBudget;
    private RecyclerView recyclerViewBudgets;
    private TextView textViewNoBudgets;

    private BudgetlyDBOpenHelper dbHelper;
    private Calendar calendar;
    private BudgetAdapter budgetAdapter;
    private List<Budget> budgetList;

    private String currentUserId;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_budget);

        dbHelper = new BudgetlyDBOpenHelper(this);
        calendar = Calendar.getInstance();
        budgetList = new ArrayList<>();

        // <-- CHANGED: Initialize SessionManager and get the real User ID
        sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getUserId();

        // --- ADD THIS CHECK ---
        if (currentUserId == null) {
            Toast.makeText(this, "Session expired. Please log in.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(AddBudgetActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        // Toolbar setup
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish()); // Go back

        // Initialize views
        textInputLayoutMonth = findViewById(R.id.textInputLayoutMonth);
        editTextMonth = findViewById(R.id.editTextMonth);
        textInputLayoutBudgetLimit = findViewById(R.id.textInputLayoutBudgetLimit);
        editTextBudgetLimit = findViewById(R.id.editTextBudgetLimit);
        buttonSaveBudget = findViewById(R.id.buttonSaveBudget);
        recyclerViewBudgets = findViewById(R.id.recyclerViewBudgets);
        textViewNoBudgets = findViewById(R.id.textViewNoBudgets);

        // Set up Month Picker
        DatePickerDialog.OnDateSetListener monthSetListener = (view, year, monthOfYear, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            updateMonthInView();
        };

        editTextMonth.setOnClickListener(v -> new DatePickerDialog(AddBudgetActivity.this,
                monthSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH))
                .show());

        updateMonthInView();

        buttonSaveBudget.setOnClickListener(v -> saveBudget());

        setupBudgetRecyclerView();
        loadBudgets();
    }

    private void updateMonthInView() {
        String displayFormat = "MMM yyyy";
        SimpleDateFormat sdfDisplay = new SimpleDateFormat(displayFormat, Locale.getDefault());
        editTextMonth.setText(sdfDisplay.format(calendar.getTime()));
    }

    private void saveBudget() {
        String monthDisplay = editTextMonth.getText().toString().trim();
        String budgetLimitStr = editTextBudgetLimit.getText().toString().trim();

        if (TextUtils.isEmpty(monthDisplay)) {
            textInputLayoutMonth.setError("Month is required");
            return;
        } else {
            textInputLayoutMonth.setError(null);
        }

        if (TextUtils.isEmpty(budgetLimitStr)) {
            textInputLayoutBudgetLimit.setError("Budget limit is required");
            return;
        } else {
            textInputLayoutBudgetLimit.setError(null);
        }

        double limitAmount;
        try {
            limitAmount = Double.parseDouble(budgetLimitStr);
            if (limitAmount <= 0) {
                textInputLayoutBudgetLimit.setError("Limit must be greater than 0");
                return;
            } else {
                textInputLayoutBudgetLimit.setError(null);
            }
        } catch (NumberFormatException e) {
            textInputLayoutBudgetLimit.setError("Invalid amount");
            return;
        }

        String dbMonthFormat = "yyyy-MM";
        SimpleDateFormat sdfDb = new SimpleDateFormat(dbMonthFormat, Locale.getDefault());
        String monthForDb = sdfDb.format(calendar.getTime());

        String id = dbHelper.insertBudget(currentUserId, monthForDb, limitAmount);

        if (id != null && !id.isEmpty()) {
            Toast.makeText(this, "Budget saved successfully!", Toast.LENGTH_SHORT).show();
            editTextBudgetLimit.setText("");
            loadBudgets();
        } else {
            Toast.makeText(this, "Failed to save budget. You might already have a budget for this month.", Toast.LENGTH_LONG).show();
        }
    }

    private void setupBudgetRecyclerView() {
        recyclerViewBudgets.setLayoutManager(new LinearLayoutManager(this));
        budgetAdapter = new BudgetAdapter(this, budgetList, this);
        recyclerViewBudgets.setAdapter(budgetAdapter);
    }

    private void loadBudgets() {
        List<Budget> newBudgetList = dbHelper.getBudgetsByUser(currentUserId);
        budgetAdapter.updateBudgets(newBudgetList);

        if (newBudgetList.isEmpty()) {
            recyclerViewBudgets.setVisibility(View.GONE);
            textViewNoBudgets.setVisibility(View.VISIBLE);
        } else {
            recyclerViewBudgets.setVisibility(View.VISIBLE);
            textViewNoBudgets.setVisibility(View.GONE);
        }
    }


    @Override
    public void onDeleteClick(Budget budget) {
        String displayMonth;
        try {
            if (budget.getMonth() == null) {
                displayMonth = "this budget";
            } else {
                SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
                SimpleDateFormat displayFormat = new SimpleDateFormat("MMM yyyy", Locale.getDefault());
                Date date = dbFormat.parse(budget.getMonth());
                displayMonth = displayFormat.format(date);
            }
        } catch (Exception e) {
            e.printStackTrace();
            displayMonth = (budget.getMonth() != null) ? budget.getMonth() : "this budget";
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete Budget")
                .setMessage("Are you sure you want to delete the budget for " + displayMonth + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    int rowsDeleted = dbHelper.softDeleteBudget(budget.getId(), currentUserId);
                    if (rowsDeleted > 0) {
                        Toast.makeText(AddBudgetActivity.this, "Budget deleted", Toast.LENGTH_SHORT).show();
                        loadBudgets();
                    } else {
                        Toast.makeText(AddBudgetActivity.this, "Error: Could not delete budget.", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}