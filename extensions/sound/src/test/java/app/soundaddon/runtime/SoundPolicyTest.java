package app.soundaddon.runtime;
import org.junit.Test;
import java.util.Arrays;
import java.util.Collections;
import static org.junit.Assert.*;
public class SoundPolicyTest {
    @Test public void noNativeProvidersDoesNotInventInstalledApps() { assertTrue(SoundPolicy.uniqueProviders(null).isEmpty()); }
    @Test public void filtersUnknownAndNativeMyMusic() { assertEquals(Arrays.asList(10,3), SoundPolicy.uniqueProviders(Arrays.asList(null,0,6,-1,10,3))); }
    @Test public void preservesNativeOrderAndDeduplicates() { assertEquals(Arrays.asList(3,10,2), SoundPolicy.uniqueProviders(Arrays.asList(3,10,3,2,10))); }
    @Test public void acceptsNativeSingleProvider() { assertEquals(Collections.singletonList(10),SoundPolicy.uniqueProviders(Collections.singletonList(10))); }
    @Test public void emptyIdentityNeverMatches() { assertFalse(SoundPolicy.sameTrack("","")); assertFalse(SoundPolicy.sameTrack(null,null)); }
    @Test public void changedTrackCannotSave() { assertFalse(SoundPolicy.sameTrack("track1","track2")); assertTrue(SoundPolicy.sameTrack("track1","track1")); }
    @Test public void preHoldSlopIsRadial() { assertFalse(SoundPolicy.cancelledBeforeHold(6,8,0,0,10)); assertTrue(SoundPolicy.cancelledBeforeHold(8,8,0,0,10)); }
}
