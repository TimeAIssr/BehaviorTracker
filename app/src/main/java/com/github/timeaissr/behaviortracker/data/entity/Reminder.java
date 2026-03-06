package com.github.timeaissr.behaviortracker.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Represents a reminder configuration for a behavior.
 * Supports two modes:
 * - FIXED_TIME: Fires at a specific time each day.
 * - INTERVAL: Fires every N minutes within a start-end time window.
 */
@Entity(
    tableName = "reminders",
    foreignKeys = @ForeignKey(
        entity = Behavior.class,
        parentColumns = "id",
        childColumns = "behaviorId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = @Index("behaviorId")
)
public class Reminder {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private long behaviorId;

    /** Whether this reminder is currently active. */
    private boolean active;

    private ReminderType type;

    /**
     * For FIXED_TIME: The time of day in minutes since midnight (e.g., 510 = 08:30).
     * For INTERVAL: Not used.
     */
    private int timeOfDayMinutes;

    /**
     * For INTERVAL: Start of the time window in minutes since midnight.
     * For FIXED_TIME: Not used.
     */
    private int startTimeMinutes;

    /**
     * For INTERVAL: End of the time window in minutes since midnight.
     * For FIXED_TIME: Not used.
     */
    private int endTimeMinutes;

    /**
     * For INTERVAL: The interval between reminders in minutes.
     * For FIXED_TIME: Not used.
     */
    private int intervalMinutes;

    public Reminder() {
        this.active = true;
        this.type = ReminderType.FIXED_TIME;
        this.timeOfDayMinutes = 480; // Default: 08:00
        this.startTimeMinutes = 540; // Default: 09:00
        this.endTimeMinutes = 1080; // Default: 18:00
        this.intervalMinutes = 120;  // Default: every 2 hours
    }

    // Getters and Setters

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getBehaviorId() {
        return behaviorId;
    }

    public void setBehaviorId(long behaviorId) {
        this.behaviorId = behaviorId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public ReminderType getType() {
        return type;
    }

    public void setType(ReminderType type) {
        this.type = type;
    }

    public int getTimeOfDayMinutes() {
        return timeOfDayMinutes;
    }

    public void setTimeOfDayMinutes(int timeOfDayMinutes) {
        this.timeOfDayMinutes = timeOfDayMinutes;
    }

    public int getStartTimeMinutes() {
        return startTimeMinutes;
    }

    public void setStartTimeMinutes(int startTimeMinutes) {
        this.startTimeMinutes = startTimeMinutes;
    }

    public int getEndTimeMinutes() {
        return endTimeMinutes;
    }

    public void setEndTimeMinutes(int endTimeMinutes) {
        this.endTimeMinutes = endTimeMinutes;
    }

    public int getIntervalMinutes() {
        return intervalMinutes;
    }

    public void setIntervalMinutes(int intervalMinutes) {
        this.intervalMinutes = intervalMinutes;
    }

    /** Helper: Convert minutes since midnight to "HH:mm" format. */
    public static String minutesToTimeString(int minutes) {
        int hours = minutes / 60;
        int mins = minutes % 60;
        return String.format("%02d:%02d", hours, mins);
    }

    /** Helper: Convert "HH:mm" to minutes since midnight. */
    public static int timeStringToMinutes(String time) {
        String[] parts = time.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }
}
