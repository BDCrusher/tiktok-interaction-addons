package app.bdcrusher.patches
import app.morphe.patcher.patch.*
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod

/** Unnamed dependency: injected once, not a third selectable patch. */
internal val settingsHubPatch = bytecodePatch {
    extendWith("extensions/settings.mpe")
    execute {
        val native="LX/0d6x;"
        val haptic=classDefByOrNull(native) ?: throw PatchException("Native Favorites feedback class missing")
        if(haptic.methods.none {it.name=="<init>" && it.parameterTypes==listOf("Landroid/content/Context;")}
            || haptic.methods.none {it.name=="LIZ" && it.parameterTypes==listOf("I") && it.returnType=="V"})
            throw PatchException("Native Favorites feedback fingerprint failed")
        val type="Lapp/bdcrusher/runtime/NativeUiBridge;"
        val clazz=mutableClassDefBy(type); val old=clazz.methods.single{it.name=="haptic"}
        val method=MutableMethod(ImmutableMethod(type,old.name,old.parameters,old.returnType,old.accessFlags,
            old.annotations,old.hiddenApiRestrictions,MutableMethodImplementation(3)))
        method.addInstructions(0,"""
            new-instance v0, $native
            invoke-direct {v0, p0}, $native-><init>(Landroid/content/Context;)V
            const/4 v1, 0x0
            invoke-virtual {v0, v1}, $native->LIZ(I)V
            return-void
        """.trimIndent())
        clazz.methods.remove(old); clazz.methods.add(method)
    }
    finalize {
        val host="Lapp/morphe/extension/tiktok/settings/preference/TikTokPreferenceFragment;"
        val hub="Lapp/bdcrusher/runtime/SettingsHub;"
        val clazz=mutableClassDefByOrNull(host) ?: throw PatchException("BDCrusher's Settings requires compatible HushFeed Settings selected in the same run.")
        for(type in listOf(hub,"Lapp/bdcrusher/runtime/SettingsHub${'$'}Page;")) {
            for(method in classDefBy(type).methods) for(insn in method.implementation?.instructions ?: emptyList()) {
                val ref=(insn as? ReferenceInstruction)?.reference as? MethodReference ?: continue
                if(!ref.definingClass.startsWith("Lapp/morphe/")) continue
                val target=classDefByOrNull(ref.definingClass)?.methods?.singleOrNull {
                    it.name==ref.name && it.parameterTypes==ref.parameterTypes && it.returnType==ref.returnType
                }
                if(target==null || target.accessFlags and 1==0) throw PatchException("Unsupported public HushFeed settings hub contract: $ref")
            }
        }
        fun hook(name:String,params:List<String>,callback:String,code:String) {
            val method=clazz.methods.singleOrNull {it.name==name && it.parameterTypes.map(CharSequence::toString)==params}
                ?: throw PatchException("Unsupported HushFeed settings hub hook: $name")
            if(method.implementation!!.instructions.any {
                ((it as? ReferenceInstruction)?.reference as? MethodReference)?.let {ref->ref.definingClass==hub && ref.name==callback}==true
            }) return
            if(method.implementation!!.instructions.none {it.opcode==Opcode.RETURN_VOID})
                throw PatchException("Unsupported HushFeed settings hub exits: $name")
            val native="bdcrusherNative_$name"
            val range=if(params.isEmpty()) "p0 .. p0" else "p0 .. p${params.size}"
            wrapHostMethod(clazz,method,native,0,"""
                invoke-direct/range {$range}, $host->$native(${params.joinToString("")})V
                $code
                return-void
            """)
        }
        hook("createMasterMenu",listOf("Landroid/content/Context;","Landroid/preference/PreferenceScreen;"),"install",
            "invoke-static/range {p0 .. p2}, $hub->install(Landroid/app/Fragment;Landroid/content/Context;Landroid/preference/PreferenceScreen;)V")
        hook("onResume",emptyList(),"refresh","invoke-static/range {p0 .. p0}, $hub->refresh(Landroid/app/Fragment;)V")
    }
}
