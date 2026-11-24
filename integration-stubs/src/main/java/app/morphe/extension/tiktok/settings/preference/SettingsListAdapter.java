package app.morphe.extension.tiktok.settings.preference;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.WrapperListAdapter;
import java.util.List;

/** Compile-only and test stand-in. The installed HushFeed bundle paints the rows. */
public final class SettingsListAdapter extends BaseAdapter implements WrapperListAdapter {
    private final ListAdapter delegate;

    public SettingsListAdapter(ListAdapter delegate, List<?> grouped) { this.delegate = delegate; }
    public void dispose() {}
    @Override public ListAdapter getWrappedAdapter() { return delegate; }
    @Override public int getCount() { return delegate.getCount(); }
    @Override public Object getItem(int position) { return delegate.getItem(position); }
    @Override public long getItemId(int position) { return delegate.getItemId(position); }
    @Override public int getItemViewType(int position) { return delegate.getItemViewType(position); }
    @Override public int getViewTypeCount() { return delegate.getViewTypeCount(); }
    @Override public boolean areAllItemsEnabled() { return delegate.areAllItemsEnabled(); }
    @Override public boolean isEnabled(int position) { return delegate.isEnabled(position); }
    @Override public View getView(int position, View reuse, ViewGroup parent) {
        return delegate.getView(position, reuse, parent);
    }
}
