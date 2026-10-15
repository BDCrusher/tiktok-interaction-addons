package app.collectionsaddon.runtime;
/** The patch adds TikTok's Rx consumer interface; no host interface ships in this bundle. */
public final class NativeCallback {
    private final CollectionsRuntime.Session session;
    private final boolean error;
    public NativeCallback(CollectionsRuntime.Session session, boolean error) { this.session = session; this.error = error; }
    public void accept(Object response) { CollectionsRuntime.page(session, response, error); }
}
