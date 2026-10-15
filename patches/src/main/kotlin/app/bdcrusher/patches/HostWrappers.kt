package app.bdcrusher.patches

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.util.proxy.mutableTypes.MutableClass
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod

/** Keep incoming arguments in a separate frame: D8 reuses parameter registers in host bodies. */
internal fun wrapHostMethod(clazz: MutableClass, method: MutableMethod, nativeName: String,
                            locals: Int, body: String): MutableMethod {
    if (method.accessFlags and (AccessFlags.STATIC.value or AccessFlags.ABSTRACT.value or AccessFlags.NATIVE.value) != 0)
        throw PatchException("Unsupported non-instance host wrapper: ${method.name}")
    if (clazz.methods.any { it.name == nativeName }) throw PatchException("Add-on is already applied or selected twice")
    val parameters = method.parameters
    val nativeFlags = (method.accessFlags and (AccessFlags.PUBLIC.value or AccessFlags.PROTECTED.value).inv()) or
        AccessFlags.PRIVATE.value or AccessFlags.SYNTHETIC.value
    val native = MutableMethod(ImmutableMethod(clazz.type, nativeName, parameters, method.returnType,
        nativeFlags, method.annotations, method.hiddenApiRestrictions, method.implementation))
    val parameterRegisters = 1 + method.parameterTypes.sumOf { if (it == "J" || it == "D") 2 else 1 }
    val wrapper = MutableMethod(ImmutableMethod(clazz.type, method.name, parameters, method.returnType,
        method.accessFlags, method.annotations, method.hiddenApiRestrictions,
        MutableMethodImplementation(parameterRegisters + locals)))
    wrapper.addInstructions(0, body.trimIndent())
    clazz.methods.remove(method)
    clazz.methods.add(native)
    clazz.methods.add(wrapper)
    return wrapper
}
