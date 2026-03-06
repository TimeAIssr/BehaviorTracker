package com.github.timeaissr.behaviortracker.ui.main;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.github.timeaissr.behaviortracker.data.entity.Behavior;
import com.github.timeaissr.behaviortracker.data.entity.Record;
import com.github.timeaissr.behaviortracker.data.entity.RecordType;
import com.github.timeaissr.behaviortracker.data.repository.BehaviorRepository;

import java.util.List;

public class MainViewModel extends AndroidViewModel {

    private final BehaviorRepository repository;
    private final LiveData<List<Behavior>> allActiveBehaviors;

    public MainViewModel(@NonNull Application application) {
        super(application);
        repository = new BehaviorRepository(application);
        allActiveBehaviors = repository.getAllActiveBehaviors();
    }

    public LiveData<List<Behavior>> getAllActiveBehaviors() {
        return allActiveBehaviors;
    }

    public LiveData<Integer> getRecordCountForDay(long behaviorId, long dayStart, long dayEnd) {
        return repository.getRecordCountForDay(behaviorId, dayStart, dayEnd);
    }

    public LiveData<Double> getSumInRange(long behaviorId, long startTime, long endTime) {
        return repository.getSumInRange(behaviorId, startTime, endTime);
    }

    /** Quick-log a boolean behavior (just inserts a record with value 1). */
    public void quickLogBoolean(long behaviorId) {
        Record record = new Record();
        record.setBehaviorId(behaviorId);
        record.setValue(1.0);
        record.setTimestamp(System.currentTimeMillis());
        repository.insertRecord(record);
    }

    /** Quick-log a numeric behavior with a given value and optional note. */
    public void quickLogNumeric(long behaviorId, double value, String note) {
        Record record = new Record();
        record.setBehaviorId(behaviorId);
        record.setValue(value);
        record.setNote(note);
        record.setTimestamp(System.currentTimeMillis());
        repository.insertRecord(record);
    }

    public BehaviorRepository getRepository() {
        return repository;
    }
}
