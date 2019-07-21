package de.parkitny.fit.myfit.app.ui.exercise;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;

import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.parkitny.fit.myfit.app.R;

/**
 * Created by Sebastian on 21.10.2017.
 */

public class EmphasisItem extends AbstractItem<EmphasisItem, EmphasisItem.ViewHolder> {

    private String emphasisText;

    private boolean checkboxSelected = false;

    private ViewHolder current;

    public EmphasisItem(String emphasisText) {
        this.emphasisText = emphasisText;
    }

    @Override
    public ViewHolder getViewHolder(View view) {
        return new ViewHolder(view);
    }

    @Override
    public int getType() {
        return R.id.emphasis_item;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.emphasis_item;
    }

    @Override
    public void bindView(ViewHolder holder, List<Object> payloads) {
        super.bindView(holder, payloads);

        current = holder;

        holder.checkBox.setText(emphasisText);
        holder.checkBox.setChecked(checkboxSelected);
    }

    @Override
    public void unbindView(ViewHolder holder) {
        super.unbindView(holder);

        holder.checkBox.setText(null);
    }

    public ViewHolder getCurrent() {
        return current;
    }

    public boolean isCheckboxSelected() {

        if (current == null) {
            return this.checkboxSelected;
        } else {
            return current.checkBox.isChecked();
        }
    }

    public void setCheckboxSelected(boolean checkboxSelected) {
        this.checkboxSelected = checkboxSelected;

        if (current != null) {
            current.checkBox.setChecked(checkboxSelected);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.emphasis_checkBox)
        public CheckBox checkBox;

        public ViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }
}
