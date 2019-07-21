package de.parkitny.fit.myfit.app.ui.routine;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gregacucnik.EditTextView;
import com.jaredrummler.materialspinner.MaterialSpinner;

import org.greenrobot.eventbus.EventBus;

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
import de.parkitny.fit.myfit.app.entities.ExerciseType;
import de.parkitny.fit.myfit.app.entities.Routine;
import de.parkitny.fit.myfit.app.events.PopBackEvent;
import de.parkitny.fit.myfit.app.ui.common.Bar;
import de.parkitny.fit.myfit.app.ui.common.Utils;
import de.parkitny.fit.myfit.app.utils.ExerciseMatrix;
import lib.kingja.switchbutton.SwitchMultiButton;
import mobi.upod.timedurationpicker.TimeDurationPickerDialog;

/**
 * A simple {@link Fragment} subclass.
 * This fucking fragment should be redesigned (14.08.2016). To many things are implicit
 */
@Bar(Position = 1)
public class ExerciseConfigurationFragment extends Fragment implements MaterialSpinner.OnItemSelectedListener {

    private static final String TAG = "ECF";

    @BindView(R.id.chosen_exercise_spinner)
    protected MaterialSpinner chosenExerciseSpinner;

    @BindView(R.id.exerciseconfiguration_exercise_type_value)
    protected SwitchMultiButton switchExerciseType;

    @BindView(R.id.numberfield)
    protected EditTextView numberField;

    @BindView(R.id.time_field)
    protected TextView timeField;

    @BindView(R.id.time_field_layout)
    protected LinearLayout timeFieldLayout;

    @BindView(R.id.set_pause)
    protected EditTextView setPause;

    @BindView(R.id.edit_switch)
    protected SwitchCompat editSwitch;

    @BindView(R.id.exercise_configuration_weight)
    protected EditTextView weight;

//    private ExerciseTypeAdapter exerciseTypeAdapter;

    /**
     * the id of the {@link ExerciseConfiguration}
     */
    private long exerciseConfigurationId = 0;

    /**
     * The id of the {@link Exercise}
     */
    private long exerciseId = 0;

    /**
     * the id of the {@link Routine}
     */
    private long routineId = 0;

    /**
     * the current {@link ExerciseType}
     */
    private ExerciseType currentExerciseType;

    /**
     * the {@link ExerciseMatrix} containing information about positions and ids
     */
    private ExerciseMatrix exerciseMatrix;

    /**
     * The {@link Routine}
     */
    private RoutineHolder routineHolder;

    /**
     * the {@link ExerciseConfigurationDao}
     */
    private ExerciseConfigurationDao exerciseConfigurationDao;
    /**
     * the {@link ExerciseDao}
     */
    private ExerciseDao exerciseDao;

    public ExerciseConfigurationFragment() {

        exerciseConfigurationDao = RpzApplication.DB.exerciseConfigurationDao();
        exerciseDao = RpzApplication.DB.exerciseDao();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exercise_configuration, container, false);

        ButterKnife.bind(this, view);

        setUpData();

        setHasOptionsMenu(true);

        switchExerciseType.setText(getString(R.string.repetition), getString(R.string.time)).setOnSwitchListener((i, s) -> {
            switch (i) {
                case 0:
                    show(ExerciseType.Repetition);
                    break;
                case 1:
                    show(ExerciseType.Time);
                    break;
            }
        });

