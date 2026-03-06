package com.github.timeaissr.behaviortracker.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.github.timeaissr.behaviortracker.R;
import com.github.timeaissr.behaviortracker.data.entity.Behavior;
import com.github.timeaissr.behaviortracker.databinding.ActivityMainBinding;
import com.github.timeaissr.behaviortracker.ui.add.AddBehaviorActivity;
import com.github.timeaissr.behaviortracker.ui.detail.BehaviorDetailActivity;
import com.github.timeaissr.behaviortracker.ui.settings.SettingsActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity implements BehaviorAdapter.OnBehaviorClickListener {

    private ActivityMainBinding binding;
    private MainViewModel viewModel;
    private BehaviorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        setupToolbar();
        setupRecyclerView();
        setupFab();
        observeData();
    }

    private void setupToolbar() {
        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }
            return false;
        });
    }

    private void setupRecyclerView() {
        adapter = new BehaviorAdapter(this, viewModel, this);
        binding.recyclerBehaviors.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerBehaviors.setAdapter(adapter);
    }

    private void setupFab() {
        binding.fabAdd.setOnClickListener(v ->
                startActivity(new Intent(this, AddBehaviorActivity.class)));
    }

    private void observeData() {
        viewModel.getAllActiveBehaviors().observe(this, behaviors -> {
            if (behaviors == null || behaviors.isEmpty()) {
                binding.layoutEmpty.setVisibility(View.VISIBLE);
                binding.recyclerBehaviors.setVisibility(View.GONE);
            } else {
                binding.layoutEmpty.setVisibility(View.GONE);
                binding.recyclerBehaviors.setVisibility(View.VISIBLE);
                adapter.submitList(behaviors);
            }
        });
    }

    // ==================== BehaviorAdapter.OnBehaviorClickListener ====================

    @Override
    public void onBehaviorClick(Behavior behavior) {
        Intent intent = new Intent(this, BehaviorDetailActivity.class);
        intent.putExtra(BehaviorDetailActivity.EXTRA_BEHAVIOR_ID, behavior.getId());
        startActivity(intent);
    }

    @Override
    public void onBooleanLogClick(Behavior behavior) {
        viewModel.quickLogBoolean(behavior.getId());
        Snackbar.make(binding.getRoot(),
                behavior.getName() + " - " + getString(R.string.logged_today),
                Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onNumericLogClick(Behavior behavior) {
        showNumericInputDialog(behavior);
    }

    private void showNumericInputDialog(Behavior behavior) {
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_numeric_input, null);
        EditText editValue = dialogView.findViewById(R.id.edit_value);
        EditText editNote = dialogView.findViewById(R.id.edit_note);

        String unit = behavior.getUnit() != null ? " (" + behavior.getUnit() + ")" : "";

        new MaterialAlertDialogBuilder(this)
                .setTitle(behavior.getName() + unit)
                .setView(dialogView)
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    String valueStr = editValue.getText().toString().trim();
                    if (!valueStr.isEmpty()) {
                        try {
                            double value = Double.parseDouble(valueStr);
                            String note = editNote.getText().toString().trim();
                            viewModel.quickLogNumeric(behavior.getId(), value,
                                    note.isEmpty() ? null : note);
                            Snackbar.make(binding.getRoot(),
                                    behavior.getName() + " - " + getString(R.string.logged_today),
                                    Snackbar.LENGTH_SHORT).show();
                        } catch (NumberFormatException e) {
                            // Ignore invalid input
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
