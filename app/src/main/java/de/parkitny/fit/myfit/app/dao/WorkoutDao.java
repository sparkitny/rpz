package de.parkitny.fit.myfit.app.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import org.joda.time.DateTime;

import java.util.List;

import de.parkitny.fit.myfit.app.entities.Workout;

/**
 * Created by Sebastian on 27.09.2017.
 */
@Dao
public interface WorkoutDao {

    @Query("select * from Workout order by startTime asc")
    List<Workout> getAll();

    @Query("select * from Workout where id = :workoutId order by starttime")
    Workout getById(long workoutId);

    @Query("select * from Workout where :start <= startTime and startTime < :end")
    List<Workout> getWorkoutsBetween(DateTime start, DateTime end);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Workout workout);

    @Update
    void update(Workout workout);

    @Delete
    void delete(Workout workout);
}
