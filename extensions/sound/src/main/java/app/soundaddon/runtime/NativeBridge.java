package app.soundaddon.runtime;

import android.app.Activity;
import android.view.View;

/** Exact APK-specific bindings are supplied and checked by the patch, not reflection. */
public final class NativeBridge {
    public static View disc(Object owner) { throw new UnsupportedOperationException(); }
    public static View alternateDisc(Object owner) { throw new UnsupportedOperationException(); }
    public static Object boundParams(Object owner) { throw new UnsupportedOperationException(); }
    public static View favoritesRoot(Object popup) { throw new UnsupportedOperationException(); }
    public static String track(Object params) { throw new UnsupportedOperationException(); }
    public static Object music(Object params) { throw new UnsupportedOperationException(); }
    public static String musicId(Object music) { throw new UnsupportedOperationException(); }
    public static long holdDelayMillis() { throw new UnsupportedOperationException(); }
    public static void collect(Activity activity, Object music) { throw new UnsupportedOperationException(); }
    private NativeBridge() {}
}
