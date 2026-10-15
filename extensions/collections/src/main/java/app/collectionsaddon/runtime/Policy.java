package app.collectionsaddon.runtime;

/** Pure rules kept separate from Android views and the host bridge. */
public final class Policy {
    public static final int DEFAULT_COUNT = 5;
    public static int clampCount(int count) { return Math.max(1, Math.min(20, count)); }
    public static boolean needsMore(int loaded, int count, boolean hasMore) {
        return loaded > clampCount(count) || hasMore;
    }
    public static int maxHeight(int usableHeight) { return Math.max(1, (int) (usableHeight * .6f)); }
    private Policy() {}
}
