package de.parkitny.fit.myfit.app.ui.exercise;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.tramsun.libs.prefcompat.Pref;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.parkitny.fit.myfit.app.R;
import de.parkitny.fit.myfit.app.entities.Exercise;
import de.parkitny.fit.myfit.app.events.AddExerciseEvent;
import de.parkitny.fit.myfit.app.ui.common.Bar;
import de.parkitny.fit.myfit.app.ui.common.FlexTwoLineItem;
import de.parkitny.fit.myfit.app.ui.common.HeaderItem;
import de.parkitny.fit.myfit.app.ui.common.Utils;
import de.parkitny.fit.myfit.app.utils.FitParams;
import de.parkitny.fit.myfit.app.viewmodels.ExerciseViewModel;


/**
 * The {@link ExerciseList} simply shows the exisiting list of {@link Exercise} in the database
 */
@Bar(Position = 0)
public class ExerciseList extends Fragment {

    @BindView(R.id.standard_fragment_list_recycler_view)
    protected RecyclerView exerciseList;
    @BindView(R.id.standard_fragment_list_button_add)
    protected FloatingActionButton addExercise;

    private FastItemAdapter<IItem> adapter = new FastItemAdapter<>();

    public ExerciseList() {

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.standard_fragment_list, container, false);

        ButterKnife.bind(this, view);

        adapter.withOnClickListener((v, adapter, item, position) -> {

            EventBus.getDefault().post(new AddExerciseEvent(item.getIdentifier()));

            return true;
        });

        exerciseList.setLayoutManager(new LinearLayoutManager(getActivity()));

        exerciseList.setAdapter(adapter);

        ExerciseViewModel exerciseViewModel = ViewModelProviders.of(getActivity()).get(ExerciseViewModel.class);
        exerciseViewModel.getExercises().observe(this, items -> initItems(items));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        Utils.broadcastTitle(getString(R.string.exercises), "");

        intro();
    }

    /**
     * Shows the intro
     */
    private void intro() {

        if (!Pref.getBoolean(FitParams.ShowedAddExercise, false)) {

            Utils.getSpotlight(
                    getActivity(),
                    addExercise,
                    getString(R.string.spot_new_exercise),
                    getString(R.string.spot_new_exercise_desc))
                    .show();

            Pref.putBoolean(FitParams.ShowedAddExercise, true);
        }
    }


    /**
     * Initializes the item of the {@link RecyclerView} using {@link FastItemAdapter} and
     * {@link FlexTwoLineItem}
     */
    private void initItems(List<Exercise> exercises) {

        adapter.clear();

        HashMap<String, HeaderItem> map = new HashMap<>();

        for (Exercise exercise : exercises) {

            FlexTwoLineItem item = new FlexTwoLineItem()
                    .withIdentifier(exercise.id);

            item.firstLine = exercise.name;
            item.description = exercise.info;

            String firstLetter = exercise.name.substring(0, 1).toUpperCase();

            if (!map.containsKey(firstLetter)) {

                HeaderItem headerItem = new HeaderItem();
                headerItem.withHeader(firstLetter);
                map.put(firstLetter, headerItem);
                adapter.add(headerItem);
            }

            adapter.add(item);
        }
    }

    @OnClick(R.id.standard_fragment_list_button_add)
    protected void addExercise(View view) {

        EventBus.getDefault().post(new AddExerciseEvent(0));
    }
}
