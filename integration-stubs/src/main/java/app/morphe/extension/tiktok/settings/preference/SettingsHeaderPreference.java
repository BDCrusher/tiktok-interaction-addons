package app.morphe.extension.tiktok.settings.preference;
/** Compile-only contract and minimal test stand-in, not a copy of HushFeed's UI. */
@SuppressWarnings("deprecation")
public final class SettingsHeaderPreference extends android.preference.Preference {
    private final String title;
    private final Runnable back;

    private SettingsHeaderPreference(android.content.Context context, String title, Runnable back) {
        super(context);
        this.title = title;
        this.back = back;
    }
    public static SettingsHeaderPreference section(android.content.Context context, String title, Runnable back) {
        return new SettingsHeaderPreference(context, title, back);
    }
    @Override protected android.view.View onCreateView(android.view.ViewGroup parent) {
        return createHeader(getContext(), title, back);
    }
    public static android.widget.LinearLayout createHeader(android.content.Context context, String title, Runnable back) {
        android.widget.LinearLayout header = new android.widget.LinearLayout(context);
        header.setTag("hushfeed_page_header");
        android.widget.TextView button = new android.widget.TextView(context);
        button.setText(title); button.setContentDescription("Back"); button.setOnClickListener(v -> back.run());
        header.addView(button); return header;
    }
}
