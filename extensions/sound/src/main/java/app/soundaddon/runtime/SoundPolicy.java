package app.soundaddon.runtime;

public final class SoundPolicy {
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
