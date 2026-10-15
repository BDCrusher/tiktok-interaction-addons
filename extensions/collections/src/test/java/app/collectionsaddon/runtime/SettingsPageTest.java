package app.collectionsaddon.runtime;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Switch;
import app.morphe.extension.tiktok.settings.preference.SectionHeadingPreference;
import app.morphe.extension.tiktok.settings.preference.SettingsMenuPreference;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, manifest = Config.NONE,instrumentedPackages={"app.bdcrusher.runtime"},shadows={SettingsPageTest.Feedback.class})
public class SettingsPageTest {
    @org.robolectric.annotation.Implements(value=app.bdcrusher.runtime.NativeUiBridge.class,isInAndroidSdk=false)
    public static class Feedback {
        static int pulses;
        @org.robolectric.annotation.Implementation public static void haptic(android.content.Context context) {pulses++;}
    }
    public static class PageActivity extends Activity {
        @Override public void onCreate(Bundle state) {
            super.onCreate(state);
            FrameLayout frame = new FrameLayout(this); frame.setId(1234); setContentView(frame);
        }
    }
    public static class Host extends PreferenceFragment {
        @Override public void onCreate(Bundle state) {
            super.onCreate(state);
            PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(getActivity());
            setPreferenceScreen(screen);
            screen.addPreference(new SectionHeadingPreference(getActivity(), "Your feed", true));
            Preference feed = new Preference(getActivity()); feed.setTitle("Feed screen"); feed.setKey("section_interface"); screen.addPreference(feed);
            Preference playback = new Preference(getActivity()); playback.setTitle("Playback"); screen.addPreference(playback);
            screen.addPreference(new SectionHeadingPreference(getActivity(), "Conversations", true));
            screen.addPreference(new SectionHeadingPreference(getActivity(), "Hushfeed", true));
            SettingsEntry.install(this, getActivity(), screen);
        }
        @Override public void onResume() { super.onResume(); SettingsEntry.refresh(this); }
    }
    private Activity activity() {
        Activity activity = Robolectric.buildActivity(Activity.class).setup().get(); Preferences.reset(activity); Feedback.pulses=0;
        FrameLayout frame = new FrameLayout(activity); frame.setId(1234); activity.setContentView(frame); return activity;
    }
    private Host host(Activity activity) {
        Host host = new Host(); activity.getFragmentManager().beginTransaction().add(1234, host).commit();
        activity.getFragmentManager().executePendingTransactions(); return host;
    }
    private CollectionSettingsFragment page(Activity activity, Host host) {
        host.findPreference(SettingsEntry.KEY).getOnPreferenceClickListener().onPreferenceClick(host.findPreference(SettingsEntry.KEY));
        activity.getFragmentManager().executePendingTransactions();
        Fragment hub = activity.getFragmentManager().findFragmentById(1234);
        assertTrue(hub instanceof app.bdcrusher.runtime.SettingsHub.Page);
        assertNull(hub.getView().findViewWithTag("sound_actions_addon.settings"));
        hub.getView().findViewWithTag("collections_addon.settings").performClick();
        activity.getFragmentManager().executePendingTransactions();
        return (CollectionSettingsFragment) activity.getFragmentManager().findFragmentById(1234);
    }
    @Test public void placedInFeedWithNoDefaultChangedBadge() {
        Activity activity = activity(); Host host = host(activity); PreferenceScreen screen = host.getPreferenceScreen();
        assertEquals("Feed screen", screen.getPreference(1).getTitle());
        assertEquals("BDCrusher's Settings", screen.getPreference(2).getTitle());
        assertEquals("Playback", screen.getPreference(3).getTitle());
        assertFalse(screen.getPreference(2).getSummary().toString().contains("changed"));
        SettingsEntry.install(host, activity, screen); assertEquals(6, screen.getPreferenceCount());
    }
    @Test public void opensFullPageAndBackRefreshesSummary() {
        Activity activity = activity(); Host host = host(activity); CollectionSettingsFragment page = page(activity, host);
        assertNotNull(page.getView().findViewWithTag("hushfeed_page_header"));
        assertEquals(2, activity.getFragmentManager().getBackStackEntryCount());
        page.getView().findViewWithTag("collections_addon.enabled").performClick();
        assertFalse(Preferences.enabled(activity));
        assertFalse(page.getView().findViewWithTag("collections_addon.count").isEnabled());
        activity.getFragmentManager().popBackStackImmediate();
        assertTrue(activity.getFragmentManager().findFragmentById(1234) instanceof app.bdcrusher.runtime.SettingsHub.Page);
        activity.getFragmentManager().popBackStackImmediate();
        assertSame(host, activity.getFragmentManager().findFragmentById(1234));
        assertTrue(host.findPreference(SettingsEntry.KEY).getSummary().toString().contains("1 changed"));
    }
    @Test public void resetNeedsConfirmationAndNeverClearsHostPreferences() {
        Activity activity = activity(); Host host = host(activity); Preferences.save(activity, false, 12);
        activity.getSharedPreferences("host", 0).edit().putInt("sentinel", 77).commit();
        CollectionSettingsFragment page = page(activity, host);
        page.getView().findViewWithTag("collections_addon.reset").performClick(); activity.getFragmentManager().executePendingTransactions();
        AlertDialog dialog = (AlertDialog) ((android.app.DialogFragment) activity.getFragmentManager().findFragmentByTag(CollectionSettingsFragment.DIALOG_TAG)).getDialog();
        assertEquals(12, Preferences.count(activity));
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
        org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();
        assertTrue(Preferences.enabled(activity)); assertEquals(5, Preferences.count(activity));
        assertEquals(77, activity.getSharedPreferences("host", 0).getInt("sentinel", 0));
        assertTrue(page.getView().findViewWithTag("collections_addon.count").isEnabled());
    }
    @Test public void sliderIsInlineAndKeyboardChangesSaveWithoutOpeningDialog() {
        Activity activity = activity(); CollectionSettingsFragment page = page(activity, host(activity));
        page.getView().findViewWithTag("collections_addon.count").performClick(); activity.getFragmentManager().executePendingTransactions();
        assertNull(activity.getFragmentManager().findFragmentByTag(CollectionSettingsFragment.DIALOG_TAG));
        CountSlider slider=page.getView().findViewWithTag("collections_addon.count.slider"); assertNotNull(slider);
        assertTrue(slider.onKeyDown(android.view.KeyEvent.KEYCODE_DPAD_RIGHT,new android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN,android.view.KeyEvent.KEYCODE_DPAD_RIGHT)));
        assertEquals(6,Preferences.count(activity)); assertEquals(6,slider.value());
        assertTrue(slider.getContentDescription().toString().contains("6"));
        assertEquals(1,Feedback.pulses);
        slider.onKeyDown(android.view.KeyEvent.KEYCODE_DPAD_RIGHT,new android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN,android.view.KeyEvent.KEYCODE_DPAD_RIGHT));
        assertEquals(2,Feedback.pulses); slider.value(8); assertEquals(2,Feedback.pulses); // Programmatic refresh is silent.
    }
    @Test public void narrowLargeFontPageStacksSwitchAndKeepsScrolling() {
        Activity activity = activity(); activity.getResources().getConfiguration().fontScale = 2;
        CollectionSettingsFragment page = page(activity, host(activity));
        assertTrue(page.getView() instanceof android.widget.ScrollView);
        LinearLayout row = page.getView().findViewWithTag("collections_addon.enabled");
        assertEquals(LinearLayout.VERTICAL, row.getOrientation());
        assertTrue(row.getChildAt(1) instanceof Switch);
    }
    @Test public void changedCountTracksBothSettingsNotEnabledState() {
        Activity activity = activity(); assertEquals(0, SettingsEntry.changedCount(activity));
        Preferences.save(activity, true, 6); assertEquals(1, SettingsEntry.changedCount(activity));
        Preferences.save(activity, false, 6); assertEquals(2, SettingsEntry.changedCount(activity));
        Preferences.reset(activity); assertEquals(0, SettingsEntry.changedCount(activity));
    }
    @Test public void activityRecreationRestoresInlineSliderSavedCount() {
        org.robolectric.android.controller.ActivityController<PageActivity> controller = Robolectric.buildActivity(PageActivity.class).setup();
        PageActivity activity = controller.get(); Preferences.reset(activity);
        CollectionSettingsFragment page = page(activity, host(activity));
        CountSlider slider=page.getView().findViewWithTag("collections_addon.count.slider");
        for(int i=0;i<9;i++) slider.onKeyDown(android.view.KeyEvent.KEYCODE_DPAD_RIGHT,new android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN,android.view.KeyEvent.KEYCODE_DPAD_RIGHT));
        controller.recreate(); activity = controller.get();
        activity.getFragmentManager().executePendingTransactions();
        assertTrue(activity.getFragmentManager().findFragmentById(1234) instanceof CollectionSettingsFragment);
        page=(CollectionSettingsFragment)activity.getFragmentManager().findFragmentById(1234);
        slider=page.getView().findViewWithTag("collections_addon.count.slider");
        assertEquals(14,slider.value()); assertNull(activity.getFragmentManager().findFragmentByTag(CollectionSettingsFragment.DIALOG_TAG));
        assertEquals(14, Preferences.count(activity));
    }
    @Test public void integerSliderHasTwentyStopsAndRtlMirrorsTicks() {
        Activity activity = activity(); CountSlider slider = new CountSlider(activity);
        activity.getApplicationInfo().flags |= android.content.pm.ApplicationInfo.FLAG_SUPPORTS_RTL;
        activity.getApplicationInfo().targetSdkVersion = 28;
        slider.setPadding(12,0,12,0); slider.layout(0,0,320,60);
        assertEquals(19,slider.getMax());
        for(int count=1;count<=20;count++) { slider.value(count); assertEquals(count,slider.value()); }
        slider.value(-3); assertEquals(1,slider.value()); slider.value(100); assertEquals(20,slider.value());
        assertEquals(12f,slider.tickX(0),.01f); assertEquals(308f,slider.tickX(19),.01f);
        for(int i=1;i<20;i++) assertTrue(slider.tickX(i)>slider.tickX(i-1));
        slider.setLayoutDirection(android.view.View.LAYOUT_DIRECTION_RTL);
        assertEquals(308f,slider.tickX(0),.01f); assertEquals(12f,slider.tickX(19),.01f);
    }
}
