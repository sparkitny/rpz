package de.parkitny.fit.myfit.app.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * A Workout takes a {@link Routine} and uses it to run the workout.
 */
@Entity
public class Workout {

    @PrimaryKey(autoGenerate = true)
    public long id = 0;
    /**
     * The firstLine of the {@link Routine} that has been executed
     */
    public String name;
    /**
     * The start time in milliseconds
     */
    public long startTime;
    /**
     * The total pause of this {@link Workout}
     */
    public long totalPause;
    /**
     * The duration of the {@link Workout}
     */
    public long workoutDuration = 0;
}
