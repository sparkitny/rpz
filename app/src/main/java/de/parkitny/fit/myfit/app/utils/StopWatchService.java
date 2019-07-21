package de.parkitny.fit.myfit.app.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.util.concurrent.TimeUnit;

import de.parkitny.fit.myfit.app.R;
import de.parkitny.fit.myfit.app.domain.WorkoutHolder;
import de.parkitny.fit.myfit.app.entities.Exercise;
import de.parkitny.fit.myfit.app.entities.ExerciseConfiguration;
import de.parkitny.fit.myfit.app.entities.ExerciseType;
import de.parkitny.fit.myfit.app.entities.Routine;
import de.parkitny.fit.myfit.app.entities.Workout;
import de.parkitny.fit.myfit.app.events.BroadcastExerciseEvent;
import de.parkitny.fit.myfit.app.events.NextExerciseEvent;
import de.parkitny.fit.myfit.app.events.PauseWorkoutEvent;
import de.parkitny.fit.myfit.app.events.StartWorkoutEvent;
import de.parkitny.fit.myfit.app.events.StopWorkoutEvent;
import de.parkitny.fit.myfit.app.events.TimeUpdateEvent;
import de.parkitny.fit.myfit.app.events.WorkoutFinishedEvent;
import de.parkitny.fit.myfit.app.events.WorkoutModeChangedEvent;
import de.parkitny.fit.myfit.app.ui.RpzNavDrawer;
import de.parkitny.fit.myfit.app.ui.common.Utils;
import de.parkitny.fit.myfit.app.ui.workout.WorkoutMode;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class StopWatchService extends Service {

    /**
     * The id of the {@link Notification}
     */
    private static final int NOTIFICATION_ID = 1000;
    /**
     * The channel id
     */
    private final String CHANNEL_ID = "rpz_workout_channel";
    /**
     * This {@link StopWatch} is used for the whole workoutHolder
     */
    private StopWatch stopWatchTotal;
    /**
     * This {@link StopWatch} is used for one specific {@link Exercise} and
     * is reset for every {@link Exercise}
     */
    private StopWatch stopWatchExercise;
    /**
     * This {@link StopWatch} tracks the planned pauses in an exercise and it's reset for every {@link Exercise}
     */
    private StopWatch stopWatchAfterExercisePause;
    /**
     * The workoutHolder executed at the moment
     */
    private WorkoutHolder workoutHolder;
    /**
     * the id of the {@link Routine} currently executed
     */
    private long routineId;
    /**
     * Used to unsubscribe in case of stopping a {@link Workout}
     */
    private Subscription intervalSubscription;

    private Subscription tickSubscription;

    private Subscription exerciseTickSubscription;

    /**
     * Controls the doings in each tick
     */
    private synchronized void doWork() {
        sendTimeUpdate();
        checkTick();
        checkExercisePauseTick();
        checkTimeBasedWorkoutItem();
        checkExerciseWorkoutItemPause();
    }

    private void checkExercisePauseTick() {

        if (GlobalIndicator.getWorkoutMode() != StopWatchMode.EXERCISE_PAUSE
                || exerciseTickSubscription != null) return;

        ExerciseConfiguration exerciseConfiguration = workoutHolder.current;

        long duration = exerciseConfiguration.pause;
        long elapsedExercisePauseTime = stopWatchAfterExercisePause.getElapsedTime();

        Period remainingTime = new Period(duration - elapsedExercisePauseTime);

        if (remainingTime.getMinutes() > 0 || remainingTime.getHours() > 0) return;

        if (remainingTime.getSeconds() > Utils.numberVoiceTicks()) return;

        if (exerciseConfiguration.ticked) {
            return;
        }

        exerciseConfiguration.ticked = true;

        exerciseTickSubscription = Observable.interval(1, TimeUnit.SECONDS).take(Utils.numberVoiceTicks())
                .subscribe(item -> {
                    Timber.d("%s exercise pause ticked", item);
                    tick();

                    if (item >= Utils.numberVoiceTicks() - 1) {
                        chill();
                        exerciseConfiguration.ticked = false;
                        exerciseTickSubscription = null;
                    }
                });
    }

    private void chill() {
        try {
            Thread.sleep(3000);
        } catch (Exception exn) {
            Timber.d("Nicht schlimm");
        }
    }

    /**
     * Checks whether we should make a noise now. It should only tick
     * if an {@link ExerciseConfiguration} of type ExerciseType.Repetition is executed
     */
    private void checkTick() {

        // prevent checking when the Workout is paused
        if (GlobalIndicator.getWorkoutMode() == StopWatchMode.PAUSED) return;

        ExerciseConfiguration exerciseConfiguration = workoutHolder.current;

        if (exerciseConfiguration == null || exerciseConfiguration.exerciseType == ExerciseType.Repetition)
            return;

        long duration = exerciseConfiguration.duration;
        long elapsedExerciseTime = getElapsedTimeExercise();

        Period remainingTime = new Period(duration - elapsedExerciseTime);

        // if there are minutes or hours to go then here's nothing to do
        if (remainingTime.getMinutes() > 0 || remainingTime.getHours() > 0) return;

        int seconds = remainingTime.getSeconds();

        if (seconds > Utils.numberVoiceTicks()) return;

        if (tickSubscription != null) return;

        Timber.d("Create tick subscription at %s", DateTime.now());

        tickSubscription = Observable.interval(1050, TimeUnit.MILLISECONDS).take(Utils.numberVoiceTicks())
                .subscribe(item -> {
                    Timber.d("%s ticked", item);
                    tick();

                    if (item >= Utils.numberVoiceTicks()) {
                        chill();
                        tickSubscription = null;
                    }
                });
    }

    public long getElapsedTimeExercise() {
        return stopWatchExercise.getElapsedTime();
    }

    /**
     * Plays the tick
     */
    private void tick() {

        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.men);
        mediaPlayer.start();
    }

    /**
     * Checks the current workoutHolder item and switches to the next one in case of time based {@link ExerciseConfiguration}
     */
    private void checkTimeBasedWorkoutItem() {

        ExerciseConfiguration exerciseConfiguration = workoutHolder.current;

        if (exerciseConfiguration == null) return;

        // we're already in exercise pause and do not need to count down time based exercises
        if (GlobalIndicator.getWorkoutMode() == StopWatchMode.EXERCISE_PAUSE) return;

        // only check the time based {@link ExerciseConfiguration}s
        if (exerciseConfiguration.exerciseType == ExerciseType.Time) {
            long duration = exerciseConfiguration.duration;
            long elapsedExerciseTime = getElapsedTimeExercise();

            // switch to the next {@link ExerciseConfiguration}
            if (elapsedExerciseTime >= duration) {
                nextExercise(new NextExerciseEvent(true));
            }
        }
    }

    private void checkExerciseWorkoutItemPause() {

        if (GlobalIndicator.getWorkoutMode() != StopWatchMode.EXERCISE_PAUSE) return;

        ExerciseConfiguration current = workoutHolder.current;

        long pauseDuration = current.pause;
        long elapsedPauseTime = stopWatchAfterExercisePause.getElapsedTime();

        if (elapsedPauseTime >= pauseDuration) {
            nextExercise(new NextExerciseEvent());
        }
    }

    /**
     * Switches to the next exercise and sets the time of the finished one
     */
    @Subscribe
    public void nextExercise(NextExerciseEvent event) {

        Timber.d("nextExercise: %s", event.getNextExerciseTime());

        Long exerciseDuration = null;

        Long exercisePause = null;

        if (workoutHolder.current != null) {
            // store the last exercise of the workoutHolder
            exerciseDuration = stopWatchExercise.getElapsedTime();

            // get the pause during this exercise
            exercisePause = stopWatchExercise.getElapsedPause();

            Timber.d("nextExercise: duration: %s pause: %s", exerciseDuration, exercisePause);
        }

        if (workoutHolder.hasNext()) {

            ExerciseConfiguration current = workoutHolder.current;

            // if we've no pause or we've already finished the pause, we can switch to the next exercise
            // the second option only occurs, when the user hits the next button during exercise pause
            if (current != null && (current.pause <= 0 || GlobalIndicator.getWorkoutMode() == StopWatchMode.EXERCISE_PAUSE)) {

                // reset & start the stopwatch of the particular exercise
                stopWatchExercise.reset();
                stopWatchExercise.start();

                // reset & start the stopwatch of the exercise pause
                stopWatchAfterExercisePause.reset();

                // send the firstLine of the new exercise
                // and make the workoutHolder store the duration of the current exercise
                sendCurrentExercise(workoutHolder.next(exerciseDuration, exercisePause));

                GlobalIndicator.setWorkoutMode(StopWatchMode.STARTED);
            } else if (current != null) {

//                // if we're already in exercise pause, we don't need to set this up
//                if (GlobalIndicator.getWorkoutMode() == StopWatchMode.EXERCISE_PAUSE) return;

                GlobalIndicator.setWorkoutMode(StopWatchMode.EXERCISE_PAUSE);

                stopWatchAfterExercisePause.reset();
                stopWatchAfterExercisePause.start();
                // start the workoutHolder
            } else {
                stopWatchTotal.reset();
                stopWatchTotal.start();
                stopWatchExercise.reset();
                stopWatchExercise.start();
                stopWatchAfterExercisePause.reset();

                // send the first exercise
                // and make the workoutHolder store the duration of the current exercise
                sendCurrentExercise(workoutHolder.next(null, null));
            }
        }
        // the workoutHolder is finished so we need to prepare to store the last exercise
        // the total time and stop the workoutHolder. After that we store it to the db
        else {
            // set the cursor to the end of the exercises (--> i.e. null) and thus set
            // the time of the the last executed exercise
            workoutHolder.next(exerciseDuration, exercisePause);

            // set the total time of the workoutHolder
            workoutHolder.setDuration(stopWatchTotal.getElapsedTime());

            // sets the total pause during the Workout
            workoutHolder.setTotalPause(stopWatchTotal.getElapsedPause());

            // pauses i.e. stops the workoutHolder
            onStopWorkout(new StopWorkoutEvent(System.currentTimeMillis()));

            workoutHolder.storeWorkout();

            sendWorkoutFinished();
        }
    }

    /**
     * Sends a broadcast to announce the current {@link ExerciseConfiguration}
     *
     * @param current the current {@link ExerciseConfiguration}
     */
    private void sendCurrentExercise(ExerciseConfiguration current) {

        if (current == null) {
            Timber.d("The exercise is empty. Don't send anything");
            return;
        }

        Exercise exercise = workoutHolder.currentExercise;

        String name;

        if (current.exerciseType == ExerciseType.Repetition) {
            name = String.format("%s %s", current.repetitions,
                    exercise.name);
        } else {
            name = exercise.name;
        }

        BroadcastExerciseEvent broadcastExerciseEvent =
                new BroadcastExerciseEvent();

        broadcastExerciseEvent.setExerciseName(name);
        broadcastExerciseEvent.setExerciseType(current.exerciseType);
        broadcastExerciseEvent.setExerciseConfigurationId(current.id);
        broadcastExerciseEvent.setOrderId(current.order);
        broadcastExerciseEvent.setWorkoutName(workoutHolder.getWorkoutName());
        broadcastExerciseEvent.setCurrentRound(workoutHolder.currentRound);
        broadcastExerciseEvent.setTotalRounds(workoutHolder.totalRounds);
        broadcastExerciseEvent.setCurrentSet(workoutHolder.currentSet);
        broadcastExerciseEvent.setTotalSets(workoutHolder.currentTotalSets);

        EventBus.getDefault().postSticky(broadcastExerciseEvent);

        updateNotification();
    }

    /**
     * Helper method to send a message, that the {@link Workout} is finished.
     */
    private void sendWorkoutFinished() {

        EventBus.getDefault().post(new WorkoutFinishedEvent(System.currentTimeMillis()));

        stopSelf();
    }

    public void hideNotification() {

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.cancel(NOTIFICATION_ID);
        EventBus.getDefault().removeStickyEvent(BroadcastExerciseEvent.class);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        stopWatchTotal = new StopWatch();
        stopWatchExercise = new StopWatch();
        stopWatchAfterExercisePause = new StopWatch();

        if (!EventBus.getDefault().isRegistered(this)) EventBus.getDefault().register(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Timber.d("Received start command from Android %s %s %s", intent, flags, startId);

//        if (intent == null) return START_STICKY;

        routineId = intent.getLongExtra("routineId", -1);

        onStartWorkout(new StartWorkoutEvent(System.currentTimeMillis()));

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (EventBus.getDefault().isRegistered(this)) EventBus.getDefault().unregister(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Start the {@link Workout}, send the firstLine of the first {@link Exercise}
     * and start the {@link StopWatch} of the {@link Workout} and the {@link Exercise}
     */
    @Subscribe
    public void onStartWorkout(StartWorkoutEvent event) {

        Timber.d("start: %s", event.getStartTime());

        // simply start the workoutHolder
        switch (GlobalIndicator.getWorkoutMode()) {
            case STARTED:
                Timber.d("Do nothing because the workoutHolder is already started.");
                break;
            case PAUSED:
                GlobalIndicator.setWorkoutMode(StopWatchMode.STARTED);
                break;
            case NONE:

                GlobalIndicator.setWorkoutMode(StopWatchMode.STARTED);
                GlobalIndicator.setRoutineId(routineId);

                workoutHolder = new WorkoutHolder(routineId);

                // set the start time for later analytics
                workoutHolder.setStartTime(event.getStartTime());

                workoutHolder.storeWorkout();

                if (workoutHolder.hasNext()) {

                    // set the cursor inside the workoutHolder to the first exercise
                    nextExercise(new NextExerciseEvent());
                } else {
                    Timber.d("Workout has no exercises");
                    return;
                }

                workoutHolder.storeWorkout();

                // sends the exercise info for the first time
                sendCurrentExercise(workoutHolder.current);
                break;
        }

        stopWatchTotal.start();
        stopWatchExercise.start();
        stopWatchAfterExercisePause.start();

        if (intervalSubscription == null) {
            intervalSubscription = Observable.interval(500, TimeUnit.MILLISECONDS)
                    .observeOn(Schedulers.io())
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe(value -> doWork());
        }
    }

    /**
     * Pauses the {@link StopWatch} of the {@link Workout} and of the last {@link Exercise}
     */
    @Subscribe
    public void onStopWorkout(StopWorkoutEvent event) {
        Timber.d("Stopped StopWatch at %s", event.getStopTime());

        if (workoutHolder.getDuration() == 0) {
            // set the total time of the workoutHolder
            workoutHolder.setDuration(stopWatchTotal.getElapsedTime());

            // sets the total pause during the Workout
            workoutHolder.setTotalPause(stopWatchTotal.getElapsedPause());
        }

        stopWatchTotal.pause();
        stopWatchExercise.pause();
        stopWatchAfterExercisePause.pause();

        workoutHolder.storeWorkout();

        GlobalIndicator.reset();

        if (intervalSubscription != null) {
            intervalSubscription.unsubscribe();
        }


        hideNotification();
        stopSelf();
    }

    /**
     * Pauses the {@link Workout} and therefore stops the total {@link StopWatch} and the current
     * {@link Exercise} {@link StopWatch}
     *
     * @param event the event indicating the pause
     */
    @Subscribe
    public void onPauseWorkout(PauseWorkoutEvent event) {

        Timber.d("Paused at %s", event.getTime());

        GlobalIndicator.setWorkoutMode(StopWatchMode.PAUSED);

        stopWatchExercise.pause();
        stopWatchTotal.pause();
        stopWatchAfterExercisePause.pause();
    }

    public void updateNotification() {

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {

                NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_DEFAULT);

                // Configure the notification channel.
                notificationChannel.setDescription("Channel description");
                notificationChannel.enableLights(false);
                notificationChannel.enableVibration(false);
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        String nameCurrentExercise = workoutHolder.currentExercise.name;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.current_exercise))
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(getPendingIntent())
                .addAction(R.drawable.ic_navigate_next_white_18dp,
                        getString(R.string.next_exercise),
                        getNextExercisePendingIntent())
                .setContentText(nameCurrentExercise);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(NOTIFICATION_ID, builder.build());
    }

    private PendingIntent getPendingIntent() {

        return PendingIntent.getActivity(this, 0, new Intent(this, RpzNavDrawer.class), 0);
    }

    private PendingIntent getNextExercisePendingIntent() {

        Intent intent = new Intent(this, RpzBroadcastReceiver.class);
        intent.setAction(RpzBroadcastReceiver.ACTION_NEXT);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        return pendingIntent;
    }

    /**
     * Sends a time update message via {@link EventBus}.
     */
    private void sendTimeUpdate() {

        if (EventBus.getDefault().hasSubscriberForEvent(TimeUpdateEvent.class)) {
            TimeUpdateEvent timeUpdateEvent = new TimeUpdateEvent();

            timeUpdateEvent.setTotalTime(Utils.formatPeriod(stopWatchTotal.getElapsedTime()));
            timeUpdateEvent.setExerciseTime(Utils.formatPeriod(getElapsedTimeExercise()));
            timeUpdateEvent.setPauseTotalTime(Utils.formatPeriod(stopWatchTotal.getElapsedPause()));
            timeUpdateEvent.setPauseExerciseTime(Utils.formatPeriod(stopWatchExercise.getElapsedPause()));
            timeUpdateEvent.setExerciseType(workoutHolder.current.exerciseType);

            if (timeUpdateEvent.getExerciseType() == ExerciseType.Time) {
                long restTime = workoutHolder.current.duration - stopWatchExercise.getElapsedTime();

                timeUpdateEvent.setCountdownExerciseTime(Utils.formatPeriod(restTime));
            }

            if (GlobalIndicator.getWorkoutMode() == StopWatchMode.EXERCISE_PAUSE) {

                long restTime = workoutHolder.current.pause - stopWatchAfterExercisePause.getElapsedTime();

                timeUpdateEvent.setAfterExercisePauseTime(Utils.formatPeriod(restTime));
            }

            EventBus.getDefault().post(timeUpdateEvent);
        }
    }

    /**
     * Created by sebas on 25.12.2015.
     */
    public static class GlobalIndicator {

        /**
         * the id of the currently running {@link Routine}
         */
        private static long routineId = -1;

        /**
         * the mode we're currently
         */
        private static StopWatchMode workoutMode = StopWatchMode.NONE;

        public static StopWatchMode getWorkoutMode() {
            return workoutMode;
        }

        /**
         * Sets the {@link WorkoutMode} and publishes the event on the {@link EventBus}
         *
         * @param workoutMode the {@link WorkoutMode} to be set
         */
        static synchronized void setWorkoutMode(StopWatchMode workoutMode) {
            GlobalIndicator.workoutMode = workoutMode;
            EventBus.getDefault().post(new WorkoutModeChangedEvent());
        }

        /**
         * Returns the id of the {@link Routine} or -1 if no {@link Routine} is executed
         *
         * @return the id of the {@link Routine} or -1
         */
        public static synchronized long getRoutineId() {
            return routineId;
        }

        static synchronized void setRoutineId(long id) {
            routineId = id;
        }

        /**
         * Resets to the default values indicating, that
         * no workoutHolder is executed
         */
        static synchronized void reset() {
            setWorkoutMode(StopWatchMode.NONE);
            routineId = -1;
        }

    }
}