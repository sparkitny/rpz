package de.parkitny.fit.myfit.app.domain;

import java.util.ArrayList;
import java.util.List;

import de.parkitny.fit.myfit.app.RpzApplication;
import de.parkitny.fit.myfit.app.dao.ExerciseConfigurationDao;
import de.parkitny.fit.myfit.app.dao.ExerciseDao;
import de.parkitny.fit.myfit.app.dao.RoutineDao;
import de.parkitny.fit.myfit.app.dao.WorkoutDao;
import de.parkitny.fit.myfit.app.dao.WorkoutItemDao;
import de.parkitny.fit.myfit.app.entities.Exercise;
import de.parkitny.fit.myfit.app.entities.ExerciseConfiguration;
import de.parkitny.fit.myfit.app.entities.Routine;
import de.parkitny.fit.myfit.app.entities.Workout;
import de.parkitny.fit.myfit.app.entities.WorkoutItem;

/**
 * Created by Sebastian on 03.10.2017.
 */

public class WorkoutHolder {

    /**
     * The {@link ExerciseConfiguration}s of the {@link Routine}
     */
    private final List<ExerciseConfiguration> exerciseConfigurations;
    /**
     * A pointer to the {@link ExerciseConfiguration}
     */
    public ExerciseConfiguration current;
    /**
     * The current {@link Exercise}
     */
    public Exercise currentExercise;
    public int currentSet = 0;
    public int currentTotalSets = 0;
    public int currentRound = 0;
    public int totalRounds = 0;
    public boolean workoutStarted = false;
    /**
     * The id of the {@link Routine}
     */
    private long routineId;
    /**
     * The {@link Routine} used for this {@link Workout}
     */
    private Routine routine;
    /**
     * the {@link Workout}
     */
    private Workout workout;
    /**
     * The {@link WorkoutItem}s
     */
    private ArrayList<WorkoutItem> workoutItems = new ArrayList<>();
    /**
     * the counter for the exercises
     */
    private int $index = 0;

    /**
     * The {@link RoutineDao} used to access the database
     */
    private RoutineDao routineDao;

    /**
     * The {@link WorkoutDao} used to access the database
     */
    private WorkoutDao workoutDao;

    /**
     * The {@link WorkoutItemDao} used to access the database
     */
    private WorkoutItemDao workoutItemDao;
    /**
     * The {@link ExerciseDao} used to access the {@link Exercise}s
     */
    private ExerciseDao exerciseDao;
    /**
     * the {@link ExerciseConfigurationDao} to get all relevant {@link ExerciseConfiguration}s
     */
    private ExerciseConfigurationDao exerciseConfigurationDao;

    public WorkoutHolder(long routineId) {

        this.routineId = routineId;

        routineDao = RpzApplication.DB.routineDao();
        workoutDao = RpzApplication.DB.workoutDao();
        workoutItemDao = RpzApplication.DB.workoutItemDao();
        exerciseDao = RpzApplication.DB.exerciseDao();
        exerciseConfigurationDao = RpzApplication.DB.exerciseConfigurationDao();

        routine = routineDao.getRoutineById(this.routineId);
        exerciseConfigurations = exerciseConfigurationDao.getByRoutineId(this.routineId);

        workout = new Workout();
        workout.name = routine.name;
        workout.startTime = System.currentTimeMillis();

        // store the workout
        storeWorkout();
    }

    public long getDuration() {
        return workout.workoutDuration;
    }

    public void setDuration(long duration) {
        workout.workoutDuration = duration;
    }

    public void setTotalPause(long totalPause) {
        workout.totalPause = totalPause;
    }

    public long getTotalPause() {
        return workout.totalPause;
    }

    public void storeWorkout() {

        long workdoutId = workoutDao.insert(workout);

        for (WorkoutItem workoutItem : workoutItems) {
            workoutItem.workoutId = workdoutId;
            workoutItemDao.insert(workoutItem);
        }
    }

    public String getWorkoutName() {
        return workout.name;
    }

    public void setStartTime(long startTime) {
        workout.startTime = startTime;
    }


    /**
     * Returns the next {@link ExerciseConfiguration} via the iterator and adds a
     * {@link WorkoutItem} to the workoutItems stored internally
     *
     * @param duration the duration of the exercise made
     * @param pause    the pause of the {@link WorkoutItem}
     * @return the next {@link ExerciseConfiguration}
     */
    public ExerciseConfiguration next(Long duration, Long pause) {

        // first store the properties of the previously done exercise
        if (current != null) {

            WorkoutItem item = new WorkoutItem(workout.id, currentExercise.name, current);
            item.duration = duration;
            item.pause = pause;
            item.round = currentRound;
            item.set = currentSet;

            workoutItems.add(item);
        } else {
            //set up first exercise and return
            workoutStarted = true;
            currentRound = 1;
            totalRounds = routine.rounds;
            currentSet = 0;
            prepareCurrent();
            return current;
        }

        if (currentSet >= current.sets) {

            // we are in the same round so pick the next exercise
            if ($index < exerciseConfigurations.size()) {
                currentSet = 0;
                prepareCurrent();
                return current;
            }
            // we have finished the round
            else {
                // if this is also the last round return null, we have finished
                if (currentRound >= routine.rounds) {
//                    Log.d($TAG, String.format("Finished workout %s", routine.firstLine));
                    return null;
                }
                // there are more rounds, so begin with the first exercise and decrement the rounds
                else {
                    currentRound = (currentRound + 1);
                    currentSet = (0);
                    $index = 0;
                    prepareCurrent();
                    return current;
                }
            }

        } else {
            // increment the set and return, we have outstanding sets to do
            currentSet = (currentSet + 1);
            return current;
        }
    }

    private void prepareCurrent() {
        current = exerciseConfigurations.get($index++);
        currentExercise = exerciseDao.getExerciseById(current.exerciseId);
        currentSet = (currentSet + 1);
        currentTotalSets = (current.sets);
    }

    /**
     * Returns whether there are more {@link ExerciseConfiguration}
     *
     * @return true if there are more {@link ExerciseConfiguration}
     */
    public boolean hasNext() {

        if (!workoutStarted) return true;

        if (currentSet >= current.sets) {

            // we are in the same round so pick the next exercise
            if ($index < exerciseConfigurations.size()) {
                return true;
            }
            // we have finished the round
            else {
                // if this is also the last round return null, we have finished
                if (currentRound >= routine.rounds) {
                    return false;
                }
                // there are more rounds, so begin with the first exercise and decrement the rounds
                else {
                    return true;
                }
            }
            // we are still within the current exercise, so we're not finished
        } else {
            return true;
        }
    }
}
