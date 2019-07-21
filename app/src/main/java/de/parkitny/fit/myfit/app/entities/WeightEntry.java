package de.parkitny.fit.myfit.app.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by Sebastian on 20.04.2016.
 */
@Entity
public class WeightEntry {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public long weightTime;

    public double weight;
}
