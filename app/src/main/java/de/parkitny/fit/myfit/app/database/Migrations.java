package de.parkitny.fit.myfit.app.database;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.migration.Migration;
import android.support.annotation.NonNull;

/**
 * Created by Sebastian on 07.10.2017.
 */

public class Migrations {

    public static final Migration MIGRATION_9_10 = new Migration(9, 10) {


        @Override
        public void migrate(SupportSQLiteDatabase database) {

            database.execSQL("alter table Exercise add column hiddenKey text");

            database.execSQL("update Routine set setPause = 0");
        }
    };

    public static final Migration MIGRATION_10_11 = new Migration(10, 11) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {

            database.execSQL("alter table WorkoutItem add column repetitions INTEGER NOT NULL default 0");
            database.execSQL("alter table WorkoutItem add column plannedDuration INTEGER NOT NULL default 0");
            database.execSQL("alter table WorkoutItem add column round INTEGER NOT NULL default 0");
        }
    };
}