        return view;
    }

    @OnClick(R.id.time_field)
    protected void durationViewClick() {

        TimeDurationPickerDialog dialog = new TimeDurationPickerDialog(getActivity(), (view, duration) -> setDuration(duration), 0);

        dialog.show();
    }

    /**
     * @return Returns the duration stored in the tag of the {@link TextView}
     */
    private long getDuration() {

        if (timeField.getTag() == null) {
            return 0;
        }

        return (Long) timeField.getTag();
    }

    /**
     * Sets the duration of the {@link TextView} and the tag of the {@link TextView}
     *
     * @param duration the duration chosen by the user
     */
    private void setDuration(long duration) {

        timeField.setText(Utils.formatPeriod(duration));
        timeField.setTag(duration);
        numberField.setText(Utils.formatPeriod(duration));
        numberField.setTag(duration);
    }

    private long getSetPause() {
        try {
            return Long.parseLong(setPause.getText()) * 1000;
        } catch (Exception exn) {
            return 0;
        }
    }

    private void setSetPause(long pause) {
        setPause.setText(String.format("%s", pause / 1000));
    }

    private boolean getEditSwitch() {
        return editSwitch.isChecked();
    }

    private void setEditSwitch(boolean checked) {
        editSwitch.setChecked(checked);
    }

    /**
     * Initializes all the needed data
     */
    private void setUpData() {

        exerciseConfigurationId = getArguments().getLong("exerciseConfigurationId");
        exerciseId = getArguments().getLong("exerciseId");
        routineId = getArguments().getLong("routineId");

        routineHolder = new RoutineHolder(routineId);

        if (exerciseConfigurationId == 0) {

            // choose the first exercise in the database
            Exercise exercise = exerciseDao.getAll().get(0);
            exerciseId = exercise.id;
        }

        setTitleByExercise();

//        exerciseTypeAdapter = new ExerciseTypeAdapter(getContext());

        chosenExerciseSpinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        initFields();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_exerciseconfiguration_fragement, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.menu_exerciseconfiguration_item_delete) {

            if (exerciseConfigurationId > 0) {

                routineHolder.removeExerciseById(exerciseConfigurationId);

                EventBus.getDefault().post(new PopBackEvent());
            } else {
                EventBus.getDefault().post(new PopBackEvent());
            }
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * This method fills the fields according to the given {@link ExerciseConfiguration}
     */
    private void initFields() {

        exerciseMatrix = new ExerciseMatrix();

        chosenExerciseSpinner.setItems(exerciseMatrix.getExerciseNames());

        numberField.setText("0");
        weight.setText("0.0");

        if (exerciseConfigurationId > 0) {

            ExerciseConfiguration exerciseConfiguration = exerciseConfigurationDao.getById(exerciseConfigurationId);
            Exercise exercise = exerciseDao.getExerciseById(exerciseConfiguration.exerciseId);

            currentExerciseType = exerciseConfiguration.exerciseType;
            setSetPause(exerciseConfiguration.pause);
            setEditSwitch(exerciseConfiguration.canOverwritePause);
            weight.setText(Double.toString(exerciseConfiguration.weight));

            switch (exerciseConfiguration.exerciseType) {
                case Repetition:
                    show(ExerciseType.Repetition);

                    numberField.setText(exerciseConfiguration.repetitions + "");

                    switchExerciseType.setSelectedTab(0);

                    break;
                case Time:
                    show(ExerciseType.Time);

                    setDuration(exerciseConfiguration.duration);

                    switchExerciseType.setSelectedTab(1);
                    break;
            }

            if (exercise != null) {
                String name = exercise.name;

                if (exerciseMatrix.getPositionByName(name) != -1) {
                    Integer position = exerciseMatrix.getPositionByName(name);

                    chosenExerciseSpinner.setSelectedIndex(position);
                    chosenExerciseSpinner.setText(name);
                }
            }
        } else {
            currentExerciseType = ExerciseType.Repetition;
            setEditSwitch(true);
        }

        show(currentExerciseType);
    }

    /**
     * Shows Time based or Repetition based exercise configurations depending
     * on the given exercise type
     *
     * @param exerciseType the exercise type chosen by the user
     */
    private void show(ExerciseType exerciseType) {

        if (exerciseType == ExerciseType.Repetition) {
            numberField.setVisibility(View.VISIBLE);
            timeFieldLayout.setVisibility(View.GONE);

        } else {
            numberField.setVisibility(View.GONE);
            timeFieldLayout.setVisibility(View.VISIBLE);
        }

        currentExerciseType = exerciseType;
    }

    /**
     * Butterknifed click implementation of the save button. Gets the value depending on the chosen exercise type
     * and stores it to the database.
     */
    @OnClick(R.id.exerciseconfiguration_button_save)
    protected void save() {

        ExerciseType chosenType = currentExerciseType;

        ExerciseConfiguration exerciseConfiguration = getExerciseConfiguration();

        exerciseConfiguration.exerciseId = exerciseId;
        exerciseConfiguration.pause = getSetPause();
        exerciseConfiguration.canOverwritePause = getEditSwitch();
        exerciseConfiguration.weight = Double.parseDouble(weight.getText());

        if (exerciseConfiguration.sets == 0) exerciseConfiguration.sets = 1;

        if (chosenType == ExerciseType.Repetition) {

            exerciseConfiguration.repetitions = Integer.parseInt(numberField.getText());
            exerciseConfiguration.exerciseType = ExerciseType.Repetition;

        } else if (chosenType == ExerciseType.Time) {

            long duration = getDuration();

            exerciseConfiguration.duration = duration;
            exerciseConfiguration.exerciseType = ExerciseType.Time;
        }

        // if not already in the routine, then add it
        if (exerciseConfiguration.id == 0) {
            routineHolder.addExercise(exerciseConfiguration.exerciseId, exerciseConfiguration);
        }

        routineHolder.storeCompleteRoutine();

        EventBus.getDefault().post(new PopBackEvent());
    }

    private ExerciseConfiguration getExerciseConfiguration() {

        ExerciseConfiguration exerciseConfiguration;

        if (exerciseConfigurationId == 0) {
            exerciseConfiguration = new ExerciseConfiguration();
        } else {
            exerciseConfiguration = routineHolder.getExerciseConfiguration(exerciseConfigurationId);
        }
        return exerciseConfiguration;
    }


    @Override
    public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {

        if (view.getId() == R.id.chosen_exercise_spinner) {
            String name = (String) item;

            exerciseId = exerciseMatrix.getIdByName(name);

            setTitleByExercise();
        }
    }

    private void setTitleByExercise() {
        Exercise exercise = exerciseDao.getExerciseById(exerciseId);

        Utils.broadcastTitle(exercise.name, routineHolder.getRoutineName());
    }
}
