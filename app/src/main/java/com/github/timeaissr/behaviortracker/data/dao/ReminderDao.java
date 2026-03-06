package com.github.timeaissr.behaviortracker.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.github.timeaissr.behaviortracker.data.entity.Reminder;

import java.util.List;

@Dao
public interface ReminderDao {

    @Insert
    long insert(Reminder reminder);

    @Update
    void update(Reminder reminder);

    @Delete
    void delete(Reminder reminder);

    @Query("SELECT * FROM reminders WHERE behaviorId = :behaviorId")
    LiveData<Reminder> getForBehavior(long behaviorId);

    @Query("SELECT * FROM reminders WHERE behaviorId = :behaviorId")
    Reminder getForBehaviorSync(long behaviorId);

    /** Get all active reminders (for rescheduling after boot). */
    @Query("SELECT * FROM reminders WHERE active = 1")
    List<Reminder> getAllActiveSync();

    /** Get all reminders (for export). */
    @Query("SELECT * FROM reminders")
    List<Reminder> getAllSync();

    /** Delete reminder for a behavior. */
    @Query("DELETE FROM reminders WHERE behaviorId = :behaviorId")
    void deleteForBehavior(long behaviorId);

    /** Insert multiple reminders (for import). */
    @Insert
    void insertAll(List<Reminder> reminders);

    /** Delete all reminders. */
    @Query("DELETE FROM reminders")
    void deleteAll();
}
