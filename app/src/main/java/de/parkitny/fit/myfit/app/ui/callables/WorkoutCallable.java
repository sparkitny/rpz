package de.parkitny.fit.myfit.app.ui.callables;

import java.util.concurrent.Callable;

import de.parkitny.fit.myfit.app.RpzApplication;
import de.parkitny.fit.myfit.app.entities.Workout;

/**
 * Created by Sebastian on 04.09.2016.
 */

public class WorkoutCallable implements Callable<Workout> {

    private final long workoutId;

    public WorkoutCallable(long workoutId) {
        this.workoutId = workoutId;
    }

    @Override
    public Workout call() throws Exception {

        return RpzApplication.DB.workoutDao().getById(workoutId);
    }
}
