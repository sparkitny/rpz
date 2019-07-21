package de.parkitny.fit.myfit.app.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import de.parkitny.fit.myfit.app.entities.Exercise;

/**
 * Created by Sebastian on 26.09.2017.
 */
@Dao
public interface ExerciseDao {

    @Query("select * from Exercise order by name")
    List<Exercise> getAll();

    @Query("select * from Exercise order by name")
    LiveData<List<Exercise>> getAllLive();

    @Query("select count(*) from Exercise")
    int getSize();

    @Query("select * from Exercise where id = :id")
    Exercise getExerciseById(long id);

    @Query("select * from Exercise where id = :id")
    LiveData<Exercise> getExerciseByIdLive(long id);

    @Query("select * from Exercise where name = :name")
    Exercise getExerciseByName(String name);

    @Query("select * from Exercise where hiddenKey = :hiddenKey")
    Exercise getExerciseByHiddenKey(String hiddenKey);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Exercise exercise);
}
