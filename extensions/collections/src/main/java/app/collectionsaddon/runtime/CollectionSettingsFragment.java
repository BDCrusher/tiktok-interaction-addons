package app.collectionsaddon.runtime;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import app.morphe.extension.shared.settings.HushfeedPause;
import app.morphe.extension.tiktok.settings.preference.SettingsHeaderPreference;
import app.morphe.extension.tiktok.settings.preference.SettingsUi;
import app.bdcrusher.runtime.PopupStyle;

/** Native page using HushFeed's public header, palette and grouped-card helpers. */
@SuppressWarnings("deprecation")
public final class CollectionSettingsFragment extends Fragment {
    static final String DIALOG_TAG = "collections_addon.dialog";
    private LinearLayout content;
    private ScrollView scroll;
    private Switch toggle;
    private TextView countSummary;
    private CountSlider countSlider;
    private View countRow;
    private TextView status;

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
        Context context = getActivity();
        SettingsUi.syncDarkMode(context);
        scroll = new ScrollView(context);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(SettingsUi.background());
        content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPaddingRelative(dp(16), 0, dp(16), dp(28));
        scroll.addView(content);
        content.addView(SettingsHeaderPreference.createHeader(context, "Collection popup",
            () -> getFragmentManager().popBackStack()));
        TextView subtitle = text("Choose how Favorites opens your collections.", 15, SettingsUi.textSecondary(), false);
        subtitle.setPaddingRelative(dp(8), dp(12), dp(8), dp(26));
        content.addView(subtitle);
        heading("Popup");
        LinearLayout enabledRow = row(true, false, "collections_addon.enabled");
        LinearLayout enabledLabels = labels("Enhanced collection popup", "Use the compact vertical menu when you hold Favorites.");
        boolean stacked = context.getResources().getConfiguration().screenWidthDp < 360
            || context.getResources().getConfiguration().fontScale > 1.3f;
        if (!stacked) {
            enabledRow.setOrientation(LinearLayout.HORIZONTAL); enabledRow.setGravity(Gravity.CENTER_VERTICAL);
            enabledRow.addView(enabledLabels, new LinearLayout.LayoutParams(0, -2, 1));
        } else enabledRow.addView(enabledLabels);
        toggle = new Switch(context);
        toggle.setContentDescription("Enhanced collection popup");
        toggle.setChecked(Preferences.enabled(context));
        SettingsUi.styleSwitch(toggle);
        LinearLayout.LayoutParams control = new LinearLayout.LayoutParams(-2, dp(48));
        control.gravity = Gravity.END;
        if (!stacked) control.setMarginStart(dp(16));
        enabledRow.addView(toggle, control);
        enabledRow.setOnClickListener(v -> toggle.setChecked(!toggle.isChecked()));
        toggle.setOnCheckedChangeListener((v, enabled) -> {
            Preferences.save(context, enabled, Preferences.count(context)); refresh();
        });
        content.addView(enabledRow);
        LinearLayout count = row(false, true, "collections_addon.count");
        count.addView(text("Collections before More", 17, SettingsUi.textPrimary(), true));
        countSummary = text("", 15, SettingsUi.textSecondary(), false);
        countSummary.setPadding(0, dp(6), 0, 0);
        count.addView(countSummary);
        countSlider = new CountSlider(context); countSlider.value(Preferences.count(context));
        count.addView(countSlider, new LinearLayout.LayoutParams(-1, -2));
        LinearLayout ends = new LinearLayout(context);
        TextView one = text("1", 14, SettingsUi.textSecondary(), false);
        TextView twenty = text("20", 14, SettingsUi.textSecondary(), false); twenty.setGravity(Gravity.END);
        ends.addView(one, new LinearLayout.LayoutParams(0, -2, 1));
        ends.addView(twenty, new LinearLayout.LayoutParams(0, -2, 1)); count.addView(ends);
        countSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar slider, int progress, boolean fromUser) {
                int value = progress + 1;
                if (fromUser && value != Preferences.count(context)) {
                    Preferences.save(context, Preferences.enabled(context), value);
                    PopupStyle.haptic(slider);
                }
                showCount(value);
            }
            public void onStartTrackingTouch(SeekBar slider) {}
            public void onStopTrackingTouch(SeekBar slider) {}
        });
        countRow = count; content.addView(count);
        heading("How it works");
        LinearLayout help = row(true, true, "collections_addon.help");
        help.setClickable(false); help.setFocusable(false);
        status = text("", 15, SettingsUi.textSecondary(), false);
        help.addView(status); content.addView(help);
        heading("Add-on");
        LinearLayout reset = row(true, false, "collections_addon.reset");
        reset.addView(labels("Reset collection settings", "Restore enabled and five collections. HushFeed settings are untouched."));
        reset.setOnClickListener(v -> showDialog("reset")); content.addView(reset);
        LinearLayout about = row(false, true, "collections_addon.about");
        about.addView(labels("About and licenses  ›", "TikTok Interaction Add-ons · 0.5.1-beta.1"));
        about.setOnClickListener(v -> showDialog("about")); content.addView(about);
        refresh();
        if (state != null) scroll.post(() -> { if (scroll != null) scroll.scrollTo(0, state.getInt("scrollY")); });
        return scroll;
    }
    @Override public void onResume() { super.onResume(); refresh(); }
    @Override public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        if (scroll != null) state.putInt("scrollY", scroll.getScrollY());
    }
    @Override public void onDestroyView() {
        content = null; scroll = null; toggle = null; countSummary = null; countSlider = null; countRow = null; status = null;
        super.onDestroyView();
    }
    void refresh() {
        if (content == null || getActivity() == null || countRow == null) return;
        boolean enabled = Preferences.enabled(getActivity());
        toggle.setChecked(enabled);
        countSlider.value(Preferences.count(getActivity())); showCount(Preferences.count(getActivity()));
        countSlider.setEnabled(enabled);
        countRow.setEnabled(enabled); countRow.setAlpha(enabled ? 1f : .55f);
        status.setText((HushfeedPause.isPaused() ? "HushFeed is paused or in safe mode. The stock popup is used; your choices are kept.\n\n" : "")
            + "A normal tap still favorites or unfavorites. More opens TikTok’s full collection list.\n\n"
            + "Colors follow TikTok and HushFeed’s popup theme. Keep Favorites hold-speed remapping off; it takes over the same gesture. Hidden save buttons stay hidden.\n\n"
            + "These choices are saved immediately, separately from HushFeed backup and reset.");
    }
    private void showCount(int value) {
        if(countSummary == null || countSlider == null) return;
        countSummary.setText("Current: " + value + " · Choose 1–20\nChanges apply the next time you open the popup.");
        countSlider.setContentDescription("Collections before More: " + value + ", from 1 to 20");
    }
    private void showDialog(String kind) {
        if (getFragmentManager().findFragmentByTag(DIALOG_TAG) != null) return;
        OptionsDialog dialog = new OptionsDialog();
        Bundle args = new Bundle(); args.putString("kind", kind); dialog.setArguments(args);
        dialog.setTargetFragment(this, 0); dialog.show(getFragmentManager(), DIALOG_TAG);
    }
    private int dp(int n) { return SettingsUi.dp(getActivity(), n); }
    private TextView text(String value, float size, int color, boolean bold) {
        return SettingsUi.text(getActivity(), value, size, color, bold ? Typeface.BOLD : Typeface.NORMAL);
    }
    private void heading(String title) {
        TextView view = SettingsUi.sectionTitle(getActivity(), title);
        view.setPaddingRelative(dp(18), dp(20), dp(18), dp(6)); content.addView(view);
    }
    private LinearLayout labels(String title, String summary) {
        LinearLayout labels = new LinearLayout(getActivity()); labels.setOrientation(LinearLayout.VERTICAL);
        labels.addView(text(title, 17, SettingsUi.textPrimary(), true));
        TextView detail = text(summary, 15, SettingsUi.textSecondary(), false);
        detail.setPadding(0, dp(6), 0, 0); labels.addView(detail); return labels;
    }
    private LinearLayout row(boolean first, boolean last, String tag) {
        LinearLayout row = new LinearLayout(getActivity()); row.setOrientation(LinearLayout.VERTICAL);
        row.setTag(tag); row.setPaddingRelative(dp(18), dp(16), dp(18), dp(16));
        row.setMinimumHeight(dp(64)); row.setBackground(SettingsUi.groupedRow(getActivity(), first, last));
        row.setFocusable(true); return row;
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
                builder.setTitle("TikTok Interaction Add-ons").setMessage("0.5.1-beta.1 · Enhanced collection popup\n\n"
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
