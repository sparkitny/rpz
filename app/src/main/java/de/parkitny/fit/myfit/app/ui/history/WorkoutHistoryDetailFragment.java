package de.parkitny.fit.myfit.app.ui.history;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.parkitny.fit.myfit.app.R;
import de.parkitny.fit.myfit.app.RpzApplication;
import de.parkitny.fit.myfit.app.dao.WorkoutDao;
import de.parkitny.fit.myfit.app.dao.WorkoutItemDao;
import de.parkitny.fit.myfit.app.entities.Workout;
import de.parkitny.fit.myfit.app.entities.WorkoutItem;
import de.parkitny.fit.myfit.app.ui.common.Bar;
import de.parkitny.fit.myfit.app.ui.common.Utils;


/**
 * A simple {@link Fragment} subclass.
 */
@Bar(Position = 2)
public class WorkoutHistoryDetailFragment extends Fragment {

    private static final String TAG = "HistoricalWorkoutsFragm";

    @BindView(R.id.workout_list)
    protected RecyclerView workoutHistoryList;

    @BindView(R.id.workout_name_text)
    protected TextView workoutName;

    @BindView(R.id.workout_time_text)
    protected TextView workoutTime;

    @BindView(R.id.workout_pause_text)
    protected TextView workoutPause;

    /**
     * the id of the {@link Workout}
     */
    private long workoutId = 0;

    private FastItemAdapter<WorkoutHistoryItemGroup> listAdapter = new FastItemAdapter<>();

    private WorkoutItemDao workoutItemDao;

    private WorkoutDao workoutDao;

    public WorkoutHistoryDetailFragment() {

        workoutItemDao = RpzApplication.DB.workoutItemDao();
        workoutDao = RpzApplication.DB.workoutDao();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_historical_workouts_fragments, container, false);

        ButterKnife.bind(this, view);

        workoutId = getArguments().getLong("workoutId", -1);

        workoutHistoryList.setAdapter(listAdapter);
        workoutHistoryList.setLayoutManager(new LinearLayoutManager(getActivity()));
        workoutHistoryList.setNestedScrollingEnabled(true);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        refreshList();
    }

    private void refreshList() {

        Workout workout = workoutDao.getById(workoutId);
        workoutName.setText(workout.name);
        workoutTime.setText(Utils.formatPeriod(workout.workoutDuration));
        workoutPause.setText(Utils.formatPeriod(workout.totalPause));

        listAdapter.clear();

        if (workoutId != -1) {

            List<WorkoutItem> workoutItems = workoutItemDao.getByWorkoutIdOrderStartTime(workoutId);

            LinkedHashMap<String, ArrayList<WorkoutItem>> groupedWorkoutItems = new LinkedHashMap<>();

            for (WorkoutItem workoutItem : workoutItems) {

                String key = String.format("%s_%s", workoutItem.globalId, workoutItem.round);

                if (!groupedWorkoutItems.containsKey(key)) {
                    groupedWorkoutItems.put(key, new ArrayList<WorkoutItem>());
                }

                groupedWorkoutItems.get(key).add(workoutItem);
            }

            for (ArrayList<WorkoutItem> group : groupedWorkoutItems.values()) {

                WorkoutHistoryItemGroup workoutHistoryItemGroup = new WorkoutHistoryItemGroup();
                workoutHistoryItemGroup.context = getContext();
                workoutHistoryItemGroup.withIdentifier(group.get(0).id);
                workoutHistoryItemGroup.groupName = group.get(0).name;

                workoutHistoryItemGroup.items.addAll(group);

                listAdapter.add(workoutHistoryItemGroup);
            }

            workoutHistoryList.requestLayout();
            workoutHistoryList.invalidate();

            Utils.broadcastTitle(workout.name, Utils.formatPeriod(workout.workoutDuration));
        }
    }
}
