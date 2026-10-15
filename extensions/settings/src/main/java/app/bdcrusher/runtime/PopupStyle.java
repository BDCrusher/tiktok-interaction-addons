package app.bdcrusher.runtime;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.TypedValue;
import android.view.View;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.WeakHashMap;

/** Shared appearance/feedback, independent of either feature's runtime. */
public final class PopupStyle {
    private static final Map<Resources, Width> WIDTHS = new WeakHashMap<>();
    private static final class Width {
        final int pixels, screen, density, direction, font;
        Width(Resources r, int pixels) {
            this.pixels=pixels; screen=r.getConfiguration().screenWidthDp;
            density=r.getDisplayMetrics().densityDpi; direction=r.getConfiguration().getLayoutDirection();
            font=Float.floatToIntBits(r.getConfiguration().fontScale);
        }
        boolean matches(Resources r) { return screen==r.getConfiguration().screenWidthDp
            && density==r.getDisplayMetrics().densityDpi && direction==r.getConfiguration().getLayoutDirection()
            && font==Float.floatToIntBits(r.getConfiguration().fontScale); }
    }
    public static void rememberWidth(Context context, int width) {
        if(width>0 && width<=context.getResources().getDisplayMetrics().widthPixels)
            WIDTHS.put(context.getResources(),new Width(context.getResources(),width));
    }
    public static int width(Context context, int available) {
        Resources r=context.getResources(); Width sample=WIDTHS.get(r);
        // Native quick-favorite layout ik3 has a 212 dp max width; prefer actual observed width.
        int desired=sample!=null && sample.matches(r) ? sample.pixels : Math.round(212*r.getDisplayMetrics().density);
        return Math.max(1,Math.min(available,desired));
    }
    public static int background(Context context) {
        int host=(context.getResources().getConfiguration().uiMode & 0x30)==0x20 ? 0xff121212 : Color.WHITE;
        TypedValue value=new TypedValue();
        try {
            if("a24".equals(context.getResources().getResourceEntryName(0x7f0603e5))
                && context.getTheme().resolveAttribute(0x7f0603e5,value,true)) {
                if(value.type>=TypedValue.TYPE_FIRST_COLOR_INT && value.type<=TypedValue.TYPE_LAST_COLOR_INT) host=value.data;
                else if(value.resourceId!=0) host=context.getResources().getColor(value.resourceId);
            }
        } catch(RuntimeException ignored) {}
        if(luminance(host)<.5) try(InputStream stream=context.getAssets().open("hushfeed-build-v1.txt")) {
            Properties properties=new Properties(); properties.load(stream); return configuredBackground(host,properties);
        } catch(Exception ignored) {}
        return host;
    }
    public static int configuredBackground(int host,Properties properties) {
        if(luminance(host)<.5 && "applied".equals(properties.getProperty("amoled"))) {
            String color=properties.getProperty("amoled_color","");
            if(color.matches("#[0-9a-fA-F]{6}|#[fF]{2}[0-9a-fA-F]{6}")) return Color.parseColor(color);
        }
        return host;
    }
    public static double luminance(int color) { return (.2126*Color.red(color)+.7152*Color.green(color)+.0722*Color.blue(color))/255.; }
    public static int textColor(int background) { return luminance(background)<.5 ? Color.WHITE : 0xff161823; }
    public static int pressedColor(int base) {
        int ink=luminance(base)<.5 ? Color.WHITE : Color.BLACK;
        return Color.rgb((Color.red(base)*85+Color.red(ink)*15)/100,
            (Color.green(base)*85+Color.green(ink)*15)/100,(Color.blue(base)*85+Color.blue(ink)*15)/100);
    }
    public static GradientDrawable rounded(Context context,int base) {
        GradientDrawable drawable=new GradientDrawable(); drawable.setColor(base);
        drawable.setCornerRadius(12*context.getResources().getDisplayMetrics().density); return drawable;
    }
    public static StateListDrawable row(int base) {
        StateListDrawable states=new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_pressed},new ColorDrawable(pressedColor(base)));
        states.addState(new int[]{android.R.attr.state_focused},new ColorDrawable(pressedColor(base)));
        states.addState(new int[]{},new ColorDrawable(Color.TRANSPARENT)); return states;
    }
    public static void haptic(View view) {
        try { NativeUiBridge.haptic(view.getContext()); }
        catch(RuntimeException | LinkageError unavailable) { view.performHapticFeedback(android.view.HapticFeedbackConstants.CLOCK_TICK); }
    }
    private PopupStyle() {}
}
