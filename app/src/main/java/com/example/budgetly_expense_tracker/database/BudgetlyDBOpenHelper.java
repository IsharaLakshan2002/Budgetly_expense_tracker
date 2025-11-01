package com.example.budgetly_expense_tracker.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.budgetly_expense_tracker.api.models.BudgetResponse;
import com.example.budgetly_expense_tracker.api.models.ExpenseResponse;
import com.example.budgetly_expense_tracker.api.models.SavingResponse;
import com.example.budgetly_expense_tracker.models.Budget;
import com.example.budgetly_expense_tracker.models.Expense;
import com.example.budgetly_expense_tracker.models.Saving;
import com.example.budgetly_expense_tracker.models.User;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class BudgetlyDBOpenHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "expense_tracker.db";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    public static final String TABLE_USERS = "users";
    public static final String TABLE_EXPENSES = "expenses";
    public static final String TABLE_BUDGETS = "budgets";
    public static final String TABLE_SAVINGS = "savings";

    // --- Universal Column Names for Syncing ---
    public static final String KEY_ID = "id"; // Now a TEXT UUID
    public static final String COLUMN_SYNC_STATUS = "sync_status"; // 0=not synced, 1=synced
    public static final String COLUMN_LAST_MODIFIED = "last_modified"; // Timestamp for conflict resolution
    public static final String COLUMN_IS_DELETED = "is_deleted"; // 0=false, 1=true (for soft delete)

    // Users Table - Columns
    public static final String USER_NAME = "name";
    public static final String USER_EMAIL = "email";
    public static final String USER_PASSWORD = "password";

    // Expenses Table - Columns
    public static final String EXPENSE_USER_ID = "user_id";
    public static final String EXPENSE_DATE = "expense_date";
    public static final String EXPENSE_CATEGORY = "category";
    public static final String EXPENSE_AMOUNT = "amount";
    public static final String EXPENSE_NOTES = "notes";

    // Budgets Table - Columns
    public static final String BUDGET_USER_ID = "user_id";
    public static final String BUDGET_MONTH = "month"; // e.g., "2023-10"
    public static final String BUDGET_LIMIT_AMOUNT = "limit_amount";

    // Savings Table - Columns
    public static final String SAVING_USER_ID = "user_id";
    public static final String SAVING_GOAL_NAME = "goal_name";
    public static final String SAVING_TARGET_AMOUNT = "target_amount";
    public static final String SAVING_CURRENT_AMOUNT = "current_amount";

    // --- Helper for timestamp ---
    private String getCurrentTimestamp() {
        // Using ISO 8601 format, which is easily comparable
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    // --- Create Table Statements (Updated) ---
    private static final String CREATE_TABLE_USERS = "CREATE TABLE "
            + TABLE_USERS + "("
            + KEY_ID + " TEXT PRIMARY KEY," // Changed to TEXT for UUID
            + USER_NAME + " TEXT NOT NULL,"
            + USER_EMAIL + " TEXT UNIQUE NOT NULL,"
            + USER_PASSWORD + " TEXT NOT NULL," // WARNING: Storing plain text!
            + COLUMN_SYNC_STATUS + " INTEGER DEFAULT 0,"
            + COLUMN_LAST_MODIFIED + " TEXT NOT NULL,"
            + COLUMN_IS_DELETED + " INTEGER DEFAULT 0" + ")";

    private static final String CREATE_TABLE_EXPENSES = "CREATE TABLE "
            + TABLE_EXPENSES + "("
            + KEY_ID + " TEXT PRIMARY KEY," // Changed to TEXT for UUID
            + EXPENSE_USER_ID + " TEXT NOT NULL REFERENCES " + TABLE_USERS + "(" + KEY_ID + ")," // TEXT reference
            + EXPENSE_DATE + " TEXT NOT NULL,"
            + EXPENSE_CATEGORY + " TEXT NOT NULL,"
            + EXPENSE_AMOUNT + " REAL NOT NULL,"
            + EXPENSE_NOTES + " TEXT,"
            + COLUMN_SYNC_STATUS + " INTEGER DEFAULT 0,"
            + COLUMN_LAST_MODIFIED + " TEXT NOT NULL,"
            + COLUMN_IS_DELETED + " INTEGER DEFAULT 0" + ")";

    private static final String CREATE_TABLE_BUDGETS = "CREATE TABLE "
            + TABLE_BUDGETS + "("
            + KEY_ID + " TEXT PRIMARY KEY," // Changed to TEXT for UUID
            + BUDGET_USER_ID + " TEXT NOT NULL REFERENCES " + TABLE_USERS + "(" + KEY_ID + ")," // TEXT reference
            + BUDGET_MONTH + " TEXT NOT NULL,"
            + BUDGET_LIMIT_AMOUNT + " REAL NOT NULL,"
            + COLUMN_SYNC_STATUS + " INTEGER DEFAULT 0,"
            + COLUMN_LAST_MODIFIED + " TEXT NOT NULL,"
            + COLUMN_IS_DELETED + " INTEGER DEFAULT 0,"
            + "UNIQUE(" + BUDGET_USER_ID + ", " + BUDGET_MONTH + ")" + ")";

    private static final String CREATE_TABLE_SAVINGS = "CREATE TABLE "
            + TABLE_SAVINGS + "("
            + KEY_ID + " TEXT PRIMARY KEY," // Changed to TEXT for UUID
            + SAVING_USER_ID + " TEXT NOT NULL REFERENCES " + TABLE_USERS + "(" + KEY_ID + ")," // TEXT reference
            + SAVING_GOAL_NAME + " TEXT NOT NULL,"
            + SAVING_TARGET_AMOUNT + " REAL NOT NULL,"
            + SAVING_CURRENT_AMOUNT + " REAL NOT NULL,"
            + COLUMN_SYNC_STATUS + " INTEGER DEFAULT 0,"
            + COLUMN_LAST_MODIFIED + " TEXT NOT NULL,"
            + COLUMN_IS_DELETED + " INTEGER DEFAULT 0,"
            + "UNIQUE(" + SAVING_USER_ID + ", " + SAVING_GOAL_NAME + ")" + ")";

    public BudgetlyDBOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_EXPENSES);
        db.execSQL(CREATE_TABLE_BUDGETS);
        db.execSQL(CREATE_TABLE_SAVINGS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This is destructive, but simple for a student project.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUDGETS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SAVINGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // --- CRUD Operations for Users ---

    /**
     * Checks if a user exists with the given email and password.
     * WARNING: Stores and checks plain text passwords! This is insecure and
     * only acceptable for this specific school project.
     *
     * @param email    The user's email
     * @param password The user's plain text password
     * @return The String UUID (user_id) if successful, or null if login fails.
     */
    public String checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String userId = null;

        String selectQuery = "SELECT " + KEY_ID + " FROM " + TABLE_USERS
                + " WHERE " + USER_EMAIL + " = ? AND " + USER_PASSWORD + " = ?"
                + " AND " + COLUMN_IS_DELETED + " = 0";

        Cursor c = null;
        try {
            c = db.rawQuery(selectQuery, new String[]{email, password});
            if (c.moveToFirst()) {
                userId = c.getString(c.getColumnIndexOrThrow(KEY_ID));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
            db.close();
        }

        return userId;
    }

    public String insertUser(String name, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        String uuid = UUID.randomUUID().toString();
        String timestamp = getCurrentTimestamp();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, uuid);
        values.put(USER_NAME, name);
        values.put(USER_EMAIL, email);
        values.put(USER_PASSWORD, password); // Storing plain text password (INSECURE!)
        values.put(COLUMN_SYNC_STATUS, 0);
        values.put(COLUMN_LAST_MODIFIED, timestamp);
        values.put(COLUMN_IS_DELETED, 0);

        // Use insertWithOnConflict to handle the UNIQUE email constraint
        long id = db.insertWithOnConflict(TABLE_USERS, null, values, SQLiteDatabase.CONFLICT_ABORT);
        db.close();
        return (id != -1) ? uuid : null; // Return UUID on success, null on failure
    }

    // Get current logged-in user for syncing
    public User getUserById(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        User user = null;

        String sql = "SELECT * FROM " + TABLE_USERS + " WHERE " + KEY_ID + "=?";
        Cursor c = db.rawQuery(sql, new String[]{userId});

        if (c.moveToFirst()) {
            user = new User();
            user.setId(c.getString(c.getColumnIndexOrThrow(KEY_ID)));
            user.setName(c.getString(c.getColumnIndexOrThrow(USER_NAME)));
            user.setEmail(c.getString(c.getColumnIndexOrThrow(USER_EMAIL)));
            user.setPassword(c.getString(c.getColumnIndexOrThrow(USER_PASSWORD)));
        }
        c.close();
        db.close();
        return user;
    }


    // --- CRUD Operations for Expenses ---

    public String insertExpense(String userId, String date, String category, double amount, String notes) {
        SQLiteDatabase db = this.getWritableDatabase();
        String uuid = UUID.randomUUID().toString();
        String timestamp = getCurrentTimestamp();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, uuid);
        values.put(EXPENSE_USER_ID, userId);
        values.put(EXPENSE_DATE, date);
        values.put(EXPENSE_CATEGORY, category);
        values.put(EXPENSE_AMOUNT, amount);
        values.put(EXPENSE_NOTES, notes);
        values.put(COLUMN_SYNC_STATUS, 0);
        values.put(COLUMN_LAST_MODIFIED, timestamp);
        values.put(COLUMN_IS_DELETED, 0);

        db.insert(TABLE_EXPENSES, null, values);
        db.close();
        return uuid;
    }

    public List<Expense> getExpensesByUser(String userId) {
        List<Expense> expenses = new ArrayList<>();
        // Updated query to filter out soft-deleted items
        String selectQuery = "SELECT * FROM " + TABLE_EXPENSES
                + " WHERE " + EXPENSE_USER_ID + " = ? AND " + COLUMN_IS_DELETED + " = 0"
                + " ORDER BY " + EXPENSE_DATE + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, new String[]{userId});

        if (c.moveToFirst()) {
            do {
                Expense expense = new Expense();
                expense.setId(c.getString(c.getColumnIndexOrThrow(KEY_ID))); // Get TEXT ID
                expense.setDate(c.getString(c.getColumnIndexOrThrow(EXPENSE_DATE)));
                expense.setCategory(c.getString(c.getColumnIndexOrThrow(EXPENSE_CATEGORY)));
                expense.setAmount(c.getDouble(c.getColumnIndexOrThrow(EXPENSE_AMOUNT)));
                expense.setNotes(c.getString(c.getColumnIndexOrThrow(EXPENSE_NOTES)));
                expense.setSyncStatus(c.getInt(c.getColumnIndexOrThrow(COLUMN_SYNC_STATUS)));
                expenses.add(expense);
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return expenses;
    }

    public int updateExpense(Expense expense, String userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(EXPENSE_DATE, expense.getDate());
        values.put(EXPENSE_CATEGORY, expense.getCategory());
        values.put(EXPENSE_AMOUNT, expense.getAmount());
        values.put(EXPENSE_NOTES, expense.getNotes());
        values.put(COLUMN_SYNC_STATUS, 0); // Mark as un-synced on any update
        values.put(COLUMN_LAST_MODIFIED, getCurrentTimestamp()); // Update timestamp

        int count = db.update(TABLE_EXPENSES, values,
                KEY_ID + " = ? AND " + EXPENSE_USER_ID + " = ? AND " + COLUMN_IS_DELETED + " = 0",
                new String[]{String.valueOf(expense.getId()), userId});
        db.close();
        return count;
    }

    public Expense getExpenseById(String expenseId, String userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Expense expense = null;

        // Query to get a specific expense for a specific user, that isn't deleted
        String selectQuery = "SELECT * FROM " + TABLE_EXPENSES
                + " WHERE " + KEY_ID + " = ? AND " + EXPENSE_USER_ID + " = ?"
                + " AND " + COLUMN_IS_DELETED + " = 0";

        Cursor c = null;
        try {
            c = db.rawQuery(selectQuery, new String[]{expenseId, userId});
            if (c.moveToFirst()) {
                expense = new Expense();
                expense.setId(c.getString(c.getColumnIndexOrThrow(KEY_ID)));
                expense.setDate(c.getString(c.getColumnIndexOrThrow(EXPENSE_DATE)));
                expense.setCategory(c.getString(c.getColumnIndexOrThrow(EXPENSE_CATEGORY)));
                expense.setAmount(c.getDouble(c.getColumnIndexOrThrow(EXPENSE_AMOUNT)));
                expense.setNotes(c.getString(c.getColumnIndexOrThrow(EXPENSE_NOTES)));
                expense.setSyncStatus(c.getInt(c.getColumnIndexOrThrow(COLUMN_SYNC_STATUS)));
                // We also need the user ID for the update logic
                expense.setUserId(c.getString(c.getColumnIndexOrThrow(EXPENSE_USER_ID)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
            db.close();
        }
        return expense;
    }

    public int softDeleteExpense(String expenseId, String userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_IS_DELETED, 1);
        values.put(COLUMN_SYNC_STATUS, 0); // Mark for sync
        values.put(COLUMN_LAST_MODIFIED, getCurrentTimestamp());

        int count = db.update(TABLE_EXPENSES, values,
                KEY_ID + " = ? AND " + EXPENSE_USER_ID + " = ?",
                new String[]{expenseId, userId});
        db.close();
        return count;
    }

    // Get all unsynced expenses for a user
    public List<Expense> getUnsyncedExpenses(String userId) {
        List<Expense> expenses = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_EXPENSES
                + " WHERE " + EXPENSE_USER_ID + " = ? AND " + COLUMN_SYNC_STATUS + " = 0";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, new String[]{userId});

        if (c.moveToFirst()) {
            do {
                Expense expense = new Expense();
                expense.setId(c.getString(c.getColumnIndexOrThrow(KEY_ID)));
                expense.setUserId(c.getString(c.getColumnIndexOrThrow(EXPENSE_USER_ID)));
                expense.setDate(c.getString(c.getColumnIndexOrThrow(EXPENSE_DATE)));
                expense.setCategory(c.getString(c.getColumnIndexOrThrow(EXPENSE_CATEGORY)));
                expense.setAmount(c.getDouble(c.getColumnIndexOrThrow(EXPENSE_AMOUNT)));
                expense.setNotes(c.getString(c.getColumnIndexOrThrow(EXPENSE_NOTES)));
                expense.setSyncStatus(c.getInt(c.getColumnIndexOrThrow(COLUMN_SYNC_STATUS)));
                expenses.add(expense);
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return expenses;
    }

    // Mark expense as synced
    public void markExpenseSynced(String expenseId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SYNC_STATUS, 1);

        db.update(TABLE_EXPENSES, values, KEY_ID + " = ?", new String[]{expenseId});
        db.close();
    }

    // Insert or update expense from server (for pulling data)
    public void upsertExpenseFromServer(ExpenseResponse serverExpense) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_ID, serverExpense.getId());
        values.put(EXPENSE_USER_ID, serverExpense.getUserId());
        values.put(EXPENSE_DATE, serverExpense.getExpenseDate());
        values.put(EXPENSE_CATEGORY, serverExpense.getCategory());
        values.put(EXPENSE_AMOUNT, serverExpense.getAmount());
        values.put(EXPENSE_NOTES, serverExpense.getNotes());
        values.put(COLUMN_SYNC_STATUS, 1); // Mark as synced since it came from server
        values.put(COLUMN_LAST_MODIFIED, serverExpense.getLastModified());
        values.put(COLUMN_IS_DELETED, serverExpense.getIsDeleted());

        // Use REPLACE to insert or update if exists
        db.insertWithOnConflict(TABLE_EXPENSES, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }


    // --- CRUD Operations for Budgets ---

    public String insertBudget(String userId, String month, double limitAmount) {
        SQLiteDatabase db = this.getWritableDatabase();
        String uuid = UUID.randomUUID().toString();
        String timestamp = getCurrentTimestamp();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, uuid);
        values.put(BUDGET_USER_ID, userId);
        values.put(BUDGET_MONTH, month);
        values.put(BUDGET_LIMIT_AMOUNT, limitAmount);
        values.put(COLUMN_SYNC_STATUS, 0);
        values.put(COLUMN_LAST_MODIFIED, timestamp);
        values.put(COLUMN_IS_DELETED, 0);

        long id = db.insertWithOnConflict(TABLE_BUDGETS, null, values, SQLiteDatabase.CONFLICT_FAIL);
        db.close();
        return (id != -1) ? uuid : null;
    }

    public List<Budget> getBudgetsByUser(String userId) {
        List<Budget> budgets = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_BUDGETS
                + " WHERE " + BUDGET_USER_ID + " = ? AND " + COLUMN_IS_DELETED + " = 0"
                + " ORDER BY " + BUDGET_MONTH + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, new String[]{userId});

        if (c.moveToFirst()) {
            do {
                Budget budget = new Budget();
                budget.setId(c.getString(c.getColumnIndexOrThrow(KEY_ID))); // Get TEXT ID
                budget.setMonth(c.getString(c.getColumnIndexOrThrow(BUDGET_MONTH)));
                budget.setLimitAmount(c.getDouble(c.getColumnIndexOrThrow(BUDGET_LIMIT_AMOUNT)));
                // budget.setSyncStatus(c.getInt(c.getColumnIndexOrThrow(COLUMN_SYNC_STATUS)));
                budgets.add(budget);
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return budgets;
    }

    public int updateBudget(Budget budget, String userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(BUDGET_MONTH, budget.getMonth());
        values.put(BUDGET_LIMIT_AMOUNT, budget.getLimitAmount());
        values.put(COLUMN_SYNC_STATUS, 0);
        values.put(COLUMN_LAST_MODIFIED, getCurrentTimestamp());

        int count = db.update(TABLE_BUDGETS, values,
                KEY_ID + " = ? AND " + BUDGET_USER_ID + " = ? AND " + COLUMN_IS_DELETED + " = 0",
                new String[]{String.valueOf(budget.getId()), userId});
        db.close();
        return count;
    }

    public int softDeleteBudget(String budgetId, String userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_IS_DELETED, 1);
        values.put(COLUMN_SYNC_STATUS, 0);
        values.put(COLUMN_LAST_MODIFIED, getCurrentTimestamp());

        int rowsAffected = db.update(TABLE_BUDGETS, values,
                KEY_ID + " = ? AND " + BUDGET_USER_ID + " = ?",
                new String[]{budgetId, userId});
        db.close();
        return rowsAffected;
    }

    public List<Budget> getUnsyncedBudgets(String userId) {
        List<Budget> list = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_BUDGETS + " WHERE " + BUDGET_USER_ID + "=? AND " + COLUMN_SYNC_STATUS + "=0";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(sql, new String[]{userId});
        if (c.moveToFirst()) {
            do {
                Budget b = new Budget();
                b.setId(c.getString(c.getColumnIndexOrThrow(KEY_ID)));
                b.setMonth(c.getString(c.getColumnIndexOrThrow(BUDGET_MONTH)));
                b.setLimitAmount(c.getDouble(c.getColumnIndexOrThrow(BUDGET_LIMIT_AMOUNT)));
                list.add(b);
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return list;
    }

    public void markBudgetSynced(String budgetId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_SYNC_STATUS, 1);
        db.update(TABLE_BUDGETS, cv, KEY_ID + "=?", new String[]{budgetId});
        db.close();
    }

    public void upsertBudgetFromServer(BudgetResponse serverBudget) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ID, serverBudget.getId());
        values.put(BUDGET_USER_ID, serverBudget.getUserId());
        values.put(BUDGET_MONTH, serverBudget.getMonth());
        values.put(BUDGET_LIMIT_AMOUNT, serverBudget.getLimitAmount());
        values.put(COLUMN_SYNC_STATUS, 1);
        values.put(COLUMN_LAST_MODIFIED, serverBudget.getLastModified());
        values.put(COLUMN_IS_DELETED, serverBudget.getIsDeleted());
        db.insertWithOnConflict(TABLE_BUDGETS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    // --- CRUD Operations for Savings ---

    public String insertSaving(String userId, String goalName, double targetAmount, double currentAmount) {
        SQLiteDatabase db = this.getWritableDatabase();
        String uuid = UUID.randomUUID().toString();
        String timestamp = getCurrentTimestamp();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, uuid);
        values.put(SAVING_USER_ID, userId);
        values.put(SAVING_GOAL_NAME, goalName);
        values.put(SAVING_TARGET_AMOUNT, targetAmount);
        values.put(SAVING_CURRENT_AMOUNT, currentAmount);
        values.put(COLUMN_SYNC_STATUS, 0);
        values.put(COLUMN_LAST_MODIFIED, timestamp);
        values.put(COLUMN_IS_DELETED, 0);

        long id = db.insertWithOnConflict(TABLE_SAVINGS, null, values, SQLiteDatabase.CONFLICT_FAIL);
        db.close();
        return (id != -1) ? uuid : null;
    }

    public List<Saving> getSavingsByUser(String userId) {
        List<Saving> savings = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_SAVINGS
                + " WHERE " + SAVING_USER_ID + " = ? AND " + COLUMN_IS_DELETED + " = 0"
                + " ORDER BY " + SAVING_GOAL_NAME + " ASC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, new String[]{userId});

        if (c.moveToFirst()) {
            do {
                Saving saving = new Saving();
                saving.setId(c.getString(c.getColumnIndexOrThrow(KEY_ID))); // Get TEXT ID
                saving.setGoalName(c.getString(c.getColumnIndexOrThrow(SAVING_GOAL_NAME)));
                saving.setTargetAmount(c.getDouble(c.getColumnIndexOrThrow(SAVING_TARGET_AMOUNT)));
                saving.setCurrentAmount(c.getDouble(c.getColumnIndexOrThrow(SAVING_CURRENT_AMOUNT)));
                // saving.setSyncStatus(c.getInt(c.getColumnIndexOrThrow(COLUMN_SYNC_STATUS)));
                savings.add(saving);
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return savings;
    }

    public int updateSaving(Saving saving, String userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SAVING_GOAL_NAME, saving.getGoalName());
        values.put(SAVING_TARGET_AMOUNT, saving.getTargetAmount());
        values.put(SAVING_CURRENT_AMOUNT, saving.getCurrentAmount());
        values.put(COLUMN_SYNC_STATUS, 0);
        values.put(COLUMN_LAST_MODIFIED, getCurrentTimestamp());

        int count = db.update(TABLE_SAVINGS, values,
                KEY_ID + " = ? AND " + SAVING_USER_ID + " = ? AND " + COLUMN_IS_DELETED + " = 0",
                new String[]{String.valueOf(saving.getId()), userId});
        db.close();
        return count;
    }

    public int softDeleteSaving(String savingId, String userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_IS_DELETED, 1);
        values.put(COLUMN_SYNC_STATUS, 0);
        values.put(COLUMN_LAST_MODIFIED, getCurrentTimestamp());

        int rowsAffected = db.update(TABLE_SAVINGS, values,
                KEY_ID + " = ? AND " + SAVING_USER_ID + " = ?",
                new String[]{savingId, userId});
        db.close();
        return rowsAffected;
    }

    public List<Saving> getUnsyncedSavings(String userId) {
        List<Saving> list = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_SAVINGS + " WHERE " + SAVING_USER_ID + "=? AND " + COLUMN_SYNC_STATUS + "=0";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(sql, new String[]{userId});
        if (c.moveToFirst()) {
            do {
                Saving s = new Saving();
                s.setId(c.getString(c.getColumnIndexOrThrow(KEY_ID)));
                s.setGoalName(c.getString(c.getColumnIndexOrThrow(SAVING_GOAL_NAME)));
                s.setTargetAmount(c.getDouble(c.getColumnIndexOrThrow(SAVING_TARGET_AMOUNT)));
                s.setCurrentAmount(c.getDouble(c.getColumnIndexOrThrow(SAVING_CURRENT_AMOUNT)));
                list.add(s);
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return list;
    }

    public void markSavingSynced(String savingId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_SYNC_STATUS, 1);
        db.update(TABLE_SAVINGS, cv, KEY_ID + "=?", new String[]{savingId});
        db.close();
    }

    public void upsertSavingFromServer(SavingResponse serverSaving) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ID, serverSaving.getId());
        values.put(SAVING_USER_ID, serverSaving.getUserId());
        values.put(SAVING_GOAL_NAME, serverSaving.getGoalName());
        values.put(SAVING_TARGET_AMOUNT, serverSaving.getTargetAmount());
        values.put(SAVING_CURRENT_AMOUNT, serverSaving.getCurrentAmount());
        values.put(COLUMN_SYNC_STATUS, 1);
        values.put(COLUMN_LAST_MODIFIED, serverSaving.getLastModified());
        values.put(COLUMN_IS_DELETED, serverSaving.getIsDeleted());
        db.insertWithOnConflict(TABLE_SAVINGS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }
}