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
import com.example.budgetly_expense_tracker.models.Saving;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import java.util.List;
import java.util.Locale;

public class SavingAdapter extends RecyclerView.Adapter<SavingAdapter.SavingViewHolder> {

    private Context context;
    private List<Saving> savingList;
    private OnSavingActionListener listener;

    // --- 1. UPDATE THE INTERFACE ---
    public interface OnSavingActionListener {
        void onEditClick(Saving saving); // <-- ADDED
        void onDeleteClick(Saving saving);
    }

    public SavingAdapter(Context context, List<Saving> savingList, OnSavingActionListener listener) {
        this.context = context;
        this.savingList = savingList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SavingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_saving_card, parent, false);
        return new SavingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SavingViewHolder holder, int position) {
        Saving saving = savingList.get(position);

        holder.textViewGoalName.setText(saving.getGoalName());

        // Format currency for Sri Lanka
        String current = String.format(Locale.getDefault(), "Rs. %.2f", saving.getCurrentAmount());
        String target = String.format(Locale.getDefault(), "/ Rs. %.2f", saving.getTargetAmount());
        holder.textViewCurrentAmount.setText(current);
        holder.textViewTargetAmount.setText(target);

        // Calculate and set progress
        int progress = 0;
        if (saving.getTargetAmount() > 0) {
            progress = (int) ((saving.getCurrentAmount() / saving.getTargetAmount()) * 100);
        }
        holder.progressBarSaving.setProgress(progress);
        holder.textViewPercentage.setText(String.format(Locale.getDefault(), "%d%% Complete", progress));

        // --- 3. ADD CLICK LISTENER FOR EDIT ---
        holder.buttonEditSaving.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(saving);
            }
        });

        holder.buttonDeleteSaving.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(saving);
            }
        });
    }

    @Override
    public int getItemCount() {
        return savingList.size();
    }

    public void updateSavings(List<Saving> newSavings) {
        this.savingList.clear();
        this.savingList.addAll(newSavings);
        notifyDataSetChanged();
    }

    // --- 2. UPDATE THE VIEWHOLDER ---
    public static class SavingViewHolder extends RecyclerView.ViewHolder {
        TextView textViewGoalName, textViewCurrentAmount, textViewTargetAmount, textViewPercentage;
        ImageButton buttonDeleteSaving, buttonEditSaving; // <-- ADDED buttonEditSaving
        LinearProgressIndicator progressBarSaving;

        public SavingViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewGoalName = itemView.findViewById(R.id.textViewGoalName);
            textViewCurrentAmount = itemView.findViewById(R.id.textViewCurrentAmount);
            textViewTargetAmount = itemView.findViewById(R.id.textViewTargetAmount);
            textViewPercentage = itemView.findViewById(R.id.textViewPercentage);
            buttonDeleteSaving = itemView.findViewById(R.id.buttonDeleteSaving);
            buttonEditSaving = itemView.findViewById(R.id.buttonEditSaving); // <-- FIND THE VIEW
            progressBarSaving = itemView.findViewById(R.id.progressBarSaving);
        }
    }
}