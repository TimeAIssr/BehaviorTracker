package com.github.timeaissr.behaviortracker.ui.detail;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.github.timeaissr.behaviortracker.data.entity.Behavior;
import com.github.timeaissr.behaviortracker.data.entity.Record;
import com.github.timeaissr.behaviortracker.data.repository.BehaviorRepository;
import com.github.timeaissr.behaviortracker.util.DateUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DetailViewModel extends AndroidViewModel {

    private final BehaviorRepository repository;

    public DetailViewModel(@NonNull Application application) {
        super(application);
        repository = new BehaviorRepository(application);
    }

    public LiveData<Behavior> getBehavior(long id) {
        return repository.getBehaviorById(id);
    }

    public LiveData<List<Record>> getRecords(long behaviorId) {
        return repository.getRecordsForBehavior(behaviorId);
    }

    public LiveData<List<Record>> getRecordsInRange(long behaviorId, long startTime, long endTime) {
        return repository.getRecordsInRange(behaviorId, startTime, endTime);
    }

    /**
     * Calculate stats in background and post results.
     */
    public LiveData<StatsData> calculateStats(long behaviorId, boolean isBoolean) {
        MutableLiveData<StatsData> result = new MutableLiveData<>();
        repository.getExecutor().execute(() -> {
            StatsData stats = new StatsData();
            stats.totalCount = repository.getTotalCountSync(behaviorId);
            stats.totalSum = repository.getTotalSumSync(behaviorId);

            if (isBoolean) {
                // Calculate streaks
                List<Record> allRecords = repository.getRecordsForBehaviorSync(behaviorId);
                calculateStreaks(allRecords, stats);
            } else {
                // Calculate daily average
                Long earliest = repository.getEarliestTimestampSync(behaviorId);
                if (earliest != null && stats.totalCount > 0) {
                    long days = Math.max(1,
                            (System.currentTimeMillis() - earliest) / (24 * 60 * 60 * 1000) + 1);
                    stats.dailyAverage = stats.totalSum / days;
                }
            }

            result.postValue(stats);
        });
        return result;
    }

    private void calculateStreaks(List<Record> records, StatsData stats) {
        if (records == null || records.isEmpty()) {
            stats.currentStreak = 0;
            stats.longestStreak = 0;
            return;
        }

        // Collect unique days
        List<Long> uniqueDays = new ArrayList<>();
        for (Record record : records) {
            long dayStart = DateUtils.getStartOfDay(record.getTimestamp());
            if (uniqueDays.isEmpty() || uniqueDays.get(uniqueDays.size() - 1) != dayStart) {
                uniqueDays.add(dayStart);
            }
        }

        // Sort days in descending order for current streak
        uniqueDays.sort((a, b) -> Long.compare(b, a));

        // Current streak
        int currentStreak = 0;
        long today = DateUtils.getStartOfDay();
        long checkDay = today;

        for (Long day : uniqueDays) {
            if (day == checkDay) {
                currentStreak++;
                checkDay -= 24 * 60 * 60 * 1000;
            } else if (day < checkDay) {
                break;
            }
        }
        stats.currentStreak = currentStreak;

        // Longest streak - sort ascending
        uniqueDays.sort(Long::compare);
        int longestStreak = 0;
        int streak = 1;
        for (int i = 1; i < uniqueDays.size(); i++) {
            long diff = uniqueDays.get(i) - uniqueDays.get(i - 1);
            if (diff == 24 * 60 * 60 * 1000) {
                streak++;
            } else {
                longestStreak = Math.max(longestStreak, streak);
                streak = 1;
            }
        }
        longestStreak = Math.max(longestStreak, streak);
        stats.longestStreak = longestStreak;
    }

    public void deleteRecord(Record record) {
        repository.deleteRecord(record);
    }

    public void insertRecord(long behaviorId, double value, String note, long timestamp) {
        Record record = new Record();
        record.setBehaviorId(behaviorId);
        record.setValue(value);
        record.setNote(note);
        record.setTimestamp(timestamp);
        repository.insertRecord(record);
    }

    public BehaviorRepository getRepository() {
        return repository;
    }

    /**
     * Data holder for computed statistics.
     */
    public static class StatsData {
        public int totalCount = 0;
        public double totalSum = 0;
        public int currentStreak = 0;
        public int longestStreak = 0;
        public double dailyAverage = 0;
    }
}
