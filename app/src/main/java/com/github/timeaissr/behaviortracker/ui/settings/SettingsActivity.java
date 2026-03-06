package com.github.timeaissr.behaviortracker.ui.settings;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.github.timeaissr.behaviortracker.R;
import com.github.timeaissr.behaviortracker.databinding.ActivitySettingsBinding;
import com.github.timeaissr.behaviortracker.export.DataManager;
import com.github.timeaissr.behaviortracker.notification.ReminderScheduler;
import com.google.android.material.snackbar.Snackbar;

import java.util.concurrent.Executors;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;
    private DataManager dataManager;

    // SAF launchers
    private final ActivityResultLauncher<Intent> exportLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        performExport(uri);
                    }
                }
            });

    private final ActivityResultLauncher<Intent> importLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        performImport(uri);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dataManager = new DataManager(this);

        setupToolbar();
        setupTheme();
        setupDataButtons();
        showVersion();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupTheme() {
        // Load current theme preference
        int currentMode = AppCompatDelegate.getDefaultNightMode();
        switch (currentMode) {
            case AppCompatDelegate.MODE_NIGHT_YES:
                binding.radioDark.setChecked(true);
                break;
            case AppCompatDelegate.MODE_NIGHT_NO:
                binding.radioLight.setChecked(true);
                break;
            default:
                binding.radioSystem.setChecked(true);
                break;
        }

        binding.radioTheme.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_system) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            } else if (checkedId == R.id.radio_light) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } else if (checkedId == R.id.radio_dark) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
        });
    }

    private void setupDataButtons() {
        binding.btnExport.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/json");
            intent.putExtra(Intent.EXTRA_TITLE, "behavior_tracker_backup.json");
            exportLauncher.launch(intent);
        });

        binding.btnImport.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/json");
            importLauncher.launch(intent);
        });
    }

    private void performExport(Uri uri) {
        Executors.newSingleThreadExecutor().execute(() -> {
            boolean success = dataManager.exportData(uri);
            runOnUiThread(() -> {
                Snackbar.make(binding.getRoot(),
                        success ? R.string.export_success : R.string.import_error,
                        Snackbar.LENGTH_SHORT).show();
            });
        });
    }

    private void performImport(Uri uri) {
        Executors.newSingleThreadExecutor().execute(() -> {
            boolean success = dataManager.importData(uri);
            runOnUiThread(() -> {
                if (success) {
                    Snackbar.make(binding.getRoot(), R.string.import_success,
                            Snackbar.LENGTH_SHORT).show();
                    // Reschedule reminders after import
                    ReminderScheduler.rescheduleAllReminders(this);
                } else {
                    Snackbar.make(binding.getRoot(), R.string.import_error,
                            Snackbar.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void showVersion() {
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            binding.textVersion.setText(String.format(getString(R.string.version), pInfo.versionName));
        } catch (PackageManager.NameNotFoundException e) {
            binding.textVersion.setText(String.format(getString(R.string.version), "1.0.0"));
        }
    }
}
