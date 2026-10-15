package app.bdcrusher.runtime;

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
import android.widget.TextView;
import android.widget.Toast;
import app.morphe.extension.tiktok.settings.L10n;
import app.morphe.extension.tiktok.settings.preference.SectionHeadingPreference;
import app.morphe.extension.tiktok.settings.preference.SettingsHeaderPreference;
import app.morphe.extension.tiktok.settings.preference.SettingsMenuPreference;
import app.morphe.extension.tiktok.settings.preference.SettingsUi;
import java.util.ArrayList;
import java.util.List;

/** Shared navigation only. Feature engines/preferences remain in their independent payloads. */
@SuppressWarnings("deprecation")
public final class SettingsHub {
    public static final String KEY = "bdcrusher.addons.settings";
    public static final String TITLE = "BDCrusher's Settings";
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
        if (screen.findPreference(KEY) != null) return;
        Preference entry = new SettingsMenuPreference(context, TITLE,
            summary(context), SettingsMenuPreference.Icon.LAYOUT,
            pref -> { navigate(host, new Page(), KEY); return true; });
        entry.setKey(KEY);
        ((SettingsMenuPreference)entry).setCompact(true);
        List<Preference> rows = new ArrayList<>();
        int insertion=-1, lastFeed=-1, hushHeading=-1;
        for(int i=0;i<screen.getPreferenceCount();i++) {
            Preference row=screen.getPreference(i); rows.add(row);
            String key=row.getKey();
            // 0.74 uses flat section_* routes, not the retired Customize/hub_* tree.
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
    }
    public static void refresh(Fragment host) {
        if(!(host instanceof PreferenceFragment) || host.getActivity()==null) return;
        Preference entry=((PreferenceFragment)host).findPreference(KEY);
        if(entry instanceof SettingsMenuPreference) entry.setSummary(summary(host.getActivity()));
    }
    private static String summary(Context context) {
        int changes=changedCount(context);
        return "Collection and sound add-ons · "+(changes==0 ? "personal settings" : changes+" changed");
    }
    private static void navigate(Fragment host, Fragment page, String key) {
        if(host==null || host.getActivity()==null) return;
        if(host.getId()==0 || host.getFragmentManager()==null || host.getActivity().isFinishing()) {
            Toast.makeText(host.getActivity(),"Reopen HushFeed settings to open "+TITLE,Toast.LENGTH_SHORT).show(); return;
        }
        host.getFragmentManager().beginTransaction().setTransition(android.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .replace(host.getId(),page).addToBackStack(key).commit();
    }
    public static final class Page extends Fragment {
        private ScrollView scroll;
        private List<Slot> slots;
        @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
            Context context=getActivity(); SettingsUi.syncDarkMode(context);
            scroll=new ScrollView(context); scroll.setFillViewport(true); scroll.setBackgroundColor(SettingsUi.background());
            LinearLayout body=new LinearLayout(context); body.setOrientation(LinearLayout.VERTICAL);
            int pad=SettingsUi.dp(context,16); body.setPaddingRelative(pad,0,pad,pad*2); scroll.addView(body);
            body.addView(SettingsHeaderPreference.createHeader(context,TITLE,()->getFragmentManager().popBackStack()));
            TextView intro=SettingsUi.text(context,"Your installed collection and sound add-ons. HushFeed's own settings stay separate.",15,SettingsUi.textSecondary(),0);
            intro.setPadding(pad,pad,pad,pad); body.addView(intro);
            body.addView(SettingsUi.sectionTitle(context,"Installed add-ons"));
            slots=features();
            for(Slot slot:slots) {
                LinearLayout row=new LinearLayout(context); row.setOrientation(LinearLayout.VERTICAL);
                row.setTag(slot.key); row.setPadding(pad,pad,pad,pad); row.setMinimumHeight(SettingsUi.dp(context,48));
                row.setBackground(SettingsUi.groupedRow(context,true,true));
                row.addView(SettingsUi.text(context,slot.title+"  ›",18,SettingsUi.textPrimary(),1));
                TextView summary=SettingsUi.text(context,"",15,SettingsUi.textSecondary(),0);
                summary.setTag(slot.key+".summary"); row.addView(summary);
                row.setEnabled(slot.feature!=null); row.setFocusable(true);
                row.setOnClickListener(view->{ if(slot.feature!=null) navigate(this,slot.feature.page(),slot.key); });
                body.addView(row,new LinearLayout.LayoutParams(-1,-2));
                View gap=new View(context); body.addView(gap,new LinearLayout.LayoutParams(1,SettingsUi.dp(context,12)));
            }
            if(slots.isEmpty()) body.addView(SettingsUi.text(context,"No add-on modules found. Repatch with Enhanced collection popup or Sound button actions selected.",15,SettingsUi.textSecondary(),0));
            body.addView(SettingsUi.text(context,"BDCrusher add-ons · 0.5.1-beta.1\nGPL-3.0 · Morphe and HushFeed UI credits. Preferences and reset remain separate for each feature.",14,SettingsUi.textSecondary(),0));
            update();
            if(saved!=null) scroll.post(()->{ if(scroll!=null) scroll.scrollTo(0,saved.getInt("scrollY")); });
            return scroll;
        }
        private void update() {
            if(scroll==null || slots==null || getActivity()==null) return;
            for(Slot slot:slots) {
                TextView summary=scroll.findViewWithTag(slot.key+".summary");
                String text=slot.feature==null ? "Module unavailable. Repatch using one current add-on bundle."
                    : slot.feature.summary()+"\n"+slot.feature.changes(getActivity())+" changed";
                summary.setText(text);
                scroll.<View>findViewWithTag(slot.key).setContentDescription(slot.title+". "+text);
            }
        }
        @Override public void onResume() { super.onResume(); update(); }
        @Override public void onSaveInstanceState(Bundle state) { super.onSaveInstanceState(state); if(scroll!=null) state.putInt("scrollY",scroll.getScrollY()); }
        @Override public void onDestroyView() { scroll=null; slots=null; super.onDestroyView(); }
    }
    private SettingsHub() {}
}
