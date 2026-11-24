package app.collectionsaddon.runtime;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import app.bdcrusher.runtime.PopupStyle;
import app.bdcrusher.runtime.SettingsPage;
import app.morphe.extension.shared.settings.HushfeedPause;
import app.morphe.extension.tiktok.settings.preference.SettingsMenuPreference;
import app.morphe.extension.tiktok.settings.preference.SettingsUi;

/** Uses the settings host's row renderer with an inline collection slider. */
@SuppressWarnings("deprecation")
public final class CollectionSettingsFragment extends SettingsPage {
    static final String DIALOG_TAG = "collections_addon.dialog";
    private SwitchPreference toggle;
    private CountPreference count;
    private Preference status;

    @Override protected String pageTitle() { return "Collection popup"; }

    @Override protected void addControls(Context context, PreferenceScreen screen) {
        heading(screen, "Popup");
        toggle = toggle(context, "collections_addon.enabled", "Enhanced collection popup",
            "Show collections when you hold Favorites.", Preferences.enabled(context),
            SettingsMenuPreference.Icon.LAYOUT);
        count = new CountPreference(context);
        count.setEnabled(toggle.isChecked());
        toggle.setOnPreferenceChangeListener((preference, value) -> {
            boolean enabled = (Boolean) value;
            Preferences.save(context, enabled, Preferences.count(context));
            count.setEnabled(enabled);
            return true;
        });
        screen.addPreference(toggle);
        screen.addPreference(count);

        heading(screen, "How it works");
        status = row(context, "collections_addon.help", "Favorites gestures", "", false);
        screen.addPreference(status);

        heading(screen, "Add-on");
        Preference reset = row(context, "collections_addon.reset", "Reset collection settings",
            "Restore the default of five collections and turn the popup on.", true);
        reset.setOnPreferenceClickListener(preference -> { showDialog("reset"); return true; });
        screen.addPreference(reset);
        Preference about = row(context, "collections_addon.about", "About and licenses",
            "TikTok Interaction Add-ons · 0.10.0-beta.1", true);
        about.setOnPreferenceClickListener(preference -> { showDialog("about"); return true; });
        screen.addPreference(about);
        refresh();
    }

    @Override public void onResume() { super.onResume(); refresh(); }

    void refresh() {
        Context context = getActivity();
        if (context == null || count == null) return;
        boolean enabled = Preferences.enabled(context);
        toggle.setChecked(enabled);
        count.setEnabled(enabled);
        count.refresh();
        status.setSummary((HushfeedPause.isPaused() ? "HushFeed is paused. TikTok’s stock gestures are active.\n\n" : "")
            + "Tap Favorites to add or remove a favorite. Hold it to choose a collection. "
            + "More opens TikTok’s full collection list. "
            + "Turn off Favorites hold-speed remapping to use the collection gesture.");
    }

    private void showDialog(String kind) {
        if (getFragmentManager().findFragmentByTag(DIALOG_TAG) != null) return;
        OptionsDialog dialog = new OptionsDialog();
        Bundle args = new Bundle();
        args.putString("kind", kind);
        dialog.setArguments(args);
        dialog.setTargetFragment(this, 0);
        dialog.show(getFragmentManager(), DIALOG_TAG);
    }

    private static final class CountPreference extends Preference {
        CountPreference(Context context) {
            super(context);
            setKey("collections_addon.count");
            setTitle("Collections before More");
            setSelectable(false);
            setPersistent(false);
        }

        @Override protected View onCreateView(ViewGroup parent) {
            Context context = getContext();
            LinearLayout row = new LinearLayout(context);
            row.setOrientation(LinearLayout.VERTICAL);
            TextView title = SettingsUi.text(context, "", 16, SettingsUi.textPrimary(), Typeface.NORMAL);
            title.setId(android.R.id.title);
            row.addView(title);
            TextView summary = SettingsUi.text(context, "", 13, SettingsUi.textSecondary(), Typeface.NORMAL);
            summary.setId(android.R.id.summary);
            row.addView(summary);
            row.addView(new CountSlider(context), new LinearLayout.LayoutParams(-1, -2));
            return row;
        }

