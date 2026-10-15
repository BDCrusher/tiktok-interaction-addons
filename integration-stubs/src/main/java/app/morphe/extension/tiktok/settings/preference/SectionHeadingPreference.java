package app.morphe.extension.tiktok.settings.preference;
/** Compile-only contract and test stand-in. */
@SuppressWarnings("deprecation")
public final class SectionHeadingPreference extends android.preference.Preference {
    public SectionHeadingPreference(android.content.Context context, String title, boolean homeGroup) {
        super(context); setTitle(title); setSelectable(false);
    }
    public static SectionHeadingPreference compact(android.content.Context context, String title) {
        return new SectionHeadingPreference(context,title,true);
    }
}
