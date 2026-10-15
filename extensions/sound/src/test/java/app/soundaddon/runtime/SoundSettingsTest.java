package app.soundaddon.runtime;
import android.app.Activity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=28,manifest=Config.NONE)
public class SoundSettingsTest {
    public static class Host extends PreferenceFragment {}
    enum Section { INTERFACE, PLAYBACK }
    @Test public void defaultsAndResetAreIsolated() {
        Activity activity = Robolectric.buildActivity(Activity.class).setup().get();
        activity.getSharedPreferences("collections_addon_v1",0).edit().putInt("count",17).commit();
        assertTrue(SoundPreferences.enabled(activity)); SoundPreferences.enabled(activity,false); assertFalse(SoundPreferences.enabled(activity));
        SoundPreferences.reset(activity); assertTrue(SoundPreferences.enabled(activity));
        assertEquals(17,activity.getSharedPreferences("collections_addon_v1",0).getInt("count",0));
    }
    @Test public void sharedHubIsAvailableWithSoundOnlyAndIsIdempotent() {
        Activity activity = Robolectric.buildActivity(Activity.class).setup().get();
        PreferenceFragment host = new Host();
        activity.getFragmentManager().beginTransaction().add(android.R.id.content,host).commit(); activity.getFragmentManager().executePendingTransactions();
        PreferenceScreen screen = host.getPreferenceManager().createPreferenceScreen(activity); host.setPreferenceScreen(screen);
        SoundSettings.install(host,activity,screen); SoundSettings.install(host,activity,screen);
        assertEquals(2,screen.getPreferenceCount()); assertNotNull(screen.findPreference(SoundSettings.KEY));
        android.preference.Preference entry = screen.findPreference(SoundSettings.KEY);
        assertEquals("BDCrusher's Settings", entry.getTitle());
        entry.getOnPreferenceClickListener().onPreferenceClick(entry);
        activity.getFragmentManager().executePendingTransactions();
        android.app.Fragment hub = activity.getFragmentManager().findFragmentById(android.R.id.content);
        assertTrue(hub instanceof app.bdcrusher.runtime.SettingsHub.Page);
        assertNull(hub.getView().findViewWithTag("collections_addon.settings"));
        hub.getView().findViewWithTag("sound_actions_addon.settings").performClick();
        activity.getFragmentManager().executePendingTransactions();
        assertTrue(activity.getFragmentManager().findFragmentById(android.R.id.content) instanceof SoundSettings.Page);
    }
    @Test public void fullPageOpensAndSurvivesRecreation() {
        org.robolectric.android.controller.ActivityController<Activity> controller = Robolectric.buildActivity(Activity.class).setup();
        Activity activity = controller.get();
        activity.getFragmentManager().beginTransaction().add(android.R.id.content,new SoundSettings.Page(),"sound").commit();
        activity.getFragmentManager().executePendingTransactions();
        assertNotNull(activity.getFragmentManager().findFragmentByTag("sound").getView());
        controller.recreate(); assertTrue(controller.get().getFragmentManager().findFragmentByTag("sound") instanceof SoundSettings.Page);
    }
}
