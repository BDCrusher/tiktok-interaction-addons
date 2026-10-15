package app.collectionsaddon.runtime;
import android.app.AlertDialog;
import android.app.Activity;
import android.content.Context;
import android.preference.PreferenceScreen;
import android.preference.PreferenceManager;
import android.preference.PreferenceActivity;
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
public class RuntimeTest {
    public static class SettingsActivity extends PreferenceActivity {}
    @Test public void countAndMoreBoundaries() {
        assertEquals(1, Policy.clampCount(-3)); assertEquals(20, Policy.clampCount(99));
        assertFalse(Policy.needsMore(5, 5, false)); assertTrue(Policy.needsMore(6, 5, false));
        assertTrue(Policy.needsMore(2, 5, true)); assertEquals(600, Policy.maxHeight(1000));
    }
    @Test public void preferencesDefaultsSaveAndReset() {
        Activity activity = Robolectric.buildActivity(Activity.class).setup().get();
        Preferences.reset(activity);
        assertTrue(Preferences.enabled(activity)); assertEquals(5, Preferences.count(activity));
        Preferences.save(activity, false, 8);
        assertFalse(Preferences.enabled(activity)); assertEquals(8, Preferences.count(activity));
        Preferences.reset(activity); assertEquals(5, Preferences.count(activity));
    }
    @Test public void settingsEntryIsIdempotent() {
        PreferenceActivity activity = Robolectric.buildActivity(SettingsActivity.class).setup().get();
        PreferenceManager manager = activity.getPreferenceManager();
        PreferenceScreen screen = manager.createPreferenceScreen(activity);
        SettingsEntry.install(null, activity, screen); SettingsEntry.install(null, activity, screen);
        assertEquals(2, screen.getPreferenceCount());
        assertEquals("Personal add-ons", screen.getPreference(0).getTitle());
        assertEquals("BDCrusher's Settings", screen.getPreference(1).getTitle());
    }
    @Test public void plainHostThemeFallbackAndContrast() {
        Activity activity = Robolectric.buildActivity(Activity.class).setup().get();
        assertEquals(0xffabcdef, Panels.resolve(activity, "missing_test_attr", 0xffabcdef));
        assertTrue(Panels.luminance(0xff000000) < .5);
        assertTrue(Panels.luminance(0xffffffff) > .5);
    }
    @Test public void pauseRestoresStockWithoutErasingPreferences() {
        Activity activity = Robolectric.buildActivity(Activity.class).setup().get();
        Preferences.save(activity, true, 12);
        app.morphe.extension.shared.settings.HushfeedPause.paused = true;
        try {
            assertFalse(CollectionsRuntime.enabled(activity));
            assertEquals(3, CollectionsRuntime.count(new android.app.Dialog(activity)));
            assertEquals(12, Preferences.count(activity));
        } finally { app.morphe.extension.shared.settings.HushfeedPause.paused = false; }
        assertTrue(CollectionsRuntime.enabled(activity));
    }
    @Test public void customAmoledOnlyChangesDarkSurfaceAndRejectsInvalidColors() {
        java.util.Properties properties = new java.util.Properties();
        properties.setProperty("amoled", "applied"); properties.setProperty("amoled_color", "#071017");
        assertEquals(0xff071017, Panels.configuredBackground(0xff121212, properties));
        assertEquals(0xffffffff, Panels.configuredBackground(0xffffffff, properties));
        properties.setProperty("amoled_color", "#00000000");
        assertEquals(0xff121212, Panels.configuredBackground(0xff121212, properties));
        properties.setProperty("amoled_color", "#000000");
        assertEquals(0xff000000, Panels.configuredBackground(0xff121212, properties));
    }
    @Test public void settingsValidateRangeAndSaveIndependentPreferences() {
        Activity activity = Robolectric.buildActivity(Activity.class).setup().get();
        Preferences.reset(activity);
        android.widget.FrameLayout frame = new android.widget.FrameLayout(activity); frame.setId(1234); activity.setContentView(frame);
        CollectionSettingsFragment page = new CollectionSettingsFragment();
        activity.getFragmentManager().beginTransaction().add(1234, page).commit();
        activity.getFragmentManager().executePendingTransactions();
        CountSlider slider = page.getView().findViewWithTag("collections_addon.count.slider");
        slider.value(21); assertEquals(20, slider.value()); assertEquals(5, Preferences.count(activity));
        for(int i=0;i<19;i++) slider.onKeyDown(android.view.KeyEvent.KEYCODE_DPAD_LEFT,new android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN,android.view.KeyEvent.KEYCODE_DPAD_LEFT));
        assertEquals(1, Preferences.count(activity));
    }
}
