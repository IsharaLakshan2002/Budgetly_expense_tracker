package com.example.budgetly_expense_tracker.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.budgetly_expense_tracker.R;
import com.example.budgetly_expense_tracker.models.Budget;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder> {

    private Context context;
    private List<Budget> budgetList;
    private OnBudgetActionListener listener;

    public interface OnBudgetActionListener {
        void onDeleteClick(Budget budget);
    }

    public BudgetAdapter(Context context, List<Budget> budgetList, OnBudgetActionListener listener) {
        this.context = context;
        this.budgetList = budgetList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // FIXED: Using the correct layout file
        View view = LayoutInflater.from(context).inflate(R.layout.item_budget_layout, parent, false);
        return new BudgetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        Budget budget = budgetList.get(position);

        // FIXED: Safer month formatting
        String formattedMonth = formatMonth(budget.getMonth());
        holder.textViewBudgetMonth.setText(formattedMonth);

        // FIXED: Currency formatting for Sri Lanka
        String formattedLimit = String.format(Locale.getDefault(), "Limit: Rs. %.2f", budget.getLimitAmount());
        holder.textViewBudgetLimit.setText(formattedLimit);

        holder.buttonDeleteBudget.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(budget);
            }
        });
    }

    private String formatMonth(String month) {
        try {
            // Input format from DB: "yyyy-MM"
            SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
            Date date = dbFormat.parse(month);
            // Output format for display: "MMMM yyyy"
            SimpleDateFormat displayFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
            return displayFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return month; // Fallback to raw data if parsing fails
        }
    }

    @Override
    public int getItemCount() {
        return budgetList.size();
    }

    public void updateBudgets(List<Budget> newBudgets) {
        this.budgetList.clear();
        this.budgetList.addAll(newBudgets);
        notifyDataSetChanged();
    }

    public static class BudgetViewHolder extends RecyclerView.ViewHolder {
        TextView textViewBudgetMonth, textViewBudgetLimit;
        ImageButton buttonDeleteBudget;

        public BudgetViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewBudgetMonth = itemView.findViewById(R.id.textViewBudgetMonth);
            textViewBudgetLimit = itemView.findViewById(R.id.textViewBudgetLimit);
            buttonDeleteBudget = itemView.findViewById(R.id.buttonDeleteBudget);
        }
    }
}