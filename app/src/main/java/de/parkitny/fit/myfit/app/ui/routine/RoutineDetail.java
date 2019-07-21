package de.parkitny.fit.myfit.app.ui.routine;


import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.listeners.ClickEventHook;
import com.mikepenz.fastadapter.listeners.TouchEventHook;
import com.mikepenz.fastadapter_extensions.drag.ItemTouchCallback;
import com.mikepenz.fastadapter_extensions.drag.SimpleDragCallback;
import com.mikepenz.fastadapter_extensions.utilities.DragDropUtil;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.itemanimators.ScaleUpAnimator;
import com.tramsun.libs.prefcompat.Pref;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.parkitny.fit.myfit.app.R;
import de.parkitny.fit.myfit.app.RpzApplication;
import de.parkitny.fit.myfit.app.dao.ExerciseConfigurationDao;
import de.parkitny.fit.myfit.app.dao.ExerciseDao;
import de.parkitny.fit.myfit.app.domain.RoutineHolder;
import de.parkitny.fit.myfit.app.entities.Exercise;
import de.parkitny.fit.myfit.app.entities.ExerciseConfiguration;
import de.parkitny.fit.myfit.app.entities.Routine;
import de.parkitny.fit.myfit.app.events.ExerciseConfigurationClickedEvent;
import de.parkitny.fit.myfit.app.events.WorkoutClickedEvent;
import de.parkitny.fit.myfit.app.ui.common.Bar;
import de.parkitny.fit.myfit.app.ui.common.Utils;
import de.parkitny.fit.myfit.app.utils.ExerciseConfigurationView;
import de.parkitny.fit.myfit.app.utils.FitParams;
import eltos.simpledialogfragment.SimpleDialog;
import eltos.simpledialogfragment.form.Input;
import eltos.simpledialogfragment.form.SimpleFormDialog;
import timber.log.Timber;

@Bar(Position = 1)
public class RoutineDetail extends Fragment implements ItemTouchCallback, SimpleDialog.OnDialogResultListener {

    private final String pauseDialogTag = "pauseDialogTag";
    @BindView(R.id.routineDetail_setPause)
    protected TextView setPause;
    @BindView(R.id.routineDetail_rounds)
    protected TextView roundsText;
    @BindView(R.id.exercise_list)
    protected RecyclerView exerciseList;
    @BindView(R.id.fab_add_exercise_to_routine)
    protected FloatingActionButton addExerciseConfiguration;

    private ItemAdapter<ExerciseItem> exerciseItemAdapter;

    private FastAdapter<ExerciseItem> fastAdapter;
    /**
     * The id of the {@link Routine} or 0 if it's a new routine
     */
    private long routineId = 0;
    /**
     * The {@link Routine} used to show all {@link ExerciseConfigurationView}s
     */
    private RoutineHolder routineHolder;
    /**
     * the {@link ItemTouchHelper} to help with drag'n drop
     */
    private ItemTouchHelper touchHelper;
    /**
     * callback class used to store the changes caused by drag'n drop
     */
    private SimpleDragCallback dragCallback;

    private ExerciseDao exerciseDao;

    private ExerciseConfigurationDao exerciseConfigurationDao;

    public RoutineDetail() {
        exerciseConfigurationDao = RpzApplication.DB.exerciseConfigurationDao();
        exerciseDao = RpzApplication.DB.exerciseDao();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        routineId = getArguments().getLong("id");

        if (routineId == 0) {
            throw new IllegalArgumentException("RoutineId should never be 0");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_routine_detail, container, false);

        ButterKnife.bind(this, view);

        routineHolder = new RoutineHolder(routineId);

        exerciseItemAdapter = new ItemAdapter<>();

        fastAdapter = FastAdapter.with(exerciseItemAdapter);

        exerciseList.setAdapter(fastAdapter);
        exerciseList.setLayoutManager(new LinearLayoutManager(getActivity()));
        exerciseList.setItemAnimator(new ScaleUpAnimator());

        dragCallback = new SimpleDragCallback(this);
        dragCallback.setIsDragEnabled(true);

        touchHelper = new ItemTouchHelper(dragCallback);
        touchHelper.attachToRecyclerView(exerciseList);

//        exerciseItemAdapter.withEventHook(getDeleteHook());
        fastAdapter.withEventHook(getMoveHook());
//        exerciseItemAdapter.withEventHook(getCopyHook());
//        exerciseItemAdapter.withEventHook(getCopyEndHook());
        fastAdapter.withEventHook(getClickHook());
        fastAdapter.withEventHook(getIncrementHook());
        fastAdapter.withEventHook(getDecrementHook());

//        IconicsDrawable routineNameIcon = new IconicsDrawable(getActivity()).icon(GoogleMaterial.Icon.gmd_assignment).color(Color.BLACK).sizeDp(18);
//        routineName.setCompoundDrawables(routineNameIcon, null, null, null);
        IconicsDrawable setPauseIcon = new IconicsDrawable(getActivity()).icon(GoogleMaterial.Icon.gmd_snooze).color(Color.BLACK).sizeDp(18);
        setPause.setCompoundDrawables(setPauseIcon, null, null, null);
        IconicsDrawable roundsIcon = new IconicsDrawable(getActivity()).icon(GoogleMaterial.Icon.gmd_replay).color(Color.BLACK).sizeDp(18);
        roundsText.setCompoundDrawables(roundsIcon, null, null, null);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_workout_editor, menu);

