package de.parkitny.fit.myfit.app.ui.history;


import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.itemanimators.AlphaInAnimator;
import com.onurciner.toastox.ToastOXDialog;

import org.greenrobot.eventbus.EventBus;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.parkitny.fit.myfit.app.R;
import de.parkitny.fit.myfit.app.RpzApplication;
import de.parkitny.fit.myfit.app.dao.WorkoutDao;
import de.parkitny.fit.myfit.app.dao.WorkoutItemDao;
import de.parkitny.fit.myfit.app.entities.Workout;
import de.parkitny.fit.myfit.app.events.WorkoutStatisticsClickedEvent;
import de.parkitny.fit.myfit.app.ui.common.Bar;
import de.parkitny.fit.myfit.app.ui.common.FlexTwoLineItem;
import de.parkitny.fit.myfit.app.ui.common.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
@Bar(Position = 2)
public class HistoryListFragment extends Fragment implements CompactCalendarView.CompactCalendarViewListener {

    @BindView(R.id.history_list_recycler_view)
    protected RecyclerView workoutList;

    @BindView(R.id.compactcalendar_view)
    protected CompactCalendarView calendarView;

    private FastItemAdapter<FlexTwoLineItem> workoutAdapter = new FastItemAdapter<>();

    private WorkoutDao workoutDao;

    private WorkoutItemDao workoutItemDao;

    public HistoryListFragment() {

        workoutDao = RpzApplication.DB.workoutDao();
        workoutItemDao = RpzApplication.DB.workoutItemDao();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_history_list, container, false);
        ButterKnife.bind(this, view);

        workoutAdapter.withOnClickListener((v, adapter, item, position) -> {

            EventBus.getDefault().post(new WorkoutStatisticsClickedEvent(item.getIdentifier(), item.firstLine));

            return true;

        });

        workoutAdapter.withOnLongClickListener((v, adapter, item, position) -> {

            new ToastOXDialog.Build(getActivity())
                    .setTitle(R.string.hint)
                    .setContent(String.format(getString(R.string.delete_workout_short), item.firstLine))
                    .setPositiveText(R.string.ok_option)
                    .setIcon(R.drawable.ic_info_outline_black_18dp)
                    .setPositiveBackgroundColor(Utils.getThemedColor(getActivity(), R.attr.myPrim))
                    .setPositiveTextColorResource(R.color.white)
                    .onPositive(toastOXDialog -> {
                        Workout workout = (Workout) item.getTag();
                        workoutItemDao.deleteById(workout.id);
                        workoutDao.delete(workout);
                        initView();
                        toastOXDialog.dismiss();
                    })
                    .setNegativeText(R.string.cancel_option)
                    .setNegativeBackgroundColor(Utils.getThemedColor(getActivity(), R.attr.myAccent))
                    .setNegativeTextColorResource(R.color.white)
                    .onNegative(toastOXDialog -> toastOXDialog.dismiss()).show();
            return true;
        });

        workoutList.setLayoutManager(new LinearLayoutManager(getActivity()));
        workoutList.setItemAnimator(new AlphaInAnimator());
        workoutList.setNestedScrollingEnabled(false);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        workoutList.setAdapter(workoutAdapter);

        calendarView.setListener(this);
        calendarView.setUseThreeLetterAbbreviation(true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            calendarView.setCurrentDate(new Date(savedInstanceState.getLong("date")));
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        Utils.broadcastTitle(getString(R.string.history), getString(R.string.history_description));

        initView();

        intro();
    }

    private void initView() {
        onMonthScroll(calendarView.getFirstDayOfCurrentMonth());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (calendarView != null) {
            outState.putLong("date", calendarView.getFirstDayOfCurrentMonth().getTime());
        }
    }

    private void setMonthTitle(DateTime localDateTime) {

        Utils.broadcastTitle(getString(R.string.progress), localDateTime.toString("MMMM yyyy"));
    }

    /**
     * Fills the {@link RecyclerView} with all {@link Workout}s of the selected month
     *
     * @param date the month
     */
    private void fillMonth(Date date) {

        ArrayList<FlexTwoLineItem> workouts = new ArrayList<>();

        DateTime dateTime = new DateTime(date);

        DateTime start = new DateTime(dateTime.getYear(), dateTime.getMonthOfYear(), 1, 0, 0);
        DateTime end = start.plusMonths(1);

        List<Workout> workoutsByMonthAndYear = workoutDao.getWorkoutsBetween(start, end);

        int i = 1;

        for (Workout workout : workoutsByMonthAndYear) {
            FlexTwoLineItem item = new FlexTwoLineItem();
            item.withTag(workout);
            item.withIdentifier(workout.id);
            item.firstLine = String.format("%s. %s", i++, workout.name);
            item.description = String.format("%s %s",
                    Utils.getDateTimeString(workout.startTime, getActivity()),
                    Utils.formatPeriod(workout.workoutDuration));

            workouts.add(item);
        }

        workoutAdapter.clear();
        workoutAdapter.add(workouts);
        addEvents2Calendar(workoutsByMonthAndYear);
    }

    /**
     * Adds the given {@link Workout}s as {@link Event}s to the {@link CompactCalendarView}
     *
     * @param workoutsByMonthAndYear the {@link Workout}s of the given month and year
     */
    private void addEvents2Calendar(List<Workout> workoutsByMonthAndYear) {

        // needed to store occupied days
        HashMap<DateTime, Boolean> dates = new HashMap<>();

        for (Workout workoutHistory : workoutsByMonthAndYear) {
            Event event = new Event(
                    Color.argb(20, 10, 10, 10),
                    workoutHistory.startTime);

            calendarView.addEvent(
                    event,
                    true);

            DateTime dateTime = new DateTime(workoutHistory.startTime).withTime(0, 0, 0, 0);

            // if not already stored as occupied day, store it
            if (!dates.containsKey(dateTime)) dates.put(dateTime, true);
        }


        DateTime currentStart = new DateTime(calendarView.getFirstDayOfCurrentMonth().getTime());
        DateTime end = currentStart.plusMonths(1);

        while (currentStart.isBefore(end.toInstant())) {

            if (!dates.containsKey(currentStart.withTime(0, 0, 0, 0))) {
                calendarView.setCurrentDate(currentStart.toDate());
                break;
            }

            currentStart = currentStart.plusDays(1);
        }
    }

    @Override
    public void onDayClick(Date dateClicked) {

    }

    @Override
    public void onMonthScroll(Date firstDayOfNewMonth) {

        setMonthTitle(new DateTime(firstDayOfNewMonth.getTime()));

        calendarView.removeAllEvents();

        fillMonth(firstDayOfNewMonth);
    }

    private void intro() {

//        if (!Pref.getBoolean(FitParams.ShowedCalendarSwipe, false)) {
//
//            Utils.getIntro(
//                    getActivity(),
//                    R.string.calendar_swipe_hint,
//                    calendarView,
//                    FitParams.ShowedCalendarSwipe,
//                    ShapeType.CIRCLE).show();
//
//            Pref.putBoolean(FitParams.ShowedCalendarSwipe, true);
//        }
    }
}
