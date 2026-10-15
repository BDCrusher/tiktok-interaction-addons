import com.android.tools.smali.dexlib2.Opcodes;
import com.android.tools.smali.dexlib2.dexbacked.DexBackedDexFile;
import com.android.tools.smali.dexlib2.iface.ClassDef;
import com.android.tools.smali.dexlib2.iface.Method;
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction;
import com.android.tools.smali.dexlib2.iface.reference.MethodReference;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/** Read-only audit of already-compiled diagnostic DEX; never assembles or installs an APK. */
public final class NativeHookAudit {
    private static final String SOUND = "Lapp/soundaddon/runtime/SoundRuntime;";
    private static final String COVER = "Lcom/ss/android/ugc/aweme/feed/assem/music/VideoMusicCoverAssem;";
    private static final String TITLE = "Lcom/ss/android/ugc/aweme/feed/assem/music/VideoMusicTitleAssem;";
    private static final Map<String, String> HOOKS = Map.of(
        COVER + "->z4", "bindDisc",
        COVER + "->unBind", "unbindDisc",
        COVER + "->onViewCreated", "viewsCreated",
        TITLE + "->z4", "bindTitle",
        TITLE + "->unBind", "unbindTitle",
        "LX/07pb;->onTouch", "touch",
        "LX/0ANF;->onTouch", "skipEdgeTouch",
        "LX/03uJ;->dispatchTouchEvent", "dispatchDiscTouch",
        "LX/0yck;->Qk", "observeFavorites");

    public static void main(String[] args) throws Exception {
        if (args.length != 1) throw new IllegalArgumentException("Expected compiled DEX directory");
        Path directory = Path.of(args[0]).toRealPath();
        Map<String, String> evidence = new LinkedHashMap<>();
        try (var files = Files.list(directory)) {
            for (Path file : files.filter(p -> p.getFileName().toString().endsWith(".dex")).sorted().toList()) {
                var dex = new DexBackedDexFile(Opcodes.getDefault(), ByteBuffer.wrap(Files.readAllBytes(file)));
                for (ClassDef clazz : dex.getClasses()) for (Method method : clazz.getMethods()) {
                    String key = clazz.getType() + "->" + method.getName();
                    String hook = HOOKS.get(key);
                    if (hook == null || method.getImplementation() == null) continue;
                    var parameters = method.getParameterTypes().stream().map(Object::toString).toList();
                    var expectedParameters = hook.equals("observeFavorites") ? java.util.List.of("Ljava/util/List;")
                        : hook.equals("viewsCreated") ? java.util.List.of("Landroid/view/View;")
                        : hook.equals("dispatchDiscTouch") ? java.util.List.of("Landroid/view/MotionEvent;")
                        : (hook.equals("touch") || hook.equals("skipEdgeTouch")) ? java.util.List.of("Landroid/view/View;", "Landroid/view/MotionEvent;")
                        : hook.startsWith("unbind") ? java.util.List.<String>of() : java.util.List.of("Ljava/lang/Object;");
                    if (!parameters.equals(expectedParameters)) continue;
                    int hooks = 0, returns = 0;
                    for (var instruction : method.getImplementation().getInstructions()) {
                        if (instruction.getOpcode().name().equals((key.endsWith("onTouch") || hook.equals("dispatchDiscTouch")) ? "RETURN" : "RETURN_VOID")) returns++;
                        if (instruction instanceof ReferenceInstruction ref && ref.getReference() instanceof MethodReference call
                            && call.getDefiningClass().equals(SOUND) && call.getName().equals(hook)) hooks++;
                    }
                    int expected = hook.startsWith("unbind") || hook.equals("skipEdgeTouch") || hook.equals("dispatchDiscTouch") ? 1 : returns;
                    if (returns == 0 || hooks != expected || evidence.put(key, hooks + " hooks / " + returns + " returns") != null)
                        throw new IllegalStateException("Missing/duplicate/overwritten sound hook: " + key + ": " + hooks + "/" + expected);
                }
            }
        }
        if (evidence.size() != HOOKS.size()) throw new IllegalStateException("Missing native methods: " + evidence.keySet());
        evidence.forEach((key, value) -> System.out.println(key + ": PASS " + value));
        System.out.println("PASS: all nine Sound lifecycle/touch/priority/width hooks survived combined compilation. Not a device test.");
    }
}
