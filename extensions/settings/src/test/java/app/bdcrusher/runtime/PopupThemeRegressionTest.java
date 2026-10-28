package app.bdcrusher.runtime;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.TypedValue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import static org.junit.Assert.*;

/** The two real 47.5.3 attributes, reproduced from the original APK resource table. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=28, manifest=Config.NONE, shadows=PopupThemeRegressionTest.HostTheme.class)
public class PopupThemeRegressionTest {
    @Implements(Resources.Theme.class)
    public static class HostTheme {
        @RealObject private Resources.Theme realTheme;
        static int sheet;
        @Implementation protected boolean resolveAttribute(int id, TypedValue out, boolean resolveRefs) {
            if (id==0x7f0603e5 || id==0x7f0603e8) {
                out.type=TypedValue.TYPE_INT_COLOR_ARGB8;
                // a24 -> aid is translucent orange; a27 -> aig is UISheetFlat1.
                out.data=id==0x7f0603e5 ? 0x47fe7c02 : sheet;
                out.resourceId=0;
                return true;
            }
            return Shadow.directlyOn(realTheme,Resources.Theme.class,"resolveAttribute",
                ClassParameter.from(int.class,id),ClassParameter.from(TypedValue.class,out),
                ClassParameter.from(boolean.class,resolveRefs));
        }
    }
    private Context host(int color, boolean night) {
        Activity activity=Robolectric.buildActivity(Activity.class).setup().get();
        HostTheme.sheet=color;
        Configuration config=new Configuration(activity.getResources().getConfiguration());
        config.uiMode=(config.uiMode & ~Configuration.UI_MODE_NIGHT_MASK)
            | (night ? Configuration.UI_MODE_NIGHT_YES : Configuration.UI_MODE_NIGHT_NO);
        Resources resources=new Resources(activity.getAssets(),activity.getResources().getDisplayMetrics(),config) {
            @Override public String getResourceEntryName(int id) {
                if(id==0x7f0603e5) return "a24";
                if(id==0x7f0603e8) return "a27";
                return super.getResourceEntryName(id);
            }
        };
        return new ContextWrapper(activity) {
            @Override public Resources getResources() { return resources; }
        };
    }
    @Test public void amoledUsesBlackSheetInsteadOfTranslucentOrange() {
        assertEquals(0xff000000,PopupStyle.background(host(0xff000000,true)));
    }
    @Test public void lightModeUsesWhiteSheetInsteadOfTranslucentOrange() {
        assertEquals(0xffffffff,PopupStyle.background(host(0xffffffff,false)));
    }
    @Test public void customDarkSheetColorIsRetained() {
        assertEquals(0xff071017,PopupStyle.background(host(0xff071017,true)));
    }
    @Test public void translucentSheetCannotBecomePopupSurface() {
        assertEquals(0xff121212,PopupStyle.background(host(0x33000000,true)));
        assertEquals(0xffffffff,PopupStyle.background(host(0x33ffffff,false)));
    }
}
