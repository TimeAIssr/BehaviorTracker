package com.github.timeaissr.behaviortracker.ui.add;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.github.timeaissr.behaviortracker.R;
import com.github.timeaissr.behaviortracker.data.entity.Behavior;
import com.github.timeaissr.behaviortracker.data.entity.RecordType;
import com.github.timeaissr.behaviortracker.data.entity.Reminder;
import com.github.timeaissr.behaviortracker.data.entity.ReminderType;
import com.github.timeaissr.behaviortracker.databinding.ActivityAddBehaviorBinding;
import com.github.timeaissr.behaviortracker.notification.ReminderScheduler;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

public class AddBehaviorActivity extends AppCompatActivity {

    public static final String EXTRA_BEHAVIOR_ID = "extra_behavior_id";

    private ActivityAddBehaviorBinding binding;
    private AddBehaviorViewModel viewModel;
    private ColorPickerAdapter colorAdapter;

    private String selectedColor;
    private RecordType selectedRecordType = RecordType.BOOLEAN;
    private ReminderType selectedReminderType = ReminderType.FIXED_TIME;
    private int fixedTimeMinutes = 480; // 08:00
    private int startTimeMinutes = 540; // 09:00
    private int endTimeMinutes = 1080; // 18:00

    private static final String[] AVAILABLE_COLORS = {
            "#E57373", "#F06292", "#BA68C8", "#64B5F6",
            "#4DD0E1", "#4DB6AC", "#81C784", "#FFB74D"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddBehaviorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(AddBehaviorViewModel.class);

        selectedColor = AVAILABLE_COLORS[0];

        setupToolbar();
        setupRecordTypeToggle();
        setupColorPicker();
        setupReminderUI();
        setupSaveButton();
        setupDeleteButton();

        // Check if editing
        long behaviorId = getIntent().getLongExtra(EXTRA_BEHAVIOR_ID, -1);
        if (behaviorId > 0) {
            viewModel.setEditingBehaviorId(behaviorId);
            binding.toolbar.setTitle(R.string.edit_behavior);
            binding.btnDelete.setVisibility(View.VISIBLE);
            loadExistingBehavior(behaviorId);
        }

        // Observe save
        viewModel.getSaveComplete().observe(this, complete -> {
            if (complete != null && complete) {
                finish();
            }
        });
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecordTypeToggle() {
        binding.toggleRecordType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btn_type_boolean) {
                    selectedRecordType = RecordType.BOOLEAN;
                    binding.layoutUnit.setVisibility(View.GONE);
                } else if (checkedId == R.id.btn_type_numeric) {
                    selectedRecordType = RecordType.NUMERIC;
                    binding.layoutUnit.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void setupColorPicker() {
        colorAdapter = new ColorPickerAdapter(AVAILABLE_COLORS, color -> selectedColor = color);
        binding.recyclerColors.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerColors.setAdapter(colorAdapter);
    }

    private void setupReminderUI() {
        binding.switchReminder.setOnCheckedChangeListener((buttonView, isChecked) ->
                binding.layoutReminderSettings.setVisibility(isChecked ? View.VISIBLE : View.GONE));

        binding.toggleReminderType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btn_reminder_fixed) {
                    selectedReminderType = ReminderType.FIXED_TIME;
                    binding.layoutFixedTime.setVisibility(View.VISIBLE);
                    binding.layoutInterval.setVisibility(View.GONE);
                } else if (checkedId == R.id.btn_reminder_interval) {
                    selectedReminderType = ReminderType.INTERVAL;
                    binding.layoutFixedTime.setVisibility(View.GONE);
                    binding.layoutInterval.setVisibility(View.VISIBLE);
                }
            }
        });

        // Time pickers
        binding.btnPickTime.setOnClickListener(v ->
                showTimePicker("选择提醒时间", fixedTimeMinutes, minutes -> {
                    fixedTimeMinutes = minutes;
                    binding.btnPickTime.setText(Reminder.minutesToTimeString(minutes));
                }));

        binding.btnPickStartTime.setOnClickListener(v ->
                showTimePicker("选择开始时间", startTimeMinutes, minutes -> {
                    startTimeMinutes = minutes;
                    binding.btnPickStartTime.setText(Reminder.minutesToTimeString(minutes));
                }));

        binding.btnPickEndTime.setOnClickListener(v ->
                showTimePicker("选择结束时间", endTimeMinutes, minutes -> {
                    endTimeMinutes = minutes;
                    binding.btnPickEndTime.setText(Reminder.minutesToTimeString(minutes));
                }));
    }

    private void showTimePicker(String title, int currentMinutes, OnTimeSelectedListener listener) {
        int hour = currentMinutes / 60;
        int minute = currentMinutes % 60;

        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(hour)
                .setMinute(minute)
                .setTitleText(title)
                .build();

        picker.addOnPositiveButtonClickListener(v -> {
            int selectedMinutes = picker.getHour() * 60 + picker.getMinute();
            listener.onTimeSelected(selectedMinutes);
        });

        picker.show(getSupportFragmentManager(), "time_picker");
    }

    private interface OnTimeSelectedListener {
        void onTimeSelected(int minutes);
    }

    private void setupSaveButton() {
        binding.btnSave.setOnClickListener(v -> {
            String name = binding.editName.getText().toString().trim();
            if (name.isEmpty()) {
                binding.layoutName.setError(getString(R.string.error_empty_name));
                return;
            }
            binding.layoutName.setError(null);

            if (selectedRecordType == RecordType.NUMERIC) {
                String unit = binding.editUnit.getText().toString().trim();
                if (unit.isEmpty()) {
                    binding.layoutUnit.setError(getString(R.string.error_empty_unit));
                    return;
                }
                binding.layoutUnit.setError(null);
            }

            // Build behavior
            Behavior behavior = new Behavior();
            behavior.setName(name);
            behavior.setRecordType(selectedRecordType);
            behavior.setColor(selectedColor);
            if (selectedRecordType == RecordType.NUMERIC) {
                behavior.setUnit(binding.editUnit.getText().toString().trim());
            }

            // Build reminder (if enabled)
            Reminder reminder = null;
            if (binding.switchReminder.isChecked()) {
                reminder = new Reminder();
                reminder.setActive(true);
                reminder.setType(selectedReminderType);

                if (selectedReminderType == ReminderType.FIXED_TIME) {
                    reminder.setTimeOfDayMinutes(fixedTimeMinutes);
                } else {
                    reminder.setStartTimeMinutes(startTimeMinutes);
                    reminder.setEndTimeMinutes(endTimeMinutes);
                    int hours = 0, mins = 0;
                    try {
                        hours = Integer.parseInt(binding.editIntervalHours.getText().toString().trim());
                    } catch (NumberFormatException ignored) {}
                    try {
                        mins = Integer.parseInt(binding.editIntervalMinutes.getText().toString().trim());
                    } catch (NumberFormatException ignored) {}
                    reminder.setIntervalMinutes(hours * 60 + mins);
                }
            }

            viewModel.saveBehavior(behavior, reminder);
        });
    }

    private void setupDeleteButton() {
        binding.btnDelete.setOnClickListener(v ->
                new MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.delete)
                        .setMessage(R.string.delete_behavior_confirm)
                        .setPositiveButton(R.string.delete, (dialog, which) -> {
                            ReminderScheduler.cancelReminder(this, viewModel.getEditingBehaviorId());
                            viewModel.deleteBehavior(viewModel.getEditingBehaviorId());
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show());
    }

    private void loadExistingBehavior(long behaviorId) {
        viewModel.getBehavior(behaviorId).observe(this, behavior -> {
            if (behavior == null) return;

            binding.editName.setText(behavior.getName());

            if (behavior.getRecordType() == RecordType.NUMERIC) {
                binding.toggleRecordType.check(R.id.btn_type_numeric);
                binding.editUnit.setText(behavior.getUnit());
            } else {
                binding.toggleRecordType.check(R.id.btn_type_boolean);
            }

            if (behavior.getColor() != null) {
                selectedColor = behavior.getColor();
                colorAdapter.setSelectedColor(selectedColor);
            }
        });

        viewModel.getReminder(behaviorId).observe(this, reminder -> {
            if (reminder == null) return;

            binding.switchReminder.setChecked(reminder.isActive());

            if (reminder.getType() == ReminderType.INTERVAL) {
                binding.toggleReminderType.check(R.id.btn_reminder_interval);
                startTimeMinutes = reminder.getStartTimeMinutes();
                endTimeMinutes = reminder.getEndTimeMinutes();
                binding.btnPickStartTime.setText(Reminder.minutesToTimeString(startTimeMinutes));
                binding.btnPickEndTime.setText(Reminder.minutesToTimeString(endTimeMinutes));
                int totalMinutes = reminder.getIntervalMinutes();
                binding.editIntervalHours.setText(String.valueOf(totalMinutes / 60));
                binding.editIntervalMinutes.setText(String.valueOf(totalMinutes % 60));
            } else {
                binding.toggleReminderType.check(R.id.btn_reminder_fixed);
                fixedTimeMinutes = reminder.getTimeOfDayMinutes();
                binding.btnPickTime.setText(Reminder.minutesToTimeString(fixedTimeMinutes));
            }
        });
    }
}
