package app.collectionsaddon.runtime;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.SeekBar;
import app.morphe.extension.tiktok.settings.preference.SettingsUi;

/** Twenty integer stops, including visible ticks on API 23 (native tick marks require API 24). */
final class CountSlider extends SeekBar {
    private final Paint ticks = new Paint(Paint.ANTI_ALIAS_FLAG);
    CountSlider(Context context) {
        super(context);
        setMax(19);
        setMinimumHeight(SettingsUi.dp(context, 56));
        int accent = SettingsUi.sectionTitle(context, "").getCurrentTextColor();
        setProgressTintList(ColorStateList.valueOf(accent));
        setThumbTintList(ColorStateList.valueOf(accent));
        setProgressBackgroundTintList(ColorStateList.valueOf(SettingsUi.textSecondary()));
        ticks.setColor(SettingsUi.textSecondary());
        ticks.setStrokeWidth(Math.max(1, SettingsUi.dp(context, 1)));
        setTag("collections_addon.count.slider");
    }
    int value() { return getProgress() + 1; }
    void value(int number) { setProgress(Math.max(1, Math.min(20, number)) - 1); }
    @Override public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setRangeInfo(AccessibilityNodeInfo.RangeInfo.obtain(AccessibilityNodeInfo.RangeInfo.RANGE_TYPE_INT, 1, 20, value()));
    }
    @Override public boolean performAccessibilityAction(int action, Bundle arguments) {
        if (Build.VERSION.SDK_INT >= 24 && action == AccessibilityNodeInfo.AccessibilityAction.ACTION_SET_PROGRESS.getId()
                && arguments != null && arguments.containsKey(AccessibilityNodeInfo.ACTION_ARGUMENT_PROGRESS_VALUE)) {
            Bundle nativeArgs = new Bundle(arguments);
            nativeArgs.putFloat(AccessibilityNodeInfo.ACTION_ARGUMENT_PROGRESS_VALUE,
                Math.max(0, Math.min(19, arguments.getFloat(AccessibilityNodeInfo.ACTION_ARGUMENT_PROGRESS_VALUE) - 1)));
            return super.performAccessibilityAction(action, nativeArgs);
        }
        return super.performAccessibilityAction(action, arguments);
    }
    float tickX(int stop) {
        float portion = stop / 19f;
        if (getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) portion = 1 - portion;
        return getPaddingLeft() + portion * (getWidth() - getPaddingLeft() - getPaddingRight());
    }
    @Override protected synchronized void onDraw(Canvas canvas) {
        float middle = getPaddingTop() + (getHeight() - getPaddingTop() - getPaddingBottom()) / 2f;
        float half = SettingsUi.dp(getContext(), 5);
        for (int stop = 0; stop < 20; stop++) canvas.drawLine(tickX(stop), middle - half, tickX(stop), middle + half, ticks);
        super.onDraw(canvas); // Keep native track/thumb drawing and keyboard/accessibility interaction.
    }
}
