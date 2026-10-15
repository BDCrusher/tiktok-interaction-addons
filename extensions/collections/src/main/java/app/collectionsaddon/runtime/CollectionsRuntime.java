package app.collectionsaddon.runtime;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.lang.ref.WeakReference;
import app.morphe.extension.shared.settings.HushfeedPause;

public final class CollectionsRuntime {
    interface Access {
        Context context(Object owner); List<?> items(Object response); long cursor(Object response);
        boolean more(Object response); String id(Object collection); Object copy(Object response, List<?> items);
        void replay(Object owner, Object response); Object load(Object owner, long cursor, NativeCallback success, NativeCallback error);
        void cancel(Object subscription); void clearPending();
    }
    static Access access = new Access() {
        public Context context(Object o) { return HostBridge.context(o); }
        public List<?> items(Object o) { return HostBridge.items(o); }
        public long cursor(Object o) { return HostBridge.cursor(o); }
        public boolean more(Object o) { return HostBridge.more(o); }
        public String id(Object o) { return HostBridge.id(o); }
        public Object copy(Object o, List<?> list) { return HostBridge.copy(o, list); }
        public void replay(Object o, Object r) { HostBridge.replay(o, r); }
        public Object load(Object o, long c, NativeCallback s, NativeCallback e) { return HostBridge.load(o, c, s, e); }
        public void cancel(Object o) { HostBridge.cancel(o); }
        public void clearPending() { HostBridge.clearPending(); }
    };
    private static final ThreadLocal<Boolean> STOCK = new ThreadLocal<>();
    private static final Map<Object, Session> SESSIONS = new IdentityHashMap<>();
    private static final List<PageState> MORE = new ArrayList<>();
    private static final class PageState {
        final WeakReference<List<?>> items; final boolean more;
        PageState(List<?> items, boolean more) { this.items = new WeakReference<>(items); this.more = more; }
    }
    private static void recordMore(List<?> items, boolean more) {
        java.util.Iterator<PageState> iterator = MORE.iterator();
        while (iterator.hasNext()) {
            List<?> existing = iterator.next().items.get();
            if (existing == null || existing == items) iterator.remove();
        }
        if (items == null) return;
        if (MORE.size() >= 32) MORE.remove(0);
        MORE.add(new PageState(items, more));
    }
    private static boolean hasMore(List<?> items) {
        for (PageState state : MORE) if (items != null && state.items.get() == items) return state.more;
        return false;
    }
    private static final Set<Dialog> FAILED = java.util.Collections.newSetFromMap(new WeakHashMap<>());
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    static final class Session {
        final Object owner, original;
        final Context context;
        final List<Object> items = new ArrayList<>();
        final Set<String> ids = new HashSet<>();
        final Set<Long> cursors = new HashSet<>();
        final int limit;
        final Runnable timeout;
        Object latest, subscription;
        boolean done;
        Session(Object owner, Object original, Context context) {
            this.owner = owner; this.original = original; this.latest = original; this.context = context;
            limit = Preferences.count(context);
            timeout = () -> finish(this, true);
        }
    }
    public static boolean enabled(Context context) {
        try { return !Boolean.TRUE.equals(STOCK.get()) && !HushfeedPause.isPaused() && Preferences.enabled(context); }
        catch (RuntimeException | LinkageError failure) { return false; }
    }
    public static int count(Dialog dialog) {
        return !FAILED.contains(dialog) && enabled(dialog.getContext()) ? Preferences.count(dialog.getContext()) : 3;
    }
    public static int threshold(Dialog dialog, List<?> items) {
        if (FAILED.contains(dialog) || !enabled(dialog.getContext())) return 4;
        return hasMore(items) ? 0 : count(dialog) + 1;
    }
    public static int layout(Object owner, int original) {
        try { return enabled(access.context(owner)) ? 1 : original; }
        catch (RuntimeException | LinkageError failure) { return original; }
    }
    /** true suspends TikTok's stock response consumer while extra pages are fetched. */
    public static boolean beforeResponse(Object owner, Object response) {
        try {
            Context context = access.context(owner);
            if (!enabled(context)) return false;
            List<?> items = access.items(response);
            recordMore(items, access.more(response));
            if (SESSIONS.containsKey(owner)) return false; // Deliberate replay.
            if (!access.more(response) || (items != null && items.size() >= Preferences.count(context))) return false;
            if (access.cursor(response) <= 0) return false;
            Session session = new Session(owner, response, context);
            SESSIONS.put(owner, session);
            append(session, response);
            MAIN.postDelayed(session.timeout, 5000);
            try { request(session); }
            catch (RuntimeException | LinkageError failure) {
                finish(session, true);
                return true; // The original consumer was replayed once in stock mode.
            }
            return true;
        } catch (RuntimeException | LinkageError failure) { return false; }
    }
    private static void append(Session session, Object response) {
        List<?> page = access.items(response);
        if (page != null) for (Object item : page) {
            String id = access.id(item);
            // Keep null-ID special collections; regular IDs are deduplicated in host order.
            if (id == null || id.isEmpty() || session.ids.add(id)) session.items.add(item);
        }
        session.latest = response;
    }
    private static void request(Session session) {
        long cursor = access.cursor(session.latest);
        if (session.done) return;
        if (!access.more(session.latest) || session.items.size() >= session.limit) { finish(session, false); return; }
        if (cursor <= 0 || !session.cursors.add(cursor) || session.cursors.size() > 5) { finish(session, true); return; }
        session.subscription = access.load(session.owner, cursor,
            new NativeCallback(session, false), new NativeCallback(session, true));
    }
    public static void page(Session session, Object response, boolean error) {
        if (Looper.myLooper() != Looper.getMainLooper()) { MAIN.post(() -> page(session, response, error)); return; }
        if (session.done) return;
        try {
            if (error || !enabled(session.context)) { finish(session, true); return; }
            append(session, response);
            request(session);
        } catch (RuntimeException | LinkageError failure) { finish(session, true); }
    }
    private static void finish(Session session, boolean failed) {
        if (session.done) return;
        session.done = true;
        MAIN.removeCallbacks(session.timeout);
        try {
            if (session.subscription != null) access.cancel(session.subscription);
            Context current = session.context;
            for (int i = 0; i < 8 && current instanceof android.content.ContextWrapper && !(current instanceof Activity); i++) {
                Context next = ((android.content.ContextWrapper) current).getBaseContext(); if (next == current) break; current = next;
            }
            if (current instanceof Activity && (((Activity) current).isFinishing() || ((Activity) current).isDestroyed())) {
                access.clearPending(); return;
            }
            // Always release TikTok's pending quick-favorite manager through its own consumer.
            Object response = failed ? session.original : access.copy(session.latest, session.items);
            recordMore(access.items(response), failed || access.more(response));
            if (failed) STOCK.set(true);
            try { access.replay(session.owner, response); }
            finally { if (failed) STOCK.remove(); }
        } catch (RuntimeException | LinkageError failure) {
            try {
                STOCK.set(true);
                access.replay(session.owner, session.original);
            } catch (RuntimeException | LinkageError ignored) { /* No second mutation or API call. */ }
            finally { STOCK.remove(); }
        } finally { SESSIONS.remove(session.owner); }
    }
    public static void install(Dialog dialog, android.view.View root) {
        if (FAILED.contains(dialog) || !enabled(dialog.getContext())) { dialog.setContentView(root); return; }
        try { Panels.install(dialog, root); }
        catch (RuntimeException | LinkageError failure) {
            Panels.release(dialog);
            FAILED.add(dialog);
            try { STOCK.set(true); HostBridge.rerender(dialog); }
            catch (RuntimeException | LinkageError stockFailure) { access.clearPending(); }
            finally { STOCK.remove(); }
        }
    }
    public static boolean touch(Dialog dialog, android.view.MotionEvent event) {
        try { return enabled(dialog.getContext()) && Panels.touch(dialog, event); }
        catch (RuntimeException failure) { return false; }
    }
    public static void release(Dialog dialog) { Panels.release(dialog); }
    public static boolean pressed(Dialog dialog, android.view.View view, boolean pressed) {
        if (FAILED.contains(dialog) || !enabled(dialog.getContext())) return false;
        try {
            Panels.pressed(dialog.getContext(), HostBridge.highlight(view), pressed);
            return true;
        } catch (RuntimeException | LinkageError failure) { return false; }
    }
    public static boolean hit(int x, int y, android.view.View view) {
        android.graphics.Rect rect = new android.graphics.Rect();
        if (enabled(view.getContext())) {
            if (!view.getGlobalVisibleRect(rect)) return false;
            // getGlobalVisibleRect is in root/window coordinates; native gestures use raw screen coordinates.
            int[] screen = new int[2], window = new int[2];
            view.getLocationOnScreen(screen); view.getLocationInWindow(window);
            rect.offset(screen[0] - window[0], screen[1] - window[1]);
            return rect.contains(x, y);
        }
        int[] location = new int[2]; view.getLocationOnScreen(location);
        rect.set(location[0], location[1], location[0] + view.getWidth(), location[1] + view.getHeight());
        return rect.contains(x, y);
    }
    private CollectionsRuntime() {}
}
