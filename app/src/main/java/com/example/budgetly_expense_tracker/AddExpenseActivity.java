package com.example.budgetly_expense_tracker;

import androidx.appcompat.app.AppCompatActivity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.Toast;

import com.example.budgetly_expense_tracker.database.BudgetlyDBOpenHelper;
import com.example.budgetly_expense_tracker.models.Expense; // <-- ADD THIS IMPORT
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.ParseException; // <-- ADD THIS IMPORT
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddExpenseActivity extends AppCompatActivity {

    private TextInputLayout textInputLayoutDate, textInputLayoutCategory, textInputLayoutAmount, textInputLayoutNotes;
    private TextInputEditText editTextDate, editTextAmount, editTextNotes;
    private AutoCompleteTextView autoCompleteCategory;
    private MaterialButton buttonSaveExpense;
    private MaterialToolbar toolbar; // <-- MAKE TOOLBAR A CLASS VARIABLE

    private BudgetlyDBOpenHelper dbHelper;
    private Calendar calendar;

    private String currentUserId;
    private SessionManager sessionManager;

    // --- ADD THESE VARIABLES FOR EDIT MODE ---
    private boolean isEditMode = false;
    private String existingExpenseId = null;
    private Expense existingExpense = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense_activty);

        dbHelper = new BudgetlyDBOpenHelper(this);
        calendar = Calendar.getInstance();

        sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getUserId();

        if (currentUserId == null) {
            Toast.makeText(this, "Session expired. Please log in.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(AddExpenseActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        // Toolbar setup
        toolbar = findViewById(R.id.toolbar); // <-- ASSIGN TO CLASS VARIABLE
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Initialize views
        textInputLayoutDate = findViewById(R.id.textInputLayoutDate);
        editTextDate = findViewById(R.id.editTextDate);
        textInputLayoutCategory = findViewById(R.id.textInputLayoutCategory);
        autoCompleteCategory = findViewById(R.id.autoCompleteCategory);
        textInputLayoutAmount = findViewById(R.id.textInputLayoutAmount);
        editTextAmount = findViewById(R.id.editTextAmount);
        textInputLayoutNotes = findViewById(R.id.textInputLayoutNotes);
        editTextNotes = findViewById(R.id.editTextNotes);
        buttonSaveExpense = findViewById(R.id.buttonSaveExpense);

        // Set up DatePicker
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, monthOfYear, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateInView();
        };

        editTextDate.setOnClickListener(v -> new DatePickerDialog(AddExpenseActivity.this,
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show());

        // Set up Category Dropdown
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                getResources().getStringArray(R.array.expense_categories)
        );
        autoCompleteCategory.setAdapter(adapter);

        // --- CHECK FOR EDIT MODE ---
        if (getIntent().hasExtra("EXPENSE_ID")) {
            isEditMode = true;
            existingExpenseId = getIntent().getStringExtra("EXPENSE_ID");

            if (currentUserId != null) {
                // This assumes you added getExpenseById() to your DB helper
                existingExpense = dbHelper.getExpenseById(existingExpenseId, currentUserId);
            }

            if (existingExpense != null) {
                toolbar.setTitle("Edit Expense");
                buttonSaveExpense.setText("Update Expense");
                populateFields(existingExpense);
            } else {
                Toast.makeText(this, "Error: Could not load expense data.", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        } else {
            isEditMode = false;
            toolbar.setTitle("Add Expense");
            buttonSaveExpense.setText("Save Expense");
            updateDateInView(); // Set date to today for new expense
        }
        // -------------------------

        buttonSaveExpense.setOnClickListener(v -> saveExpense());
    }

    /**
     * Fills the form fields with data from an existing expense.
     *
     * @param expense The expense object to load.
     */
    private void populateFields(Expense expense) {
        editTextAmount.setText(String.valueOf(expense.getAmount()));
        editTextNotes.setText(expense.getNotes());
        autoCompleteCategory.setText(expense.getCategory(), false); // false to not show dropdown

        // Parse the database date string ("yyyy-MM-dd") and set the calendar
        String dbFormat = "yyyy-MM-dd";
        SimpleDateFormat sdfDb = new SimpleDateFormat(dbFormat, Locale.getDefault());
        try {
            calendar.setTime(sdfDb.parse(expense.getDate()));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Now update the UI date field based on the calendar
        updateDateInView();
    }


    private void updateDateInView() {
        String myFormat = "dd/MM/yyyy"; // User-friendly display format
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());
        editTextDate.setText(sdf.format(calendar.getTime()));
    }

    private void saveExpense() {
        String dateDisplay = editTextDate.getText().toString().trim();
        String category = autoCompleteCategory.getText().toString().trim();
        String amountStr = editTextAmount.getText().toString().trim();
        String notes = editTextNotes.getText().toString().trim();

        if (TextUtils.isEmpty(dateDisplay)) {
            textInputLayoutDate.setError("Date is required");
            return;
        } else {
            textInputLayoutDate.setError(null);
        }

        if (TextUtils.isEmpty(category)) {
            textInputLayoutCategory.setError("Category is required");
            return;
        } else {
            textInputLayoutCategory.setError(null);
        }

        if (TextUtils.isEmpty(amountStr)) {
            textInputLayoutAmount.setError("Amount is required");
            return;
        } else {
            textInputLayoutAmount.setError(null);
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                textInputLayoutAmount.setError("Amount must be greater than 0");
                return;
            } else {
                textInputLayoutAmount.setError(null);
            }
        } catch (NumberFormatException e) {
            textInputLayoutAmount.setError("Invalid amount");
            return;
        }

        // This format is required for the database (sorting, querying)
        String dbFormat = "yyyy-MM-dd";
        SimpleDateFormat sdfDb = new SimpleDateFormat(dbFormat, Locale.getDefault());
        String dateForDb = sdfDb.format(calendar.getTime());

        // --- MODIFIED SAVE/UPDATE LOGIC ---
        if (isEditMode) {
            // UPDATE existing expense
            existingExpense.setDate(dateForDb);
            existingExpense.setCategory(category);
            existingExpense.setAmount(amount);
            existingExpense.setNotes(notes);

            int rowsAffected = dbHelper.updateExpense(existingExpense, currentUserId);
            if (rowsAffected > 0) {
                Toast.makeText(this, "Expense updated successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to update expense.", Toast.LENGTH_SHORT).show();
            }

        } else {
            // INSERT new expense
            String id = dbHelper.insertExpense(currentUserId, dateForDb, category, amount, notes);
            if (id != null && !id.isEmpty()) {
                Toast.makeText(this, "Expense saved successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to save expense.", Toast.LENGTH_SHORT).show();
            }
        }

        finish();
    }
}