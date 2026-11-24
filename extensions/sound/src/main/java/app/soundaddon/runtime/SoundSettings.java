package app.soundaddon.runtime;

import android.app.Fragment;
import android.content.Context;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import app.bdcrusher.runtime.SettingsHub;
import app.bdcrusher.runtime.SettingsPage;
import app.morphe.extension.shared.settings.HushfeedPause;
import app.morphe.extension.tiktok.settings.preference.SettingsMenuPreference;

@SuppressWarnings("deprecation")
public final class SoundSettings implements SettingsHub.Feature {
    static final String KEY = SettingsHub.KEY;
    public SoundSettings() {}
    public static void install(Fragment host, Context context, PreferenceScreen screen) {
        SettingsHub.install(host, context, screen);
    }
    public static void refresh(Fragment host) { SettingsHub.refresh(host); }
    @Override public Fragment page() { return new Page(); }
    @Override public int changes(Context context) { return SoundPreferences.enabled(context) ? 0 : 1; }
    @Override public String summary() { return "Hold the music disc to save a sound in TikTok"; }

    public static final class Page extends SettingsPage {
        private SwitchPreference toggle;
        private Preference help;
        private Preference diagnostics;

        @Override protected String pageTitle() { return "Sound button actions"; }

        @Override protected void addControls(Context context, PreferenceScreen screen) {
            heading(screen, "Long press");
            toggle = toggle(context, "sound_actions_addon.enabled", "Sound button actions",
                "Hold the record to open Save sound to TikTok.", SoundPreferences.enabled(context),
                SettingsMenuPreference.Icon.PLAYBACK);
            toggle.setOnPreferenceChangeListener((preference, value) -> {
                SoundPreferences.enabled(context, (Boolean) value);
                refreshStatus();
                return true;
            });
            screen.addPreference(toggle);

            heading(screen, "How it works");
            help = row(context, "sound_actions_addon.help", "Record gestures", "", false);
            screen.addPreference(help);
            diagnostics = row(context, "sound_actions_addon.diagnostics", "Gesture status", "", false);
            screen.addPreference(diagnostics);

            heading(screen, "Add-on");
            Preference reset = row(context, "sound_actions_addon.reset", "Reset sound settings",
                "Turn sound button actions back on.", true);
            reset.setOnPreferenceClickListener(preference -> {
                SoundPreferences.reset(context);
                toggle.setChecked(true);
                refreshStatus();
                return true;
            });
            screen.addPreference(reset);
            screen.addPreference(row(context, "sound_actions_addon.about", "Sound button actions · 0.10.0-beta.1",
                "GPL-3.0 · Morphe template and HushFeed settings integration", false));
            refreshStatus();
        }

        private void refreshStatus() {
            if (help == null) return;
            help.setSummary((HushfeedPause.isPaused() ? "HushFeed is paused. TikTok’s stock gestures are active.\n\n" : "")
                + "Tap the record to open the sound page. Hold it, then tap or drag onto Save sound to TikTok "
                + "to add it to Favorites. Sign in to TikTok if prompted.");
            diagnostics.setSummary(SoundRuntime.diagnosticStatus());
        }

        @Override public void onResume() { super.onResume(); refreshStatus(); }
    }
}
