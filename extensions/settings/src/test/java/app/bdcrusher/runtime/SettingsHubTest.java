package app.bdcrusher.runtime;
import android.app.*;
import android.os.Bundle;
import android.preference.*;
import android.view.*;
import android.widget.TextView;
import app.morphe.extension.tiktok.settings.preference.SettingsMenuPreference;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=28,manifest=Config.NONE)
public class SettingsHubTest {
    private PreferenceScreen screen(Activity activity) {
        PreferenceFragment owner=new Host();
        activity.getFragmentManager().beginTransaction().add(android.R.id.content,owner).commit();
        activity.getFragmentManager().executePendingTransactions();
        return owner.getPreferenceManager().createPreferenceScreen(activity);
    }
    public static class Host extends PreferenceFragment {
        public void onCreate(Bundle state) {
            super.onCreate(state);
            PreferenceScreen screen=getPreferenceManager().createPreferenceScreen(getActivity());
            setPreferenceScreen(screen);
            SettingsHub.install(this,getActivity(),screen);
        }
        @Override public void onActivityCreated(Bundle state) {
            super.onActivityCreated(state);
            SettingsHub.restoreExpansion(this, state);
        }
        @Override public void onSaveInstanceState(Bundle state) {
            super.onSaveInstanceState(state);
            SettingsHub.saveExpansion(this, state);
        }
    }
    public static class ChildPage extends Fragment {
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) { return new TextView(getActivity()); }
    }
    @Test public void bothModulesHaveOneRootAndSeparatePages() {
        Activity activity=Robolectric.buildActivity(Activity.class).setup().get();
        Host host=new Host();
        activity.getFragmentManager().beginTransaction().add(android.R.id.content,host).commit();
        activity.getFragmentManager().executePendingTransactions();
        SettingsHub.install(host,activity,host.getPreferenceScreen());
        assertEquals(2,host.getPreferenceScreen().getPreferenceCount());
        assertEquals(2,SettingsHub.features().size());
        SettingsMenuPreference root=(SettingsMenuPreference) host.findPreference(SettingsHub.KEY);
        assertTrue(root.isExpandable());
        assertFalse(root.isExpanded());
        root.getOnPreferenceClickListener().onPreferenceClick(root);
        assertTrue(root.isExpanded());
        assertEquals(0, activity.getFragmentManager().getBackStackEntryCount());
        assertEquals(2, root.getPreferenceCount());
        Preference sound=root.findPreference("sound_actions_addon.settings");
        sound.getOnPreferenceClickListener().onPreferenceClick(sound);
        activity.getFragmentManager().executePendingTransactions();
        assertTrue(activity.getFragmentManager().findFragmentById(android.R.id.content) instanceof ChildPage);
        activity.getFragmentManager().popBackStackImmediate();
        assertSame(host,activity.getFragmentManager().findFragmentById(android.R.id.content));
        assertTrue(root.isExpanded());
        Preference collection=root.findPreference("collections_addon.settings");
        collection.getOnPreferenceClickListener().onPreferenceClick(collection);
        activity.getFragmentManager().executePendingTransactions();
        assertTrue(activity.getFragmentManager().findFragmentById(android.R.id.content) instanceof ChildPage);
    }
    @Test public void restoredSectionKeepsExpansionAndModuleSummaries() {
        org.robolectric.android.controller.ActivityController<Activity> controller=Robolectric.buildActivity(Activity.class).setup();
        Activity activity=controller.get();
        activity.getSharedPreferences("hub-test",0).edit().putInt("collection",2).putInt("sound",1).commit();
        Host host = new Host();
        activity.getFragmentManager().beginTransaction().add(android.R.id.content,host,"hub").commit();
        activity.getFragmentManager().executePendingTransactions();
        SettingsMenuPreference menu = (SettingsMenuPreference) host.findPreference(SettingsHub.KEY);
        menu.setExpanded(true);
        assertEquals(3,SettingsHub.changedCount(activity));
        controller.recreate();
        activity=controller.get();
        host=(Host) activity.getFragmentManager().findFragmentByTag("hub");
        menu=(SettingsMenuPreference) host.findPreference(SettingsHub.KEY);
        assertTrue(menu.isExpanded());
        assertNotNull(menu.findPreference("sound_actions_addon.settings"));
        assertTrue(menu.findPreference("collections_addon.settings").getSummary().toString().contains("2 changed"));
        assertEquals(2,activity.getSharedPreferences("hub-test",0).getInt("collection",0));
    }
    @Test public void listViewKeepsChildrenVisibleAndReturnsToCollapsibleSections() {
        Activity activity=Robolectric.buildActivity(Activity.class).setup().get();
        Host host=new Host();
        activity.getFragmentManager().beginTransaction().add(android.R.id.content,host).commit();
        activity.getFragmentManager().executePendingTransactions();
        PreferenceScreen screen=host.getPreferenceManager().createPreferenceScreen(activity);
        host.setPreferenceScreen(screen);
        SettingsHub.install(host, activity, screen, true);
        SettingsMenuPreference menu=(SettingsMenuPreference) host.findPreference(SettingsHub.KEY);
        assertFalse(menu.isExpandable());
        assertFalse(menu.isSelectable());
        assertEquals(4, screen.getRootAdapter().getCount());
        SettingsHub.showAsList(host, false);
        org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();
        assertTrue(menu.isExpandable());
        assertTrue(menu.isSelectable());
        assertEquals(2, screen.getRootAdapter().getCount());
        menu.getOnPreferenceClickListener().onPreferenceClick(menu);
        org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();
        assertEquals(4, screen.getRootAdapter().getCount());
        menu.getOnPreferenceClickListener().onPreferenceClick(menu);
        org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();
        assertEquals(2, screen.getRootAdapter().getCount());
    }
    @Test public void flatHomeInsertsAfterInterfaceWithoutOldHeadingsOrDuplicates() {
        Activity activity=Robolectric.buildActivity(Activity.class).setup().get();
        PreferenceScreen screen=screen(activity);
        Preference header=new Preference(activity); header.setKey("header"); header.setOrder(-1000); screen.addPreference(header);
        Preference feed=new Preference(activity); feed.setKey("section_interface"); feed.setTitle("Translated feed"); screen.addPreference(feed);
        Preference playback=new Preference(activity); playback.setKey("section_playback"); playback.setTitle("Playback"); screen.addPreference(playback);
        Preference app=new Preference(activity); app.setKey("section_behavior"); app.setTitle("App"); screen.addPreference(app);
        SettingsHub.install(new Fragment(),activity,screen);
        SettingsHub.install(new Fragment(),activity,screen);
        assertEquals(5,screen.getPreferenceCount());
        assertSame(header,screen.getPreference(0)); assertSame(feed,screen.getPreference(1));
        assertEquals(SettingsHub.KEY,screen.getPreference(2).getKey());
        assertSame(playback,screen.getPreference(3)); assertSame(app,screen.getPreference(4));
        for(int i=0;i<screen.getPreferenceCount();i++) assertFalse(screen.getPreference(i) instanceof app.morphe.extension.tiktok.settings.preference.SectionHeadingPreference);
    }
    @Test public void settingsOnlyHomeAddsPersonalHeadingBeforeHushfeedAndPreservesPreferences() {
        Activity activity=Robolectric.buildActivity(Activity.class).setup().get();
        activity.getSharedPreferences("collections_addon_v1",0).edit().putInt("count",12).commit();
        PreferenceScreen screen=screen(activity);
        Preference backup=new Preference(activity); backup.setKey("section_backup"); screen.addPreference(backup);
        Preference heading=app.morphe.extension.tiktok.settings.preference.SectionHeadingPreference.compact(activity,"Hushfeed"); screen.addPreference(heading);
        Preference about=new Preference(activity); about.setKey("hub_about"); screen.addPreference(about);
        SettingsHub.install(new Fragment(),activity,screen);
        assertSame(backup,screen.getPreference(0));
        assertEquals("Personal add-ons",screen.getPreference(1).getTitle());
        assertEquals(SettingsHub.KEY,screen.getPreference(2).getKey());
        assertSame(heading,screen.getPreference(3)); assertSame(about,screen.getPreference(4));
        assertEquals(12,activity.getSharedPreferences("collections_addon_v1",0).getInt("count",0));
    }
    @Test public void missingInterfaceFallsBackToLastFeedRouteNotLocalizedHeading() {
        Activity activity=Robolectric.buildActivity(Activity.class).setup().get(); PreferenceScreen screen=screen(activity);
        Preference title=new app.morphe.extension.tiktok.settings.preference.SectionHeadingPreference(activity,"Votre fil",true); screen.addPreference(title);
        Preference playback=new Preference(activity); playback.setKey("section_playback"); screen.addPreference(playback);
        Preference next=new app.morphe.extension.tiktok.settings.preference.SectionHeadingPreference(activity,"Conversations",true); screen.addPreference(next);
        SettingsHub.install(new Fragment(),activity,screen);
        assertEquals(4,screen.getPreferenceCount()); assertSame(title,screen.getPreference(0)); assertSame(playback,screen.getPreference(1));
        assertEquals(SettingsHub.KEY,screen.getPreference(2).getKey()); assertSame(next,screen.getPreference(3));
    }
    @Test public void rootSummaryRefreshUsesPreferencesNotRemovedBadgeApi() {
        Activity activity=Robolectric.buildActivity(Activity.class).setup().get(); Host host=new Host();
        activity.getFragmentManager().beginTransaction().add(android.R.id.content,host).commit(); activity.getFragmentManager().executePendingTransactions();
        Preference root=host.findPreference(SettingsHub.KEY); assertFalse(root.getSummary().toString().contains("changed"));
        activity.getSharedPreferences("hub-test",0).edit().putInt("collection",2).putInt("sound",1).commit();
        SettingsHub.refresh(host); assertTrue(root.getSummary().toString().contains("3 changed"));
        SettingsHub.install(host,activity,host.getPreferenceScreen()); assertEquals(2,host.getPreferenceScreen().getPreferenceCount());
    }
    @Test public void compileOnlyMenuContractHasFiveArgumentsAndNoBadgeSetter() throws Exception {
        Class<?> type=app.morphe.extension.tiktok.settings.preference.SettingsMenuPreference.class;
        assertNotNull(type.getConstructor(android.content.Context.class,String.class,String.class,
            app.morphe.extension.tiktok.settings.preference.SettingsMenuPreference.Icon.class,Preference.OnPreferenceClickListener.class));
        for(java.lang.reflect.Method method:type.getMethods()) assertNotEquals("setActiveCount",method.getName());
        assertEquals(1,type.getConstructors().length);
    }
}
