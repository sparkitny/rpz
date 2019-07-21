package de.parkitny.fit.myfit.app.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by Sebastian on 20.10.2017.
 */
@Entity(foreignKeys = {
        @ForeignKey(
                entity = Exercise.class,
                childColumns = "exerciseId",
                parentColumns = "id"
        )},
        indices = {
                @Index(value = "exerciseId")
        })
public class ExerciseEmphasis {

    @PrimaryKey(autoGenerate = true)
    public long id = 0;

    public long exerciseId = 0;

    public EmphasisType emphasisType;
}
