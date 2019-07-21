package de.parkitny.fit.myfit.app.ui.callables;

import java.util.List;
import java.util.concurrent.Callable;

import de.parkitny.fit.myfit.app.RpzApplication;
import de.parkitny.fit.myfit.app.entities.WorkoutItem;

/**
 * Created by Sebastian on 30.08.2016.
 */

public class SlowestWorkoutItemCallable implements Callable<WorkoutItem> {

    private final long workoutId;

    public SlowestWorkoutItemCallable(long workoutId) {
        this.workoutId = workoutId;
    }

    @Override
    public WorkoutItem call() throws Exception {

        List<WorkoutItem> result = RpzApplication.DB.workoutItemDao().getByWorkoutIdOrderStartTime(workoutId);

        if (result == null || result.size() <= 0) return null;

        WorkoutItem current = result.get(0);

        for (int i = 1; i < result.size(); i++) {
            if (result.get(i).duration > current.duration) {
                current = result.get(i);
            }
        }

        return current;
    }
}
