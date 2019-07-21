package de.parkitny.fit.myfit.app.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import de.parkitny.fit.myfit.app.ui.common.Utils;

/**
 * Created by sebas on 26.12.2015.
 */
@Entity(
        foreignKeys = {
                @ForeignKey(
                        entity = Workout.class,
                        parentColumns = "id",
                        childColumns = "workoutId"
                )
        },
        indices = {
                @Index(value = "workoutId")
        }
)
public class WorkoutItem {

    @PrimaryKey(autoGenerate = true)
    public long id = 0;

    /**
     * The id of the {@link ExerciseConfiguration}
     */
    public long exerciseConfigurationId = 0;

    /**
     * The firstLine and the quantity of the {@link Exercise}
     */
    public String exerciseNameAndQuantity;
    /**
     * The duration of the exercise in milliseconds
     */
    public long duration;
    /**
     * the start time
     */
    public long starttime;
    /**
     * The pause of this {@link WorkoutItem}
     */
    public long pause;
    /**
     * The firstLine of the {@link Exercise}
     */
    public String name;
    /**
     * The type of the {@link Exercise}
     */
    public ExerciseType exerciseType;
    /**
     * the repetitions of the {@link ExerciseConfiguration}
     */
    public int repetitions;
    /**
     * the planned duration of the {@link ExerciseConfiguration}
     */
    public long plannedDuration;
    /**
     * the orderid of the {@link Exercise} executed
     */
    public long orderId;
    /**
     * The id of the {@link Workout}
     */
    public long workoutId;
    /**
     * The round if the {@link Workout}
     */
    public int round;
    /**
     * The set of the {@link WorkoutItem}
     */
    public int set;
    /**
     * The weight of the {@link WorkoutItem}
     */
    public double weight;

    /**
     * the id to group the {@link WorkoutItem}s together
     */
    public String globalId;

    public WorkoutItem() {
    }

    /**
     * Initializes the class for later storage
     *
     * @param workoutId             the id of the {@link Workout}
     * @param exerciseName the firstLine of the {@link Exercise}
     * @param exerciseConfiguration the {@link ExerciseConfiguration} executed
     */
    public WorkoutItem(long workoutId, String exerciseName, ExerciseConfiguration exerciseConfiguration) {

        this.globalId = exerciseConfiguration.globalId;
        this.workoutId = workoutId;
        this.exerciseConfigurationId = exerciseConfiguration.id;
        this.exerciseType = exerciseConfiguration.exerciseType;
        this.name = exerciseName;
        this.orderId = exerciseConfiguration.order;
        this.starttime = System.currentTimeMillis();
        this.weight = exerciseConfiguration.weight;

        switch (exerciseConfiguration.exerciseType) {
            case Repetition:

                this.exerciseNameAndQuantity = (String.format("%s %s",
                        exerciseConfiguration.repetitions,
                        exerciseName));
                this.repetitions = exerciseConfiguration.repetitions;
                break;
            case Time:
                this.exerciseNameAndQuantity = (String.format("%s %s",
                        Utils.formatPeriod(exerciseConfiguration.duration),
                        exerciseName));
                this.plannedDuration = exerciseConfiguration.duration;
                break;
        }
    }
}
