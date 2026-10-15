package app.morphe.extension.tiktok.settings.preference;
import android.app.AlertDialog;
import android.content.Context;
/** Compile-only signatures; NEVER shipped in the payload. */
public final class SettingsUi {
    public static void syncDarkMode(Context context) {}
    public static void styleStandardAlertDialog(AlertDialog dialog) {}
    public static int dp(Context context, int value) { return Math.round(value * context.getResources().getDisplayMetrics().density); }
    public static int background() { return 0xff000000; }
    public static int textPrimary() { return 0xfff5f5f7; }
    public static int textSecondary() { return 0xffa8a8b3; }
    public static void styleSwitch(android.widget.Switch control) {}
    public static void styleEditText(android.widget.EditText input) {}
    public static android.graphics.drawable.Drawable groupedRow(Context context, boolean first, boolean last) {
        return new android.graphics.drawable.ColorDrawable(0xff111115);
    }
    public static android.widget.TextView sectionTitle(Context context, String value) {
        return text(context, value, 13, 0xffff4f87, android.graphics.Typeface.BOLD);
    }
    public static android.widget.TextView text(Context context, String value, float size, int color, int style) {
        android.widget.TextView text = new android.widget.TextView(context);
        text.setText(value); text.setTextSize(size); text.setTextColor(color); text.setTypeface(null, style); return text;
    }
}
