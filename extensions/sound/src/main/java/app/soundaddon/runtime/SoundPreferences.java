package app.soundaddon.runtime;

import android.content.Context;

public final class SoundPreferences {
    static final String FILE = "sound_actions_addon_v1";
    public static boolean enabled(Context c) { return c.getSharedPreferences(FILE, 0).getBoolean("enabled", true); }
    public static void enabled(Context c, boolean value) { c.getSharedPreferences(FILE, 0).edit().putBoolean("enabled", value).apply(); }
    public static void reset(Context c) { c.getSharedPreferences(FILE, 0).edit().clear().apply(); }
    private SoundPreferences() {}
}
