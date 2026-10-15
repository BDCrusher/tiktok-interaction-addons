package app.collectionsaddon.runtime;
import android.app.Activity;
import android.content.Context;
import android.os.Looper;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.Shadows;
import java.time.Duration;
import java.util.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, manifest = Config.NONE)
public class PagerTest {
    static final class Response {
        final List<?> items; final long cursor; final boolean more;
        Response(long cursor, boolean more, String... items) { this(Arrays.asList(items), cursor, more); }
        Response(List<?> items, long cursor, boolean more) { this.items = items; this.cursor = cursor; this.more = more; }
    }
    final class Fake implements CollectionsRuntime.Access {
        NativeCallback success, error;
        Response replayed;
        int loads; boolean cancelled, stock, cleared, throwLoad;
        public Context context(Object owner) { return activity; }
        public List<?> items(Object response) { return ((Response) response).items; }
        public long cursor(Object response) { return ((Response) response).cursor; }
        public boolean more(Object response) { return ((Response) response).more; }
        public String id(Object item) { return (String) item; }
        public Object copy(Object response, List<?> items) { Response r = (Response) response; return new Response(new ArrayList<>(items), r.cursor, r.more); }
        public void replay(Object owner, Object response) { replayed = (Response) response; stock = !CollectionsRuntime.enabled(activity); }
        public Object load(Object owner, long cursor, NativeCallback success, NativeCallback error) {
            if (throwLoad) throw new IllegalStateException("Loader unavailable");
            this.success = success; this.error = error; loads++; return new Object();
        }
        public void cancel(Object subscription) { cancelled = true; }
        public void clearPending() { cleared = true; }
    }
    Activity activity;
    Fake fake;
    CollectionsRuntime.Access original;
    @Before public void setup() {
        activity = Robolectric.buildActivity(Activity.class).setup().get();
        Preferences.reset(activity);
        original = CollectionsRuntime.access; fake = new Fake(); CollectionsRuntime.access = fake;
    }
    @After public void teardown() { Shadows.shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(6)); CollectionsRuntime.access = original; }
    @Test public void fillsCountAndDeduplicatesInOrder() {
        assertTrue(CollectionsRuntime.beforeResponse(new Object(), new Response(1, true, "A", "B", "C")));
        fake.success.accept(new Response(2, true, "C", "D", "E", "F"));
        assertEquals(Arrays.asList("A", "B", "C", "D", "E", "F"), fake.replayed.items);
        assertEquals(1, fake.loads); assertTrue(fake.cancelled); assertFalse(fake.stock);
    }
    @Test public void exhaustionRendersShortList() {
        assertTrue(CollectionsRuntime.beforeResponse(new Object(), new Response(1, true, "A")));
        fake.success.accept(new Response(2, false, "B"));
        assertEquals(Arrays.asList("A", "B"), fake.replayed.items); assertFalse(fake.replayed.more);
    }
    @Test public void errorRestoresStockConsumerAndRejectsLatePage() {
        Response initial = new Response(1, true, "A");
        assertTrue(CollectionsRuntime.beforeResponse(new Object(), initial));
        NativeCallback late = fake.success;
        fake.error.accept(new IllegalStateException("test"));
        assertSame(initial, fake.replayed); assertTrue(fake.stock); assertTrue(fake.cancelled);
        late.accept(new Response(2, false, "WRONG"));
        assertSame(initial, fake.replayed);
    }
    @Test public void repeatedCursorStopsWithoutLoop() {
        assertTrue(CollectionsRuntime.beforeResponse(new Object(), new Response(1, true, "A")));
        fake.success.accept(new Response(1, true, "B"));
        assertEquals(1, fake.loads); assertTrue(fake.stock);
    }
    @Test public void timeoutReturnsOriginalStockRoute() {
        Response initial = new Response(1, true, "A");
        assertTrue(CollectionsRuntime.beforeResponse(new Object(), initial));
        Shadows.shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(6));
        assertSame(initial, fake.replayed); assertTrue(fake.stock); assertTrue(fake.cancelled);
    }
    @Test public void disabledAndAlreadySufficientSkipFetching() {
        Preferences.save(activity, false, 5);
        assertFalse(CollectionsRuntime.beforeResponse(new Object(), new Response(1, true, "A")));
        Preferences.save(activity, true, 1);
        assertFalse(CollectionsRuntime.beforeResponse(new Object(), new Response(1, true, "A", "B")));
        assertEquals(0, fake.loads);
    }
    @Test public void destroyedActivityDoesNotReceiveLatePopup() {
        assertTrue(CollectionsRuntime.beforeResponse(new Object(), new Response(1, true, "A")));
        activity.finish();
        fake.success.accept(new Response(2, false, "B"));
        assertNull(fake.replayed); assertTrue(fake.cleared); assertTrue(fake.cancelled);
    }
    @Test public void exactCountStopsLoadingButKeepsMore() {
        Response response = new Response(1, true, "A", "B", "C", "D", "E");
        assertFalse(CollectionsRuntime.beforeResponse(new Object(), response));
        assertEquals(0, fake.loads);
        assertEquals(0, CollectionsRuntime.threshold(new android.app.Dialog(activity), response.items));
    }
    @Test public void equalCachedListsDoNotShareMoreState() {
        Response paged = new Response(1, true, "A", "B", "C", "D", "E");
        Response exhausted = new Response(0, false, "A", "B", "C", "D", "E");
        assertFalse(CollectionsRuntime.beforeResponse(new Object(), paged));
        assertFalse(CollectionsRuntime.beforeResponse(new Object(), exhausted));
        android.app.Dialog dialog = new android.app.Dialog(activity);
        assertEquals(0, CollectionsRuntime.threshold(dialog, paged.items));
        assertEquals(6, CollectionsRuntime.threshold(dialog, exhausted.items));
    }
    @Test public void initialLoaderExceptionReplaysStockOnce() {
        fake.throwLoad = true;
        Response original = new Response(1, true, "A");
        assertTrue(CollectionsRuntime.beforeResponse(new Object(), original));
        assertSame(original, fake.replayed); assertTrue(fake.stock);
    }
}
