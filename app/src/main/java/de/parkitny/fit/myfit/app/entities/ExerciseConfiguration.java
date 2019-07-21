package de.parkitny.fit.myfit.app.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import java.util.UUID;

/**
 * Created by sebas on 14.11.2015.
 */
@Entity(foreignKeys = {
        @ForeignKey(entity = Exercise.class, parentColumns = "id", childColumns = "exerciseId"),
        @ForeignKey(entity = Routine.class, parentColumns = "id", childColumns = "routineId")},
        indices = {
                @Index(value = "exerciseId"),
                @Index(value = "routineId")})
public class ExerciseConfiguration {

    /**
     * The id is by default set to 0 to signal that this is a new
     * and unsafed instance. Otherwise it's an already stored instance
     */
    @PrimaryKey(autoGenerate = true)
    public long id = 0;

    /**
     * the id of the {@link Routine}
     */
    public long routineId;

    /**
     * the position of the {@link ExerciseConfiguration} in the {@link Routine}
     */
    public int order;

    /**
     * the type of this special {@link Exercise} within the {@link Routine}
     */
    public ExerciseType exerciseType;

    /**
     * the number of repetitions --> only valid when of {@link ExerciseType} Repetitions
     */
    public int repetitions;

    /**
     * the duration in miliseconds --> only valid when of {@link ExerciseType} Time
     */
    public long duration;

    /**
     * the number of sets to be done
     */
    public int sets;

    /**
     * the current weight used in this exercise
     */
    public double weight;

    /**
     * the pause in ms to start the next {@link ExerciseConfiguration}
     */
    public long pause;

    /**
     * Indicates whether the {@link Routine} can overwrite the pause of the {@link ExerciseConfiguration}
     */
    public boolean canOverwritePause = true;

    /**
     * id used to identify {@link ExerciseConfiguration} of the same set
     */
    public String globalId;

    /**
     * the id of the {@link Exercise}
     */
    public long exerciseId;

    @Ignore
    public boolean ticked;

    @Ignore
    public ExerciseConfiguration(int order, long exerciseId) {
        this();
        this.order = order;
        this.exerciseId = exerciseId;
    }

    public ExerciseConfiguration() {
        globalId = (UUID.randomUUID().toString());
    }

    /**
     * Makes a deep copy of the {@link ExerciseConfiguration}
     *
     * @return a deep copy of the {@link ExerciseConfiguration}
     */
    public ExerciseConfiguration copy() {

        ExerciseConfiguration exerciseConfiguration = new ExerciseConfiguration(order, 0);
        exerciseConfiguration.routineId = routineId;
        exerciseConfiguration.order = order;
        exerciseConfiguration.exerciseType = exerciseType;
        exerciseConfiguration.repetitions = repetitions;
        exerciseConfiguration.duration = duration;
        exerciseConfiguration.sets = sets;
        exerciseConfiguration.pause = pause;
        exerciseConfiguration.canOverwritePause = canOverwritePause;
        exerciseConfiguration.exerciseId = exerciseId;
        exerciseConfiguration.weight = weight;

        return exerciseConfiguration;
    }
}
