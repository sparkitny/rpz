package de.parkitny.fit.myfit.app.ui.common;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IClickable;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.parkitny.fit.myfit.app.R;

public class FlexTwoLineItem extends AbstractItem<FlexTwoLineItem, FlexTwoLineItem.ViewHolder> implements IClickable<FlexTwoLineItem>, ViewHolderGetterInterface {

    public long id;
    public String firstLine;
    public String description;
    public Drawable icon;
    public Drawable rightIcon;

    protected ViewHolder myViewHolder;

    @Override
    public ViewHolder getMyViewHolder() {
        return myViewHolder;
    }

    @NonNull
    @Override
    public ViewHolder getViewHolder(View view) {
        return new ViewHolder(view);
    }

    @Override
    public int getType() {
        return R.id.flex_two_line_item;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.flex_two_line_item;
    }

    public static class ViewHolder extends FastAdapter.ViewHolder<FlexTwoLineItem> {

        @BindView(R.id.exercise_item_text)
        public TextView firstLine;

        @BindView(R.id.exercise_item_description)
        public TextView secondLine;

        @BindView(R.id.exercise_item_icon)
        public ImageView exerciseIcon;

        @BindView(R.id.right_icon)
        public ImageView exerciseRightIcon;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bindView(@NonNull FlexTwoLineItem item, @NonNull List<Object> payloads) {

            item.myViewHolder = this;

            secondLine.setText(item.description);
            exerciseIcon.setImageDrawable(item.icon);
            firstLine.setText(item.firstLine);
            exerciseRightIcon.setImageDrawable(item.rightIcon);

            if (item.isSelected()) {
                firstLine.setTextColor(Utils.getThemedColor(itemView.getContext(), R.attr.myAccent));
                secondLine.setTextColor(Utils.getThemedColor(itemView.getContext(), R.attr.myAccent));
            } else {

                int themedColor = Utils.getThemedColor(itemView.getContext(), android.R.attr.textColorSecondary);

                firstLine.setTextColor(themedColor);
                secondLine.setTextColor(themedColor);
            }
        }

        @Override
        public void unbindView(@NonNull FlexTwoLineItem item) {

            item.myViewHolder = null;

            secondLine.setText(null);
            exerciseIcon.setImageDrawable(null);
            firstLine.setText(null);
            exerciseRightIcon.setImageDrawable(null);
        }


    }
}
