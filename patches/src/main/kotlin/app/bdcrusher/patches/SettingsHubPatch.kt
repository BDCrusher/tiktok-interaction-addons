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
        if (packageMetadata.versionName != "47.9.3" || packageMetadata.versionCode != "2024709030")
            throw PatchException("TikTok Interaction Add-ons requires original TikTok 47.9.3 (2024709030).")
        val native="LX/1825;"
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
        val adapterType = "Lapp/morphe/extension/tiktok/settings/preference/SettingsListAdapter;"
        val adapter = mutableClassDefByOrNull(adapterType)
            ?: throw PatchException("HushFeed settings list renderer is missing.")
        if (adapter.superclass != "Landroid/widget/BaseAdapter;" ||
            adapter.methods.none { it.name == "<init>" && it.accessFlags and 1 != 0 &&
                it.parameterTypes == listOf("Landroid/widget/ListAdapter;", "Ljava/util/List;") && it.returnType == "V" } ||
            adapter.methods.none { it.name == "dispose" && it.accessFlags and 1 != 0 &&
                it.parameterTypes.isEmpty() && it.returnType == "V" })
            throw PatchException("Unsupported HushFeed settings list renderer.")
        // The released adapter has public entry points but package-private class visibility.
        adapter.setAccessFlags(adapter.accessFlags or 1)
        for (type in listOf(hub, "Lapp/bdcrusher/runtime/SettingsPage;")) {
            for (method in classDefBy(type).methods) for (insn in method.implementation?.instructions ?: emptyList()) {
                val ref=(insn as? ReferenceInstruction)?.reference as? MethodReference ?: continue
                if(!ref.definingClass.startsWith("Lapp/morphe/")) continue
                val target=classDefByOrNull(ref.definingClass)?.methods?.singleOrNull {
                    it.name==ref.name && it.parameterTypes==ref.parameterTypes && it.returnType==ref.returnType
                }
                if(target==null || target.accessFlags and 1==0) throw PatchException("Unsupported public HushFeed settings contract: $ref")
            }
        }
        if (clazz.methods.none { it.name == "settingsAsList" && it.parameterTypes.isEmpty() &&
                it.returnType == "Z" && it.accessFlags and 8 != 0 })
            throw PatchException("Unsupported HushFeed settings list mode")
        fun hook(name:String,params:List<String>,callback:String,code:String,locals:Int=0) {
            val method=clazz.methods.singleOrNull {it.name==name && it.returnType=="V" && it.parameterTypes.map(CharSequence::toString)==params}
                ?: throw PatchException("Unsupported HushFeed settings hub hook: $name")
            if(method.implementation!!.instructions.any {
                ((it as? ReferenceInstruction)?.reference as? MethodReference)?.let {ref->ref.definingClass==hub && ref.name==callback}==true
            }) return
            if(method.implementation!!.instructions.none {it.opcode==Opcode.RETURN_VOID})
                throw PatchException("Unsupported HushFeed settings hub exits: $name")
            val native="bdcrusherNative_$name"
            val range=if(params.isEmpty()) "p0 .. p0" else "p0 .. p${params.size}"
            wrapHostMethod(clazz,method,native,locals,"""
                invoke-direct/range {$range}, $host->$native(${params.joinToString("")})V
                $code
                return-void
            """)
        }
        hook("createMasterMenu",listOf("Landroid/content/Context;","Landroid/preference/PreferenceScreen;"),"install",
            """
                invoke-static {}, $host->settingsAsList()Z
                move-result v0
                invoke-static {p0, p1, p2, v0}, $hub->install(Landroid/app/Fragment;Landroid/content/Context;Landroid/preference/PreferenceScreen;Z)V
            """.trimIndent(),1)
        hook("onResume",emptyList(),"refresh","invoke-static/range {p0 .. p0}, $hub->refresh(Landroid/app/Fragment;)V")
        hook("showAsList",listOf("Z"),"showAsList",
            "invoke-static/range {p0 .. p1}, $hub->showAsList(Landroid/app/Fragment;Z)V")
        hook("onActivityCreated",listOf("Landroid/os/Bundle;"),"restoreExpansion",
            "invoke-static/range {p0 .. p1}, $hub->restoreExpansion(Landroid/app/Fragment;Landroid/os/Bundle;)V")
        hook("onSaveInstanceState",listOf("Landroid/os/Bundle;"),"saveExpansion",
            "invoke-static/range {p0 .. p1}, $hub->saveExpansion(Landroid/app/Fragment;Landroid/os/Bundle;)V")
    }
}
