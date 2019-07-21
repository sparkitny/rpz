package de.parkitny.fit.myfit.app.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import de.parkitny.fit.myfit.app.entities.Routine;

/**
 * Created by Sebastian on 27.09.2017.
 */
@Dao
public interface RoutineDao {

    @Query("select * from Routine order by name asc")
    List<Routine> getAll();

    @Query("select * from Routine where id = :routineId")
    Routine getRoutineById(long routineId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Routine routine);

    @Delete
    void delete(Routine routine);
}
