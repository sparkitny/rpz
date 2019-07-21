package de.parkitny.fit.myfit.app.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import de.parkitny.fit.myfit.app.dao.ExerciseConfigurationDao;
import de.parkitny.fit.myfit.app.dao.ExerciseDao;
import de.parkitny.fit.myfit.app.dao.ExerciseEmphasisDao;
import de.parkitny.fit.myfit.app.dao.RoutineDao;
import de.parkitny.fit.myfit.app.dao.WeightEntryDao;
import de.parkitny.fit.myfit.app.dao.WorkoutDao;
import de.parkitny.fit.myfit.app.dao.WorkoutItemDao;
import de.parkitny.fit.myfit.app.entities.Exercise;
import de.parkitny.fit.myfit.app.entities.ExerciseConfiguration;
import de.parkitny.fit.myfit.app.entities.ExerciseEmphasis;
import de.parkitny.fit.myfit.app.entities.Routine;
import de.parkitny.fit.myfit.app.entities.WeightEntry;
import de.parkitny.fit.myfit.app.entities.Workout;
import de.parkitny.fit.myfit.app.entities.WorkoutItem;

/**
 * Created by Sebastian on 27.09.2017.
 */
@Database(version = 11,
        exportSchema = true,
        entities = {
                Exercise.class,
                ExerciseConfiguration.class,
                Workout.class,
                WorkoutItem.class,
                Routine.class,
                WeightEntry.class,
                ExerciseEmphasis.class})
@TypeConverters(value = {Converters.class})
public abstract class FitDB extends RoomDatabase {

    public abstract ExerciseDao exerciseDao();

    public abstract ExerciseConfigurationDao exerciseConfigurationDao();

    public abstract RoutineDao routineDao();

    public abstract WorkoutItemDao workoutItemDao();

    public abstract WorkoutDao workoutDao();

    public abstract WeightEntryDao weightEntryDao();

    public abstract ExerciseEmphasisDao exerciseEmphasisDao();
}
