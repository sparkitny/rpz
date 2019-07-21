package de.parkitny.fit.myfit.app.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import de.parkitny.fit.myfit.app.entities.WorkoutItem;

/**
 * Created by Sebastian on 27.09.2017.
 */
@Dao
public interface WorkoutItemDao {

    @Query("select * from WorkoutItem where workoutId = :workoutId order by orderId")
    List<WorkoutItem> getByWorkoutId(long workoutId);

    @Query("select * from WorkoutItem where workoutId = :workoutId order by starttime")
    List<WorkoutItem> getByWorkoutIdOrderStartTime(long workoutId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(WorkoutItem workoutItem);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<WorkoutItem> workoutItems);

    @Delete
    void delete(WorkoutItem workoutItem);

    @Query("delete from WorkoutItem where workoutId = :workoutId")
    void deleteById(long workoutId);
}
