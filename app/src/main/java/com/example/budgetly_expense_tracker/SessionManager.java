package com.example.budgetly_expense_tracker;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "BudgetlySession";
    private static final String KEY_USER_ID = "user_id";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context _context;

    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    /**
     * Saves the user's ID to SharedPreferences, starting their session.
     * @param userId The String UUID of the logged-in user.
     */
    public void saveUserSession(String userId) {
        editor.putString(KEY_USER_ID, userId);
        editor.commit();
    }

    /**
     * Retrieves the logged-in user's ID.
     * @return The String UUID of the user, or null if not logged in.
     */
    public String getUserId() {
        return pref.getString(KEY_USER_ID, null);
    }

    /**
     * Clears the session data, effectively logging the user out.
     */
    public void clearSession() {
        editor.clear();
        editor.commit();
    }
}