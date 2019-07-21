package de.parkitny.fit.myfit.app.database;

import android.arch.persistence.room.TypeConverter;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import de.parkitny.fit.myfit.app.entities.EmphasisType;
import de.parkitny.fit.myfit.app.entities.ExerciseType;

/**
 * Created by Sebastian on 27.09.2017.
 */

public class Converters {

    @TypeConverter
    public String fromExerciseType(ExerciseType exerciseType) {
        return exerciseType.toString();
    }

    @TypeConverter
    public ExerciseType fromExerciseTypeString(String exerciseTypeString) {
        return ExerciseType.valueOf(exerciseTypeString);
    }

    @TypeConverter
    public String fromEmphasisType(EmphasisType emphasisType) {
        return emphasisType.toString();
    }

    @TypeConverter
    public EmphasisType fromEmphasisString(String emphasisTypeString) {
        return EmphasisType.valueOf(emphasisTypeString);
    }

    @TypeConverter
    public DateTime fromLong(long value) {
        return new DateTime(value, DateTimeZone.UTC);
    }

    @TypeConverter
    public long fromDateTime(DateTime value) {
        return value.getMillis();
    }
}
