package app.collectionsaddon.runtime;
/** Test-only optional adapter: never included in the runtime payload. */
public class SettingsEntry implements app.bdcrusher.runtime.SettingsHub.Feature {
    public android.app.Fragment page() { return new app.bdcrusher.runtime.SettingsHubTest.ChildPage(); }
    public int changes(android.content.Context context) { return context.getSharedPreferences("hub-test",0).getInt("collection",0); }
    public String summary() { return "Collection test module"; }
}
