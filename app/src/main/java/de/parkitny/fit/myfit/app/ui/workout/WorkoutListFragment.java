package de.parkitny.fit.myfit.app.ui.workout;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.listeners.OnClickListener;
import com.mikepenz.fastadapter_extensions.dialog.FastAdapterBottomSheetDialog;
import com.mikepenz.fastadapter_extensions.drag.ItemTouchCallback;
import com.mikepenz.fastadapter_extensions.drag.SimpleDragCallback;
import com.mikepenz.fastadapter_extensions.items.TwoLineItem;
import com.mikepenz.fastadapter_extensions.swipe.SimpleSwipeCallback;
import com.mikepenz.fastadapter_extensions.swipe.SimpleSwipeDragCallback;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.context.IconicsContextWrapper;
import com.onurciner.toastox.ToastOXDialog;
import com.tramsun.libs.prefcompat.Pref;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.parkitny.fit.myfit.app.R;
import de.parkitny.fit.myfit.app.RpzApplication;
import de.parkitny.fit.myfit.app.dao.RoutineDao;
import de.parkitny.fit.myfit.app.domain.RoutineHolder;
import de.parkitny.fit.myfit.app.entities.Routine;
import de.parkitny.fit.myfit.app.events.RoutineClickedEvent;
import de.parkitny.fit.myfit.app.events.WorkoutClickedEvent;
import de.parkitny.fit.myfit.app.ui.common.Bar;
import de.parkitny.fit.myfit.app.ui.common.FlexTwoLineItem;
import de.parkitny.fit.myfit.app.ui.common.HeaderItem;
import de.parkitny.fit.myfit.app.ui.common.Utils;
import de.parkitny.fit.myfit.app.ui.routine.RoutineHelper;
import de.parkitny.fit.myfit.app.utils.FitParams;
import eltos.simpledialogfragment.SimpleDialog;
import eltos.simpledialogfragment.form.Input;
import eltos.simpledialogfragment.form.SimpleFormDialog;

@Bar(Position = 1)
public class WorkoutListFragment extends Fragment implements ItemTouchCallback, SimpleSwipeCallback.ItemSwipeCallback, OnClickListener<IItem>, SimpleDialog.OnDialogResultListener {

    private final String routineadd = "routineadd";
    private final String newRoutineName = "name";
    private final String changename = "changename";
    @BindView(R.id.standard_fragment_list_recycler_view)
    protected RecyclerView workoutList;
    @BindView(R.id.standard_fragment_list_button_add)
    protected FloatingActionButton floatingActionButton;

    /**
     * Contains all the items displayed in the {@link RecyclerView}
     */
    private FastItemAdapter<IItem> routineAdapter = new FastItemAdapter<>();

    private FastAdapterBottomSheetDialog<TwoLineItem> dialogAdapter;

    private RoutineDao routineDao;

    private SimpleDragCallback touchCallback;

    private ItemTouchHelper touchHelper;

    public WorkoutListFragment() {

        routineDao = RpzApplication.DB.routineDao();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(IconicsContextWrapper.wrap(context));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.standard_fragment_list, container, false);

        ButterKnife.bind(this, view);

        setHasOptionsMenu(true);

        workoutList.setAdapter(routineAdapter);

        routineAdapter.withOnClickListener(this);

        workoutList.setLayoutManager(new LinearLayoutManager(getActivity()));

        dialogAdapter = new FastAdapterBottomSheetDialog<TwoLineItem>(getActivity())
                .withAdapter(new FastAdapter<>());

