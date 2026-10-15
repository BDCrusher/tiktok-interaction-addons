package app.soundaddon.runtime;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import app.bdcrusher.runtime.SettingsHub;
import app.morphe.extension.shared.settings.HushfeedPause;
import app.morphe.extension.tiktok.settings.preference.SectionHeadingPreference;
import app.morphe.extension.tiktok.settings.preference.SettingsHeaderPreference;
import app.morphe.extension.tiktok.settings.preference.SettingsMenuPreference;
import app.morphe.extension.tiktok.settings.preference.SettingsUi;

@SuppressWarnings("deprecation")
public final class SoundSettings implements SettingsHub.Feature {
    static final String KEY = SettingsHub.KEY;
    public SoundSettings() {}
    public static void install(Fragment host,Context context,PreferenceScreen screen) { SettingsHub.install(host,context,screen); }
    public static void refresh(Fragment host) { SettingsHub.refresh(host); }
    @Override public Fragment page() { return new Page(); }
    @Override public int changes(Context context) { return SoundPreferences.enabled(context)?0:1; }
    @Override public String summary() { return "Hold the music disc for TikTok and eligible music-app saving"; }
    /** Restorable full page; no crowded multi-purpose Save/Cancel dialog. */
    public static final class Page extends Fragment {
        private ScrollView scroll;
        @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
            Context context = getActivity(); SettingsUi.syncDarkMode(context);
            scroll = new ScrollView(context); scroll.setFillViewport(true); scroll.setBackgroundColor(SettingsUi.background());
            LinearLayout body = new LinearLayout(context); body.setOrientation(LinearLayout.VERTICAL);
            int pad = SettingsUi.dp(context, 16); body.setPaddingRelative(pad, 0, pad, pad * 2); scroll.addView(body);
            body.addView(SettingsHeaderPreference.createHeader(context, "Sound button", () -> getFragmentManager().popBackStack()));
            body.addView(SettingsUi.sectionTitle(context, "Long press"));
            LinearLayout row = new LinearLayout(context); row.setOrientation(LinearLayout.VERTICAL); row.setPadding(pad,pad,pad,pad);
            row.setBackground(SettingsUi.groupedRow(context, true, true));
            row.addView(SettingsUi.text(context, "Sound button actions", 17, SettingsUi.textPrimary(), 1));
            TextView summary = SettingsUi.text(context, "Hold the spinning music disc for a save menu. Ordinary taps still open the sound page.", 15, SettingsUi.textSecondary(), 0);
            row.addView(summary);
            Switch toggle = new Switch(context); toggle.setContentDescription("Sound button actions");
            toggle.setChecked(SoundPreferences.enabled(context)); SettingsUi.styleSwitch(toggle);
            toggle.setOnCheckedChangeListener((button,value) -> { SoundPreferences.enabled(context,value); refreshDiagnostics(); });
            row.addView(toggle); row.setOnClickListener(view -> toggle.setChecked(!toggle.isChecked())); body.addView(row);
            body.addView(SettingsUi.sectionTitle(context, "Availability and privacy"));
            TextView help = SettingsUi.text(context,
                (HushfeedPause.isPaused() ? "HushFeed is paused: stock gestures are used.\n\n" : "")
                + "TikTok supplies the available music apps for each sound. Only eligible destinations appear; installed apps and search results are not used as substitutes. If none are eligible, only TikTok saving appears.\n\n"
                + "When feed data is incomplete, opening this menu checks TikTok's existing sound-detail service (up to four seconds). Destinations can appear after loading. Only the exact sound or TikTok's explicitly matched song is accepted. Leaving the menu cancels the check.\n\n"
                + "Drag onto a row and release, or release on the disc and tap a row. Releasing outside a row never saves. Hidden music discs stay hidden.\n\n"
                + "The visible disc owns its touch area before feed speed-up gestures. Holding elsewhere keeps HushFeed's usual behavior.\n\n"
                + "TikTok handles saving, authorization and provider linking. The add-on never reads or logs credentials. These settings are separate from HushFeed backup/reset.\n\n"
                + "Experimental beta: phone validation is still required, including account linking, drag gestures and accessibility.", 15, SettingsUi.textSecondary(), 0);
            help.setPadding(pad,pad,pad,pad); body.addView(help);
            body.addView(SettingsUi.sectionTitle(context, "Gesture status"));
            TextView diagnostics = SettingsUi.text(context, SoundRuntime.diagnosticStatus(), 14, SettingsUi.textSecondary(), 0);
            diagnostics.setTag("sound_actions_addon.diagnostics"); diagnostics.setPadding(pad,pad,pad,pad);
            body.addView(diagnostics);
            body.addView(SettingsUi.sectionTitle(context, "Add-on"));
            TextView reset = SettingsUi.text(context, "Reset sound settings", 17, SettingsUi.textPrimary(), 1);
            reset.setPadding(pad,pad,pad,pad); reset.setBackground(SettingsUi.groupedRow(context,true,true));
            reset.setOnClickListener(view -> { SoundPreferences.reset(context); toggle.setChecked(true); }); body.addView(reset);
            TextView about = SettingsUi.text(context, "Sound button actions · 0.5.1-beta.1\nGPL-3.0 · Morphe template and HushFeed UI integration. HushFeed is an external requirement, not bundled.", 14, SettingsUi.textSecondary(), 0);
            about.setPadding(pad,pad,pad,pad); body.addView(about);
            if(saved != null) scroll.post(() -> { if(scroll != null) scroll.scrollTo(0,saved.getInt("scrollY")); });
            return scroll;
        }
        @Override public void onSaveInstanceState(Bundle state) { super.onSaveInstanceState(state); if(scroll != null) state.putInt("scrollY",scroll.getScrollY()); }
        private void refreshDiagnostics() {
            if(scroll == null) return;
            TextView diagnostics = scroll.findViewWithTag("sound_actions_addon.diagnostics");
            if(diagnostics != null) diagnostics.setText(SoundRuntime.diagnosticStatus());
        }
        @Override public void onResume() { super.onResume(); refreshDiagnostics(); }
        @Override public void onDestroyView() { scroll = null; super.onDestroyView(); }
    }
}
