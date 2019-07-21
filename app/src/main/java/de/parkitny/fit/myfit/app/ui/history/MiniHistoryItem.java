package de.parkitny.fit.myfit.app.ui.history;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.parkitny.fit.myfit.app.R;

/**
 * Created by Sebastian on 15.01.2018.
 */
public class MiniHistoryItem extends AbstractItem<MiniHistoryItem, MiniHistoryItem.ViewHolder> {

    public String name;
    public String setName;
    public String duration;

    @Override
    public ViewHolder getViewHolder(View view) {
        return new ViewHolder(view);
    }

    @Override
    public int getType() {
        return R.id.mini_history_item;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.mini_item_model;
    }

    @Override
    public void bindView(ViewHolder holder, List<Object> payloads) {
        super.bindView(holder, payloads);

        holder.name.setText(name);
        holder.setHint.setText(setName);
        holder.duration.setText(duration);
    }

    @Override
    public void unbindView(ViewHolder holder) {
        super.unbindView(holder);

        holder.name.setText(null);
        holder.setHint.setText(null);
        holder.duration.setText(null);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.mini_item_exercise_name)
        public TextView name;

        @BindView(R.id.mini_item_set_hint)
        public TextView setHint;

        @BindView(R.id.mini_item_exercise_duration)
        public TextView duration;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
