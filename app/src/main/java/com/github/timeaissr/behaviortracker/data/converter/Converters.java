package com.github.timeaissr.behaviortracker.data.converter;

import androidx.room.TypeConverter;

import com.github.timeaissr.behaviortracker.data.entity.RecordType;
import com.github.timeaissr.behaviortracker.data.entity.ReminderType;

/**
 * Room TypeConverters for enum types.
 */
public class Converters {

    @TypeConverter
    public static String fromRecordType(RecordType type) {
        return type == null ? null : type.name();
    }

    @TypeConverter
    public static RecordType toRecordType(String value) {
        return value == null ? null : RecordType.valueOf(value);
    }

    @TypeConverter
    public static String fromReminderType(ReminderType type) {
        return type == null ? null : type.name();
    }

    @TypeConverter
    public static ReminderType toReminderType(String value) {
        return value == null ? null : ReminderType.valueOf(value);
    }
}
