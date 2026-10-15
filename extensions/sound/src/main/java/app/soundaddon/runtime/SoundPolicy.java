package app.soundaddon.runtime;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Never enumerates installed apps or invents a destination from a music search. */
public final class SoundPolicy {
    public static List<Integer> uniqueProviders(List<Integer> nativeEligible) {
        Set<Integer> result = new LinkedHashSet<>();
        if (nativeEligible != null) for (Integer code : nativeEligible)
            if (code != null && code > 0 && code != 6) result.add(code);
        return new ArrayList<>(result);
    }
    public static boolean sameTrack(String start, String now) { return start != null && !start.isEmpty() && start.equals(now); }
    public static boolean inside(float x,float y,float left,float top,float right,float bottom) {
        return !Float.isNaN(x) && !Float.isNaN(y) && right>left && bottom>top
            && x>=left && x<right && y>=top && y<bottom;
    }
    public static boolean cancelledBeforeHold(float x, float y, float startX, float startY, int slop) {
        float dx = x - startX, dy = y - startY; return dx * dx + dy * dy > slop * slop;
    }
    private SoundPolicy() {}
}
