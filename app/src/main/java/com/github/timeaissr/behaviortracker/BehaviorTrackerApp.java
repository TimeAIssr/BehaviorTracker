package com.github.timeaissr.behaviortracker;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;

import com.google.android.material.color.DynamicColors;

public class BehaviorTrackerApp extends Application {

    public static final String NOTIFICATION_CHANNEL_ID = "behavior_reminder_channel";

    @Override
    public void onCreate() {
        super.onCreate();

        // Apply Material Dynamic Colors
        DynamicColors.applyToActivitiesIfAvailable(this);

        // Create notification channel
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "行为提醒",
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription("用于提醒您记录行为的通知");
        channel.enableVibration(true);
        channel.setShowBadge(true);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }
}
