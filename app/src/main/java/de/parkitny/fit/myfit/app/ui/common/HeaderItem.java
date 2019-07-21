package de.parkitny.fit.myfit.app.ui.common;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.parkitny.fit.myfit.app.R;

public class HeaderItem extends AbstractItem<HeaderItem, HeaderItem.ViewHolder> implements ViewHolderGetterInterface {

    private String header;

    protected ViewHolder myViewHolder;

    public HeaderItem withHeader(String header) {
        this.header = header;
        return this;
    }

    @Override
    public int getType() {
        return R.id.header_item;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.header_item;
    }

    @NonNull
    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    @Override
    public ViewHolder getMyViewHolder() {
        return myViewHolder;
    }

    protected static class ViewHolder extends FastAdapter.ViewHolder<HeaderItem> {

        protected View view;
        @BindView(R.id.material_drawer_name)
        TextView name;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            this.view = view;
        }

        @Override
        public void bindView(HeaderItem item, List<Object> payloads) {
            item.myViewHolder = this;
            name.setText(item.header);
        }

        @Override
        public void unbindView(HeaderItem item) {
            item.myViewHolder = null;
            name.setText(null);
        }
    }
}