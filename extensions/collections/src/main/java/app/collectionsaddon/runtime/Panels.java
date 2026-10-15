package app.collectionsaddon.runtime;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Properties;
import java.util.WeakHashMap;
import app.bdcrusher.runtime.PopupStyle;

final class Panels {
    private static final Map<Dialog, State> STATES = new WeakHashMap<>();
    private static final class State {
        final WeakReference<ScrollView> scroll;
        final WeakReference<View> root;
        final int slop;
        boolean scrolling;
        boolean hasDown;
        float downX, downY, lastY;
        State(ScrollView scroll, View root) {
            this.scroll = new WeakReference<>(scroll); this.root = new WeakReference<>(root);
            slop = ViewConfiguration.get(root.getContext()).getScaledTouchSlop();
        }
    }
    static int resolve(Context context, String attr, int fallback) {
        // This token is verified against the sole supported APK. Its ID survives app renaming.
        int id = 0;
        if ("a24".equals(attr)) try {
            if ("a24".equals(context.getResources().getResourceEntryName(0x7f0603e5))) id = 0x7f0603e5;
        } catch (android.content.res.Resources.NotFoundException ignored) { }
        TypedValue value = new TypedValue();
        if (id != 0 && context.getTheme().resolveAttribute(id, value, true)) {
            if (value.type >= TypedValue.TYPE_FIRST_COLOR_INT && value.type <= TypedValue.TYPE_LAST_COLOR_INT) return value.data;
            if (value.resourceId != 0) {
                try { return context.getResources().getColor(value.resourceId); }
                catch (RuntimeException ignored) { }
            }
        }
        return fallback;
    }
    static int background(Context context) {
        return PopupStyle.background(context);
    }
    static int configuredBackground(int host, Properties properties) {
        return PopupStyle.configuredBackground(host, properties);
    }
    static double luminance(int color) {
        return (.2126 * Color.red(color) + .7152 * Color.green(color) + .0722 * Color.blue(color)) / 255.;
    }
    static void pressed(Context context, View overlay, boolean pressed) {
        if (!pressed) { overlay.setBackground(null); return; }
        int base = background(context);
        overlay.setBackgroundColor(PopupStyle.pressedColor(base));
    }
    static void install(Dialog dialog, View root) {
        Context context = dialog.getContext();
        Window window = dialog.getWindow();
        if (window == null) throw new IllegalStateException("Popup has no window");
        int background = background(context);
        int text = luminance(background) < .5 ? Color.WHITE : 0xff161823;
        restyle(root, context, text, background);
        GradientDrawable rounded = new GradientDrawable();
        rounded.setColor(background); rounded.setCornerRadius(12 * context.getResources().getDisplayMetrics().density);
        root.setBackground(rounded);
        Rect usable = new Rect(); window.getDecorView().getWindowVisibleDisplayFrame(usable);
        if (usable.height() <= 0) usable.set(0, 0, context.getResources().getDisplayMetrics().widthPixels, context.getResources().getDisplayMetrics().heightPixels);
        int maximum = Policy.maxHeight(usable.height());
        root.measure(View.MeasureSpec.makeMeasureSpec(usable.width(), View.MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        int height = Math.min(maximum, root.getMeasuredHeight());
        int width = Math.min(usable.width(), root.getMeasuredWidth());
        PopupStyle.rememberWidth(context,width);
        View content = root;
        if (root.getMeasuredHeight() > maximum) {
            ScrollView scroll = new ScrollView(context);
            scroll.setFillViewport(false); scroll.setClipToOutline(true); scroll.setBackground(rounded);
            scroll.addView(root, new ViewGroup.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT));
            STATES.put(dialog, new State(scroll, root));
            content = scroll;
        }
        dialog.setContentView(content);
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = width; params.height = Math.max(1, height);
        // TikTok anchors relative to the rail; clamp the result rather than altering its trigger.
        params.y = Math.max(0, Math.min(params.y, usable.height() - params.height));
        params.x = Math.max(0, Math.min(params.x, usable.width() - params.width));
        window.setAttributes(params);
    }
    private static void restyle(View view, Context context, int text, int background) {
        if (view instanceof TextView) ((TextView) view).setTextColor(text);
        if (view.getClass().getName().equals("com.bytedance.tux.icon.TuxIconView")) HostBridge.tint(view, text);
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) restyle(group.getChildAt(i), context, text, background);
        } else if (view.getClass() == View.class) {
            // Only native separator/plain state views; never tint collection thumbnails.
            ViewGroup.LayoutParams params = view.getLayoutParams();
            if (params != null && params.height > 0 && params.height <= 3) view.setBackgroundColor((text & 0xffffff) | 0x33000000);
        }
    }
    static boolean touch(Dialog dialog, MotionEvent event) {
        State state = STATES.get(dialog);
        if (state == null) return false;
        ScrollView scroll = state.scroll.get(); View root = state.root.get();
        if (scroll == null || root == null) { STATES.remove(dialog); return false; }
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            state.hasDown = true; state.scrolling = false; state.downX = event.getRawX(); state.downY = state.lastY = event.getRawY(); return false;
        }
        if (action == MotionEvent.ACTION_MOVE) {
            if (!state.hasDown) { state.hasDown = true; state.downX = event.getRawX(); state.downY = state.lastY = event.getRawY(); return false; }
            if (!state.scrolling && (Math.abs(event.getRawY() - state.downY) > state.slop || Math.abs(event.getRawX() - state.downX) > state.slop)) {
                MotionEvent cancel = MotionEvent.obtain(event); cancel.setAction(MotionEvent.ACTION_CANCEL);
                try { ((View.OnTouchListener) dialog).onTouch(root, cancel); } finally { cancel.recycle(); }
                state.hasDown = true; state.scrolling = true;
            }
            if (state.scrolling) { scroll.scrollBy(0, (int) (state.lastY - event.getRawY())); state.lastY = event.getRawY(); return true; }
        }
        if (action == MotionEvent.ACTION_CANCEL) { state.hasDown = false; state.scrolling = false; return false; }
        if (action == MotionEvent.ACTION_UP && state.scrolling) { state.hasDown = false; state.scrolling = false; return true; }
        return false;
    }
    static void release(Dialog dialog) { STATES.remove(dialog); }
}
