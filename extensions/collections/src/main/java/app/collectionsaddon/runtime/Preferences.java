package app.collectionsaddon.runtime;
import android.content.Context;
import android.content.SharedPreferences;

public final class Preferences {
    public static final String FILE = "collections_addon_v1";
    private static SharedPreferences store(Context c) { return c.getSharedPreferences(FILE, Context.MODE_PRIVATE); }
    public static boolean enabled(Context c) { return store(c).getBoolean("enabled", true); }
    public static int count(Context c) { return Policy.clampCount(store(c).getInt("count", Policy.DEFAULT_COUNT)); }
    public static void save(Context c, boolean enabled, int count) {
        store(c).edit().putBoolean("enabled", enabled).putInt("count", Policy.clampCount(count)).apply();
    }
    public static void reset(Context c) { store(c).edit().clear().apply(); }
    private Preferences() {}
}
