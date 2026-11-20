package app.bdcrusher.runtime;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.widget.Toast;
import app.morphe.extension.tiktok.settings.L10n;
import app.morphe.extension.tiktok.settings.preference.SectionHeadingPreference;
import app.morphe.extension.tiktok.settings.preference.SettingsMenuPreference;
import java.util.ArrayList;
import java.util.List;

/** Shared navigation only. Feature engines/preferences remain in their independent payloads. */
@SuppressWarnings("deprecation")
public final class SettingsHub {
    public static final String KEY = "bdcrusher.addons.settings";
    public static final String TITLE = "BDCrusher's Settings";
    private static final String EXPANDED_STATE = KEY + ".expanded";
    public interface Feature {
        Fragment page();
        int changes(Context context);
        String summary();
    }
    static final class Slot {
        final String key, title;
        final Feature feature;
        Slot(String key, String title, Feature feature) { this.key=key; this.title=title; this.feature=feature; }
    }
    static List<Slot> features() {
        List<Slot> result = new ArrayList<>();
        discover(result, "collections_addon.settings", "Collection popup", "app.collectionsaddon.runtime.SettingsEntry");
        discover(result, "sound_actions_addon.settings", "Sound button actions", "app.soundaddon.runtime.SoundSettings");
        return result;
    }
    private static void discover(List<Slot> result, String key, String title, String adapter) {
        // Only our two fixed optional adapters are inspected, never host internals or installed apps.
        try {
            Class<?> type = Class.forName(adapter, false, SettingsHub.class.getClassLoader());
            result.add(new Slot(key, title, (Feature)type.getDeclaredConstructor().newInstance()));
        } catch (ClassNotFoundException absent) {
            // Unselected feature: no row and no dependency on its classes.
        } catch (ReflectiveOperationException | LinkageError | RuntimeException broken) {
            // A present but broken adapter stays visible with a useful recovery message.
            result.add(new Slot(key, title, null));
        }
    }
    static int changedCount(Context context) {
        int changed=0;
        for (Slot slot : features()) if(slot.feature != null) changed += slot.feature.changes(context);
        return changed;
    }
    public static void install(Fragment host, Context context, PreferenceScreen screen) {
        install(host, context, screen, false);
    }
    public static void install(Fragment host, Context context, PreferenceScreen screen, boolean list) {
        if (screen.findPreference(KEY) != null) return;
        SettingsMenuPreference menu = new SettingsMenuPreference(context, TITLE,
            summary(context), SettingsMenuPreference.Icon.LAYOUT,
            pref -> {
                SettingsMenuPreference group = (SettingsMenuPreference) pref;
                group.setExpanded(!group.isExpanded());
                return true;
            });
        PreferenceGroup entry = menu;
        entry.setKey(KEY);
        menu.setCompact(true);
        menu.setExpandable(!list);
        entry.setSelectable(!list);
        List<Preference> rows = new ArrayList<>();
        int insertion=-1, lastFeed=-1, hushHeading=-1;
        for(int i=0;i<screen.getPreferenceCount();i++) {
            Preference row=screen.getPreference(i); rows.add(row);
            String key=row.getKey();
            // Use the host's section keys so placement is independent of language.
            if("section_interface".equals(key)) insertion=i+1;
            if("section_feed_filter".equals(key) || "section_feed_navigation".equals(key)
                    || "section_playback".equals(key) || "section_screen_time".equals(key)) lastFeed=i+1;
            if(row instanceof SectionHeadingPreference && row.getTitle()!=null
                    && L10n.t(context,"Hushfeed").contentEquals(row.getTitle())) hushHeading=i;
        }
        if(insertion<0) insertion=lastFeed;
        Preference personalHeading=null;
        if(insertion<0) {
            insertion=hushHeading>=0 ? hushHeading : rows.size();
            personalHeading=SectionHeadingPreference.compact(context,"Personal add-ons");
            rows.add(insertion++,personalHeading);
        }
        rows.add(insertion,entry);
        int order=0; for(Preference row:rows) if(row.getOrder()>=0) row.setOrder(order++);
        if(personalHeading!=null) screen.addPreference(personalHeading);
        screen.addPreference(entry);
        for (Slot slot : features()) {
            SettingsMenuPreference route = new SettingsMenuPreference(context, slot.title,
                featureSummary(slot, context), SettingsMenuPreference.Icon.LAYOUT, pref -> {
                    if (slot.feature != null) navigate(host, slot.feature.page(), slot.key);
                    return true;
                });
            route.setCompact(true);
            Preference child = route;
            child.setKey(slot.key);
            child.setEnabled(slot.feature != null);
            entry.addPreference(child);
        }
    }
    public static void refresh(Fragment host) {
        SettingsMenuPreference menu = menu(host);
        Context context = host.getActivity();
        if (menu == null || context == null) return;
        PreferenceGroup entry = menu;
        entry.setSummary(summary(context));
        for (Slot slot : features()) {
            Preference child = entry.findPreference(slot.key);
            if (child != null) child.setSummary(featureSummary(slot, context));
        }
    }
    public static void showAsList(Fragment host, boolean list) {
        SettingsMenuPreference menu = menu(host);
        if (menu == null) return;
        menu.setExpandable(!list);
        Preference entry = menu;
        entry.setSelectable(!list);
    }
    public static void saveExpansion(Fragment host, Bundle state) {
        SettingsMenuPreference menu = menu(host);
        if (menu != null) state.putBoolean(EXPANDED_STATE, menu.isExpanded());
    }
    public static void restoreExpansion(Fragment host, Bundle state) {
        SettingsMenuPreference menu = menu(host);
        if (menu != null && state != null) menu.setExpanded(state.getBoolean(EXPANDED_STATE));
    }
    private static SettingsMenuPreference menu(Fragment host) {
        if (!(host instanceof PreferenceFragment)) return null;
        Preference entry = ((PreferenceFragment) host).findPreference(KEY);
        return entry instanceof SettingsMenuPreference ? (SettingsMenuPreference) entry : null;
    }
    private static String featureSummary(Slot slot, Context context) {
        if (slot.feature == null) return "Module unavailable. Repatch using one current add-on bundle.";
        int changes = slot.feature.changes(context);
        return slot.feature.summary() + (changes == 0 ? "" : " · " + changes + " changed");
    }
    private static String summary(Context context) {
        int changes=changedCount(context);
        return changes == 0 ? "Personal add-on settings" : changes + " changed";
    }
    private static void navigate(Fragment host, Fragment page, String key) {
        if(host==null || host.getActivity()==null) return;
        if(host.getId()==0 || host.getFragmentManager()==null || host.getActivity().isFinishing()) {
            Toast.makeText(host.getActivity(),"Reopen HushFeed settings to open "+TITLE,Toast.LENGTH_SHORT).show(); return;
        }
        host.getFragmentManager().beginTransaction().setTransition(android.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .replace(host.getId(),page).addToBackStack(key).commit();
    }
    private SettingsHub() {}
}
