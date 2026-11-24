package app.bdcrusher.runtime;

import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import app.morphe.extension.tiktok.settings.SystemBarInsets;
import app.morphe.extension.tiktok.settings.preference.SectionHeadingPreference;
import app.morphe.extension.tiktok.settings.preference.SettingsHeaderPreference;
import app.morphe.extension.tiktok.settings.preference.SettingsListAdapter;
import app.morphe.extension.tiktok.settings.preference.SettingsMenuPreference;
import app.morphe.extension.tiktok.settings.preference.SettingsUi;
import java.util.ArrayList;
import java.util.List;

/** Hosts add-on controls in the same pinned bar and preference list as HushFeed. */
@SuppressWarnings("deprecation")
public abstract class SettingsPage extends PreferenceFragment {
    private SettingsListAdapter styledAdapter;

    protected abstract String pageTitle();
    protected abstract void addControls(Context context, PreferenceScreen screen);

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        Context context = getActivity();
        SettingsUi.syncDarkMode(context);
        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(context);
        addControls(context, screen);
        setPreferenceScreen(screen);
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
        Context context = getActivity();
        SettingsUi.syncDarkMode(context);
        View rows = super.onCreateView(inflater, container, state);
        LinearLayout page = new LinearLayout(context);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setBackgroundColor(SettingsUi.background());
        FrameLayout bar = new FrameLayout(context);
        bar.setTag("hushfeed_page_header");
        Preference header = SettingsHeaderPreference.section(context, pageTitle(),
            () -> getFragmentManager().popBackStack());
        bar.addView(header.getView(null, bar), new FrameLayout.LayoutParams(-1, -2));
        page.addView(bar, new LinearLayout.LayoutParams(-1, -2));
        page.addView(rows, new LinearLayout.LayoutParams(-1, 0, 1f));
        return page;
    }

    @Override public void onViewCreated(View view, Bundle state) {
        super.onViewCreated(view, state);
        ListView list = view.findViewById(android.R.id.list);
        SettingsUi.styleScrollableList(list);
        list.setItemsCanFocus(true);
        list.setBackgroundColor(SettingsUi.background());
        list.setCacheColorHint(SettingsUi.background());
        list.setDivider(null);
        list.setDividerHeight(0);
        int bottom = SettingsUi.dp(getActivity(), 24);
        list.setPadding(0, 0, 0, bottom);
        SystemBarInsets.padUnderBottomBar(list, bottom);
        list.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
    }

    @Override public void onActivityCreated(Bundle state) {
        super.onActivityCreated(state);
        ListView list = getView().findViewById(android.R.id.list);
        PreferenceScreen screen = getPreferenceScreen();
        List<Preference> grouped = new ArrayList<>();
        for (int index = 0; index < screen.getPreferenceCount(); index++) {
            Preference row = screen.getPreference(index);
            if (!(row instanceof SectionHeadingPreference)) grouped.add(row);
        }
        styledAdapter = new SettingsListAdapter(list.getAdapter(), grouped);
        list.setAdapter(styledAdapter);
    }

    @Override public void onDestroyView() {
        if (styledAdapter != null) {
            styledAdapter.dispose();
            styledAdapter = null;
        }
        super.onDestroyView();
    }

    protected static void heading(PreferenceScreen screen, String title) {
        screen.addPreference(SectionHeadingPreference.compact(screen.getContext(), title));
    }

    protected static Preference row(Context context, String key, String title, String summary,
                                    boolean selectable) {
        Preference row = new Preference(context);
        row.setKey(key);
        row.setTitle(title);
        row.setSummary(summary);
        row.setSelectable(selectable);
        row.setPersistent(false);
        return row;
    }

    protected static SwitchPreference toggle(Context context, String key, String title, String summary,
                                             boolean enabled, SettingsMenuPreference.Icon icon) {
        SwitchPreference toggle = new SwitchPreference(context);
        toggle.setKey(key);
        toggle.setTitle(title);
        toggle.setSummary(summary);
        toggle.setIcon(SettingsMenuPreference.lineIconDrawable(context, icon));
        toggle.setPersistent(false);
        toggle.setChecked(enabled);
        return toggle;
    }
}
