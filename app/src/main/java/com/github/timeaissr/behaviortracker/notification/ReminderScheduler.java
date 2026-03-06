package com.github.timeaissr.behaviortracker.notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.github.timeaissr.behaviortracker.data.AppDatabase;
import com.github.timeaissr.behaviortracker.data.entity.Behavior;
import com.github.timeaissr.behaviortracker.data.entity.Reminder;
import com.github.timeaissr.behaviortracker.data.entity.ReminderType;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Manages scheduling and canceling of alarm-based reminders.
 * Uses AlarmManager for precise timing (both fixed-time and interval-based reminders).
 */
public final class ReminderScheduler {

    private static final int REQUEST_CODE_BASE = 10000;
    // For interval reminders, we use a different base to avoid collision with fixed time reminders
    private static final int INTERVAL_REQUEST_CODE_BASE = 50000;

    private ReminderScheduler() {}

    /**
     * Schedule a reminder based on its type.
     */
    public static void scheduleReminder(Context context, Reminder reminder, String behaviorName) {
        if (!reminder.isActive()) return;

        if (reminder.getType() == ReminderType.FIXED_TIME) {
            scheduleFixedTimeReminder(context, reminder, behaviorName);
        } else {
            scheduleIntervalReminder(context, reminder, behaviorName);
        }
    }

    /**
     * Schedule a daily fixed-time alarm.
     */
    private static void scheduleFixedTimeReminder(Context context, Reminder reminder, String behaviorName) {
        AlarmManager alarmManager = context.getSystemService(AlarmManager.class);
        PendingIntent pendingIntent = createPendingIntent(context, reminder, behaviorName,
                REQUEST_CODE_BASE + (int) reminder.getId());

        Calendar calendar = Calendar.getInstance();
        int hour = reminder.getTimeOfDayMinutes() / 60;
        int minute = reminder.getTimeOfDayMinutes() % 60;
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // If the time has already passed today, schedule for tomorrow
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        // Use setAlarmClock for precise timing that survives Doze mode
        AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(
                calendar.getTimeInMillis(), pendingIntent);
        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);
    }

    /**
     * Schedule the next interval-based alarm within the configured time window.
     */
    private static void scheduleIntervalReminder(Context context, Reminder reminder, String behaviorName) {
        AlarmManager alarmManager = context.getSystemService(AlarmManager.class);

        Calendar now = Calendar.getInstance();
        Calendar startCal = Calendar.getInstance();
        Calendar endCal = Calendar.getInstance();

        startCal.set(Calendar.HOUR_OF_DAY, reminder.getStartTimeMinutes() / 60);
        startCal.set(Calendar.MINUTE, reminder.getStartTimeMinutes() % 60);
        startCal.set(Calendar.SECOND, 0);
        startCal.set(Calendar.MILLISECOND, 0);

        endCal.set(Calendar.HOUR_OF_DAY, reminder.getEndTimeMinutes() / 60);
        endCal.set(Calendar.MINUTE, reminder.getEndTimeMinutes() % 60);
        endCal.set(Calendar.SECOND, 0);
        endCal.set(Calendar.MILLISECOND, 0);

        long intervalMillis = reminder.getIntervalMinutes() * 60 * 1000L;
        long nextTrigger;

        if (now.before(startCal)) {
            // Before window starts today: schedule at start time
            nextTrigger = startCal.getTimeInMillis();
        } else if (now.after(endCal)) {
            // After window ends today: schedule at start time tomorrow
            startCal.add(Calendar.DAY_OF_YEAR, 1);
            nextTrigger = startCal.getTimeInMillis();
        } else {
            // Within window: schedule at next interval point
            long timeSinceStart = now.getTimeInMillis() - startCal.getTimeInMillis();
            long intervalsElapsed = timeSinceStart / intervalMillis;
            nextTrigger = startCal.getTimeInMillis() + (intervalsElapsed + 1) * intervalMillis;

            // If next trigger is past the end of the window, schedule for tomorrow
            if (nextTrigger > endCal.getTimeInMillis()) {
                startCal.add(Calendar.DAY_OF_YEAR, 1);
                nextTrigger = startCal.getTimeInMillis();
            }
        }

        PendingIntent pendingIntent = createPendingIntent(context, reminder, behaviorName,
                INTERVAL_REQUEST_CODE_BASE + (int) reminder.getId());

        AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(
                nextTrigger, pendingIntent);
        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);
    }

    /**
     * Cancel all alarms associated with a behavior.
     */
    public static void cancelReminder(Context context, long behaviorId) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(context);
            Reminder reminder = db.reminderDao().getForBehaviorSync(behaviorId);
            if (reminder != null) {
                cancelReminderAlarms(context, reminder);
            }
        });
    }

    private static void cancelReminderAlarms(Context context, Reminder reminder) {
        AlarmManager alarmManager = context.getSystemService(AlarmManager.class);

        // Cancel fixed time alarm
        PendingIntent fixedPi = createPendingIntent(context, reminder, "",
                REQUEST_CODE_BASE + (int) reminder.getId());
        alarmManager.cancel(fixedPi);

        // Cancel interval alarm
        PendingIntent intervalPi = createPendingIntent(context, reminder, "",
                INTERVAL_REQUEST_CODE_BASE + (int) reminder.getId());
        alarmManager.cancel(intervalPi);
    }

    /**
     * Reschedule all active reminders (e.g., after device boot).
     */
    public static void rescheduleAllReminders(Context context) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(context);
            List<Reminder> activeReminders = db.reminderDao().getAllActiveSync();
            for (Reminder reminder : activeReminders) {
                Behavior behavior = db.behaviorDao().getByIdSync(reminder.getBehaviorId());
                if (behavior != null) {
                    scheduleReminder(context, reminder, behavior.getName());
                }
            }
        });
    }

    private static PendingIntent createPendingIntent(Context context, Reminder reminder,
                                                      String behaviorName, int requestCode) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra(ReminderReceiver.EXTRA_REMINDER_ID, reminder.getId());
        intent.putExtra(ReminderReceiver.EXTRA_BEHAVIOR_ID, reminder.getBehaviorId());
        intent.putExtra(ReminderReceiver.EXTRA_BEHAVIOR_NAME, behaviorName);
        intent.putExtra(ReminderReceiver.EXTRA_REMINDER_TYPE, reminder.getType().name());

        return PendingIntent.getBroadcast(context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}
