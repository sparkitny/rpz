package de.parkitny.fit.myfit.app.ui.exercise;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.nex3z.flowlayout.FlowLayout;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.parkitny.fit.myfit.app.R;
import de.parkitny.fit.myfit.app.entities.EmphasisType;
import de.parkitny.fit.myfit.app.entities.Exercise;
import de.parkitny.fit.myfit.app.entities.ExerciseEmphasis;
import de.parkitny.fit.myfit.app.events.PopBackEvent;
import de.parkitny.fit.myfit.app.ui.common.Bar;
import de.parkitny.fit.myfit.app.ui.common.Utils;
import de.parkitny.fit.myfit.app.viewmodels.ExerciseViewModel;
import eltos.simpledialogfragment.SimpleDialog;
import eltos.simpledialogfragment.form.Input;
import eltos.simpledialogfragment.form.SimpleFormDialog;

@Bar(Position = 0)
public class ExerciseDetail extends Fragment implements SimpleDialog.OnDialogResultListener {

    private final String exerciseName = "exerciseName";
    private final String exerciseNameTag = "exerciseNameTag";

    @BindView(R.id.exercise_detail_edit_name)
    protected TextView name;

    @BindView(R.id.emphasis_flow_list)
    protected FlowLayout emphasisFlowList;

    private HashMap<EmphasisType, ToggleButton> mapping;

    /**
     * the id of the exercise treated here
     */
    private long id = 0;

    private ExerciseViewModel model;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        id = getArguments().getLong("id");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.fragment_exercise_detail, container, false);

        ButterKnife.bind(this, mainView);

        model = ViewModelProviders.of(getActivity()).get(ExerciseViewModel.class);

        if (id > 0) {

            model.getExerciseByIdLive(id).observe(this, exercise ->
                    {
                        Utils.broadcastTitle(getString(R.string.exercise), exercise.name);
                        name.setText(exercise.name);
                    }
            );
        } else {
            name.setText(getString(R.string.new_exercise));
            Utils.broadcastTitle(getString(R.string.new_exercise), name.getText().toString());
        }

        model.getExerciseEmphasisById(id).observe(this, this::initEmphasisList);

        return mainView;
    }

    private void initEmphasisList(List<ExerciseEmphasis> exerciseEmphases) {

        mapping = new HashMap<>();

        EmphasisType[] emphasisTypes = EmphasisType.values();

        emphasisFlowList.removeAllViews();

        for (EmphasisType emphasisType : emphasisTypes) {

            ToggleButton toggleButton = new ToggleButton(getContext());
            String name = Utils.getEmphasisTypeName(getActivity(), emphasisType);
            toggleButton.setText(name);
            toggleButton.setTextOff(name);
            toggleButton.setTextOn(name);
            toggleButton.setTag(emphasisType);
            toggleButton.setPadding(8, 8, 8, 8);
            toggleButton.setBackgroundResource(R.drawable.toggle_button);

            mapping.put(emphasisType, toggleButton);

            emphasisFlowList.addView(toggleButton);
        }

        for (ExerciseEmphasis exerciseEmphasis : exerciseEmphases) {

            ToggleButton item = mapping.get(exerciseEmphasis.emphasisType);
            item.setChecked(true);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @OnClick(R.id.play_video)
    protected void playVideo() {

    }

    @OnClick(R.id.exercise_detail_edit_name)
    protected void changeExerciseName() {

        SimpleFormDialog.build()
                .title(R.string.exercise_name)
                .pos(R.string.ok_option)
                .neg(R.string.cancel_option)
                .fields(
                        Input.name(exerciseName).required().inputType(InputType.TYPE_CLASS_TEXT)
                )
                .show(this, exerciseNameTag);
    }

    @Override
    public boolean onResult(@NonNull String dialogTag, int which, @NonNull Bundle extras) {

        if (which == BUTTON_NEGATIVE) return false;

        if (dialogTag.equals(exerciseNameTag)) {

            name.setText(extras.getString(exerciseName));
        }

        return false;
    }

    @OnClick(R.id.exercise_detail_button_save)
    public void saveOrUpdate() {

        Exercise exercise;

        if (id == 0) {
            exercise = new Exercise();
        } else {
            exercise = model.getExerciseById(id);
        }

        exercise.name = name.getText().toString().trim();

        id = model.insertExercise(exercise);
        exercise.id = id;

        model.deleteExerciseEmphasisById(id);

        for (ToggleButton item : mapping.values()) {

            if (item.isChecked()) {

                ExerciseEmphasis exerciseEmphasis = new ExerciseEmphasis();
                exerciseEmphasis.exerciseId = id;
                exerciseEmphasis.emphasisType = (EmphasisType) item.getTag();
                model.insertExerciseEmphasis(exerciseEmphasis);
            }
        }

        exercise.info = Utils.getEmphasisTypeInfo(id, getContext());

        model.insertExercise(exercise);

        EventBus.getDefault().post(new PopBackEvent());
    }
}
