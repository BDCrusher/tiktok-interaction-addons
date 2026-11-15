package app.soundaddon.runtime;
import org.junit.Test;
import static org.junit.Assert.*;
public class SoundPolicyTest {
    @Test public void emptyIdentityNeverMatches() { assertFalse(SoundPolicy.sameTrack("","")); assertFalse(SoundPolicy.sameTrack(null,null)); }
    @Test public void changedTrackCannotSave() { assertFalse(SoundPolicy.sameTrack("track1","track2")); assertTrue(SoundPolicy.sameTrack("track1","track1")); }
    @Test public void preHoldSlopIsRadial() { assertFalse(SoundPolicy.cancelledBeforeHold(6,8,0,0,10)); assertTrue(SoundPolicy.cancelledBeforeHold(8,8,0,0,10)); }
}
