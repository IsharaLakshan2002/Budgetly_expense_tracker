package com.example.budgetly_expense_tracker;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.budgetly_expense_tracker.database.BudgetlyDBOpenHelper;
import com.example.budgetly_expense_tracker.sync.SyncScheduler;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText editTextEmail, editTextPassword;
    private MaterialButton buttonLogin;
    private TextView textViewRegisterLink;

    private BudgetlyDBOpenHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new BudgetlyDBOpenHelper(this);
        sessionManager = new SessionManager(this);

        // If user is already logged in, go straight to MainActivity
        if (sessionManager.getUserId() != null) {
            goToMainActivity();
            return;
        }

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewRegisterLink = findViewById(R.id.textViewRegisterLink);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        textViewRegisterLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }

    private void loginUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Email is required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Password is required");
            return;
        }

        // Call the checkUser method
        String userId = dbHelper.checkUser(email, password);

        if (userId != null) {
            // --- SUCCESS ---
            // Save the user's ID in the session (using your method name)
            sessionManager.saveUserSession(userId);

            // Schedule periodic background sync (every 15 minutes)
            SyncScheduler.schedulePeriodicSync(this);

            // Go to MainActivity
            goToMainActivity();

        } else {
            // --- FAILURE ---
            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
        }
    }

    private void goToMainActivity() {
        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }
}
