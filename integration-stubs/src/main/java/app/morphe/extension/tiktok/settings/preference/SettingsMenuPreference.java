package app.morphe.extension.tiktok.settings.preference;
import android.content.Context;
import android.preference.Preference;
/** Compile-only signature and test stand-in; NEVER shipped in the payload. */
@SuppressWarnings("deprecation")
public class SettingsMenuPreference extends Preference {
    public enum Icon { LAYOUT }
    public SettingsMenuPreference(Context context, String title, String summary, Icon icon,
                                 OnPreferenceClickListener listener) {
        super(context); setTitle(title); setSummary(summary); setOnPreferenceClickListener(listener);
    }
    public void setCompact(boolean compact) {}
}
