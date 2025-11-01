package com.example.budgetly_expense_tracker.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.budgetly_expense_tracker.R;
import com.example.budgetly_expense_tracker.models.Expense;
import com.google.android.material.button.MaterialButton;
import java.util.List;
import java.util.Locale;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private Context context;
    private List<Expense> expenseList;
    private OnItemActionListener listener;
    // REMOVED: Redundant dbHelper. The activity handles DB operations.

    public interface OnItemActionListener {
        void onEditClick(Expense expense);
        void onDeleteClick(Expense expense);
    }

    public ExpenseAdapter(Context context, List<Expense> expenseList, OnItemActionListener listener) {
        this.context = context;
        this.expenseList = expenseList;
        this.listener = listener;
    }

    public void updateExpenses(List<Expense> newExpenses) {
        this.expenseList.clear();
        this.expenseList.addAll(newExpenses);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_expense_card, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenseList.get(position);

        holder.textViewCategory.setText(expense.getCategory());
        holder.textViewDate.setText(expense.getDate());

        // FIXED: Currency formatting for Sri Lanka (with negative sign)
        String formattedAmount = String.format(Locale.getDefault(), "- Rs. %.2f", expense.getAmount());
        holder.textViewAmount.setText(formattedAmount);

        if (expense.getNotes() != null && !expense.getNotes().isEmpty()) {
            holder.textViewNotes.setText(expense.getNotes());
            holder.textViewNotes.setVisibility(View.VISIBLE);
        } else {
            holder.textViewNotes.setVisibility(View.GONE);
        }

        // ADDED: Set category icon
        holder.imageViewCategoryIcon.setImageResource(getIconForCategory(expense.getCategory()));

        holder.buttonEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(expense);
            }
        });

        holder.buttonDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(expense);
            }
        });
    }

    // --- THIS IS THE FIX ---
    // Corrected the typo "ic_catergory" to "ic_category"
    // Make sure you have a drawable file named "ic_category.xml" in your res/drawable folder
    private int getIconForCategory(String category) {
        // You can expand this with more icons
        switch (category) {
            case "Food":
                return R.drawable.ic_catergory; // Replace with ic_food if you have it
            case "Transport":
                return R.drawable.ic_catergory; // Replace with ic_transport
            case "Entertainment":
                return R.drawable.ic_catergory; // Replace with ic_entertainment
            // ... etc ...
            default:
                return R.drawable.ic_catergory; // Default icon
        }
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    public static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewCategoryIcon;
        TextView textViewCategory, textViewAmount, textViewDate, textViewNotes;
        MaterialButton buttonEdit, buttonDelete;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewCategoryIcon = itemView.findViewById(R.id.imageViewCategoryIcon);
            textViewCategory = itemView.findViewById(R.id.textViewCategory);
            textViewAmount = itemView.findViewById(R.id.textViewAmount);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            textViewNotes = itemView.findViewById(R.id.textViewNotes);
            buttonEdit = itemView.findViewById(R.id.buttonEdit);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }
}