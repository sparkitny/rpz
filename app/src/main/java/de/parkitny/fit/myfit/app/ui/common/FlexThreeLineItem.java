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

public class FlexThreeLineItem extends AbstractItem<FlexThreeLineItem, FlexThreeLineItem.ViewHolder> implements IClickable<FlexThreeLineItem>, ViewHolderGetterInterface {

    public long id;
    public String firstLine;
    public String secondLine;
    public String thirdLine;
    public Drawable icon;
    public Drawable rightIcon;

    protected ViewHolder myViewHolder;

    @NonNull
    @Override
    public ViewHolder getViewHolder(View view) {
        return new ViewHolder(view);
    }

    @Override
    public int getType() {
        return R.id.flex_three_line_item;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.flex_three_line_item;
    }

    @Override
    public FlexTwoLineItem.ViewHolder getMyViewHolder() {
        return null;
    }


    public static class ViewHolder extends FastAdapter.ViewHolder<FlexThreeLineItem> {

        @BindView(R.id.exercise_item_text)
        public TextView firstLine;

        @BindView(R.id.exercise_item_description)
        public TextView secondLine;

        @BindView(R.id.exercise_item_third_line)
        public TextView thirdLine;

        @BindView(R.id.exercise_item_icon)
        public ImageView exerciseIcon;

        @BindView(R.id.right_icon)
        public ImageView exerciseRightIcon;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bindView(@NonNull FlexThreeLineItem item, @NonNull List<Object> payloads) {
            item.myViewHolder = this;
            firstLine.setText(item.firstLine);
            secondLine.setText(item.secondLine);
            thirdLine.setText(item.thirdLine);
            exerciseIcon.setImageDrawable(item.icon);
            exerciseRightIcon.setImageDrawable(item.rightIcon);

            if (item.isSelected()) {
                firstLine.setTextColor(Utils.getThemedColor(itemView.getContext(), R.attr.myAccent));
                secondLine.setTextColor(Utils.getThemedColor(itemView.getContext(), R.attr.myAccent));
                thirdLine.setTextColor(Utils.getThemedColor(itemView.getContext(), R.attr.myAccent));
            } else {

                int themedColor = Utils.getThemedColor(itemView.getContext(), android.R.attr.textColorSecondary);

                firstLine.setTextColor(themedColor);
                secondLine.setTextColor(themedColor);
                thirdLine.setTextColor(themedColor);
            }
        }

        @Override
        public void unbindView(@NonNull FlexThreeLineItem item) {
            item.myViewHolder = null;
            firstLine.setText(null);
            secondLine.setText(null);
            thirdLine.setText(null);
            exerciseIcon.setImageDrawable(null);
            exerciseRightIcon.setImageDrawable(null);
        }


    }
}
