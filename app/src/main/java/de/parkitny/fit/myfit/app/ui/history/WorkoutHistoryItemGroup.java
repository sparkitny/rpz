package de.parkitny.fit.myfit.app.ui.history;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.parkitny.fit.myfit.app.R;
import de.parkitny.fit.myfit.app.entities.WorkoutItem;
import de.parkitny.fit.myfit.app.ui.common.Utils;

/**
 * Created by Sebastian on 15.01.2018.
 */

public class WorkoutHistoryItemGroup extends AbstractItem<WorkoutHistoryItemGroup, WorkoutHistoryItemGroup.ViewHolder> {

    public String groupName;
    public int round;
    public ArrayList<WorkoutItem> items = new ArrayList<>();
    public Context context;

    @Override
    public ViewHolder getViewHolder(View view) {
        return new ViewHolder(view);
    }

    @Override
    public int getType() {
        return R.id.workout_history_item_group;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.workout_history_item_group;
    }

    @Override
    public void bindView(ViewHolder holder, List<Object> payloads) {
        super.bindView(holder, payloads);

        holder.groupName.setText(groupName);

        holder.adapter.clear();
        holder.itemList.setAdapter(holder.adapter);
        holder.itemList.setLayoutManager(new LinearLayoutManager(context));
        holder.itemList.setNestedScrollingEnabled(false);

        int i = 1;

        for (WorkoutItem item : items) {

            round = item.round;

            MiniHistoryItem miniHistoryItem = new MiniHistoryItem();
            miniHistoryItem.name = item.exerciseNameAndQuantity;
            miniHistoryItem.setName = String.format("%s. %s", i++, context.getString(R.string.set));
            miniHistoryItem.duration = Utils.formatPeriod(item.duration);
            holder.adapter.add(miniHistoryItem);
        }

        holder.round.setText(String.format("%s %s",
                context.getString(R.string.round),
                round));
    }

    @Override
    public void unbindView(ViewHolder holder) {
        super.unbindView(holder);

        holder.groupName.setText(null);
        holder.adapter.clear();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.group_name)
        public TextView groupName;

        @BindView(R.id.workout_history_group_items)
        public RecyclerView itemList;

        @BindView(R.id.round)
        public TextView round;

        public FastItemAdapter<MiniHistoryItem> adapter = new FastItemAdapter<>();

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
