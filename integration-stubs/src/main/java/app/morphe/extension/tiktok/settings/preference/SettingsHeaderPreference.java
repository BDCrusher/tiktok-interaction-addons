package app.morphe.extension.tiktok.settings.preference;
/** Compile-only contract and minimal test stand-in, not a copy of HushFeed's UI. */
public final class SettingsHeaderPreference {
    public static android.widget.LinearLayout createHeader(android.content.Context context, String title, Runnable back) {
        android.widget.LinearLayout header = new android.widget.LinearLayout(context);
        header.setTag("hushfeed_page_header");
        android.widget.TextView button = new android.widget.TextView(context);
        button.setText(title); button.setContentDescription("Back"); button.setOnClickListener(v -> back.run());
        header.addView(button); return header;
    }
}
