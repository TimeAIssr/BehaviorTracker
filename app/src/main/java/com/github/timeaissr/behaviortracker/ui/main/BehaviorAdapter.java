package com.github.timeaissr.behaviortracker.ui.main;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.github.timeaissr.behaviortracker.R;
import com.github.timeaissr.behaviortracker.data.entity.Behavior;
import com.github.timeaissr.behaviortracker.data.entity.RecordType;
import com.github.timeaissr.behaviortracker.util.DateUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class BehaviorAdapter extends ListAdapter<Behavior, BehaviorAdapter.ViewHolder> {

    public interface OnBehaviorClickListener {
        void onBehaviorClick(Behavior behavior);
        void onBooleanLogClick(Behavior behavior);
        void onNumericLogClick(Behavior behavior);
    }

    private final OnBehaviorClickListener listener;
    private final MainViewModel viewModel;
    private final LifecycleOwner lifecycleOwner;

    public BehaviorAdapter(OnBehaviorClickListener listener, MainViewModel viewModel,
                           LifecycleOwner lifecycleOwner) {
        super(DIFF_CALLBACK);
        this.listener = listener;
        this.viewModel = viewModel;
        this.lifecycleOwner = lifecycleOwner;
    }

    private static final DiffUtil.ItemCallback<Behavior> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Behavior>() {
                @Override
                public boolean areItemsTheSame(@NonNull Behavior oldItem, @NonNull Behavior newItem) {
                    return oldItem.getId() == newItem.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull Behavior oldItem, @NonNull Behavior newItem) {
                    return oldItem.getName().equals(newItem.getName())
                            && oldItem.getRecordType() == newItem.getRecordType()
                            && String.valueOf(oldItem.getColor()).equals(String.valueOf(newItem.getColor()));
                }
            };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_behavior, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Behavior behavior = getItem(position);
        holder.bind(behavior);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final MaterialCardView card;
        private final View colorIndicator;
        private final TextView textName;
        private final TextView textStatus;
        private final MaterialButton btnBooleanLog;
        private final MaterialButton btnNumericLog;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.card_behavior);
            colorIndicator = itemView.findViewById(R.id.view_color_indicator);
            textName = itemView.findViewById(R.id.text_behavior_name);
            textStatus = itemView.findViewById(R.id.text_behavior_status);
            btnBooleanLog = itemView.findViewById(R.id.btn_quick_log_boolean);
            btnNumericLog = itemView.findViewById(R.id.btn_quick_log_numeric);
        }

        void bind(Behavior behavior) {
            textName.setText(behavior.getName());

            // Set color indicator
            if (behavior.getColor() != null && !behavior.getColor().isEmpty()) {
                try {
                    colorIndicator.setBackgroundColor(Color.parseColor(behavior.getColor()));
                } catch (IllegalArgumentException e) {
                    // fallback
                }
            }

            // Show appropriate quick-log button
            if (behavior.getRecordType() == RecordType.BOOLEAN) {
                btnBooleanLog.setVisibility(View.VISIBLE);
                btnNumericLog.setVisibility(View.GONE);

                // Observe today's status
                long dayStart = DateUtils.getStartOfDay();
                long dayEnd = DateUtils.getEndOfDay();
                viewModel.getRecordCountForDay(behavior.getId(), dayStart, dayEnd)
                        .observe(lifecycleOwner, count -> {
                            boolean loggedToday = count != null && count > 0;
                            textStatus.setText(loggedToday
                                    ? itemView.getContext().getString(R.string.logged_today)
                                    : itemView.getContext().getString(R.string.not_logged_today));
                            // Update button appearance based on logged state
                            btnBooleanLog.setIconResource(loggedToday
                                    ? android.R.drawable.checkbox_on_background
                                    : android.R.drawable.checkbox_off_background);
                        });

                btnBooleanLog.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onBooleanLogClick(behavior);
                    }
                });

            } else {
                btnBooleanLog.setVisibility(View.GONE);
                btnNumericLog.setVisibility(View.VISIBLE);

                // Show today's sum for numeric type
                long dayStart = DateUtils.getStartOfDay();
                long dayEnd = DateUtils.getEndOfDay();
                viewModel.getSumInRange(behavior.getId(), dayStart, dayEnd)
                        .observe(lifecycleOwner, sum -> {
                            String unit = behavior.getUnit() != null ? behavior.getUnit() : "";
                            if (sum != null && sum > 0) {
                                textStatus.setText(String.format("今日: %.1f %s", sum, unit));
                            } else {
                                textStatus.setText(itemView.getContext().getString(R.string.not_logged_today));
                            }
                        });

                btnNumericLog.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onNumericLogClick(behavior);
                    }
                });
            }

            // Card click -> detail
            card.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBehaviorClick(behavior);
                }
            });
        }
    }
}
