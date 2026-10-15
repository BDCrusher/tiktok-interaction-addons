package app.soundaddon.runtime;
/** Test-only optional adapter: never included in the runtime payload. */
public class SoundSettings implements app.bdcrusher.runtime.SettingsHub.Feature {
    public android.app.Fragment page() { return new app.bdcrusher.runtime.SettingsHubTest.ChildPage(); }
    public int changes(android.content.Context context) { return context.getSharedPreferences("hub-test",0).getInt("sound",0); }
    public String summary() { return "Sound test module"; }
}
