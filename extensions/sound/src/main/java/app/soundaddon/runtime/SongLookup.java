package app.soundaddon.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/** Bounded, cancellable use of TikTok's native detail client. No credentials or HTTP implementation. */
final class SongLookup {
    interface Source {
        Future<?> query(String id);
        Object music(Object detail);
        Object matchedMusic(Object detail);
        String id(Object music);
        String matchedId(Object music);
        boolean eligible(Object music);
    }
    interface Callback { void complete(Object music,String status); }
    static final Source NATIVE = new Source() {
        public Future<?> query(String id) { return NativeBridge.queryMusic(id); }
        public Object music(Object detail) { return NativeBridge.detailMusic(detail); }
        public Object matchedMusic(Object detail) { return NativeBridge.detailMatchedMusic(detail); }
        public String id(Object music) { return NativeBridge.musicId(music); }
        public String matchedId(Object music) { return NativeBridge.matchedId(music); }
        public boolean eligible(Object music) { List<?> items=NativeBridge.destinations(music); return items!=null && !items.isEmpty(); }
    };
    private static final ThreadPoolExecutor WORKERS=new ThreadPoolExecutor(0,2,30,TimeUnit.SECONDS,
        new ArrayBlockingQueue<>(2), task->{ Thread worker=new Thread(task,"bdcrusher-song-detail"); worker.setDaemon(true); return worker; },
        new ThreadPoolExecutor.AbortPolicy());
    static final class Request {
        volatile boolean cancelled;
        volatile Future<?> nativeRequest, worker;
        void cancel() { cancelled=true; Future<?> current=nativeRequest; if(current!=null) current.cancel(true);
            Future<?> task=worker; if(task!=null) task.cancel(true); }
    }
    static Request start(String clipId,String matchedId,Callback callback) {
        Request request=new Request();
        try { request.worker=WORKERS.submit(()->resolve(request,NATIVE,clipId,matchedId,4000,callback)); }
        catch(RejectedExecutionException busy) { callback.complete(null,"Music detail queue busy"); }
        return request;
    }
    static void resolve(Request request,Source source,String clipId,String originalMatch,long timeoutMs,Callback callback) {
        long deadline=System.nanoTime()+TimeUnit.MILLISECONDS.toNanos(timeoutMs);
        List<String> ids=new ArrayList<>(); if(valid(clipId)) ids.add(clipId);
        String approvedMatch=valid(originalMatch) && !originalMatch.equals(clipId) ? originalMatch : null;
        if(approvedMatch!=null) ids.add(approvedMatch);
        try {
            for(int index=0;index<ids.size() && index<2;index++) {
                if(request.cancelled) return;
                String requested=ids.get(index); Future<?> nativeRequest=source.query(requested);
                request.nativeRequest=nativeRequest;
                if(request.cancelled) { nativeRequest.cancel(true); return; }
                long remaining=deadline-System.nanoTime(); if(remaining<=0) throw new TimeoutException();
                Object detail=nativeRequest.get(remaining,TimeUnit.NANOSECONDS);
                if(request.cancelled) return;
                Object music=source.music(detail);
                // Only the requested id is accepted. Never choose a recommendation/similar title.
                if(music!=null && requested.equals(source.id(music))) {
                    if(source.eligible(music)) { callback.complete(music,"Native music destinations loaded"); return; }
                    if(requested.equals(clipId) && approvedMatch==null) {
                        String match=source.matchedId(music);
                        if(valid(match) && !match.equals(clipId)) { approvedMatch=match; ids.add(match); }
                    }
                }
                Object matched=source.matchedMusic(detail);
                if(matched!=null && approvedMatch!=null && approvedMatch.equals(source.id(matched)) && source.eligible(matched)) {
                    callback.complete(matched,"Native matched-song destinations loaded"); return;
                }
            }
            if(!request.cancelled) callback.complete(null,"No eligible native music destinations");
        } catch(InterruptedException cancelled) { Thread.currentThread().interrupt(); }
        catch(TimeoutException timeout) { if(!request.cancelled) callback.complete(null,"Music detail timed out"); }
        catch(ExecutionException | RuntimeException | LinkageError failure) {
            if(!request.cancelled) callback.complete(null,"Native music detail unavailable");
        } finally { Future<?> nativeRequest=request.nativeRequest; if(nativeRequest!=null && !nativeRequest.isDone()) nativeRequest.cancel(true); }
    }
    private static boolean valid(String id) { return id!=null && !id.isEmpty(); }
    private SongLookup() {}
}
