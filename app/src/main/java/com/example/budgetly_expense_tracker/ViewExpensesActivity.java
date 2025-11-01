package com.example.budgetly_expense_tracker;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.budgetly_expense_tracker.adapter.ExpenseAdapter;
import com.example.budgetly_expense_tracker.database.BudgetlyDBOpenHelper;
import com.example.budgetly_expense_tracker.models.Expense;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

public class ViewExpensesActivity extends AppCompatActivity implements ExpenseAdapter.OnItemActionListener {

    private RecyclerView recyclerViewExpenses;
    private TextView textViewNoExpenses;
    private ExpenseAdapter expenseAdapter;
    private List<Expense> expenseList;
    private BudgetlyDBOpenHelper dbHelper;

    private String currentUserId;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_expense);

        // <-- CHANGED: Initialize SessionManager and get the real User ID
        sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getUserId();

        // --- ADD THIS CHECK ---
        if (currentUserId == null) {
            Toast.makeText(this, "Session expired. Please log in.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(ViewExpensesActivity.this, LoginActivity.class);
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

        recyclerViewExpenses = findViewById(R.id.recyclerViewExpenses);
        textViewNoExpenses = findViewById(R.id.textViewNoExpenses);
        dbHelper = new BudgetlyDBOpenHelper(this);
        expenseList = new ArrayList<>();

        setupRecyclerView();
        loadExpenses();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check if user is still logged in when returning to the activity
        if (currentUserId != null) {
            loadExpenses(); // Refresh expenses
        }
    }

    private void setupRecyclerView() {
        recyclerViewExpenses.setLayoutManager(new LinearLayoutManager(this));
        expenseAdapter = new ExpenseAdapter(this, expenseList, this); // Pass 'this' as listener
        recyclerViewExpenses.setAdapter(expenseAdapter);
    }

    private void loadExpenses() {
        expenseList.clear();
        expenseList.addAll(dbHelper.getExpensesByUser(currentUserId));
        expenseAdapter.notifyDataSetChanged();

        if (expenseList.isEmpty()) {
            recyclerViewExpenses.setVisibility(View.GONE);
            textViewNoExpenses.setVisibility(View.VISIBLE);
        } else {
            recyclerViewExpenses.setVisibility(View.VISIBLE);
            textViewNoExpenses.setVisibility(View.GONE);
        }
    }

    @Override
    public void onEditClick(Expense expense) {
//        Toast.makeText(this, "Edit functionality not yet implemented.", Toast.LENGTH_SHORT).show();
         Intent intent = new Intent(ViewExpensesActivity.this, AddExpenseActivity.class);
         intent.putExtra("EXPENSE_ID", expense.getId()); // expense.getId() is now a String
         startActivity(intent);
    }

    @Override
    public void onDeleteClick(Expense expense) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Expense")
                .setMessage("Are you sure you want to delete this expense?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    int rowsDeleted = dbHelper.softDeleteExpense(expense.getId(), currentUserId);
                    if (rowsDeleted > 0) {
                        Toast.makeText(ViewExpensesActivity.this, "Expense deleted", Toast.LENGTH_SHORT).show();
                        loadExpenses();
                    } else {
                        Toast.makeText(ViewExpensesActivity.this, "Error: Could not delete expense.", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}