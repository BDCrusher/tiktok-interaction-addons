package app.soundaddon.runtime;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import app.morphe.extension.shared.settings.HushfeedPause;
import app.morphe.extension.tiktok.settings.preference.SettingsUi;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import app.bdcrusher.runtime.PopupStyle;

/** UI-thread-only registry: no global current song, no replacement click/touch listeners. */
public final class SoundRuntime {
    static final Map<View, Binding> DISCS = new WeakHashMap<>();
    private static final Map<Object, List<Binding>> OWNERS = new WeakHashMap<>();
    private static final Map<Activity, DiscPress> WINDOW_PRESSES = new WeakHashMap<>();
    private static int directDispatchDepth;
    private static long windowEvents;
    private static long capturedPresses, menuOpens;
    private static String lastStage = "No disc press yet";
    private static final Map<View, Long> EDGE_PRESSES = new WeakHashMap<>();
    interface Identity {
        String track(Object data);
        boolean hasSound(Object data);
    }
    private static final Identity NATIVE_IDENTITY = new Identity() {
        public String track(Object data) { return NativeBridge.track(data); }
        public boolean hasSound(Object data) {
            Object music=NativeBridge.music(data);
            String id=music==null ? null : NativeBridge.musicId(music);
            return id!=null && !id.isEmpty();
        }
    };

