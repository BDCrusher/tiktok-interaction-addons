package app.collectionsaddon.runtime;

import android.app.Fragment;
import android.content.Context;
import android.preference.PreferenceScreen;
import app.bdcrusher.runtime.SettingsHub;

@SuppressWarnings("deprecation")
public final class SettingsEntry implements SettingsHub.Feature {
    static final String KEY = SettingsHub.KEY;
    public SettingsEntry() {}
    public static void install(Fragment host, Context context, PreferenceScreen screen) {
        SettingsHub.install(host,context,screen);
    }
    static int changedCount(Context context) {
        return (Preferences.enabled(context) ? 0 : 1) + (Preferences.count(context) == 5 ? 0 : 1);
    }
    public static void refresh(Fragment host) {
        SettingsHub.refresh(host);
    }
    @Override public Fragment page() { return new CollectionSettingsFragment(); }
    @Override public int changes(Context context) { return changedCount(context); }
    @Override public String summary() { return "Favorites long-press menu and collection count"; }
}
