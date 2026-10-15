package app.collectionsaddon.runtime;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import java.util.List;

/** Bodies are bound to validated APK references by the patch, never reflective private APIs. */
public final class HostBridge {
    public static Context context(Object handler) { throw new IllegalStateException("Unbound host bridge"); }
    public static List<?> items(Object response) { throw new IllegalStateException("Unbound host bridge"); }
    public static long cursor(Object response) { throw new IllegalStateException("Unbound host bridge"); }
    public static boolean more(Object response) { throw new IllegalStateException("Unbound host bridge"); }
    public static String id(Object collection) { throw new IllegalStateException("Unbound host bridge"); }
    public static Object copy(Object response, List<?> items) { throw new IllegalStateException("Unbound host bridge"); }
    public static void replay(Object handler, Object response) { throw new IllegalStateException("Unbound host bridge"); }
    public static Object load(Object handler, long cursor, NativeCallback success, NativeCallback error) { throw new IllegalStateException("Unbound host bridge"); }
    public static void cancel(Object subscription) { throw new IllegalStateException("Unbound host bridge"); }
    public static void rerender(Dialog dialog) { throw new IllegalStateException("Unbound host bridge"); }
    public static void tint(View view, int color) { throw new IllegalStateException("Unbound host bridge"); }
    public static View highlight(View view) { throw new IllegalStateException("Unbound host bridge"); }
    public static void clearPending() { throw new IllegalStateException("Unbound host bridge"); }
    private HostBridge() {}
}
