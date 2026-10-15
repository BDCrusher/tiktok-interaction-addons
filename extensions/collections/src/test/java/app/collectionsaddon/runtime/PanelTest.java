package app.collectionsaddon.runtime;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, manifest = Config.NONE)
public class PanelTest {
    static class TestDialog extends Dialog implements View.OnTouchListener {
        int cancelled;
        TestDialog(Context context) { super(context); }
        public boolean onTouch(View view, MotionEvent event) {
            if (event.getActionMasked() == MotionEvent.ACTION_CANCEL) cancelled++;
            return Panels.touch(this, event);
        }
    }
    private TestDialog panel(Activity activity, int rows) {
        TestDialog dialog = new TestDialog(activity);
        LinearLayout root = new LinearLayout(activity); root.setOrientation(LinearLayout.VERTICAL);
        for (int i = 0; i < rows; i++) {
            TextView text = new TextView(activity); text.setText("Collection " + i);
            root.addView(text, new LinearLayout.LayoutParams(212, 100));
        }
        Panels.install(dialog, root); return dialog;
    }
    private boolean touch(TestDialog dialog, int action, float y) {
        MotionEvent event = MotionEvent.obtain(0, 10, action, 20, y, 0);
        try { return Panels.touch(dialog, event); } finally { event.recycle(); }
    }
    @Test public void longListIsBoundedAndScrollReleaseCannotSelect() {
        Activity activity = Robolectric.buildActivity(Activity.class).setup().get();
        TestDialog dialog = panel(activity, 30);
        assertTrue(dialog.getWindow().getAttributes().height <= Policy.maxHeight(activity.getResources().getDisplayMetrics().heightPixels));
        assertFalse(touch(dialog, MotionEvent.ACTION_DOWN, 200));
        assertTrue(touch(dialog, MotionEvent.ACTION_MOVE, 100));
        assertTrue(touch(dialog, MotionEvent.ACTION_MOVE, 80));
        assertTrue(touch(dialog, MotionEvent.ACTION_UP, 80));
        assertEquals(1, dialog.cancelled);
        Panels.release(dialog);
        assertFalse(touch(dialog, MotionEvent.ACTION_MOVE, 50));
    }
    @Test public void shortListLeavesNativeDragSelectionUntouched() {
        Activity activity = Robolectric.buildActivity(Activity.class).setup().get();
        TestDialog dialog = panel(activity, 2);
        assertFalse(touch(dialog, MotionEvent.ACTION_DOWN, 200));
        assertFalse(touch(dialog, MotionEvent.ACTION_MOVE, 100));
        assertFalse(touch(dialog, MotionEvent.ACTION_UP, 100));
    }
    @Test public void firstMoveAfterLongPressSeedsPositionRatherThanScrolling() {
        Activity activity = Robolectric.buildActivity(Activity.class).setup().get();
        TestDialog dialog = panel(activity, 30);
        assertFalse(touch(dialog, MotionEvent.ACTION_MOVE, 200));
        assertTrue(touch(dialog, MotionEvent.ACTION_MOVE, 100));
        assertTrue(touch(dialog, MotionEvent.ACTION_UP, 100));
    }
    @Test public void pressedOverlayIsThemeContrastingAndClears() {
        Activity activity = Robolectric.buildActivity(Activity.class).setup().get();
        View overlay = new View(activity);
        Panels.pressed(activity, overlay, true); assertNotNull(overlay.getBackground());
        Panels.pressed(activity, overlay, false); assertNull(overlay.getBackground());
    }
    @Test public void largeFontAndRtlRemainHeightBoundedInSimulation() {
        Activity activity = Robolectric.buildActivity(Activity.class).setup().get();
        android.content.res.Configuration config = new android.content.res.Configuration(activity.getResources().getConfiguration());
        config.fontScale = 2; config.setLayoutDirection(new java.util.Locale("ar"));
        Context context = activity.createConfigurationContext(config);
        TestDialog dialog = new TestDialog(context);
        LinearLayout root = new LinearLayout(context); root.setOrientation(LinearLayout.VERTICAL);
        root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        for (int i = 0; i < 20; i++) {
            TextView text = new TextView(context); text.setText("مجموعة "+i); text.setTextSize(48);
            root.addView(text);
        }
        Panels.install(dialog, root);
        assertTrue(dialog.getWindow().getAttributes().height <= Policy.maxHeight(context.getResources().getDisplayMetrics().heightPixels));
    }
    @Test public void clippedHitTestingUsesRawScreenRatherThanWindowCoordinates() {
        Activity activity = Robolectric.buildActivity(Activity.class).setup().get();
        Preferences.reset(activity);
        View row = new View(activity) {
            @Override public boolean getGlobalVisibleRect(android.graphics.Rect rect, android.graphics.Point offset) {
                rect.set(20, 30, 60, 70); if (offset != null) offset.set(20, 30); return true;
            }
            @Override public void getLocationOnScreen(int[] position) { position[0] = 120; position[1] = 230; }
            @Override public void getLocationInWindow(int[] position) { position[0] = 20; position[1] = 30; }
        };
        row.layout(0, 0, 40, 100);
        assertTrue(CollectionsRuntime.hit(125, 235, row));
        assertFalse(CollectionsRuntime.hit(25, 35, row));
        assertFalse(CollectionsRuntime.hit(125, 275, row));
        Preferences.save(activity, false, 5);
        assertTrue(CollectionsRuntime.hit(125, 275, row)); // Stock includes the entire row.
    }
}
