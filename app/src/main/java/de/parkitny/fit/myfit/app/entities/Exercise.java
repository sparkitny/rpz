package de.parkitny.fit.myfit.app.entities;


import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;


/**
 * Created by sebas on 25.10.2015.
 */
@Entity
public class Exercise {

    @PrimaryKey(autoGenerate = true)
    public long id = 0;

    public String name;

    public String youtube;

    public String info;

    public String hiddenKey;

    public Exercise() {
    }

    @Ignore
    public Exercise(String name) {
        this.name = name;
    }
}
