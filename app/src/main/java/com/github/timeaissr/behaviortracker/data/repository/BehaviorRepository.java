package com.github.timeaissr.behaviortracker.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.github.timeaissr.behaviortracker.data.AppDatabase;
import com.github.timeaissr.behaviortracker.data.dao.BehaviorDao;
import com.github.timeaissr.behaviortracker.data.dao.RecordDao;
import com.github.timeaissr.behaviortracker.data.dao.ReminderDao;
import com.github.timeaissr.behaviortracker.data.entity.Behavior;
import com.github.timeaissr.behaviortracker.data.entity.Record;
import com.github.timeaissr.behaviortracker.data.entity.Reminder;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Single source of truth for all data operations.
 * Mediates between ViewModels and Room DAOs.
 */
public class BehaviorRepository {

    private final BehaviorDao behaviorDao;
    private final RecordDao recordDao;
    private final ReminderDao reminderDao;
    private final ExecutorService executor;

    public BehaviorRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        behaviorDao = db.behaviorDao();
        recordDao = db.recordDao();
        reminderDao = db.reminderDao();
        executor = Executors.newFixedThreadPool(4);
    }

    // ==================== Behavior Operations ====================

    public LiveData<List<Behavior>> getAllActiveBehaviors() {
        return behaviorDao.getAllActive();
    }

    public LiveData<List<Behavior>> getAllBehaviors() {
        return behaviorDao.getAll();
    }

    public LiveData<Behavior> getBehaviorById(long id) {
        return behaviorDao.getById(id);
    }

    public void insertBehavior(Behavior behavior, OnInsertCallback callback) {
        executor.execute(() -> {
            long id = behaviorDao.insert(behavior);
            if (callback != null) {
                callback.onInserted(id);
            }
        });
    }

    public void updateBehavior(Behavior behavior) {
        executor.execute(() -> behaviorDao.update(behavior));
    }

    public void deleteBehavior(Behavior behavior) {
        executor.execute(() -> behaviorDao.delete(behavior));
    }

    // ==================== Record Operations ====================

    public LiveData<List<Record>> getRecordsForBehavior(long behaviorId) {
        return recordDao.getRecordsForBehavior(behaviorId);
    }

    public LiveData<List<Record>> getRecordsInRange(long behaviorId, long startTime, long endTime) {
        return recordDao.getRecordsInRange(behaviorId, startTime, endTime);
    }

    public LiveData<Integer> getRecordCountForDay(long behaviorId, long dayStart, long dayEnd) {
        return recordDao.getRecordCountForDay(behaviorId, dayStart, dayEnd);
    }

    public LiveData<Double> getSumInRange(long behaviorId, long startTime, long endTime) {
        return recordDao.getSumInRange(behaviorId, startTime, endTime);
    }

    public void insertRecord(Record record) {
        executor.execute(() -> recordDao.insert(record));
    }

    public void deleteRecord(Record record) {
        executor.execute(() -> recordDao.delete(record));
    }

    // ==================== Reminder Operations ====================

    public LiveData<Reminder> getReminderForBehavior(long behaviorId) {
        return reminderDao.getForBehavior(behaviorId);
    }

    public void insertReminder(Reminder reminder, OnInsertCallback callback) {
        executor.execute(() -> {
            long id = reminderDao.insert(reminder);
            if (callback != null) {
                callback.onInserted(id);
            }
        });
    }

    public void updateReminder(Reminder reminder) {
        executor.execute(() -> reminderDao.update(reminder));
    }

    public void deleteReminderForBehavior(long behaviorId) {
        executor.execute(() -> reminderDao.deleteForBehavior(behaviorId));
    }

    // ==================== Sync Operations (for export/import) ====================

    public List<Behavior> getAllBehaviorsSync() {
        return behaviorDao.getAllSync();
    }

    public List<Record> getAllRecordsSync() {
        return recordDao.getAllSync();
    }

    public List<Reminder> getAllRemindersSync() {
        return reminderDao.getAllSync();
    }

    public List<Record> getRecordsForBehaviorSync(long behaviorId) {
        return recordDao.getRecordsForBehaviorSync(behaviorId);
    }

    public List<Record> getRecordsInRangeSync(long behaviorId, long startTime, long endTime) {
        return recordDao.getRecordsInRangeSync(behaviorId, startTime, endTime);
    }

    public Reminder getReminderForBehaviorSync(long behaviorId) {
        return reminderDao.getForBehaviorSync(behaviorId);
    }

    public Behavior getBehaviorByIdSync(long id) {
        return behaviorDao.getByIdSync(id);
    }

    public double getTotalSumSync(long behaviorId) {
        return recordDao.getTotalSumSync(behaviorId);
    }

    public int getTotalCountSync(long behaviorId) {
        return recordDao.getTotalCountSync(behaviorId);
    }

    public Long getEarliestTimestampSync(long behaviorId) {
        return recordDao.getEarliestTimestampSync(behaviorId);
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    // Callback interface
    public interface OnInsertCallback {
        void onInserted(long id);
    }
}
