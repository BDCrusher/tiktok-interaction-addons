package app.soundaddon.runtime;

import android.app.Activity;
import android.view.MotionEvent;
import android.os.Looper;
import android.view.ViewConfiguration;
import android.view.View;
import android.widget.FrameLayout;
import app.morphe.extension.shared.settings.HushfeedPause;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Implementation;
import java.util.concurrent.TimeUnit;
import static org.robolectric.Shadows.shadowOf;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=28,manifest=Config.NONE,
    instrumentedPackages={"app.soundaddon.runtime","app.bdcrusher.runtime"},shadows={SoundGestureTest.NativeFixture.class,SoundGestureTest.Feedback.class})
public class SoundGestureTest {
    @Implements(value=app.bdcrusher.runtime.NativeUiBridge.class,isInAndroidSdk=false)
    public static class Feedback {
        static int pulses;
        @Implementation public static void haptic(android.content.Context context) {pulses++;}
    }
    @Implements(value=NativeBridge.class,isInAndroidSdk=false)
    public static class NativeFixture {
        static int saves;
        static View primary, alternate;
        static Object boundData;
        static Activity titleActivity;
        static int externalSaves;
        @Implementation public static View disc(Object owner) {return primary;}
        @Implementation public static View alternateDisc(Object owner) {return alternate;}
        @Implementation public static Object boundParams(Object owner) {return boundData;}
        @Implementation public static String track(Object data) {return "track";}
        @Implementation public static Object music(Object data) {return "music";}
        @Implementation public static String musicId(Object music) {return "sound";}
        @Implementation public static String matchedId(Object music) {return null;}
        @Implementation public static Activity titleActivity(Object owner) {return titleActivity;}
        @Implementation public static java.util.List<?> destinations(Object music) {
            return "enriched".equals(music)?java.util.Collections.singletonList(10):java.util.Collections.emptyList();
        }
        @Implementation public static Integer provider(Object info) {return (Integer)info;}
        @Implementation public static Object platform(Integer code) {return code;}
        @Implementation public static String label(Object platform) {return "YT Music";}
        @Implementation public static void add(Object owner,Object params,Object music,Object platform,java.util.List<?> infos) {externalSaves++;}
        @Implementation public static void collect(Object music) {saves++;}
    }
    static class FeedRoot extends FrameLayout {
        int events; boolean speedStarted;
        final Runnable speed = () -> speedStarted = true;
        FeedRoot(Activity activity) { super(activity); }
        @Override public boolean dispatchTouchEvent(MotionEvent event) {
            events++;
            if(event.getActionMasked()==MotionEvent.ACTION_DOWN) postDelayed(speed,300);
            if(event.getActionMasked()==MotionEvent.ACTION_CANCEL || event.getActionMasked()==MotionEvent.ACTION_UP) removeCallbacks(speed);
            return super.dispatchTouchEvent(event);
        }
    }
    Activity activity; FrameLayout root; View disc; Object data=new Object();
    @Before public void setup() {
        SoundRuntime.DISCS.clear(); HushfeedPause.paused=false; NativeFixture.saves=0; NativeFixture.externalSaves=0; Feedback.pulses=0;
        NativeFixture.titleActivity=null;
        NativeFixture.primary=null; NativeFixture.alternate=null; NativeFixture.boundData=data;
        activity=Robolectric.buildActivity(Activity.class).setup().visible().get();
        root=new FeedRoot(activity); activity.setContentView(root);
        disc=new View(activity); FrameLayout.LayoutParams layout=new FrameLayout.LayoutParams(60,60);
        layout.leftMargin=220; layout.topMargin=200; root.addView(disc,layout);
        View decor=activity.getWindow().getDecorView();
        decor.measure(View.MeasureSpec.makeMeasureSpec(320,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(640,View.MeasureSpec.EXACTLY));
        decor.layout(0,0,320,640);
        root.measure(View.MeasureSpec.makeMeasureSpec(320,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(600,View.MeasureSpec.EXACTLY));
        root.layout(0,0,320,600);
        SoundRuntime.DISCS.put(disc,new SoundRuntime.Binding(disc,data,new SoundRuntime.Identity() {
            public String track(Object params) {return "track";}
            public boolean hasSound(Object params) {return true;}
        }));
        SoundPreferences.enabled(activity,true);
        SoundRuntime.DISCS.get(disc).lookupStarter=(id,match,callback)->new SongLookup.Request();
    }
    @After public void teardown() { SoundRuntime.DISCS.clear(); HushfeedPause.paused=false; }
    boolean window(long down,int action,boolean onDisc) {
        MotionEvent event=event(down,action,onDisc);
        try {
            if(SoundRuntime.dispatchDiscTouch(activity,event)) return true;
            return activity.dispatchTouchEvent(event);
        } finally {event.recycle();}
    }
    MotionEvent event(long down,int action,boolean onDisc) {
        int[] location=new int[2]; disc.getLocationOnScreen(location);
        return MotionEvent.obtain(down,down+20,action,onDisc?location[0]+30:location[0]-100,location[1]+30,0);
    }
    boolean touch(long down,int action,boolean onDisc) {
        MotionEvent event=event(down,action,onDisc);
        try {return SoundRuntime.skipEdgeTouch(root,event);} finally {event.recycle();}
    }
    @Test public void discDownReservesHoldAndTailWhileOutsideUsesStock() {
        assertFalse(touch(1,MotionEvent.ACTION_DOWN,false));
        assertTrue(touch(2,MotionEvent.ACTION_DOWN,true));
        assertTrue(touch(2,MotionEvent.ACTION_MOVE,false));
        assertTrue(touch(2,MotionEvent.ACTION_UP,false));
        assertFalse(touch(2,MotionEvent.ACTION_MOVE,true));
        assertFalse(touch(3,MotionEvent.ACTION_DOWN,false));
    }
    @Test public void disabledPausedHiddenAndTransparentDiscsDoNotReserve() {
        SoundPreferences.enabled(activity,false); assertFalse(touch(1,MotionEvent.ACTION_DOWN,true));
        SoundPreferences.enabled(activity,true); HushfeedPause.paused=true; assertFalse(touch(2,MotionEvent.ACTION_DOWN,true));
        HushfeedPause.paused=false; disc.setVisibility(View.INVISIBLE); assertFalse(touch(3,MotionEvent.ACTION_DOWN,true));
        disc.setVisibility(View.VISIBLE); disc.setAlpha(0); assertFalse(touch(4,MotionEvent.ACTION_DOWN,true));
    }
    @Test public void cancelMultitouchAndNewSequenceNeverReuseOldReservation() {
        assertTrue(touch(1,MotionEvent.ACTION_DOWN,true));
        assertTrue(touch(1,MotionEvent.ACTION_POINTER_DOWN,true));
        assertTrue(touch(1,MotionEvent.ACTION_CANCEL,true));
        assertFalse(touch(2,MotionEvent.ACTION_MOVE,true));
        assertTrue(touch(3,MotionEvent.ACTION_DOWN,true));
        assertFalse(touch(4,MotionEvent.ACTION_DOWN,false));
        assertFalse(touch(3,MotionEvent.ACTION_UP,true));
    }
    @Test public void discTouchDownAndUpLeaveOrdinaryTapUnconsumed() {
        MotionEvent down=event(1,MotionEvent.ACTION_DOWN,true),up=event(1,MotionEvent.ACTION_UP,true);
        try { assertFalse(SoundRuntime.touch(disc,down)); assertFalse(SoundRuntime.touch(disc,up)); }
        finally { down.recycle(); up.recycle(); }
    }
    @Test public void invalidSoundAndOtherWindowsUseStock() {
        SoundRuntime.DISCS.put(disc,new SoundRuntime.Binding(disc,data,new SoundRuntime.Identity() {
            public String track(Object params) {return "track";}
            public boolean hasSound(Object params) {return false;}
        }));
        assertFalse(touch(1,MotionEvent.ACTION_DOWN,true));
        MotionEvent event=event(2,MotionEvent.ACTION_DOWN,true);
        try {assertFalse(SoundRuntime.skipEdgeTouch(new View(activity),event));} finally {event.recycle();}
    }
    @Test public void clippedOffscreenPixelsDoNotReserve() {
        disc.layout(220,400,280,460);
        assertFalse(touch(1,MotionEvent.ACTION_DOWN,true));
    }
    @Test public void reservedDiscSurvives300msThenShowsMenuAndSavesOnlyOnExplicitRow() {
        MotionEvent down=event(1,MotionEvent.ACTION_DOWN,true);
        try { assertTrue(SoundRuntime.skipEdgeTouch(root,down)); assertFalse(SoundRuntime.touch(disc,down)); }
        finally {down.recycle();}
        shadowOf(Looper.getMainLooper()).idleFor(301,TimeUnit.MILLISECONDS);
        SoundRuntime.Binding binding=SoundRuntime.DISCS.get(disc);
        assertFalse(binding.opened);
        shadowOf(Looper.getMainLooper()).idleFor(Math.max(0,ViewConfiguration.getLongPressTimeout()-301)+1,TimeUnit.MILLISECONDS);
        assertTrue(binding.opened); assertNotNull(binding.popup);
        MotionEvent up=event(1,MotionEvent.ACTION_UP,true);
        try {assertTrue(SoundRuntime.touch(disc,up));} finally {up.recycle();}
        assertEquals(0,NativeFixture.saves);
        View row=binding.rows.get(0); row.performClick(); row.performClick();
        assertEquals(1,NativeFixture.saves);
        assertFalse(binding.opened);
    }
    @Test public void windowDiscHoldDoesNotReachFeedTimersAndTapStillUsesNativeClick() {
        int[] clicks={0}; disc.setOnClickListener(v -> clicks[0]++);
        disc.setOnTouchListener((view,event) -> SoundRuntime.touch(view,event));
        assertTrue(window(10,MotionEvent.ACTION_DOWN,true));
        shadowOf(Looper.getMainLooper()).idleFor(ViewConfiguration.getLongPressTimeout()+1,TimeUnit.MILLISECONDS);
        SoundRuntime.Binding binding=SoundRuntime.DISCS.get(disc);
        assertTrue(binding.opened); assertFalse(((FeedRoot)root).speedStarted); assertEquals(0,((FeedRoot)root).events);
        assertTrue(window(10,MotionEvent.ACTION_UP,true)); assertEquals(0,clicks[0]); assertEquals(0,NativeFixture.saves);
        binding.cancel();
        assertTrue(window(20,MotionEvent.ACTION_DOWN,true)); assertTrue(window(20,MotionEvent.ACTION_UP,true));
        shadowOf(Looper.getMainLooper()).idle();
        assertEquals(1,clicks[0]); assertEquals(2,((FeedRoot)root).events);
        shadowOf(Looper.getMainLooper()).idleFor(301,TimeUnit.MILLISECONDS); assertFalse(((FeedRoot)root).speedStarted);
    }
    @Test public void outsidePressStillReachesFeedAndIts300msSpeedTimer() {
        window(10,MotionEvent.ACTION_DOWN,false);
        shadowOf(Looper.getMainLooper()).idleFor(301,TimeUnit.MILLISECONDS);
        assertTrue(((FeedRoot)root).speedStarted); assertTrue(((FeedRoot)root).events>0);
        assertFalse(SoundRuntime.DISCS.get(disc).opened);
        window(10,MotionEvent.ACTION_CANCEL,false);
    }
    @Test public void windowScrollBeforeHoldAndMultitouchDoNotClickOrSave() {
        int[] clicks={0}; disc.setOnClickListener(v -> clicks[0]++);
        window(10,MotionEvent.ACTION_DOWN,true); window(10,MotionEvent.ACTION_MOVE,false);
        shadowOf(Looper.getMainLooper()).idleFor(ViewConfiguration.getLongPressTimeout()+1,TimeUnit.MILLISECONDS);
        assertFalse(SoundRuntime.DISCS.get(disc).opened);
        window(10,MotionEvent.ACTION_UP,false);
        window(20,MotionEvent.ACTION_DOWN,true); window(20,MotionEvent.ACTION_POINTER_DOWN,true);
        window(20,MotionEvent.ACTION_UP,true); shadowOf(Looper.getMainLooper()).idle();
        assertEquals(0,clicks[0]); assertEquals(0,NativeFixture.saves); assertEquals(0,((FeedRoot)root).events);
    }
    @Test public void changingTrackOrDisablingMidPressDrainsSequenceWithoutPassingUpToParent() {
        window(10,MotionEvent.ACTION_DOWN,true); SoundPreferences.enabled(activity,false);
        assertTrue(window(10,MotionEvent.ACTION_UP,true)); assertEquals(0,((FeedRoot)root).events);
        assertFalse(SoundRuntime.DISCS.get(disc).opened);
        MotionEvent next=event(20,MotionEvent.ACTION_DOWN,true);
        try { assertFalse(SoundRuntime.dispatchDiscTouch(activity,next)); } finally {next.recycle();}
    }
    @Test public void bothNativeDiscContainersAreRegisteredAndUnboundWithoutRemovingNewOwner() {
        View alternate=new View(activity); FrameLayout.LayoutParams layout=new FrameLayout.LayoutParams(60,60);
        layout.leftMargin=220; layout.topMargin=200; root.addView(alternate,layout); alternate.layout(220,200,280,260);
        NativeFixture.primary=disc; NativeFixture.alternate=alternate;
        Object first=new Object(),second=new Object();
        SoundRuntime.bindDisc(first,data); assertEquals(2,SoundRuntime.DISCS.size());
        disc.setVisibility(View.GONE);
        assertTrue(window(10,MotionEvent.ACTION_DOWN,true)); window(10,MotionEvent.ACTION_CANCEL,true);
        SoundRuntime.bindDisc(second,data); SoundRuntime.unbindDisc(first);
        assertEquals(2,SoundRuntime.DISCS.size()); SoundRuntime.unbindDisc(second); assertTrue(SoundRuntime.DISCS.isEmpty());
    }
    @Test public void failedNativeViewCancelsCapturedSequenceWithoutCrashing() {
        disc.setOnTouchListener((view,event) -> {throw new IllegalStateException("fixture failure");});
        assertTrue(window(10,MotionEvent.ACTION_DOWN,true));
        assertTrue(window(10,MotionEvent.ACTION_UP,true));
        assertEquals(0,((FeedRoot)root).events); assertEquals(0,NativeFixture.saves);
    }
    @Test public void bindBeforeViewInflationIsRegisteredAfterNativeViewCreation() {
        SoundRuntime.DISCS.clear(); Object owner=new Object();
        SoundRuntime.bindDisc(owner,data); assertTrue(SoundRuntime.DISCS.isEmpty());
        NativeFixture.primary=disc; SoundRuntime.viewsCreated(owner);
        assertEquals(1,SoundRuntime.DISCS.size()); assertTrue(window(10,MotionEvent.ACTION_DOWN,true));
        window(10,MotionEvent.ACTION_CANCEL,true); SoundRuntime.unbindDisc(owner);
    }
    @Test public void nativeTitleContextMatchesSameTrackAndWindowNotParamsObjectIdentity() {
        SoundRuntime.Binding binding=SoundRuntime.DISCS.get(disc); binding.startTrack="track";
        Object title=new Object(); Object separateData=new Object(); NativeFixture.boundData=separateData;
        NativeFixture.titleActivity=activity; SoundRuntime.bindTitle(title,separateData);
        assertSame(title,SoundRuntime.titleFor(binding));
        NativeFixture.titleActivity=Robolectric.buildActivity(Activity.class).setup().get();
        assertNull(SoundRuntime.titleFor(binding)); SoundRuntime.unbindTitle(title);
    }
    @Test public void hoverUsesMatchingPaletteAndOnePulsePerChangedRow() {
        window(10,MotionEvent.ACTION_DOWN,true);
        shadowOf(Looper.getMainLooper()).idleFor(ViewConfiguration.getLongPressTimeout()+1,TimeUnit.MILLISECONDS);
        SoundRuntime.Binding binding=SoundRuntime.DISCS.get(disc); int before=Feedback.pulses;
        binding.select(0); assertEquals(before+1,Feedback.pulses); binding.select(0); assertEquals(before+1,Feedback.pulses);
        android.graphics.drawable.ColorDrawable highlight=(android.graphics.drawable.ColorDrawable)binding.rows.get(0).getBackground().getCurrent();
        assertEquals(app.bdcrusher.runtime.PopupStyle.pressedColor(binding.background),highlight.getColor());
        binding.select(-1); binding.select(0); assertEquals(before+2,Feedback.pulses);
        assertEquals(app.bdcrusher.runtime.PopupStyle.width(activity,320),binding.popup.getWidth());
        window(10,MotionEvent.ACTION_CANCEL,true);
    }
    @Test public void nativeDetailAddsMusicRowButReflowCannotSaveWithoutFreshSelection() {
        SoundRuntime.Binding binding=SoundRuntime.DISCS.get(disc); SongLookup.Callback[] pending={null};
        binding.lookupStarter=(id,match,callback)-> {pending[0]=callback; return new SongLookup.Request();};
        Object title=new Object(); NativeFixture.titleActivity=activity; SoundRuntime.bindTitle(title,data);
        window(10,MotionEvent.ACTION_DOWN,true);
        shadowOf(Looper.getMainLooper()).idleFor(ViewConfiguration.getLongPressTimeout()+1,TimeUnit.MILLISECONDS);
        binding.select(0); binding.moved=true;
        pending[0].complete("enriched","loaded"); shadowOf(Looper.getMainLooper()).idle();
        assertEquals(2,binding.rows.size()); assertEquals(-1,binding.selected); assertFalse(binding.moved);
        window(10,MotionEvent.ACTION_UP,true); assertEquals(0,NativeFixture.saves); assertEquals(0,NativeFixture.externalSaves);
        binding.rows.get(1).performClick(); assertEquals(1,NativeFixture.externalSaves); SoundRuntime.unbindTitle(title);
    }
    @Test public void hoverAccountsForPopupWindowScreenOriginAndClippedRows() {
        SoundRuntime.Binding binding=SoundRuntime.DISCS.get(disc);
        View row=new View(activity) {
            @Override public boolean getGlobalVisibleRect(android.graphics.Rect rect,android.graphics.Point offset) {
                rect.set(10,20,90,70); return true;
            }
            @Override public void getLocationOnScreen(int[] position) {position[0]=100;position[1]=200;}
        };
        binding.rows.add(row); binding.highlight(115,225); assertEquals(0,binding.selected);
        binding.highlight(15,25); assertEquals(-1,binding.selected);
        binding.highlight(195,225); assertEquals(-1,binding.selected);
    }
    @Test public void dismissedOrObsoleteDetailResultCannotReopenOrChangePopup() {
        SoundRuntime.Binding binding=SoundRuntime.DISCS.get(disc); SongLookup.Callback[] pending={null};
        binding.lookupStarter=(id,match,callback)-> {pending[0]=callback; return new SongLookup.Request();};
        window(10,MotionEvent.ACTION_DOWN,true);
        shadowOf(Looper.getMainLooper()).idleFor(ViewConfiguration.getLongPressTimeout()+1,TimeUnit.MILLISECONDS);
        binding.cancel(); pending[0].complete("enriched","loaded"); shadowOf(Looper.getMainLooper()).idle();
        assertNull(binding.popup); assertTrue(binding.rows.isEmpty()); assertEquals(0,NativeFixture.externalSaves);
        window(10,MotionEvent.ACTION_CANCEL,true);
    }
}
