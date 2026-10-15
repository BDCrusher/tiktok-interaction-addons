package app.soundaddon.runtime;

import org.junit.Test;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.Assert.*;

public class SongLookupTest {
    static class Music {
        final String id,match; final boolean eligible;
        Music(String id,String match,boolean eligible) {this.id=id;this.match=match;this.eligible=eligible;}
    }
    static class Detail {
        final Music music,matched;
        Detail(Music music,Music matched) {this.music=music;this.matched=matched;}
    }
    static class Source implements SongLookup.Source {
        final Map<String,Detail> details=new HashMap<>(); final List<String> queried=new ArrayList<>();
        Future<?> last;
        public Future<?> query(String id) {
            queried.add(id); FutureTask<Detail> future=new FutureTask<>(()->details.get(id)); future.run(); last=future; return future;
        }
        public Object music(Object detail) {return detail==null?null:((Detail)detail).music;}
        public Object matchedMusic(Object detail) {return detail==null?null:((Detail)detail).matched;}
        public String id(Object music) {return ((Music)music).id;}
        public String matchedId(Object music) {return ((Music)music).match;}
        public boolean eligible(Object music) {return ((Music)music).eligible;}
    }
    static class Result implements SongLookup.Callback {
        Object music; String status; int calls;
        public void complete(Object music,String status) {this.music=music;this.status=status;calls++;}
    }
    @Test public void richerSameIdDetailIsAcceptedOnce() {
        Source source=new Source(); Music richer=new Music("clip",null,true); source.details.put("clip",new Detail(richer,null));
        Result result=new Result(); SongLookup.resolve(new SongLookup.Request(),source,"clip",null,1000,result);
        assertSame(richer,result.music); assertEquals(1,result.calls); assertEquals(Collections.singletonList("clip"),source.queried);
    }
    @Test public void unrelatedIdAndUnapprovedMatchedRecommendationAreRejected() {
        Source source=new Source(); source.details.put("clip",new Detail(new Music("other",null,true),new Music("recommendation",null,true)));
        Result result=new Result(); SongLookup.resolve(new SongLookup.Request(),source,"clip",null,1000,result);
        assertNull(result.music); assertEquals(1,result.calls); assertEquals(1,source.queried.size());
    }
    @Test public void onlyExplicitNativeMatchedIdIsQueried() {
        Source source=new Source(); source.details.put("clip",new Detail(new Music("clip","matched",false),null));
        Music matched=new Music("matched",null,true); source.details.put("matched",new Detail(matched,null));
        Result result=new Result(); SongLookup.resolve(new SongLookup.Request(),source,"clip",null,1000,result);
        assertSame(matched,result.music); assertEquals(Arrays.asList("clip","matched"),source.queried);
    }
    @Test public void approvedMatchedMusicInSameResponseAvoidsSecondRequest() {
        Source source=new Source(); Music matched=new Music("matched",null,true);
        source.details.put("clip",new Detail(new Music("clip",null,false),matched));
        Result result=new Result(); SongLookup.resolve(new SongLookup.Request(),source,"clip","matched",1000,result);
        assertSame(matched,result.music); assertEquals(1,source.queried.size());
    }
    @Test public void timeoutCancelsNativeFutureAndReportsFailureNotNoResult() {
        Source source=new Source() {
            public Future<?> query(String id) {queried.add(id); last=new FutureTask<>(()->null); return last;}
        };
        Result result=new Result(); SongLookup.resolve(new SongLookup.Request(),source,"clip",null,5,result);
        assertTrue(source.last.isCancelled()); assertTrue(result.status.contains("timed out")); assertEquals(1,result.calls);
    }
    @Test public void cancelledBeforeStartDoesNotQueryOrDeliverCallback() {
        Source source=new Source(); Result result=new Result(); SongLookup.Request request=new SongLookup.Request(); request.cancel();
        SongLookup.resolve(request,source,"clip",null,1000,result); assertTrue(source.queried.isEmpty()); assertEquals(0,result.calls);
    }
    @Test public void cancellationDuringQueryCancelsFutureAndRejectsLateData() {
        SongLookup.Request request=new SongLookup.Request(); Source source=new Source() {
            public Future<?> query(String id) {last=new FutureTask<>(()->new Detail(new Music(id,null,true),null)); request.cancel(); return last;}
        };
        Result result=new Result(); SongLookup.resolve(request,source,"clip",null,1000,result);
        assertTrue(source.last.isCancelled()); assertEquals(0,result.calls);
    }
    @Test public void nativeFailureIsReportedWithoutExceptionOrSensitiveDetails() {
        Source source=new Source() {public Future<?> query(String id) {throw new IllegalStateException("private fixture detail");}};
        Result result=new Result(); SongLookup.resolve(new SongLookup.Request(),source,"clip",null,1000,result);
        assertNull(result.music); assertEquals(1,result.calls); assertFalse(result.status.contains("private"));
    }
}
