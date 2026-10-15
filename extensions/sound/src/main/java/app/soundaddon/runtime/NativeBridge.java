package app.soundaddon.runtime;

import android.view.View;
import java.util.List;
import java.util.concurrent.Future;
import android.app.Activity;

/** Exact APK-specific bindings are supplied and checked by the patch, not reflection. */
public final class NativeBridge {
    public static View disc(Object owner) { throw new UnsupportedOperationException(); }
    public static View alternateDisc(Object owner) { throw new UnsupportedOperationException(); }
    public static Object boundParams(Object owner) { throw new UnsupportedOperationException(); }
    public static Activity titleActivity(Object owner) { throw new UnsupportedOperationException(); }
    public static Future<?> queryMusic(String id) { throw new UnsupportedOperationException(); }
    public static Object detailMusic(Object detail) { throw new UnsupportedOperationException(); }
    public static Object detailMatchedMusic(Object detail) { throw new UnsupportedOperationException(); }
    public static String matchedId(Object music) { throw new UnsupportedOperationException(); }
    public static View favoritesRoot(Object popup) { throw new UnsupportedOperationException(); }
    public static String track(Object params) { throw new UnsupportedOperationException(); }
    public static Object music(Object params) { throw new UnsupportedOperationException(); }
    public static String musicId(Object music) { throw new UnsupportedOperationException(); }
    public static List<?> destinations(Object music) { throw new UnsupportedOperationException(); }
    public static Integer provider(Object info) { throw new UnsupportedOperationException(); }
    public static Object platform(Integer code) { throw new UnsupportedOperationException(); }
    public static String label(Object platform) { throw new UnsupportedOperationException(); }
    public static void add(Object titleOwner, Object params, Object music, Object platform, List<?> infos) { throw new UnsupportedOperationException(); }
    public static void collect(Object music) { throw new UnsupportedOperationException(); }
    private NativeBridge() {}
}