        MenuItem startWorkoutItem = menu.findItem(R.id.menu_show_workout_timer);
        startWorkoutItem.setIcon(
                new IconicsDrawable(getActivity())
                        .icon(GoogleMaterial.Icon.gmd_timer)
                        .color(Color.BLACK)
                        .sizeDp(16)
        );
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.menu_show_workout_timer) {
            EventBus.getDefault().post(new WorkoutClickedEvent(this.routineId));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @NonNull
    private ClickEventHook<ExerciseItem> getDecrementHook() {
        return new ClickEventHook<ExerciseItem>() {

            @Nullable
            @Override
            public View onBind(@NonNull RecyclerView.ViewHolder viewHolder) {

                return ((ExerciseItem.ViewHolder) viewHolder).decrementButton;
            }

            @Override
            public void onClick(View v, int position, FastAdapter<ExerciseItem> fastAdapter, ExerciseItem item) {

                storeRoutine();

                ExerciseConfigurationView ecv = ((ExerciseConfigurationView) item.getTag());

                if (ecv.getSets() <= 1) return;

                ecv.setSets(ecv.getSets() - 1);
                ExerciseConfiguration exerciseConfiguration = exerciseConfigurationDao.getById(ecv.getConfigId());
                exerciseConfiguration.sets = (ecv.getSets());

                exerciseConfigurationDao.insert(exerciseConfiguration);
                item.getCurrentViewHolder().numberOfSets.setText(Integer.toString(ecv.getSets()));
            }
        };
    }

    @NonNull
    private ClickEventHook<ExerciseItem> getIncrementHook() {
        return new ClickEventHook<ExerciseItem>() {

            @Nullable
            @Override
            public View onBind(@NonNull RecyclerView.ViewHolder viewHolder) {

                return ((ExerciseItem.ViewHolder) viewHolder).incrementButton;
            }

            @Override
            public void onClick(View v, int position, FastAdapter<ExerciseItem> fastAdapter, ExerciseItem item) {

                storeRoutine();

                ExerciseConfigurationView ecv = ((ExerciseConfigurationView) item.getTag());
                ecv.setSets(ecv.getSets() + 1);
                ExerciseConfiguration exerciseConfiguration = exerciseConfigurationDao.getById(ecv.getConfigId());
                exerciseConfiguration.sets = (ecv.getSets());
                exerciseConfigurationDao.insert(exerciseConfiguration);
                item.getCurrentViewHolder().numberOfSets.setText(Integer.toString(ecv.getSets()));
            }
        };
    }

    private ClickEventHook<ExerciseItem> getClickHook() {

        return new ClickEventHook<ExerciseItem>() {

            @Nullable
            @Override
            public View onBind(@NonNull RecyclerView.ViewHolder viewHolder) {
                if (viewHolder instanceof ExerciseItem.ViewHolder) {

                    return ((ExerciseItem.ViewHolder) viewHolder).exerciseName;
                }
                return null;
            }

            @Override
            public void onClick(View v, int position, FastAdapter<ExerciseItem> fastAdapter, ExerciseItem item) {
                showExerciseDetails(item);
            }
        };
    }

    //
//    @NonNull
//    private ClickEventHook<ExerciseItem> getCopyEndHook() {
//        return new ClickEventHook<ExerciseItem>() {
//
//            @Nullable
//            @Override
//            public View onBind(@NonNull RecyclerView.ViewHolder viewHolder) {
//                if (viewHolder instanceof ExerciseItem.ViewHolder) {
//
//                    return ((ExerciseItem.ViewHolder) viewHolder).copyEndButton;
//                }
//                return null;
//            }
//
//            @Override
//            public void onClick(View v, int position, FastAdapter<ExerciseItem> fastAdapter, ExerciseItem item) {
//
//                ExerciseConfigurationView exerciseConfigurationView = ((ExerciseConfigurationView) item.getTag());
//
//                RpzNavDrawer.db.getExerciseConfigurationById(exerciseConfigurationView.getConfigId());
//            }
//        };
//    }
//
//    @NonNull
//    private ClickEventHook<ExerciseItem> getCopyHook() {
//        return new ClickEventHook<ExerciseItem>() {
//
//            @Nullable
//            @Override
//            public View onBind(@NonNull RecyclerView.ViewHolder viewHolder) {
//                if (viewHolder instanceof ExerciseItem.ViewHolder) {
//
//                    return ((ExerciseItem.ViewHolder) viewHolder).copyButton;
//                }
//                return null;
//            }
//
//            @Override
//            public void onClick(View v, int position, FastAdapter<ExerciseItem> fastAdapter, ExerciseItem item) {
//
//
//            }
//        };
//    }

    @NonNull
    private TouchEventHook<ExerciseItem> getMoveHook() {
        return new TouchEventHook<ExerciseItem>() {

            @Override
            public boolean onTouch(View v, MotionEvent event, int position, FastAdapter<ExerciseItem> fastAdapter, ExerciseItem item) {

                Timber.d("%s %s", v, fastAdapter);

                ExerciseItem.ViewHolder viewHolder = item.getCurrentViewHolder();

                touchHelper.startDrag(viewHolder);
                return true;
            }

            @Nullable
            @Override
            public View onBind(@NonNull RecyclerView.ViewHolder viewHolder) {

                return ((ExerciseItem.ViewHolder) viewHolder).moveButton;
            }
        };
    }

//    @NonNull
//    private ClickEventHook<ExerciseItem> getDeleteHook() {
//        return new ClickEventHook<ExerciseItem>() {
//
//            @Nullable
//            @Override
//            public View onBind(@NonNull RecyclerView.ViewHolder viewHolder) {
//
//                Log.d(TAG, String.format("%s", viewHolder));
//
//                if (viewHolder instanceof ExerciseItem.ViewHolder) {
//
//                    return ((ExerciseItem.ViewHolder) viewHolder).deleteButton;
//                }
//                return null;
//            }
//
//            @Override
//            public void onClick(View v, int position, FastAdapter<ExerciseItem> fastAdapter, ExerciseItem item) {
//
//                Log.d(TAG, String.format("delete clicked %s %s %s", v.getId(), position, ((ExerciseConfigurationView) item.getTag()).getExerciseName()));
//            }
//        };
//    }

    @OnClick(R.id.routineDetail_setPause)
    protected void clickSetPause() {

        SimpleFormDialog.build()
                .title(R.string.set_pause)
                .pos(R.string.ok_option)
                .neg(R.string.cancel_option)
                .fields(
                        Input.plain(pauseDialogTag).inputType(InputType.TYPE_CLASS_NUMBER)
                )
                .show(this, pauseDialogTag);
    }

    @Override
    public boolean onResult(@NonNull String dialogTag, int which, @NonNull Bundle extras) {

        if (which == BUTTON_NEGATIVE) return false;

        if (dialogTag.equals(pauseDialogTag)) {

            String pauseText = extras.getString(pauseDialogTag);
            setPause.setText(pauseText);

            long pause = Long.parseLong(extras.getString(pauseDialogTag));
            storeSetPause(pause);

            return true;
        }

        return false;
    }

    private void storeSetPause(long pause) {

        pause = pause * 1000;

        long storedPause = routineHolder.getRoutinePause();

        if (storedPause == pause) {
            Timber.d("Stored pause %s and Textfield %s. No need to store anything.",
                    storedPause,
                    routineHolder.getRoutinePause());
            return;
        }

        routineHolder.updatePause(pause);
    }

    private void showExerciseDetails(ExerciseItem item) {

        //store
        storeRoutine();

        // open the ui to show exercise details
        ExerciseConfigurationView exerciseConfigurationView = (ExerciseConfigurationView) item.getTag();

        ExerciseConfigurationClickedEvent event = new ExerciseConfigurationClickedEvent(
                exerciseConfigurationView.getConfigId(),
                exerciseConfigurationView.getExerciseId(),
                exerciseConfigurationView.getRoutineId()
        );

        EventBus.getDefault().post(event);
    }

    @Override
    public void onResume() {
        super.onResume();

        Utils.broadcastTitle(routineHolder.getRoutineName(), getString(R.string.editor_mode));

        initFields();

        intro2();
    }

    private void initFields() {

        setRoutineRounds();
        setRoutineSetPause();

        List<ExerciseConfigurationView> exerciseConfigurations = exerciseConfigurationDao.getExerciseConfigurationViewsByRoutineId(routineId);
        ArrayList<ExerciseItem> items = new ArrayList<>();

        for (ExerciseConfigurationView exerciseConfiguration : exerciseConfigurations) {

            ExerciseItem item = new ExerciseItem().withTag(exerciseConfiguration);
            items.add(item);
        }

        exerciseItemAdapter.clear();
        exerciseItemAdapter.add(items);
    }

    @Override
    public void onPause() {
        super.onPause();

        storeRoutine();
    }

    private void storeRoutine() {

        List<ExerciseItem> items = exerciseItemAdapter.getAdapterItems();

        int newOrder = 0;

        for (ExerciseItem item : items) {
            ExerciseConfigurationView ecv = (ExerciseConfigurationView) item.getTag();
            exerciseConfigurationDao.updateOrderById(ecv.getConfigId(), newOrder++);
        }
    }

    /**
     * Shows a dialog with all available exercises. {@link Exercise}
     */
    @OnClick(R.id.fab_add_exercise_to_routine)
    protected void addExerciseConfiguration() {

        int length = exerciseDao.getSize();

        if (length > 0)
            EventBus.getDefault().post(new ExerciseConfigurationClickedEvent(0, 0, routineId));
        else
            Utils.toast(getString(R.string.error_no_exercises));
    }

    @OnClick(R.id.rounds_increment_button)
    protected void incrementRounds() {
        routineHolder.setRoutineRounds(routineHolder.getRountineRounds() + 1);
        saveRoutine();
        setRoutineRounds();
    }

    @OnClick(R.id.rounds_decrement_button)
    protected void decrementButton() {

        if (routineHolder.getRountineRounds() <= 1) return;

        routineHolder.setRoutineRounds(routineHolder.getRountineRounds() - 1);
        saveRoutine();
        setRoutineRounds();
    }

    private void setRoutineRounds() {

        roundsText.setText(Integer.toString(routineHolder.getRountineRounds()));
    }

    private void setRoutineSetPause() {
        setPause.setText(Long.toString(routineHolder.getRoutinePause() / 1000));
    }


    /**
     * Saves the firstLine of the {@link Routine}, notifies the new title and finally
     * closes the keyboard.
     */
    protected void saveRoutine() {

        routineHolder.storeRoutine();
        Utils.broadcastTitle(routineHolder.getRoutineName(), getString(R.string.editor_mode));
    }

    /**
     * We need to override the method to set the dirty flag
     *
     * @param oldPosition the old position
     * @param newPosition the new position
     * @return true or false
     */
    @Override
    public boolean itemTouchOnMove(int oldPosition, int newPosition) {

        Timber.d("Moved from %s to %s", oldPosition, newPosition);

        DragDropUtil.onMove(exerciseItemAdapter, oldPosition, newPosition);

        return true;
    }

    @Override
    public void itemTouchDropped(int oldPosition, int newPosition) {

        Timber.d("Stored routine %s", routineHolder.getRoutineName());
        Timber.d("Old: %s New: %s", oldPosition, newPosition);

        storeRoutine();
    }

    private void intro2() {

//        if (!Pref.getBoolean(FitParams.ShowedWorkoutRounds)) {
//
//            MaterialIntroView.Builder builder = Utils.getIntro(getActivity(), R.string.spot_workout_rounds_desc, roundsText, FitParams.ShowedWorkoutRounds, ShapeType.RECTANGLE);
//            builder.setListener(s -> intro3());
//            builder.show();
//
//            Pref.putBoolean(FitParams.ShowedWorkoutRounds, true);
//        }
    }

    private void intro3() {

//        if (!Pref.getBoolean(FitParams.ShowedWorkoutPause)) {
//
//            MaterialIntroView.Builder builder = Utils.getIntro(getActivity(), R.string.spot_workout_pause_desc, setPause, FitParams.ShowedWorkoutPause, ShapeType.RECTANGLE);
//            builder.setListener(s -> intro4());
//            builder.show();
//
//            Pref.putBoolean(FitParams.ShowedWorkoutPause, true);
//        }
    }

    private void intro4() {

        if (!Pref.getBoolean(FitParams.ShowedAddExerciseConfiguration, false)) {

            Utils.getSpotlight(getActivity(),
                    addExerciseConfiguration,
                    getString(R.string.spot_new_exercise_configuration),
                    getString(R.string.spot_new_exercise_configuration_desc))
                    .show();


            Pref.putBoolean(FitParams.ShowedAddExerciseConfiguration, true);
        }
    }
}
