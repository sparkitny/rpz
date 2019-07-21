package de.parkitny.fit.myfit.app.utils;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import de.parkitny.fit.myfit.app.RpzApplication;
import de.parkitny.fit.myfit.app.dao.ExerciseConfigurationDao;
import de.parkitny.fit.myfit.app.dao.ExerciseDao;
import de.parkitny.fit.myfit.app.entities.Exercise;

/**
 * Created by Sebastian on 14.08.2016.
 */

public class ExerciseMatrix {

    /**
     * The {@link Hashtable} with names to positions
     */
    private Hashtable<String, Integer> names2positions;

    /**
     * The {@link Hashtable} with names to db ids
     */
    private Hashtable<String, Long> names2ids;

    /**
     * The names of the {@link Exercise}s
     */
    private ArrayList<String> exerciseNames;

    private ExerciseConfigurationDao exerciseConfigurationDao;

    private ExerciseDao exerciseDao;

    /**
     * Creates the instance
     */
    public ExerciseMatrix() {

        exerciseConfigurationDao = RpzApplication.DB.exerciseConfigurationDao();
        exerciseDao = RpzApplication.DB.exerciseDao();

        names2ids = new Hashtable<>();
        names2positions = new Hashtable<>();
        exerciseNames = new ArrayList<>();

        List<Exercise> exercises = exerciseDao.getAll();

        for (int i = 0; i < exercises.size(); i++) {

            Exercise exercise = exercises.get(i);
            addName(exercise.name);
            addNameAndId(exercise.name, exercise.id);
            addNameAndPosition(exercise.name, i);
        }
    }

    /**
     * Adds the exercise firstLine
     *
     * @param name the firstLine
     */
    public void addName(String name) {
        exerciseNames.add(name);
    }

    /**
     * Adds a firstLine and a position to the matrix
     *
     * @param name     the firstLine of the {@link Exercise}
     * @param position the position of the {@link Exercise} depending on the order of the used db statement
     */
    public void addNameAndPosition(String name, int position) {

        if (!names2positions.containsKey(name)) {
            names2positions.put(name, position);
        }
    }

    /**
     * Adds a firstLine and an id of an {@link Exercise} to the matrix
     *
     * @param name the firstLine of the {@link Exercise}
     * @param id   the id of the {@link Exercise}
     */
    public void addNameAndId(String name, long id) {

        if (!names2ids.containsKey(name)) {
            names2ids.put(name, id);
        }
    }

    /**
     * Returns the position of a {@link Exercise}
     *
     * @param name the firstLine of the {@link Exercise}
     * @return the position depending the order used
     */
    public int getPositionByName(String name) {

        if (names2positions.containsKey(name)) {
            return names2positions.get(name);
        } else {
            return -1;
        }
    }

    /**
     * Returns the id of an {@link Exercise}
     *
     * @param name the firstLine of the {@link Exercise}
     * @return the id
     */
    public long getIdByName(String name) {

        if (names2ids.containsKey(name)) {
            return names2ids.get(name);
        } else {
            return -1;
        }
    }

    /**
     * Returns the names of the {@link Exercise}s
     *
     * @return the names of the {@link Exercise}s
     */
    public ArrayList<String> getExerciseNames() {
        return exerciseNames;
    }
}