        @Override protected void onBindView(View view) {
            super.onBindView(view);
            Context context = getContext();
            TextView summary = view.findViewById(android.R.id.summary);
            CountSlider slider = view.findViewWithTag("collections_addon.count.slider");
            slider.setOnSeekBarChangeListener(null);
            slider.value(Preferences.count(context));
            slider.setEnabled(isEnabled());
            updateValue(summary, slider);
            slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override public void onProgressChanged(SeekBar control, int progress, boolean fromUser) {
                    int value = progress + 1;
                    if (fromUser && value != Preferences.count(context)) {
                        Preferences.save(context, Preferences.enabled(context), value);
                        PopupStyle.haptic(control);
                    }
                    updateValue(summary, slider);
                }
                @Override public void onStartTrackingTouch(SeekBar control) {}
                @Override public void onStopTrackingTouch(SeekBar control) {}
            });
        }

        private static void updateValue(TextView summary, CountSlider slider) {
            int value = slider.value();
            summary.setText("Current: " + value + " · Choose 1–20");
            summary.setVisibility(View.VISIBLE);
            slider.setContentDescription("Collections before More: " + value + ", from 1 to 20");
        }

        void refresh() { notifyChanged(); }
    }

    /** Restorable child dialogs, rather than an untracked window attached to the old Activity. */
    public static final class OptionsDialog extends DialogFragment {
        private CountSlider slider;
        @Override public Dialog onCreateDialog(Bundle saved) {
            Context context = getActivity(); SettingsUi.syncDarkMode(context);
            String kind = getArguments().getString("kind");
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            if ("count".equals(kind)) {
                slider = new CountSlider(context);
                // Accept the old string draft when restoring across an app update.
                int draft = Preferences.count(context);
                if (saved != null) {
                    try { draft = Integer.parseInt(saved.getString("draft", Integer.toString(draft))); }
                    catch (NumberFormatException ignored) { /* Keep the saved preference. */ }
                }
                slider.value(draft);
                TextView selected = SettingsUi.text(context, "", 24, SettingsUi.textPrimary(), Typeface.BOLD);
                selected.setTag("collections_addon.count.value");
                selected.setGravity(Gravity.CENTER);
                Runnable update = () -> {
                    selected.setText(Integer.toString(slider.value()));
                    slider.setContentDescription("Collections before More: " + slider.value() + ", from 1 to 20");
                };
                slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    public void onProgressChanged(SeekBar control, int progress, boolean fromUser) { update.run(); }
                    public void onStartTrackingTouch(SeekBar control) {}
                    public void onStopTrackingTouch(SeekBar control) {}
                });
                update.run();
                LinearLayout body = new LinearLayout(context); body.setOrientation(LinearLayout.VERTICAL);
                int pad = SettingsUi.dp(context, 20); body.setPadding(pad, pad, pad, pad);
                body.addView(selected); body.addView(slider, new LinearLayout.LayoutParams(-1, -2));
                LinearLayout ends = new LinearLayout(context);
                TextView one = SettingsUi.text(context, "1", 14, SettingsUi.textSecondary(), Typeface.NORMAL);
                TextView twenty = SettingsUi.text(context, "20", 14, SettingsUi.textSecondary(), Typeface.NORMAL);
                twenty.setGravity(Gravity.END);
                ends.addView(one, new LinearLayout.LayoutParams(0, -2, 1));
                ends.addView(twenty, new LinearLayout.LayoutParams(0, -2, 1)); body.addView(ends);
                builder.setTitle("Collections before More").setMessage("Choose 1–20. Default: 5.")
                    .setView(body).setPositiveButton("Save", null).setNegativeButton("Cancel", null);
            } else if ("reset".equals(kind)) {
                builder.setTitle("Reset collection settings?")
                    .setMessage("Restore enabled and five collections. This does not reset HushFeed.")
                    .setPositiveButton("Reset", (d, w) -> { Preferences.reset(context); refreshPage(); })
                    .setNegativeButton("Cancel", null);
            } else {
                builder.setTitle("TikTok Interaction Add-ons").setMessage("0.10.0-beta.1 · Enhanced collection popup\n\n"
                    + "Independent add-on, not a HushFeed fork.\n\nGPL-3.0-only. Built with the Morphe patches template. Uses HushFeed’s settings hooks, header and styling helpers (GPL-3.0). Credits: MorpheApp, icysymmetra/tiktok-patches-for-morphe and SysAdminDoc/HushFeed contributors.\n\n"
                    + "Morphe trademark notice: this project is not affiliated with, sponsored or endorsed by the Morphe project. Full notices are included with the source bundle.")
                    .setPositiveButton("Close", null);
            }
            return builder.create();
        }
        @Override public void onStart() {
            super.onStart(); AlertDialog dialog = (AlertDialog) getDialog();
            SettingsUi.styleStandardAlertDialog(dialog);
            if (slider != null) dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                Preferences.save(getActivity(), Preferences.enabled(getActivity()), slider.value()); refreshPage(); dismiss();
            });
        }
        private void refreshPage() {
            Fragment page = getTargetFragment();
            if (page instanceof CollectionSettingsFragment) ((CollectionSettingsFragment) page).refresh();
        }
        @Override public void onSaveInstanceState(Bundle state) {
            super.onSaveInstanceState(state); if (slider != null) state.putString("draft", Integer.toString(slider.value()));
        }
        @Override public void onDestroyView() { slider = null; super.onDestroyView(); }
    }
}
