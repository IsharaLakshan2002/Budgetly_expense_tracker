package com.example.budgetly_expense_tracker.api;

import com.example.budgetly_expense_tracker.api.models.*;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.*;

public interface OrdsApiService {

    // ========== USERS ==========
    @POST("users/create")
    Call<Void> createUser(@Body Map<String, Object> user);

    @GET("users/list")
    Call<OrdsItemsWrapper<UserResponse>> getUsers();

    // ========== EXPENSES ==========
    @POST("expenses/create")
    Call<Void> createExpense(@Body Map<String, Object> expense);

    @GET("expenses/list")
    Call<OrdsItemsWrapper<ExpenseResponse>> getExpenses();

    // ========== BUDGETS ==========
    @POST("budgets/create")
    Call<Void> createBudget(@Body Map<String, Object> budget);

    @GET("budgets/list")
    Call<OrdsItemsWrapper<BudgetResponse>> getBudgets();

    // ========== SAVINGS ==========
    @POST("savings/create")
    Call<Void> createSaving(@Body Map<String, Object> saving);

    @GET("savings/list")
    Call<OrdsItemsWrapper<SavingResponse>> getSavings();
}
