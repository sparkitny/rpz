package de.parkitny.fit.myfit.app.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import de.parkitny.fit.myfit.app.entities.EmphasisType;
import de.parkitny.fit.myfit.app.entities.ExerciseEmphasis;

/**
 * Created by Sebastian on 20.10.2017.
 */
@Dao
public interface ExerciseEmphasisDao {

    @Query("select * from ExerciseEmphasis where exerciseId = :exerciseId")
    List<ExerciseEmphasis> getByExerciseId(long exerciseId);

    @Query("select * from ExerciseEmphasis where exerciseId = :exerciseId")
    LiveData<List<ExerciseEmphasis>> getByExerciseIdLive(long exerciseId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(ExerciseEmphasis exerciseEmphasis);

    @Delete
    int delete(ExerciseEmphasis exerciseEmphasis);

    @Query("delete from ExerciseEmphasis where exerciseId = :exerciseId")
    void deleteByExerciseId(long exerciseId);

    @Query("delete from ExerciseEmphasis where emphasisType = :emphasisType and exerciseId = :exerciseId")
    void deleteByEmphasisTypeAndExerciseId(EmphasisType emphasisType, long exerciseId);

    @Query("delete from ExerciseEmphasis where id = :id")
    void deleteById(long id);
}
