package de.parkitny.fit.myfit.app.ui.workout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.onurciner.toastox.ToastOXDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import de.parkitny.fit.myfit.app.R;
import de.parkitny.fit.myfit.app.RpzApplication;
import de.parkitny.fit.myfit.app.dao.ExerciseConfigurationDao;
import de.parkitny.fit.myfit.app.dao.RoutineDao;
import de.parkitny.fit.myfit.app.entities.ExerciseConfiguration;
import de.parkitny.fit.myfit.app.entities.ExerciseType;
import de.parkitny.fit.myfit.app.entities.Routine;
import de.parkitny.fit.myfit.app.entities.Workout;
import de.parkitny.fit.myfit.app.events.BroadcastExerciseEvent;
import de.parkitny.fit.myfit.app.events.LockDrawerEvent;
import de.parkitny.fit.myfit.app.events.NextExerciseEvent;
import de.parkitny.fit.myfit.app.events.PauseWorkoutEvent;
import de.parkitny.fit.myfit.app.events.RoutineClickedEvent;
import de.parkitny.fit.myfit.app.events.StartWorkoutEvent;
import de.parkitny.fit.myfit.app.events.StopWorkoutEvent;
import de.parkitny.fit.myfit.app.events.TimeUpdateEvent;
import de.parkitny.fit.myfit.app.events.WorkoutFinishedEvent;
import de.parkitny.fit.myfit.app.events.WorkoutModeChangedEvent;
import de.parkitny.fit.myfit.app.ui.common.Bar;
import de.parkitny.fit.myfit.app.ui.common.FlexThreeLineItem;
import de.parkitny.fit.myfit.app.ui.common.Utils;
import de.parkitny.fit.myfit.app.utils.ExerciseConfigurationView;
import de.parkitny.fit.myfit.app.utils.StopWatchMode;
import de.parkitny.fit.myfit.app.utils.StopWatchService;
import mehdi.sakout.fancybuttons.FancyButton;
import timber.log.Timber;

@Bar(Position = 1)
public class WorkoutTimerFragment extends Fragment {

    @BindView(R.id.exercise_sets_text_view)
    protected TextView exerciseSetInfo;
    @BindView(R.id.routine_rounds_info)
    protected TextView roundsInfo;
    @BindView(R.id.current_total_time)
    protected TextView currentTotalTime;
    @BindView(R.id.current_exercise_time)
    protected TextView currentExerciseTime;
    @BindView(R.id.workout_timer_list_exercises)
    protected RecyclerView exerciseList;
    @BindView(R.id.workout_timer_button_start_workout)
    protected FancyButton startButton;
    @BindView(R.id.workout_timer_button_stop_workout)
    protected FancyButton stopButton;
    @BindView(R.id.workout_timer_button_next_exercise)
    protected FancyButton nextExerciseButton;

    /**
     * The id of the routine
     */
    private long routineId = 0;
    /**
     * The {@link Routine} to display the {@link ExerciseConfiguration}
     */
    private Routine routine;

    private FastItemAdapter<FlexThreeLineItem> exerciseConfigurationItemAdapter = new FastItemAdapter<>();

    private int currentSet = -1;

    private int currentRound = -1;

    /**
     * we need this to unbind in a {@link Fragment}
     */
    private Unbinder unbinder;

    private RoutineDao routineDao;

    private ExerciseConfigurationDao exerciseConfigurationDao;

    private LinearLayoutManager linearLayoutManager;

    /**
     * Constructor to initialize necessary values
     */
    public WorkoutTimerFragment() {

        routineDao = RpzApplication.DB.routineDao();
        exerciseConfigurationDao = RpzApplication.DB.exerciseConfigurationDao();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        routineId = getArguments().getLong("id");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_workout_timer, container, false);

        unbinder = ButterKnife.bind(this, view);

        if (routine == null) {
            routine = routineDao.getRoutineById(routineId);
        }

        linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setSmoothScrollbarEnabled(true);
        exerciseList.setLayoutManager(linearLayoutManager);
        exerciseList.setAdapter(exerciseConfigurationItemAdapter);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_workouttimer_fragement, menu);

        MenuItem item = menu.findItem(R.id.menu_workouttimer_item_edit);
        item.setIcon(new IconicsDrawable(getActivity()).icon(GoogleMaterial.Icon.gmd_edit).color(Color.BLACK).sizeDp(16));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.menu_workouttimer_item_edit) {

            EventBus.getDefault().post(new RoutineClickedEvent(this.routineId));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        unbinder.unbind();
    }

    @Override
    public void onResume() {
        super.onResume();

        fillItems();

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        Utils.broadcastTitle(routine.name, getString(R.string.workout_ongoing));

        initTextViews();

        BroadcastExerciseEvent broadcastExerciseEvent = EventBus.getDefault().getStickyEvent(BroadcastExerciseEvent.class);

        updateExerciseInformation(broadcastExerciseEvent);

        updateUiByMode();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @SuppressLint("StringFormatMatches")
    private void initTextViews() {

        exerciseSetInfo.setText(String.format(getString(R.string.set_info), 0, 0));
        roundsInfo.setText(String.format(getString(R.string.rounds_info), 0, 0));

        currentTotalTime.setText(Utils.formatPeriod(0));
        currentExerciseTime.setText(Utils.formatPeriod(0));
    }

    @SuppressLint("StringFormatMatches")
    private void fillItems() {

        exerciseConfigurationItemAdapter.clear();

        List<ExerciseConfigurationView> configs = exerciseConfigurationDao.getExerciseConfigurationViewsByRoutineId(routineId);

        for (ExerciseConfigurationView ecv : configs) {
            FlexThreeLineItem item = new FlexThreeLineItem().withTag(ecv);
            item.withIdentifier(ecv.getConfigId());
            item.firstLine = ecv.getReadableString();
            item.secondLine = (String.format(
                    getString(R.string.sets),
                    ecv.getSets()));
            item.thirdLine = String.format("%s kg", ecv.getWeight());

            exerciseConfigurationItemAdapter.add(item);
        }
    }

    /**
     * En- or disables the buttons according to the current mode
     */
    private void updateUiByMode() {
        Timber.d("Set mode: %s", StopWatchService.GlobalIndicator.getWorkoutMode());
        switch (StopWatchService.GlobalIndicator.getWorkoutMode()) {

            case STARTED:
                EventBus.getDefault().post(new LockDrawerEvent(true));
                startButton.setIconResource("\uf04c");
                setFancyButtons(true, nextExerciseButton);
                setFancyButtons(true, stopButton);
                setHasOptionsMenu(false);

                break;
            case NONE:
                EventBus.getDefault().post(new LockDrawerEvent(false));

                currentTotalTime.setText(null);
                currentExerciseTime.setText(null);
                startButton.setIconResource("\uf04b");
                setFancyButtons(false, nextExerciseButton);
                setFancyButtons(false, stopButton);

                BroadcastExerciseEvent event = new BroadcastExerciseEvent();
                event.setExerciseConfigurationId(0);
                updateSelectedItem(event);

                initTextViews();
                setHasOptionsMenu(true);

                break;
            case PAUSED:
                EventBus.getDefault().post(new LockDrawerEvent(true));

                startButton.setIconResource("\uf04b");
                setFancyButtons(false, nextExerciseButton);
                setFancyButtons(false, stopButton);
                setHasOptionsMenu(false);

                break;
            case EXERCISE_PAUSE:
                EventBus.getDefault().post(new LockDrawerEvent(true));
                startButton.setIconResource("\uf017");
                setFancyButtons(true, nextExerciseButton);
                setFancyButtons(true, stopButton);
                setHasOptionsMenu(false);

                break;
        }
    }

    /**
     * Enables and colors the {@link FancyButton}s
     *
     * @param enabled true if enabled
     * @param button  the {@link FancyButton}
     */
    private void setFancyButtons(boolean enabled, FancyButton button) {

        button.setEnabled(enabled);

        if (enabled) {
            button.setBackgroundColor(Utils.getColor(getActivity(), Utils.getAttributeId(getActivity(), R.attr.myAccent)));
        } else {
            button.setBackgroundColor(Utils.getColor(getActivity(), Utils.getAttributeId(getActivity(), R.attr.myDisabled)));
        }
    }

    @OnClick(R.id.workout_timer_button_start_workout)
    public void startWorkout() {

        switch (StopWatchService.GlobalIndicator.getWorkoutMode()) {
            case NONE:

                Intent intent = new Intent(getActivity(), StopWatchService.class);
                intent.putExtra("routineId", routineId);

                getActivity().startService(intent);

                updateUiByMode();
                animateCurrentTotalTime();
                animateCurrentExerciseTime();
                animateCurrentRound();
                animateCurrentSet();

                break;
            case PAUSED:
                EventBus.getDefault().post(new StartWorkoutEvent(System.currentTimeMillis()));
                break;
            case STARTED:
                EventBus.getDefault().post(new PauseWorkoutEvent(System.currentTimeMillis()));
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void WorkoutModeChanged(WorkoutModeChangedEvent event) {
        updateUiByMode();
    }

    @OnClick(R.id.workout_timer_button_stop_workout)
    public void stopWorkout() {

        new ToastOXDialog.Build(getActivity())
                .setTitle(R.string.hint)
                .setContent(R.string.stop_workout)
                .setPositiveText(R.string.ok_option)
                .setIcon(R.drawable.ic_info_outline_black_18dp)
                .setPositiveBackgroundColor(Utils.getThemedColor(getActivity(), R.attr.myPrim))
                .setPositiveTextColorResource(R.color.white)
                .onPositive(toastOXDialog -> {
                    EventBus.getDefault().post(new StopWorkoutEvent(System.currentTimeMillis()));
                    updateUiByMode();
                    toastOXDialog.dismiss();
                })
                .setNegativeText(R.string.cancel_option)
                .setNegativeBackgroundColor(Utils.getThemedColor(getActivity(), R.attr.myAccent))
                .setNegativeTextColorResource(R.color.white)
                .onNegative(toastOXDialog -> toastOXDialog.dismiss()).show();

    }

    /**
     * Fires a message via {@link EventBus} to the service
     * to switch to the next exercise
     */
    @OnClick(R.id.workout_timer_button_next_exercise)
    public void nextExercise() {
        EventBus.getDefault().post(new NextExerciseEvent());

        animateCurrentExerciseTime();
    }

    /**
     * Updates the current total time of the {@link Workout}
     *
     * @param event the {@link TimeUpdateEvent} containing the time information
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateTime(TimeUpdateEvent event) {

        switch (StopWatchService.GlobalIndicator.getWorkoutMode()) {
            case STARTED:
                currentTotalTime.setText(event.getTotalTime());
                if (event.getExerciseType() == ExerciseType.Repetition) {
                    currentExerciseTime.setText(event.getExerciseTime());
                } else if (event.getExerciseType() == ExerciseType.Time) {
                    currentExerciseTime.setText(event.getCountdownExerciseTime());
                }
                break;
            case PAUSED:
                currentTotalTime.setText(event.getPauseTotalTime());
                currentExerciseTime.setText(event.getPauseExerciseTime());
                break;
            case NONE:
                currentTotalTime.setText(getString(R.string.not_started));
                break;
            case EXERCISE_PAUSE:
                currentTotalTime.setText(event.getTotalTime());
                currentExerciseTime.setText(event.getAfterExercisePauseTime());
                break;
        }
    }

    /**
     * Called internally when the {@link Workout} has been terminated
     *
     * @param event the {@link WorkoutFinishedEvent} received by the {@link StopWatchService}
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void workoutFinished(WorkoutFinishedEvent event) {

        Timber.d("workoutFinished: %s", event.getFinishedTime());
        updateUiByMode();
        initTextViews();
    }

    @SuppressLint("StringFormatMatches")
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void updateExerciseInformation(BroadcastExerciseEvent event) {

        if (StopWatchService.GlobalIndicator.getWorkoutMode() == StopWatchMode.STARTED ||
                StopWatchService.GlobalIndicator.getWorkoutMode() == StopWatchMode.PAUSED ||
                StopWatchService.GlobalIndicator.getWorkoutMode() == StopWatchMode.EXERCISE_PAUSE) {

            exerciseSetInfo.setText(
                    String.format(getString(R.string.set_info),
                            event.getCurrentSet(),
                            event.getTotalSets()));

            if (currentSet != event.getCurrentSet()) {
                animateCurrentSet();
                currentSet = event.getCurrentSet();
            }

            roundsInfo.setText(
                    String.format(getString(R.string.rounds_info),
                            event.getCurrentRound(),
                            event.getTotalRounds()));

            if (currentRound != event.getCurrentRound()) {
                animateCurrentRound();
                currentRound = event.getCurrentRound();
            }

            updateSelectedItem(event);
        } else {
            Timber.d("This message has been received after the workout has been stopped.");
        }
    }


    /**
     * Updates the {@link TextView} to display the current exercise
     *
     * @param event the {@link BroadcastExerciseEvent} containing all details
     */
    private void updateSelectedItem(BroadcastExerciseEvent event) {

        for (FlexThreeLineItem item : exerciseConfigurationItemAdapter.getAdapterItems()) {

            item.icon = null;
            item.rightIcon = null;
            item.withSetSelected(false);

            if (event.getExerciseConfigurationId() == item.getIdentifier()) {
                item.rightIcon = Utils.getBadgeNumber(getActivity(), event.getCurrentSet());
                item.icon = Utils.getBadgeRounds(getContext(), event.getCurrentRound());
                item.withSetSelected(true);
                int position = exerciseConfigurationItemAdapter.getPosition(item);
                linearLayoutManager.scrollToPositionWithOffset(position, 0);
                exerciseList.invalidate();
            }
        }

        exerciseConfigurationItemAdapter.notifyAdapterDataSetChanged();
    }

    private void animateCurrentExerciseTime() {

        YoYo.with(Techniques.SlideInRight)
                .duration(400)
                .playOn(getActivity().findViewById(R.id.workout_timer_exercise));

        YoYo.with(Techniques.SlideInRight)
                .delay(100)
                .duration(500)
                .playOn(getActivity().findViewById(R.id.current_exercise_time));
    }

    private void animateCurrentTotalTime() {

        YoYo.with(Techniques.SlideInLeft)
                .duration(400)
                .playOn(getActivity().findViewById(R.id.workout_timer_total));

        YoYo.with(Techniques.SlideInLeft)
                .delay(100)
                .duration(500)
                .playOn(getActivity().findViewById(R.id.current_total_time));
    }

    private void animateCurrentRound() {
        YoYo.with(Techniques.SlideInUp)
                .duration(500)
                .playOn(getActivity().findViewById(R.id.routine_rounds_info));
    }

    private void animateCurrentSet() {
        YoYo.with(Techniques.SlideInUp)
                .duration(500)
                .playOn(getActivity().findViewById(R.id.exercise_sets_text_view));
    }
}
