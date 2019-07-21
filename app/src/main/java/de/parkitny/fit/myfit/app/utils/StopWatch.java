package de.parkitny.fit.myfit.app.utils;

import timber.log.Timber;

public class StopWatch {

    private DefaultTime defaultTime = () -> System.currentTimeMillis();
    private DefaultTime time;
    private long workoutStartTime = 0;
    private long workoutStopTime = 0;
    private long alreadyElapsed = 0;
    private long alreadyPaused = 0;
    private State state;

    public StopWatch() {
        time = defaultTime;
        reset();
    }

    /**
     * Reset the stopwatch to the initial state, clearing all stored times.
     */
    public void reset() {
        state = State.RESET;
        workoutStartTime = 0;
        workoutStopTime = 0;
        alreadyElapsed = 0;
        alreadyPaused = 0;
    }

    public StopWatch(DefaultTime time) {
        this.time = time;
        reset();
    }

    public void start() {
        if (state != State.RUNNING) {
            alreadyElapsed = getElapsedTime();
            alreadyPaused = getElapsedPause();
            workoutStopTime = 0;
            workoutStartTime = time.now();
            state = State.RUNNING;

            Timber.d("Start: %s Stop: %s Pause: %s", workoutStartTime, workoutStopTime, alreadyElapsed);
        }
    }
//region Calculations
//    1: 111
//    po:0-0+0=0
//    stop:0
//    start:111
//    => 115 -> 115 - 111 + 0 = 4
//    2: 120
//    stop:120
//    => 120 - 111 + 0 = 9
//    => 124: 120 -111 + 0 = 9
//
//    3: 125
//    po: 120 - 111 + 0 = 9
//    stop: 0
//    start: 125
//    => 135 -> 135 - 125 + 9 = 19
//    => 140 -> 140 - 125 + 9 = 24
//
//    4: 142
//    stop = 142
//
//    => 142 -> 142 - 125 + 9 = 26
//    => 150 -> 142 - 125 + 9 = 26
//
//    5: 151
//    po:142 - 125 + 9 = 26
//endregion

    /***
     * @return The amount of time recorded by the stopwatch, in milliseconds
     */
    public long getElapsedTime() {

        switch (state) {
            case PAUSED:
                return (workoutStopTime - workoutStartTime) + alreadyElapsed;
            case RUNNING:
                return (time.now() - workoutStartTime) + alreadyElapsed;
            default:
                return 0;
        }
    }

    /**
     * Returns the amount of time paused using this {@link StopWatch}
     *
     * @return
     */
    public long getElapsedPause() {

        switch (state) {
            case PAUSED:
                return time.now() - workoutStopTime + alreadyPaused;
            case RUNNING:
                return alreadyPaused;
            default:
                return 0;
        }
    }

    public void pause() {
        if (state == State.RUNNING) {
            workoutStopTime = time.now();
            state = State.PAUSED;
            Timber.d("Start: %s Stop: %s Pause: %s", workoutStartTime, workoutStopTime, alreadyElapsed);
        }
    }

    /**
     * @return true if the stopwatch is currently running and recording
     * time, false otherwise.
     */
    public boolean isRunning() {
        return (state == State.RUNNING);
    }

    /**
     * The current state of the {@link StopWatch}
     */
    public enum State {
        PAUSED, RUNNING, RESET
    }

    /**
     * Implements a method that returns the current time, in milliseconds.
     * Used for testing
     */
    public interface DefaultTime {
        long now();
    }
}