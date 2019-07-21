package de.parkitny.fit.myfit.app.ui.routine;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.fastadapter_extensions.drag.IDraggable;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.parkitny.fit.myfit.app.R;
import de.parkitny.fit.myfit.app.utils.ExerciseConfigurationView;
import mehdi.sakout.fancybuttons.FancyButton;
import timber.log.Timber;

/**
 * Created by Sebastian on 03.07.2017.
 */
public class ExerciseItem extends AbstractItem<ExerciseItem, ExerciseItem.ViewHolder> implements IDraggable<ExerciseItem, IItem> {

    private ViewHolder current;

    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    @Override
    public int getType() {
        return R.id.exercise_item;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.exercise_item_viewholder;
    }

    @Override
    public void bindView(ViewHolder holder, List<Object> payloads) {
        super.bindView(holder, payloads);

        current = holder;

        ExerciseConfigurationView exerciseConfigurationView = (ExerciseConfigurationView) getTag();

        Timber.d("Setting %s", exerciseConfigurationView);

        assert exerciseConfigurationView != null;

        holder.exerciseName.setText(exerciseConfigurationView.getReadableString());
        holder.numberOfSets.setText(exerciseConfigurationView.getSets() + "");
    }

    @Override
    public void unbindView(ViewHolder holder) {
        super.unbindView(holder);

        current = null;
    }

    public ViewHolder getCurrentViewHolder() {

        return current;
    }

    @Override
    public boolean isDraggable() {
        return true;
    }

    @Override
    public ExerciseItem withIsDraggable(boolean draggable) {
        return this;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.exercise_name)
        public TextView exerciseName;

//        @BindView(R.id.delete_button)
//        public ImageView deleteButton;
//
//        @BindView(R.id.copy_button)
//        public ImageView copyButton;
//
//        @BindView(R.id.copy_end_button)
//        public ImageView copyEndButton;

        @BindView(R.id.increment_button)
        public FancyButton incrementButton;

        @BindView(R.id.decrement_button)
        public FancyButton decrementButton;

        @BindView(R.id.number_sets)
        public TextView numberOfSets;

        @BindView(R.id.move_button)
        public ImageView moveButton;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