    /** Before the Activity delegates to feed parents. No coordinate guess or global gesture replacement. */
    public static boolean dispatchDiscTouch(Activity activity, MotionEvent event) {
        if (activity == null || event == null || directDispatchDepth != 0) return false;
        windowEvents++;
        int action = event.getActionMasked();
        DiscPress press = WINDOW_PRESSES.get(activity);
        if (action == MotionEvent.ACTION_DOWN) {
            if (press != null) press.cancel(event);
            WINDOW_PRESSES.remove(activity);
            if (activity.isFinishing() || activity.getWindow() == null) return false;
            Binding binding;
            try { binding = discAt(activity.getWindow().getDecorView(), event.getRawX(), event.getRawY()); }
            catch (RuntimeException | LinkageError unavailable) { lastStage = "Disc hit test failed; stock route"; return false; }
            if (binding == null) return false;
            press = new DiscPress(binding, event);
            WINDOW_PRESSES.put(activity, press);
            capturedPresses++;
            lastStage = "Disc touch reserved before feed dispatch";
        }
        if (press == null) return false;
        if (press.downTime != event.getDownTime()) {
            press.cancel(event); WINDOW_PRESSES.remove(activity); return false;
        }
        try { press.route(activity,event); }
        catch (RuntimeException | LinkageError unavailable) {
            press.cancel(event); lastStage = "Reserved press cancelled after UI failure";
        } finally {
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                press.release(); WINDOW_PRESSES.remove(activity);
            }
        }
        // Even a cancelled sequence is drained: parents never saw its DOWN.
        return true;
    }

    static void dispatchNative(View disc, MotionEvent event, boolean cancel) {
        if (disc == null) return;
        MotionEvent local = MotionEvent.obtain(event);
        int[] position = new int[2]; disc.getLocationOnScreen(position);
        local.offsetLocation(event.getRawX() - event.getX() - position[0], event.getRawY() - event.getY() - position[1]);
        if (cancel) local.setAction(MotionEvent.ACTION_CANCEL);
        directDispatchDepth++;
        try { disc.dispatchTouchEvent(local); }
        finally { directDispatchDepth--; local.recycle(); }
    }

    private static final class DiscPress {
        final Binding binding;
        final long downTime;
        MotionEvent originalDown;
        boolean cancelled;
        DiscPress(Binding binding, MotionEvent event) {
            this.binding = binding; this.downTime = event.getDownTime(); originalDown=MotionEvent.obtain(event);
        }
        void route(Activity activity,MotionEvent event) {
            if (cancelled) return;
            int action = event.getActionMasked();
            if (!binding.usable() && action != MotionEvent.ACTION_DOWN
                    || action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_POINTER_DOWN) {
                cancel(event); return;
            }
            boolean wasOpen = binding.opened;
            boolean shortTap = action==MotionEvent.ACTION_UP && binding.down && !wasOpen
                && event.getEventTime()-downTime<binding.holdDelayMillis;
            boolean consumed = binding.touch(event);
            if(shortTap && !consumed) {
                dispatchNative(binding.anchor.get(),event,true);
                // Re-enter the original window route, not an assumed clickable child/placeholder.
                directDispatchDepth++;
                try { activity.dispatchTouchEvent(originalDown); activity.dispatchTouchEvent(event); }
                finally { directDispatchDepth--; }
                lastStage="Short tap forwarded through native window dispatch";
                return;
            }
            if(action==MotionEvent.ACTION_UP) { dispatchNative(binding.anchor.get(),event,true); return; }
            if (!wasOpen && !consumed && !binding.opened) {
                View disc = binding.anchor.get();
                dispatchNative(disc, event, false);
                if (disc != null && action == MotionEvent.ACTION_DOWN) disc.cancelLongPress();
            }
        }
        void cancel(MotionEvent event) {
            cancelled = true; binding.cancel();
            try { dispatchNative(binding.anchor.get(), event, true); }
            catch (RuntimeException | LinkageError ignored) { /* Do not crash while recovering a failed native view. */ }
            release();
        }
        void release() { if(originalDown!=null) originalDown.recycle(); originalDown=null; }
    }

    /** Session-only counters/state, never song IDs, user data or credentials. */
    public static String diagnosticStatus() {
        int eligible = 0;
        for (Binding binding : DISCS.values()) {
            try { if (binding.readyForHold()) eligible++; }
            catch (RuntimeException | LinkageError ignored) {}
        }
        return "Window touch events: " + windowEvents + "\nDisc bindings: " + DISCS.size()
            + " · visible/eligible: " + eligible + "\nCaptured presses: " + capturedPresses
            + " · menus opened: " + menuOpens + "\n" + lastStage;
    }

    /** Runs before the native edge listener queues its 300 ms speed-up timer. */
    public static boolean skipEdgeTouch(View root, MotionEvent event) {
        if(root==null || event==null || directDispatchDepth!=0) return false;
        int action=event.getActionMasked();
        if(action==MotionEvent.ACTION_DOWN) {
            EDGE_PRESSES.remove(root);
            try {
                if(discAt(root,event.getRawX(),event.getRawY()) == null) return false;
                EDGE_PRESSES.put(root,event.getDownTime());
                return true;
            } catch(RuntimeException | LinkageError unavailable) { return false; }
        }
        Long start=EDGE_PRESSES.get(root);
        if(start==null || start.longValue()!=event.getDownTime()) {
            EDGE_PRESSES.remove(root);
            return false;
        }
        // Swallow the tail too: native UP must not act on stale coordinates from an earlier DOWN.
        if(action==MotionEvent.ACTION_UP || action==MotionEvent.ACTION_CANCEL) EDGE_PRESSES.remove(root);
        return true;
    }
    private static Binding discAt(View root,float x,float y) {
        Rect visible=new Rect(); int[] origin=new int[2];
        for(Map.Entry<View,Binding> entry:DISCS.entrySet()) {
            View disc=entry.getKey(); Binding binding=entry.getValue();
            if(disc.getRootView()!=root.getRootView() || !binding.readyForHold()
                || !disc.getGlobalVisibleRect(visible)) continue;
            // Global visible rectangles use root coordinates; MotionEvent raw values use screen coordinates.
            disc.getRootView().getLocationOnScreen(origin);
            visible.offset(origin[0],origin[1]);
            if(SoundPolicy.inside(x,y,visible.left,visible.top,visible.right,visible.bottom)) return binding;
        }
        return null;
    }
    /** Covers bind-before-inflation as well as view recreation after a bind. */
    public static void viewsCreated(Object owner) {
        try {
            Object data = NativeBridge.boundParams(owner);
            if (data != null) bindDisc(owner, data);
        } catch (RuntimeException | LinkageError ignored) { lastStage = "View registration unavailable; stock route"; }
    }
    public static void bindDisc(Object owner, Object params) {
        unbindDisc(owner);
        try {
            View primary = NativeBridge.disc(owner), alternate = NativeBridge.alternateDisc(owner);
            List<Binding> views = new ArrayList<>();
            for (View view : new View[] { primary, alternate }) {
                if (view == null || view == alternate && view == primary && !views.isEmpty()) continue;
                Binding old = DISCS.remove(view);
                if (old != null) old.dispose();
                Binding next = new Binding(view, params); DISCS.put(view, next); views.add(next);
            }
            OWNERS.put(owner, views);
            lastStage = views.isEmpty() ? "Disc binding has no views" : "Disc views bound";
        } catch (RuntimeException | LinkageError ignored) { lastStage = "Disc registration failed; stock route"; }
    }
    public static void unbindDisc(Object owner) {
        List<Binding> views = OWNERS.remove(owner);
        if (views != null) for (Binding binding : views) {
            View view = binding.anchor.get();
            if (view != null && DISCS.get(view) == binding) DISCS.remove(view);
            binding.dispose();
        }
    }
    /** Called after TikTok's own press feedback. False leaves stock tap handling intact. */
    public static boolean touch(View view, MotionEvent event) {
        if (directDispatchDepth != 0) return false; // Window router already updated this binding.
        Binding binding = DISCS.get(view);
        if (binding == null) return false;
        try { return binding.touch(event); }
        catch (RuntimeException | LinkageError ignored) {
            boolean consumed = binding.opened;
            binding.cancel(); return consumed;
        }
    }
    public static void observeFavorites(Object popup) {
        try {
            View root=NativeBridge.favoritesRoot(popup); if(root==null) return;
            int width=root.getWidth();
            if(popup instanceof android.app.Dialog) {
                android.view.Window window=((android.app.Dialog)popup).getWindow();
                if(window!=null && window.getAttributes().width>0) width=window.getAttributes().width;
            }
            if(width<=0) {
                root.measure(View.MeasureSpec.makeMeasureSpec(root.getResources().getDisplayMetrics().widthPixels,View.MeasureSpec.AT_MOST),
                    View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED)); width=root.getMeasuredWidth();
            }
            PopupStyle.rememberWidth(root.getContext(),width);
        } catch(RuntimeException | LinkageError ignored) {}
    }
    static final class Binding implements View.OnAttachStateChangeListener {
        final WeakReference<View> anchor;
        final WeakReference<Object> params;
        final Identity identity;
        final List<View> rows = new ArrayList<>();
        final List<Runnable> actions = new ArrayList<>();
        final Runnable hold = this::show;
        PopupWindow popup;
        long holdDelayMillis;
        int background;
        String startTrack;
        float startX, startY, rawX, rawY;
        boolean down, opened, moved;
        int selected = -1;
        Binding(View view, Object params) {
            this(view,params,NATIVE_IDENTITY);
        }
        Binding(View view,Object params,Identity identity) {
            anchor = new WeakReference<>(view); this.params = new WeakReference<>(params);
            this.identity=identity;
            view.addOnAttachStateChangeListener(this);
        }
        @Override public void onViewAttachedToWindow(View view) {}
        @Override public void onViewDetachedFromWindow(View view) { cancel(); }
        void dispose() {
            cancel(); View view = anchor.get(); if(view != null) view.removeOnAttachStateChangeListener(this);
        }
        boolean readyForHold() {
            View view = anchor.get(); Object data = params.get();
            return view != null && data != null && visible(view) && view.isEnabled() && view.isAttachedToWindow()
                && SoundPreferences.enabled(view.getContext()) && !HushfeedPause.isPaused()
                && identity.track(data)!=null && !identity.track(data).isEmpty() && identity.hasSound(data);
        }
        private boolean visible(View view) {
            if(!view.isShown()) return false;
            View next=view;
            while(next!=null) {
                if(next.getAlpha()<=0) return false;
                next=next.getParent() instanceof View ? (View)next.getParent() : null;
            }
            return true;
        }
        boolean usable() {
            Object data=params.get();
            return readyForHold() && SoundPolicy.sameTrack(startTrack,identity.track(data));
        }
        boolean touch(MotionEvent event) {
            View view = anchor.get(); if (view == null) return false;
            rawX = event.getRawX(); rawY = event.getRawY();
            int action = event.getActionMasked();
            if (action == MotionEvent.ACTION_DOWN) {
                cancel();
                Object data = params.get(); if (data == null) return false;
                startTrack = identity.track(data);
                if (!usable()) return false;
                startX = rawX; startY = rawY; down = true;
                try { holdDelayMillis=NativeBridge.holdDelayMillis(); }
                catch (RuntimeException | LinkageError unavailable) { holdDelayMillis=200L; }
                view.postDelayed(hold, holdDelayMillis);
                return false;
            }
            if (action == MotionEvent.ACTION_POINTER_DOWN || action == MotionEvent.ACTION_CANCEL) {
                boolean handled = opened; cancel(); return handled;
            }
            if (opened) {
                if (!usable()) { cancel(); return true; }
                if (action == MotionEvent.ACTION_MOVE) {
                    moved = moved || SoundPolicy.cancelledBeforeHold(rawX, rawY, startX, startY,
                        ViewConfiguration.get(view.getContext()).getScaledTouchSlop());
                    highlight(rawX, rawY); return true;
                }
                if (action == MotionEvent.ACTION_UP) {
                    down = false; view.removeCallbacks(hold);
                    if (moved && selected >= 0) run(selected);
                    // Holding and releasing on the disc never silently saves. Tap a menu row instead.
                    return true;
                }
                return true;
            }
            if (action == MotionEvent.ACTION_MOVE && SoundPolicy.cancelledBeforeHold(rawX, rawY, startX, startY,
                    ViewConfiguration.get(view.getContext()).getScaledTouchSlop())) {
                down = false; view.removeCallbacks(hold);
            }
            if (action == MotionEvent.ACTION_UP) { down = false; view.removeCallbacks(hold); }
            return false;
        }
        void show() {
            try {
                if (!down || !usable()) return;
                View view = anchor.get(); Object data = params.get();
                Object music = NativeBridge.music(data);
                if (music == null || NativeBridge.musicId(music) == null || NativeBridge.musicId(music).isEmpty()) return;
                Activity activity=activityOf(view.getContext());
                if(activity==null || activity.isFinishing()) return;
                SettingsUi.syncDarkMode(view.getContext());
                background=PopupStyle.background(view.getContext());
                LinearLayout menu = new LinearLayout(view.getContext()); menu.setOrientation(LinearLayout.VERTICAL);
                add(menu, "Save sound to TikTok", () -> NativeBridge.collect(activity,music));
                ScrollView scroll = new ScrollView(view.getContext()); scroll.addView(menu);
                scroll.setBackground(PopupStyle.rounded(view.getContext(),background)); scroll.setClipToOutline(true);
                Rect frame = new Rect(); view.getWindowVisibleDisplayFrame(frame);
                if (frame.width() <= 0 || frame.height() <= 0) return;
                int width = PopupStyle.width(view.getContext(),frame.width());
                menu.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                int height = Math.min(menu.getMeasuredHeight(), (int) (frame.height() * .6f));
                int[] location = new int[2]; view.getLocationOnScreen(location);
                int left = Math.max(frame.left, Math.min(location[0] - width, frame.right - width));
                int top = Math.max(frame.top, Math.min(location[1] + view.getHeight() - height, frame.bottom - height));
                popup = new PopupWindow(scroll, width, height, false);
                popup.setBackgroundDrawable(PopupStyle.rounded(view.getContext(),background));
                popup.setOutsideTouchable(true); popup.setElevation(SettingsUi.dp(view.getContext(), 8));
                PopupWindow created=popup;
                popup.setOnDismissListener(() -> {
                    if(popup!=created) return;
                    popup=null; opened=false; down=false;
                    rows.clear(); actions.clear(); selected=-1;
                });
                popup.showAtLocation(view, Gravity.TOP | Gravity.LEFT, left, top);
                // Cancel the host's pending click only after successful popup creation.
                MotionEvent cancel = MotionEvent.obtain(android.os.SystemClock.uptimeMillis(), android.os.SystemClock.uptimeMillis(),
                    MotionEvent.ACTION_CANCEL, 0, 0, 0);
                dispatchNative(view, cancel, true); cancel.recycle(); view.cancelLongPress();
                PopupStyle.haptic(view);
                opened = true;
                menuOpens++;
                lastStage = "Sound menu opened";
            } catch (RuntimeException | LinkageError ignored) { cancel(); }
        }
        void add(LinearLayout menu, String title, Runnable action) {
            View view = anchor.get(); int index = actions.size();
            TextView row = SettingsUi.text(view.getContext(), title, 16, PopupStyle.textColor(background), Typeface.BOLD);
            int pad = SettingsUi.dp(view.getContext(), 16); row.setPaddingRelative(pad, pad, pad, pad);
            row.setMinimumHeight(SettingsUi.dp(view.getContext(), 52)); row.setGravity(Gravity.CENTER_VERTICAL);
            row.setFocusable(true); row.setClickable(true); row.setOnClickListener(v -> run(index));
            row.setOnTouchListener((v,event)-> { if(event.getActionMasked()==MotionEvent.ACTION_DOWN) PopupStyle.haptic(v); return false; });
            row.setBackground(PopupStyle.row(background));
            menu.addView(row); rows.add(row); actions.add(action);
        }
        void highlight(float x, float y) {
            int next=-1;
            for (int i = 0; i < rows.size(); i++) {
                View row=rows.get(i); Rect rect = new Rect(); int[] origin=new int[2];
                boolean visible=row.getGlobalVisibleRect(rect);
                // PopupWindow has its own root origin. Compare screen coordinates, not root-local rectangles.
                row.getRootView().getLocationOnScreen(origin); rect.offset(origin[0],origin[1]);
                boolean hit = visible && rect.contains((int)x, (int)y);
                if (hit) next=i;
            }
            select(next);
        }
        void select(int next) {
            for(int i=0;i<rows.size();i++) rows.get(i).setPressed(i==next);
            if(next!=selected && next>=0) PopupStyle.haptic(rows.get(next));
            selected=next;
        }
        void run(int index) {
            if (!usable() || index < 0 || index >= actions.size()) { cancel(); return; }
            Runnable action = actions.get(index);
            View feedback=anchor.get(); if(feedback!=null) PopupStyle.haptic(feedback);
            cancel(); // Exactly once, and dismiss before native auth/sheets open.
            try { action.run(); }
            catch (RuntimeException | LinkageError ignored) {
                View view = anchor.get(); if (view != null)
                    Toast.makeText(view.getContext(), "Could not save this sound. Open its sound page to try again.", Toast.LENGTH_SHORT).show();
            }
        }
        void cancel() {
            View view = anchor.get(); if (view != null) view.removeCallbacks(hold);
            down = false; opened = false; moved = false; selected = -1;
            if (popup != null) {
                try { popup.dismiss(); } catch (RuntimeException | LinkageError ignored) {}
            }
            popup = null; rows.clear(); actions.clear();
        }
    }
    private static Activity activityOf(Context context) {
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) return (Activity) context;
            Context base=((ContextWrapper)context).getBaseContext();
            if (base==context) break;
            context=base;
        }
        return null;
    }
    private SoundRuntime() {}
}
