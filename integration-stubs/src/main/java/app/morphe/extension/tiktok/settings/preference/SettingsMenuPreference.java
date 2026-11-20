package app.morphe.extension.tiktok.settings.preference;
import android.content.Context;
import android.preference.PreferenceGroup;
/** Compile-only signature and test stand-in; NEVER shipped in the payload. */
@SuppressWarnings("deprecation")
public class SettingsMenuPreference extends PreferenceGroup {
    public enum Icon { LAYOUT }
    private boolean expandable;
    private boolean expanded;
    public SettingsMenuPreference(Context context, String title, String summary, Icon icon,
                                 OnPreferenceClickListener listener) {
        super(context, null); setTitle(title); setSummary(summary); setOnPreferenceClickListener(listener);
    }
    public void setCompact(boolean compact) {}
    public void setExpandable(boolean value) {
        expandable = value;
        if (!value) expanded = false;
        notifyHierarchyChanged();
    }
    public boolean isExpandable() { return expandable; }
    public boolean isExpanded() { return expanded; }
    public void setExpanded(boolean value) {
        if (expandable) { expanded = value; notifyHierarchyChanged(); }
    }
    @Override protected boolean isOnSameScreenAsChildren() { return !expandable || expanded; }
}
