package com.github.timeaissr.behaviortracker.ui.add;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

public class ColorPickerAdapter extends RecyclerView.Adapter<ColorPickerAdapter.ViewHolder> {

    private final String[] colors;
    private int selectedPosition = 0;
    private OnColorSelectedListener listener;

    public interface OnColorSelectedListener {
        void onColorSelected(String color);
    }

    public ColorPickerAdapter(String[] colors, OnColorSelectedListener listener) {
        this.colors = colors;
        this.listener = listener;
    }

    public void setSelectedColor(String color) {
        for (int i = 0; i < colors.length; i++) {
            if (colors[i].equals(color)) {
                int oldPosition = selectedPosition;
                selectedPosition = i;
                notifyItemChanged(oldPosition);
                notifyItemChanged(selectedPosition);
                break;
            }
        }
    }

    public String getSelectedColor() {
        return colors[selectedPosition];
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = new View(parent.getContext());
        int size = (int) (48 * parent.getContext().getResources().getDisplayMetrics().density);
        int margin = (int) (4 * parent.getContext().getResources().getDisplayMetrics().density);
        RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(size, size);
        params.setMargins(margin, margin, margin, margin);
        view.setLayoutParams(params);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String color = colors[position];
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(Color.parseColor(color));

        if (position == selectedPosition) {
            drawable.setStroke(
                    (int) (3 * holder.itemView.getContext().getResources().getDisplayMetrics().density),
                    Color.parseColor("#000000")
            );
        }

        holder.itemView.setBackground(drawable);
        holder.itemView.setOnClickListener(v -> {
            int oldPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(oldPosition);
            notifyItemChanged(selectedPosition);
            if (listener != null) {
                listener.onColorSelected(color);
            }
        });
    }

    @Override
    public int getItemCount() {
        return colors.length;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