        dialogAdapter.withItems(getMenuItems(Pref.getBoolean(FitParams.WorkoutListMenuArchiveSwitch, false)));

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_switch, menu);

        MenuItem menuItem = menu.findItem(R.id.menu_archive_switch);
        final SwitchCompat switchCompat = ((SwitchCompat) menuItem.getActionView());
        switchCompat.setButtonDrawable(new IconicsDrawable(getActivity()).icon(GoogleMaterial.Icon.gmd_archive).color(Color.BLACK).sizeDp(18));

        switchCompat.setChecked(Pref.getBoolean(FitParams.WorkoutListMenuArchiveSwitch, false));

        switchCompat.setOnClickListener(event -> {
            Pref.putBoolean(FitParams.WorkoutListMenuArchiveSwitch, switchCompat.isChecked());
            initRoutineList();
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    @NonNull
    private ArrayList<TwoLineItem> getMenuItems(boolean archived) {

        ArrayList<TwoLineItem> result = new ArrayList<>();

        result.add(new TwoLineItem()
                .withIcon(new IconicsDrawable(getActivity()).icon(GoogleMaterial.Icon.gmd_edit).color(Color.BLACK).sizeDp(18))
                .withName(getString(R.string.rename_workout))
                .withDescription(getString(R.string.rename_workout_desc)));
        result.add(new TwoLineItem()
                .withIcon(new IconicsDrawable(getActivity()).icon(GoogleMaterial.Icon.gmd_assignment).color(Color.BLACK).sizeDp(18))
                .withName(getString(R.string.action_edit))
                .withDescription(getString(R.string.action_edit_description)));
        result.add(new TwoLineItem()
                .withIcon(new IconicsDrawable(getActivity()).icon(GoogleMaterial.Icon.gmd_content_copy).color(Color.BLACK).sizeDp(18))
                .withName(getString(R.string.action_copy))
                .withDescription(getString(R.string.action_copy_description)));

        if (!archived) {
            result.add(new TwoLineItem()
                    .withIcon(new IconicsDrawable(getActivity()).icon(GoogleMaterial.Icon.gmd_archive).color(Color.BLACK).sizeDp(18))
                    .withName(getString(R.string.action_archive))
                    .withDescription(getString(R.string.action_archive_description)));
        } else {
            result.add(new TwoLineItem()
                    .withIcon(new IconicsDrawable(getActivity()).icon(GoogleMaterial.Icon.gmd_unarchive).color(Color.GREEN).sizeDp(18))
                    .withName(getString(R.string.action_dearchive))
                    .withDescription(getString(R.string.action_dearchive_description)));
        }
        result.add(new TwoLineItem()
                .withIcon(new IconicsDrawable(getActivity()).icon(GoogleMaterial.Icon.gmd_delete).color(Color.RED).sizeDp(18))
                .withName(getString(R.string.action_delete))
                .withDescription(getString(R.string.action_delete_description)));

        return result;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        routineAdapter.withOnLongClickListener((v, adapter, clickedItem, routinePosition) -> {

            if (!(clickedItem instanceof FlexTwoLineItem)) return false;

            dialogAdapter.setNewList(getMenuItems(Pref.getBoolean(FitParams.WorkoutListMenuArchiveSwitch, false)));

            dialogAdapter.withOnClickListener((v1, adapter1, item, position) -> {

                if (position == 0) {
                    FlexTwoLineItem flexTwoLineItem = ((FlexTwoLineItem) clickedItem);
                    renameWorkout(flexTwoLineItem.getIdentifier(), flexTwoLineItem.firstLine);
                }

                if (position == 1) {
                    EventBus.getDefault()
                            .post(new RoutineClickedEvent(clickedItem.getIdentifier()));
                }
                if (position == 2) {
                    RoutineHelper.copyRoutine(clickedItem.getIdentifier(), getString(R.string.copy_suffix));
                    initRoutineList();
                }
                if (position == 3)
                    toggleArchiveWorkoutAndUpdate(((FlexTwoLineItem) clickedItem));

                if (position == 4) {
                    showDeleteDialog(((FlexTwoLineItem) clickedItem), routinePosition);
                }

                dialogAdapter.dismiss();

                return true;
            });

            dialogAdapter.show();

            return true;
        });

        touchCallback = new SimpleSwipeDragCallback(
                this,
                this,
                new IconicsDrawable(getActivity()).icon(GoogleMaterial.Icon.gmd_edit).sizeDp(18),
                ItemTouchHelper.LEFT
        )
                .withBackgroundSwipeLeft(Utils.getThemedColor(getActivity(), R.attr.myPrim))
                .withLeaveBehindSwipeRight(new IconicsDrawable(getActivity()).icon(GoogleMaterial.Icon.gmd_delete).sizeDp(18))
                .withBackgroundSwipeRight(Utils.getColor(getActivity(), R.color.md_red_400));

        touchCallback.setIsDragEnabled(false);

        touchHelper = new ItemTouchHelper(touchCallback);
        touchHelper.attachToRecyclerView(workoutList);
    }

    /**
     * Shows a deletion dialogAdapter to delete the given {@link Routine}
     *
     * @param item
     * @param position
     */
    @NonNull
    private void deleteWorkoutAndUpdate(final FlexTwoLineItem item, final int position) {

        Routine routine = (Routine) item.getTag();
        RoutineHolder routineHolder = new RoutineHolder(routine.id);
        routineHolder.deleteCompleteRoutine();
        routineAdapter.remove(position);
        initRoutineList();
    }

    private void toggleArchiveWorkoutAndUpdate(final FlexTwoLineItem item) {

        Routine routine = ((Routine) item.getTag());
        routine.archived = (!routine.archived);
        routineDao.insert(routine);

        initRoutineList();
    }

    @Override
    public void onResume() {
        super.onResume();

        Utils.broadcastTitle(getString(R.string.workouts), getString(R.string.all_workouts));

        initRoutineList();

    }

    @Override
    public void onAttachFragment(Fragment childFragment) {
        super.onAttachFragment(childFragment);

        intro();
    }

    /**
     * Initializes the list
     */
    private void initRoutineList() {

        routineAdapter.clear();

        HashMap<String, HeaderItem> map = new HashMap<>();

        List<Routine> routineList = routineDao.getAll();

        for (Routine routine : routineList) {

            if (!Pref.getBoolean(FitParams.WorkoutListMenuArchiveSwitch, false) && routine.archived)
                continue;

            FlexTwoLineItem item = new FlexTwoLineItem()
                    .withIdentifier(routine.id);
            item.firstLine = routine.name;
            item.description = String.format("%s %s", routine.rounds, getResources().getQuantityString(R.plurals.rounds, routine.rounds));
            item.withTag(routine);

            if (routine.archived) {
                item.rightIcon = new IconicsDrawable(getActivity()).icon(GoogleMaterial.Icon.gmd_archive).sizeDp(16).color(Color.BLACK);
            }

            if (routine.name.length() == 0) {
                routine.name = getString(R.string.empty_name);
            }

            String firstLetter = routine.name.substring(0, 1).toUpperCase();
            if (!map.containsKey(firstLetter)) {
                HeaderItem headerItem = new HeaderItem().withHeader(firstLetter);
                map.put(firstLetter, headerItem);
                routineAdapter.add(headerItem);
            }

            routineAdapter.add(item);
        }
    }

    @Override
    public boolean onClick(View v, IAdapter<IItem> adapter, @NonNull IItem item, int position) {

        if (item instanceof HeaderItem) return false;

        EventBus.getDefault().post(new WorkoutClickedEvent(item.getIdentifier()));

        return true;
    }

    /**
     * Adds a {@link Routine}
     *
     * @param view
     */
    @OnClick(R.id.standard_fragment_list_button_add)
    protected void addRoutine(View view) {

        SimpleFormDialog.build()
                .title(R.string.routine_name)
                .fields(
                        Input.name(newRoutineName)
                                .hint(R.string.routine_name)
                                .inputType(InputType.TYPE_CLASS_TEXT)
                                .required())
                .cancelable(true)
                .neg(R.string.cancel_option)
                .pos(R.string.ok_option)
                .show(this, routineadd);
    }

    private void renameWorkout(long id, String routineName) {

        Bundle bundle = new Bundle();
        bundle.putLong("id", id);

        SimpleFormDialog.build()
                .title(R.string.routine_name)
                .extra(bundle)
                .fields(
                        Input.name(newRoutineName)
                                .text(routineName)
                                .hint(R.string.routine_name)
                                .inputType(InputType.TYPE_CLASS_TEXT)
                                .required())
                .cancelable(true)
                .neg(R.string.cancel_option)
                .pos(R.string.ok_option)
                .show(this, changename);
    }

    @Override
    public boolean onResult(@NonNull String dialogTag, int which, @NonNull Bundle extras) {

        if (which == BUTTON_NEGATIVE) return false;

        if (dialogTag.equals(routineadd)) {

            RoutineHolder routineHolder = new RoutineHolder();
            long id = routineHolder.newRoutine(extras.getString(newRoutineName));

            EventBus.getDefault().post(new RoutineClickedEvent(id));

            return true;
        }

        if (dialogTag.equals(changename)) {

            long id = extras.getLong("id");

            RoutineHolder routineHolder = new RoutineHolder(id);
            routineHolder.setRoutineName(extras.getString(newRoutineName));
            routineHolder.storeRoutine();

            initRoutineList();

            return true;
        }

        return false;
    }

    @Override
    public boolean itemTouchOnMove(int oldPosition, int newPosition) {
        return false;
    }

    @Override
    public void itemTouchDropped(int oldPosition, int newPosition) {

    }

    @Override
    public void itemSwiped(int position, int direction) {

        if (direction == ItemTouchHelper.LEFT) {
            FlexTwoLineItem item = ((FlexTwoLineItem) routineAdapter.getAdapterItem(position));

            EventBus.getDefault().post(new RoutineClickedEvent(item.getIdentifier()));
        } else if (direction == ItemTouchHelper.RIGHT) {

            showDeleteDialog(((FlexTwoLineItem) routineAdapter.getAdapterItem(position)), position);
        }
    }

    /**
     * Shows a deletion dialogAdapter to delete the given {@link Routine}
     *
     * @param item
     * @param position
     */
    @NonNull
    private void showDeleteDialog(final FlexTwoLineItem item, final int position) {

        new ToastOXDialog.Build(getActivity())
                .setTitle(R.string.hint)
                .setContent(String.format(getString(R.string.delete_routine_short), item.firstLine))
                .setPositiveText(R.string.ok_option)
                .setIcon(R.drawable.ic_info_outline_black_18dp)
                .setPositiveBackgroundColor(Utils.getThemedColor(getActivity(), R.attr.myPrim))
                .setPositiveTextColorResource(R.color.white)
                .onPositive(toastOXDialog -> {
                    deleteWorkoutAndUpdate(((FlexTwoLineItem) routineAdapter.getAdapterItem(position)), position);
                    touchCallback.clearView(workoutList, item.getMyViewHolder());
                    toastOXDialog.dismiss();
                })
                .setNegativeText(R.string.cancel_option)
                .setNegativeBackgroundColor(Utils.getThemedColor(getActivity(), R.attr.myAccent))
                .setNegativeTextColorResource(R.color.white)
                .onNegative(toastOXDialog -> {

                    toastOXDialog.dismiss();
                })
                .show();
    }

    private void intro() {

        if (!Pref.getBoolean(FitParams.ShowedAddRoutine)) {

            Utils.getSpotlight(getActivity(),
                    floatingActionButton,
                    getString(R.string.spot_new_routine),
                    getString(R.string.spot_new_routine_desc))
                    .show();

            Pref.putBoolean(FitParams.ShowedAddRoutine, true);
        }
    }
}
