package de.parkitny.fit.myfit.app.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.eightbitlab.bottomnavigationbar.BottomBarItem;
import com.eightbitlab.bottomnavigationbar.BottomNavigationBar;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.context.IconicsLayoutInflater2;
import com.onurciner.toastox.ToastOX;
import com.tramsun.libs.prefcompat.Pref;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.parkitny.fit.myfit.app.R;
import de.parkitny.fit.myfit.app.events.AddExerciseEvent;
import de.parkitny.fit.myfit.app.events.ExerciseConfigurationClickedEvent;
import de.parkitny.fit.myfit.app.events.FragmentType;
import de.parkitny.fit.myfit.app.events.LockDrawerEvent;
import de.parkitny.fit.myfit.app.events.PopBackEvent;
import de.parkitny.fit.myfit.app.events.RoutineClickedEvent;
import de.parkitny.fit.myfit.app.events.SetTitleEvent;
import de.parkitny.fit.myfit.app.events.StartFragmentEvent;
import de.parkitny.fit.myfit.app.events.ToastEvent;
import de.parkitny.fit.myfit.app.events.WorkoutClickedEvent;
import de.parkitny.fit.myfit.app.events.WorkoutStatisticsClickedEvent;
import de.parkitny.fit.myfit.app.ui.common.Bar;
import de.parkitny.fit.myfit.app.ui.common.Utils;
import de.parkitny.fit.myfit.app.ui.exercise.ExerciseDetail;
import de.parkitny.fit.myfit.app.ui.exercise.ExerciseList;
import de.parkitny.fit.myfit.app.ui.history.HistoryListFragment;
import de.parkitny.fit.myfit.app.ui.history.WorkoutHistoryDetailFragment;
import de.parkitny.fit.myfit.app.ui.routine.ExerciseConfigurationFragment;
import de.parkitny.fit.myfit.app.ui.routine.RoutineDetail;
import de.parkitny.fit.myfit.app.ui.settings.SettingsFragment;
import de.parkitny.fit.myfit.app.ui.showcase.ShowcaseActivity;
import de.parkitny.fit.myfit.app.ui.weight.WeightFragment;
import de.parkitny.fit.myfit.app.ui.workout.WorkoutListFragment;
import de.parkitny.fit.myfit.app.ui.workout.WorkoutTimerFragment;
import de.parkitny.fit.myfit.app.utils.FitParams;
import de.parkitny.fit.myfit.app.utils.StopWatchMode;
import de.parkitny.fit.myfit.app.utils.StopWatchService;
import timber.log.Timber;

public class RpzNavDrawer extends AppCompatActivity {

    @BindView(R.id.toolbar)
    protected Toolbar toolbar;

    @BindView(R.id.bottom_bar)
    protected BottomNavigationBar bottomBar;

//    @Override
//    protected void onStart() {
//        super.onStart();
//
//        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
//
//        if (account == null) {
//            Fragment fragment = new UserLogin(this);
//            changeFragment(fragment, "userlogin");
//        }
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        switch (Utils.getTheme(this)) {
            case "Fitness":
                setTheme(R.style.Men);
                break;
            case "Yoga":
                setTheme(R.style.Women);
                break;
            case "None":
                setTheme(R.style.Men);
                break;
        }

