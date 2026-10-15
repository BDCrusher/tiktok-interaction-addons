import com.android.tools.smali.dexlib2.Opcodes;
import com.android.tools.smali.dexlib2.dexbacked.DexBackedDexFile;
import com.android.tools.smali.dexlib2.iface.*;
import com.android.tools.smali.dexlib2.iface.instruction.*;
import java.nio.ByteBuffer;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;

/** Read-only targeted DEX probe: no APK extraction, rewriting or installation. */
public final class DexProbe {
    public static void main(String[] args) throws Exception {
        Pattern types = Pattern.compile(args[1]);
        boolean annotations = args[2].equals("@annotations");
        boolean signatures = args[2].equals("@methods") || annotations;
        Pattern methods = Pattern.compile(signatures ? ".*" : args[2]);
        try (ZipFile zip = new ZipFile(args[0])) {
            var entries = zip.entries();
            while (entries.hasMoreElements()) {
                var entry = entries.nextElement();
                if (!entry.getName().matches(".*\\.(dex|mpe)")) continue;
                var dex = new DexBackedDexFile(Opcodes.getDefault(), ByteBuffer.wrap(zip.getInputStream(entry).readAllBytes()));
                for (ClassDef clazz : dex.getClasses()) {
                    if (!types.matcher(clazz.getType()).find()) continue;
                    System.out.println("CLASS " + entry.getName() + " " + clazz.getType() + " extends " + clazz.getSuperclass());
                    for (Field field : clazz.getFields()) System.out.println("FIELD " + field.getName() + ":" + field.getType());
                    for (Method method : clazz.getMethods()) {
                        if (!methods.matcher(method.getName()).matches()) continue;
                        var impl = method.getImplementation();
                        System.out.println("METHOD " + method.getName() + method.getParameterTypes() + ":" + method.getReturnType()
                            + " flags=" + method.getAccessFlags() + " regs=" + (impl == null ? 0 : impl.getRegisterCount()));
                        if (annotations) {
                            for (var annotation : method.getAnnotations()) {
                                System.out.println("ANNOTATION " + annotation.getType());
                                for (var element : annotation.getElements()) System.out.println("  " + element.getName() + "=" + element.getValue());
                            }
                            for (var parameter : method.getParameters()) for (var annotation : parameter.getAnnotations()) {
                                System.out.println("PARAMETER " + parameter.getType() + " " + annotation.getType());
                                for (var element : annotation.getElements()) System.out.println("  " + element.getName() + "=" + element.getValue());
                            }
                        }
                        if (impl == null || signatures) continue;
                        int address = 0;
                        for (Instruction instruction : impl.getInstructions()) {
                            StringBuilder line = new StringBuilder(String.format("%04x %s", address, instruction.getOpcode()));
                            if (instruction instanceof FiveRegisterInstruction r) line.append(" {v").append(r.getRegisterC()).append(",v").append(r.getRegisterD()).append(",v").append(r.getRegisterE()).append(",v").append(r.getRegisterF()).append(",v").append(r.getRegisterG()).append("} count=").append(r.getRegisterCount());
                            else if (instruction instanceof RegisterRangeInstruction r) line.append(" v").append(r.getStartRegister()).append(" count=").append(r.getRegisterCount());
                            else {
                                if (instruction instanceof OneRegisterInstruction r) line.append(" v").append(r.getRegisterA());
                                if (instruction instanceof TwoRegisterInstruction r) line.append(",v").append(r.getRegisterB());
                                if (instruction instanceof ThreeRegisterInstruction r) line.append(",v").append(r.getRegisterC());
                            }
                            if (instruction instanceof NarrowLiteralInstruction r) line.append(" #").append(r.getNarrowLiteral());
                            if (instruction instanceof ReferenceInstruction r) line.append(" ").append(r.getReference());
                            if (instruction instanceof OffsetInstruction r) line.append(" -> ").append(String.format("%04x", address + r.getCodeOffset()));
                            System.out.println(line);
                            address += instruction.getCodeUnits();
                        }
                    }
                }
            }
        }
    }
}
