package com.github.timeaissr.behaviortracker.ui.add;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.github.timeaissr.behaviortracker.data.entity.Behavior;
import com.github.timeaissr.behaviortracker.data.entity.Reminder;
import com.github.timeaissr.behaviortracker.data.repository.BehaviorRepository;

public class AddBehaviorViewModel extends AndroidViewModel {

    private final BehaviorRepository repository;
    private final MutableLiveData<Boolean> saveComplete = new MutableLiveData<>();
    private long editingBehaviorId = -1;

    public AddBehaviorViewModel(@NonNull Application application) {
        super(application);
        repository = new BehaviorRepository(application);
    }

    public LiveData<Boolean> getSaveComplete() {
        return saveComplete;
    }

    public void setEditingBehaviorId(long id) {
        this.editingBehaviorId = id;
    }

    public long getEditingBehaviorId() {
        return editingBehaviorId;
    }

    public boolean isEditing() {
        return editingBehaviorId > 0;
    }

    public LiveData<Behavior> getBehavior(long id) {
        return repository.getBehaviorById(id);
    }

    public LiveData<Reminder> getReminder(long behaviorId) {
        return repository.getReminderForBehavior(behaviorId);
    }

    public void saveBehavior(Behavior behavior, Reminder reminder) {
        if (isEditing()) {
            behavior.setId(editingBehaviorId);
            repository.updateBehavior(behavior);

            if (reminder != null) {
                reminder.setBehaviorId(editingBehaviorId);
                repository.getExecutor().execute(() -> {
                    Reminder existing = repository.getReminderForBehaviorSync(editingBehaviorId);
                    if (existing != null) {
                        reminder.setId(existing.getId());
                        repository.updateReminder(reminder);
                    } else {
                        repository.insertReminder(reminder, null);
                    }
                    saveComplete.postValue(true);
                });
            } else {
                repository.deleteReminderForBehavior(editingBehaviorId);
                saveComplete.postValue(true);
            }
        } else {
            repository.insertBehavior(behavior, id -> {
                if (reminder != null) {
                    reminder.setBehaviorId(id);
                    repository.insertReminder(reminder, null);
                }
                saveComplete.postValue(true);
            });
        }
    }

    public void deleteBehavior(long behaviorId) {
        repository.getExecutor().execute(() -> {
            Behavior behavior = repository.getBehaviorByIdSync(behaviorId);
            if (behavior != null) {
                repository.deleteBehavior(behavior);
            }
            saveComplete.postValue(true);
        });
    }
}
