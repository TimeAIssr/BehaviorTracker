package com.github.timeaissr.behaviortracker.notification;

import android.Manifest;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.github.timeaissr.behaviortracker.BehaviorTrackerApp;
import com.github.timeaissr.behaviortracker.R;
import com.github.timeaissr.behaviortracker.data.AppDatabase;
import com.github.timeaissr.behaviortracker.data.entity.Behavior;
import com.github.timeaissr.behaviortracker.data.entity.Reminder;
import com.github.timeaissr.behaviortracker.data.entity.ReminderType;

import java.util.concurrent.Executors;

/**
 * BroadcastReceiver that fires when an alarm triggers.
 * Posts a notification and reschedules the next alarm if needed.
 */
public class ReminderReceiver extends BroadcastReceiver {

    public static final String EXTRA_REMINDER_ID = "extra_reminder_id";
    public static final String EXTRA_BEHAVIOR_ID = "extra_behavior_id";
    public static final String EXTRA_BEHAVIOR_NAME = "extra_behavior_name";
    public static final String EXTRA_REMINDER_TYPE = "extra_reminder_type";

    @Override
    public void onReceive(Context context, Intent intent) {
        long reminderId = intent.getLongExtra(EXTRA_REMINDER_ID, -1);
        long behaviorId = intent.getLongExtra(EXTRA_BEHAVIOR_ID, -1);
        String behaviorName = intent.getStringExtra(EXTRA_BEHAVIOR_NAME);
        String reminderTypeStr = intent.getStringExtra(EXTRA_REMINDER_TYPE);

        if (behaviorId < 0 || behaviorName == null) return;

        // Check notification permission
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Post notification
        postNotification(context, behaviorId, behaviorName);

        // Reschedule: for both fixed time (next day) and interval (next interval)
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(context);
            Reminder reminder = db.reminderDao().getForBehaviorSync(behaviorId);
            if (reminder != null && reminder.isActive()) {
                Behavior behavior = db.behaviorDao().getByIdSync(behaviorId);
                String name = behavior != null ? behavior.getName() : behaviorName;
                ReminderScheduler.scheduleReminder(context, reminder, name);
            }
        });
    }

    private void postNotification(Context context, long behaviorId, String behaviorName) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context, BehaviorTrackerApp.NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentTitle(context.getString(R.string.reminder_notification_title))
                .setContentText(String.format(
                        context.getString(R.string.reminder_notification_text), behaviorName))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL);

        NotificationManager notificationManager =
                context.getSystemService(NotificationManager.class);
        // Use behaviorId as notification ID so each behavior has its own notification
        notificationManager.notify((int) behaviorId, builder.build());
    }
}