        LayoutInflaterCompat.setFactory2(getLayoutInflater(), new IconicsLayoutInflater2(getDelegate()));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_drawer);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        initBottomNavigation();

        startDashboard();
    }

    private void initBottomNavigation() {

        BottomBarItem exerciseItem = new BottomBarItem(
                new IconicsDrawable(this).icon(FontAwesome.Icon.faw_list).sizeDp(24).color(Color.WHITE),
                R.string.exercises);
        bottomBar.addTab(exerciseItem);

        BottomBarItem workoutItem = new BottomBarItem(
                new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_timer).sizeDp(24).color(Color.WHITE),
                R.string.workouts);
        bottomBar.addTab(workoutItem);

        BottomBarItem historyItem = new BottomBarItem(
                new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_trending_up).sizeDp(24).color(Color.WHITE),
                R.string.statistics);
        bottomBar.addTab(historyItem);

        BottomBarItem weightItem = new BottomBarItem(
                R.mipmap.weight_icon,
                R.string.weight);
        bottomBar.addTab(weightItem);

        BottomBarItem settingsItem = new BottomBarItem(
                new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_settings).sizeDp(24).color(Color.WHITE),
                R.string.action_settings);
        bottomBar.addTab(settingsItem);

        if (!bottomBar.isSelected())
            bottomBar.selectTab(1, true);

        bottomBar.setOnSelectListener(position -> {
            switch (position) {
                case 0:
                    startFragment(new StartFragmentEvent(FragmentType.EXERCISES, null));
                    break;
                case 1:
                    startFragment(new StartFragmentEvent(FragmentType.WORKOUTS, null));
                    break;
                case 2:
                    startFragment(new StartFragmentEvent(FragmentType.HISTORY, null));
                    break;
                case 3:
                    startFragment(new StartFragmentEvent(FragmentType.WEIGHT, null));
                    break;
                case 4:
                    startFragment(new StartFragmentEvent(FragmentType.SETTINGS, null));
                    break;
            }
        });
    }

    @Subscribe
    public void toast(ToastEvent event) {
        ToastOX.info(this, event.getMessage(), Toast.LENGTH_SHORT);
    }

    /**
     * Starts a {@link Fragment} given the {@link FragmentType}
     *
     * @param event the {@link StartFragmentEvent}
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void startFragment(StartFragmentEvent event) {

        cleanBackStack();

        Bundle args;

        if (event.getBundle() != null) {
            args = event.getBundle();
        } else {
            args = new Bundle();
        }

        switch (event.getFragmentType()) {
            case EXERCISES: {
                Fragment fragment = new ExerciseList();
                fragment.setArguments(args);

                changeFragment(fragment, "Open Exercises");
            }
            break;
            case WORKOUTS: {
                if (StopWatchService.GlobalIndicator.getWorkoutMode() != StopWatchMode.NONE) {

                } else {

                    Fragment fragment = new WorkoutListFragment();
                    fragment.setArguments(args);

                    changeFragment(fragment, "Open Workout");
                }
            }
            break;
            case HISTORY: {
                Fragment fragment = new HistoryListFragment();
                fragment.setArguments(args);

                changeFragment(fragment, "Open Statistic");
            }
            break;
            case WEIGHT: {
                Fragment fragment = new WeightFragment();
                fragment.setArguments(args);

                changeFragment(fragment, "Open Weight");
            }
            break;
            case SETTINGS: {
                Fragment fragment = new SettingsFragment();
                fragment.setArguments(args);
                changeFragment(fragment, "Settings");
            }
            break;
            case WORKOUT_DETAIL: {
                Fragment fragment = new WorkoutTimerFragment();

                fragment.setArguments(args);
                changeFragment(fragment, "Open WorkoutTimer");
            }
            break;
        }
    }

    /**
     * Starts the {@link WorkoutListFragment} only if there's no item on the back stack
     */
    private void startDashboard() {

        if (getSupportFragmentManager().getBackStackEntryCount() > 0) return;

        if (!Pref.getBoolean(FitParams.ShowedGlobalIntro, false)) {

            Pref.putBoolean(FitParams.ShowedGlobalIntro, true);

            Intent intent = new Intent(this, ShowcaseActivity.class);
            startActivity(intent);

        } else {

            startFragment(new StartFragmentEvent(FragmentType.WORKOUTS, null));
        }
    }

    /**
     * Removes all {@link Fragment}s currently on the back stack
     */
    private void cleanBackStack() {
        FragmentManager fragmentManager = getSupportFragmentManager();

        // remove all elements from the back stack
        if (fragmentManager.getBackStackEntryCount() > 0) {
            FragmentManager.BackStackEntry entry = fragmentManager.getBackStackEntryAt(0);
            fragmentManager.popBackStack(entry.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    /**
     * Changes the fragment
     *
     * @param fragment
     * @param name
     */
    private void changeFragment(Fragment fragment, String name) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .setCustomAnimations(
                        R.anim.abc_grow_fade_in_from_bottom,
                        R.anim.abc_shrink_fade_out_from_bottom,
                        R.anim.abc_grow_fade_in_from_bottom,
                        R.anim.abc_shrink_fade_out_from_bottom)
                .replace(R.id.content_frame, fragment)
                .addToBackStack(name)
                .commit();

        Class<?> fragmentClass = fragment.getClass();

        if (fragmentClass.isAnnotationPresent(Bar.class)) {
            Bar barAnnotation = fragmentClass.getAnnotation(Bar.class);
            int position = barAnnotation.Position();
            bottomBar.selectTab(position, true);
        }
    }

    /**
     * Override the default behavior when pressing the back button
     */
    @Override
    public void onBackPressed() {

        // disable the back button when the user does a workout
        if (StopWatchService.GlobalIndicator.getWorkoutMode() != StopWatchMode.NONE)
            return;

        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        // check whether a {@link Workout} is running and if so start the {@link WorkoutTimerFragment}
        if (StopWatchService.GlobalIndicator.getWorkoutMode() != StopWatchMode.NONE) {
            onWorkoutClicked(new WorkoutClickedEvent(StopWatchService.GlobalIndicator.getRoutineId()));
        }
    }

    @Subscribe
    public void onWorkoutClicked(WorkoutClickedEvent event) {
        Fragment fragment = new WorkoutTimerFragment();
        Bundle args = new Bundle();
        args.putLong("id", event.getId());

        fragment.setArguments(args);
        changeFragment(fragment, "Open WorkoutTimer");
    }

    @Subscribe
    public void onExerciseClicked(AddExerciseEvent event) {
        Fragment fragment = new ExerciseDetail();
        Bundle args = new Bundle();
        args.putLong("id", event.getId());
        fragment.setArguments(args);

        changeFragment(fragment, "Open Exercise Detail");
    }

    /**
     * Sets the title of the {@link ActionBar}
     *
     * @param event the {@link SetTitleEvent} containing the title to be shown
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void setTitle(SetTitleEvent event) {

        setTitle(event.getTitle());
        getSupportActionBar().setSubtitle(event.getSubtitle());
    }

    /**
     * Is used to lock the {@link DrawerLayout} such that it's impossible to
     * choose another section to go
     *
     * @param event <code>true</code> if should be locked, <code>false</code> is normal operation
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void lockDrawer(LockDrawerEvent event) {
        bottomBar.setVisibility(event.isLock() ? View.GONE : View.VISIBLE);
    }

    @Subscribe
    public void onRoutineClicked(RoutineClickedEvent event) {

        Fragment fragment = new RoutineDetail();
        Bundle args = new Bundle();

        args.putLong("id", event.getRoutineId());
        fragment.setArguments(args);

        changeFragment(fragment, "Open Exercise Detail");
    }

    @Subscribe
    public void onExerciseConfigurationClicked(ExerciseConfigurationClickedEvent event) {
        exerciseConfigurationClickedOrChosen(event.getExerciseConfigurationId(), event.getExerciseId(), event.getRoutineId());
    }

    private void exerciseConfigurationClickedOrChosen(long exerciseConfigurationId, long exerciseId, long routineId) {
        Fragment fragment = new ExerciseConfigurationFragment();
        Bundle args = new Bundle();

        args.putLong("exerciseConfigurationId", exerciseConfigurationId);
        args.putLong("exerciseId", exerciseId);
        args.putLong("routineId", routineId);
        fragment.setArguments(args);

        changeFragment(fragment, "Open Exercise Configuration Detail");
    }

    @Subscribe
    public void popBackStack(PopBackEvent event) {
        getSupportFragmentManager().popBackStack();
    }

    @Subscribe
    public void statisticsListItemClicked(WorkoutStatisticsClickedEvent event) {
        Timber.d("Id of the StatisticsWorkout %s", event.getId());

        Fragment fragment = new WorkoutHistoryDetailFragment();
        Bundle args = new Bundle();
        args.putLong("id", event.getId());
        args.putLong("workoutId", event.getId());
        args.putString("firstLine", event.getName());
        fragment.setArguments(args);

        changeFragment(fragment, "Open Statistics Detail");
    }
}
