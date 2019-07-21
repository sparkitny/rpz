package de.parkitny.fit.myfit.app.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import de.parkitny.fit.myfit.app.entities.WeightEntry;

/**
 * Created by Sebastian on 27.09.2017.
 */
@Dao
public interface WeightEntryDao {

    @Query("select * from WeightEntry order by weightTime asc")
    List<WeightEntry> getAll();

    @Query("select * from WeightEntry order by weightTime desc")
    WeightEntry getLatest();

    @Insert
    void insert(WeightEntry weightEntry);

    @Update
    void update(WeightEntry weightEntry);

    @Delete
    void delete(WeightEntry weightEntry);
}
