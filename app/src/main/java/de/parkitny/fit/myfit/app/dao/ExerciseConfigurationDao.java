package de.parkitny.fit.myfit.app.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import de.parkitny.fit.myfit.app.entities.ExerciseConfiguration;
import de.parkitny.fit.myfit.app.utils.ExerciseConfigurationView;

/**
 * Created by Sebastian on 26.09.2017.
 */
@Dao
public interface ExerciseConfigurationDao {

    @Query("select " +
            "ex.id as configId, " +
            "ex.routineId, " +
            "ex.[order] as orderId, " +
            "ex.exerciseType, " +
            "ex.exerciseId, " +
            "ex.repetitions, " +
            "ex.duration, " +
            "ex.sets, " +
            "ex.pause, " +
            "ex.globalId, " +
            "c.name as exerciseName, " +
            "ex.weight " +
            "from Exercise as c, ExerciseConfiguration as ex " +
            "where ex.routineId = :routineId and ex.exerciseId = c.id " +
            "order by orderId asc")
    List<ExerciseConfigurationView> getExerciseConfigurationViewsByRoutineId(long routineId);

    @Query("select * from ExerciseConfiguration")
    List<ExerciseConfiguration> getAll();

    @Query("select * from ExerciseConfiguration where routineId = :routineId order by [order]")
    List<ExerciseConfiguration> getByRoutineId(long routineId);

    @Query("select * from ExerciseConfiguration where id = :id")
    ExerciseConfiguration getById(long id);

    @Query("update ExerciseConfiguration set [order] = :newOrder where id = :id")
    void updateOrderById(long id, int newOrder);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ExerciseConfiguration exerciseConfiguration);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<ExerciseConfiguration> exerciseConfigurations);

    @Delete
    void delete(ExerciseConfiguration exerciseConfiguration);

    @Query("delete from ExerciseConfiguration where routineId = :routineId")
    void deleteAllByRoutineId(long routineId);
}
