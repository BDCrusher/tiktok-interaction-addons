package app.bdcrusher.patches

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.util.proxy.mutableTypes.MutableClass
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.immutable.ImmutableClassDef
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodParameter
import com.android.tools.smali.dexlib2.iface.instruction.RegisterRangeInstruction
import org.junit.Assert.*
import org.junit.Test

class HostWrappersTest {
    @Test fun wrapperPreservesIncomingArgumentsEvenWhenHostClobbersContext() {
        val type="Ltest/Host;"
        val original=MutableMethod(ImmutableMethod(type,"createMasterMenu",listOf(
            ImmutableMethodParameter("Landroid/content/Context;",emptySet(),null),
            ImmutableMethodParameter("Landroid/preference/PreferenceScreen;",emptySet(),null)),"V",
            AccessFlags.PRIVATE.value,emptySet(),emptySet(),MutableMethodImplementation(3)))
        original.addInstructions(0,"const/4 p1, 0x0\nreturn-void")
        val clazz=MutableClass(ImmutableClassDef(type,1,"Ljava/lang/Object;",emptyList(),null,emptySet(),emptyList(),listOf(original)))
        val wrapper=wrapHostMethod(clazz,clazz.methods.single(),"savedNative",0,"""
            invoke-direct/range {p0 .. p2}, $type->savedNative(Landroid/content/Context;Landroid/preference/PreferenceScreen;)V
            invoke-static/range {p0 .. p2}, Ltest/Bridge;->install(Ljava/lang/Object;Landroid/content/Context;Landroid/preference/PreferenceScreen;)V
            return-void
        """)
        assertEquals(2,clazz.methods.size)
        val native=clazz.methods.single {it.name=="savedNative"}
        assertEquals(Opcode.CONST_4,native.implementation!!.instructions.first().opcode)
        assertTrue(native.accessFlags and AccessFlags.PRIVATE.value != 0)
        assertEquals(3,wrapper.implementation!!.registerCount)
        val calls=wrapper.implementation!!.instructions.filterIsInstance<RegisterRangeInstruction>().toList()
        assertEquals(2,calls.size)
        assertTrue(calls.all {it.startRegister==0 && it.registerCount==3})
        assertEquals(Opcode.RETURN_VOID,wrapper.implementation!!.instructions.last().opcode)
    }
}
