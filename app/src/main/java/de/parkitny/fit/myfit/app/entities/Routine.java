package de.parkitny.fit.myfit.app.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by sebas on 25.10.2015.
 */
@Entity
public class Routine {

    @PrimaryKey(autoGenerate = true)
    public long id = 0;

    public String name;
    /**
     * the number of rounds to repeat this {@link Routine}
     */
    public int rounds = 1;
    /**
     * Indicates whether the routine is archived
     */
    public boolean archived = false;
    /**
     * The pause between each set of the contained {@link ExerciseConfiguration}s
     */
    public long setPause;
}
