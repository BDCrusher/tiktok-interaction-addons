package app.bdcrusher.runtime;

import android.app.Activity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=28,manifest=Config.NONE)
public class PopupStyleTest {
    @Test public void observedFavoritesWidthIsUsedAndClampedToScreen() {
        Activity activity=Robolectric.buildActivity(Activity.class).setup().get();
        PopupStyle.rememberWidth(activity,180); assertEquals(180,PopupStyle.width(activity,300));
        assertEquals(120,PopupStyle.width(activity,120));
    }
    @Test public void widthSampleExpiresOnFontOrConfigurationChange() {
        Activity activity=Robolectric.buildActivity(Activity.class).setup().get();
        PopupStyle.rememberWidth(activity,180); activity.getResources().getConfiguration().fontScale=2;
        int expected=Math.round(212*activity.getResources().getDisplayMetrics().density);
        assertEquals(Math.min(1000,expected),PopupStyle.width(activity,1000));
    }
    @Test public void pressedColorsUseExactlyTheCollectionsBlend() {
        assertEquals(0xff262626,PopupStyle.pressedColor(0xff000000));
        assertEquals(0xffd8d8d8,PopupStyle.pressedColor(0xffffffff));
        assertEquals(0xffffffff,PopupStyle.textColor(0xff000000));
    }
}
