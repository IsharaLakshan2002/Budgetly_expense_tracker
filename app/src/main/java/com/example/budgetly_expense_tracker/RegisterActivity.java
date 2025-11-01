package com.example.budgetly_expense_tracker;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.budgetly_expense_tracker.database.BudgetlyDBOpenHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText editTextName, editTextEmail, editTextPassword, editTextConfirmPassword;
    private MaterialButton buttonRegister;
    private TextView textViewLoginLink;
    private BudgetlyDBOpenHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbHelper = new BudgetlyDBOpenHelper(this);

        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        buttonRegister = findViewById(R.id.buttonRegister);
        textViewLoginLink = findViewById(R.id.textViewLoginLink);

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        textViewLoginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish this activity and go back to Login
                finish();
            }
        });
    }

    private void registerUser() {
        String name = editTextName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        // --- Validation ---
        if (TextUtils.isEmpty(name)) {
            editTextName.setError("Name is required");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Email is required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Password is required");
            return;
        }
        if (password.length() < 6) {
            editTextPassword.setError("Password must be at least 6 characters");
            return;
        }
        if (!password.equals(confirmPassword)) {
            editTextConfirmPassword.setError("Passwords do not match");
            return;
        }
        // --- End Validation ---

        try {
            String userId = dbHelper.insertUser(name, email, password);
            if (userId != null && !userId.isEmpty()) {
                Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
                finish(); // Go back to LoginActivity
            } else {
                Toast.makeText(this, "Failed to register. Email might already be in use.", Toast.LENGTH_LONG).show();
                editTextEmail.setError("Email already in use");
            }
        } catch (android.database.sqlite.SQLiteConstraintException e) {
            // This catch block handles the UNIQUE constraint on the email column
            Toast.makeText(this, "Failed to register. Email might already be in use.", Toast.LENGTH_LONG).show();
            editTextEmail.setError("Email already in use");
        } catch (Exception e) {
            Toast.makeText(this, "An error occurred: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}