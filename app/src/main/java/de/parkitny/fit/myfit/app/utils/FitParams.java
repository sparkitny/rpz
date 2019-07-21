package de.parkitny.fit.myfit.app.utils;

import com.tramsun.libs.prefcompat.Pref;

/**
 * Created by Sebastian on 09.09.2017.
 */

public class FitParams {

    public static final String ShowCased = "showcased";

    public static final String ShowedAddExercise = "showedaddexercise";

    public static String UserSize = "size";

    public static String ShowedAddExerciseConfiguration = "showedaddexerciseconfiguration";

    public static String ShowedRenameWorkout = "showedrenameworkout";

    public static String ShowedWorkoutRounds = "showedworkoutrounds";

    public static String ShowedWorkoutPause = "showedworkoutpause";

    public static String ShowedAddRoutine = "showedaddroutine";

    public static String MenuStyle = "menu_style";

    public static String About = "about";

    public static String ResetOptions = "reset_options";

    public static String ShowedCalendarSwipe = "showed_calendar_swipe";

    public static String ShowedGlobalIntro = "showed_global_intro";

    public static String InitDB = "initdb151";

    public static String WorkoutListMenuArchiveSwitch = "workoutlist_menu_archive_switch";

    public static void resetShowcase() {

        Pref.putBoolean(MenuStyle, false);
        Pref.putBoolean(ShowedAddExercise, false);
        Pref.putBoolean(ShowCased, false);
        Pref.putBoolean(ShowedAddExerciseConfiguration, false);
        Pref.putBoolean(ShowedRenameWorkout, false);
        Pref.putBoolean(ShowedWorkoutRounds, false);
        Pref.putBoolean(ShowedWorkoutPause, false);
        Pref.putBoolean(ShowedCalendarSwipe, false);
        Pref.putBoolean(ShowedGlobalIntro, false);
        Pref.putBoolean(WorkoutListMenuArchiveSwitch, false);
    }
}
