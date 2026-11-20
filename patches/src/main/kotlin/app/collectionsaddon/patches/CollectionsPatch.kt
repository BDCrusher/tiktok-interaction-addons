package app.collectionsaddon.patches

import app.morphe.patcher.patch.*
import app.bdcrusher.patches.settingsHubPatch
import app.bdcrusher.patches.wrapHostMethod
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import app.morphe.patcher.util.smali.ExternalLabel
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.*
import com.android.tools.smali.dexlib2.iface.reference.*
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod

private const val R = "Lapp/collectionsaddon/runtime/CollectionsRuntime;"
private const val B = "Lapp/collectionsaddon/runtime/HostBridge;"
private const val CALLBACK = "Lapp/collectionsaddon/runtime/NativeCallback;"
private const val RESPONSE = "Lcom/ss/android/ugc/aweme/favorites/business/collection/CollectionListResponse;"
private const val DETAIL = "Lcom/ss/android/ugc/aweme/favorites/business/collection/CollectionDetail;"
private const val OWNER = "LX/0wHU;"
private const val PANEL = "LX/0wHV;"
private const val HUSH = "Lapp/morphe/extension/tiktok/settings/preference/TikTokPreferenceFragment;"

private fun Instruction.ref() = (this as? ReferenceInstruction)?.reference
private fun Method.calls(owner: String, name: String) = implementation?.instructions?.any {
    (it.ref() as? MethodReference)?.let { ref -> ref.definingClass == owner && ref.name == name } == true
} == true
private fun Method.hasString(text: String) = implementation?.instructions?.any {
    (it.ref() as? StringReference)?.string == text
} == true
private fun BytecodePatchContext.method(type: String, name: String, params: List<String>? = null): MutableMethod {
    val found = mutableClassDefBy(type).methods.filter { it.name == name && (params == null || it.parameterTypes.map(CharSequence::toString) == params) }
    return found.singleOrNull() ?: throw PatchException("Collections add-on: missing/ambiguous $type->$name; use the supported original APK and compatible HushFeed Settings.")
}
private fun BytecodePatchContext.field(type: String, name: String, fieldType: String) {
    if (classDefBy(type).fields.none { it.name == name && it.type == fieldType }) throw PatchException("Collections add-on: unsupported field $type->$name")
}
private fun BytecodePatchContext.body(name: String, registers: Int, text: String) {
    val clazz = mutableClassDefBy(B)
    val old = clazz.methods.single { it.name == name }
    val replacement = MutableMethod(ImmutableMethod(B, old.name, old.parameters, old.returnType,
        old.accessFlags, old.annotations, old.hiddenApiRestrictions, MutableMethodImplementation(registers)))
    replacement.addInstructions(0, text.trimIndent())
    clazz.methods.remove(old); clazz.methods.add(replacement)
}
private fun BytecodePatchContext.bindBridge() {
    body("context", 1, """
        check-cast p0, $OWNER
        iget-object p0, p0, $OWNER->LLJJIJI:Landroid/content/Context;
        return-object p0
    """)
    body("items", 1, """
        check-cast p0, $RESPONSE
        iget-object p0, p0, $RESPONSE->items:Ljava/util/List;
        return-object p0
    """)
    body("id", 1, """
        check-cast p0, $DETAIL
        iget-object p0, p0, $DETAIL->id:Ljava/lang/String;
        return-object p0
    """)
    body("cursor", 3, """
        check-cast p0, $RESPONSE
        iget-object p0, p0, $RESPONSE->cursor:Ljava/lang/Long;
        if-eqz p0, :none
        invoke-virtual {p0}, Ljava/lang/Long;->longValue()J
        move-result-wide v0
        return-wide v0
        :none
        const-wide/16 v0, 0
        return-wide v0
    """)
    body("more", 2, """
        check-cast p0, $RESPONSE
        iget-object p0, p0, $RESPONSE->hasMore:Ljava/lang/Boolean;
        if-eqz p0, :none
        invoke-virtual {p0}, Ljava/lang/Boolean;->booleanValue()Z
        move-result v0
        return v0
        :none
        const/4 v0, 0
        return v0
    """)
    body("copy", 8, """
        check-cast p0, $RESPONSE
        new-instance v0, $RESPONSE
        move-object v1, p1
        iget-object v2, p0, $RESPONSE->pinSection:Ljava/util/List;
        iget-object v3, p0, $RESPONSE->cursor:Ljava/lang/Long;
        iget-object v4, p0, $RESPONSE->hasMore:Ljava/lang/Boolean;
        iget-object v5, p0, $RESPONSE->total:Ljava/lang/Long;
        invoke-direct/range {v0 .. v5}, $RESPONSE-><init>(Ljava/util/List;Ljava/util/List;Ljava/lang/Long;Ljava/lang/Boolean;Ljava/lang/Long;)V
        return-object v0
    """)
    body("replay", 2, """
        check-cast p0, $OWNER
        invoke-virtual {p0, p1}, $OWNER->accept(Ljava/lang/Object;)V
        return-void
    """)
    body("cancel", 1, """
        check-cast p0, LX/03AY;
        invoke-interface {p0}, LX/03AY;->dispose()V
        return-void
    """)
    body("render", 2, """
        check-cast p0, $OWNER
        invoke-virtual {p0, p1}, $OWNER->bdcrusherRenderResponse(Ljava/lang/Object;)V
        return-void
    """)
    body("tint", 2, """
        check-cast p0, Lcom/bytedance/tux/icon/TuxIconView;
        invoke-virtual {p0, p1}, Lcom/bytedance/tux/icon/TuxIconView;->setTintColor(I)V
        return-void
    """)
    body("highlight", 2, """
        instance-of v0, p0, LX/0wHT;
        if-eqz v0, :plain
        check-cast p0, LX/0wHT;
        iget-object p0, p0, LX/0wHT;->LLJJIJIL:Landroid/view/View;
        :plain
        return-object p0
    """)
    body("clearPending", 1, """
        const/4 v0, 0
        sput-boolean v0, LX/0CUf;->LIZJ:Z
        sput-object v0, LX/0CUf;->LIZIZ:LX/0wHV;
        return-void
    """)
    body("load", 20, """
        new-instance v10, LX/0I1j;
        invoke-direct {v10}, LX/0I1j;-><init>()V
        iget-object v0, v10, LX/0I1j;->LLJJIJIL:LX/0I1o;
        move-wide/from16 v1, p1
        const/4 v3, 0
        const/4 v4, 0x1
        const/4 v5, 0
        const/4 v6, 0
        const/4 v7, 0x1
        move-object/from16 v10, p0
        check-cast v10, $OWNER
        iget-boolean v8, v10, $OWNER->LLJJJ:Z
        const/4 v9, 0x1
        invoke-virtual/range {v0 .. v9}, LX/0I1o;->Y(JLjava/lang/String;ZLjava/lang/String;Ljava/lang/String;ZZZ)LX/0BSV;
        move-result-object v0
        invoke-static {}, LX/03p5;->LIZ()LX/03pf;
        move-result-object v1
        invoke-virtual {v0, v1}, LX/03AS;->LJLIIL(LX/03pf;)LX/051f;
        move-result-object v0
        invoke-static {}, LX/051D;->LIZIZ()LX/03pf;
        move-result-object v1
        invoke-virtual {v0, v1}, LX/03AS;->LJJLI(LX/03pf;)LX/05pq;
        move-result-object v0
        move-object/from16 v1, p3
        move-object/from16 v2, p4
        invoke-virtual {v0, v1, v2}, LX/03AS;->LJJZZIII(LX/02uC;LX/02uC;)LX/03AY;
        move-result-object v0
        return-object v0
    """)
    mutableClassDefBy(CALLBACK).interfaces.add("LX/02uC;")
}

@Suppress("unused")
val enhancedCollectionsPatch = bytecodePatch(
    name = "Enhanced collection popup",
    description = "Adjust collections before More, with bounded scrolling and host-theme colors. Configure under BDCrusher's Settings. Requires compatible HushFeed Settings in the same run.",
    default = true,
) {
    category("Interaction")
    compatibleWith(Compatibility(
        name = "TikTok", packageName = "com.zhiliaoapp.musically", appIconColor = 0xFE2C55,
        signatures = setOf("9041803e91bcb814b4b4399fb5c85a91640b755e5e8ba76813814bf4cf2ab5ba"),
        targets = listOf(AppTarget(version = "47.8.3", versionCodes = mapOf(
            SupportedAbi.ARM64_V8A to 2024708030, SupportedAbi.ARMEABI_V7A to 2024708030),
            isExperimental = true, minSdk = 23)),
    ))
    extendWith("extensions/collections.mpe")
    dependsOn(settingsHubPatch)
    execute {
        if (packageMetadata.versionName != "47.8.3" || packageMetadata.versionCode != "2024708030")
            throw PatchException("Collections add-on supports original TikTok 47.8.3 (2024708030) only.")
        // The native trace/cap moved out of accept into our rendering frame. Detect that
        // frame before original-body fingerprints, so duplicate selection has a useful error.
        if (mutableClassDefBy(OWNER).methods.any { it.name == "bdcrusherRenderResponse" })
            throw PatchException("Collections add-on is already applied or selected twice. Use the original APK and select only one copy of the add-on.")
        field(OWNER, "LLJJIJI", "Landroid/content/Context;")
        field(OWNER, "LLJJJ", "Z")
        field(PANEL, "LLJLLL", "Ljava/util/List;")
        field(RESPONSE, "items", "Ljava/util/List;")
        field(RESPONSE, "cursor", "Ljava/lang/Long;")
        field(RESPONSE, "hasMore", "Ljava/lang/Boolean;")
        field(RESPONSE, "pinSection", "Ljava/util/List;")
        field(RESPONSE, "total", "Ljava/lang/Long;")
        field("LX/0I1j;", "LLJJIJIL", "LX/0I1o;")
        field("LX/0CUf;", "LIZJ", "Z")
        field("LX/0CUf;", "LIZIZ", PANEL)
        field(DETAIL, "id", "Ljava/lang/String;")
        field("LX/0wHT;", "LLJJIJIL", "Landroid/view/View;")
        // 47.8.3 already clips native row hit rectangles and maps them to raw screen
        // coordinates. Keep that complete stock path for enhanced and disabled modes.
        val hit = method(PANEL, "LJJZ", listOf("I", "I", "Landroid/view/View;"))
        if (hit.returnType != "Z" || !hit.calls("Landroid/view/View;", "getGlobalVisibleRect") ||
            !hit.calls("Landroid/view/View;", "getLocationOnScreen") ||
            !hit.calls("Landroid/view/View;", "getLocationInWindow") ||
            !hit.calls("Landroid/graphics/Rect;", "offset") || !hit.calls("Landroid/graphics/Rect;", "contains"))
            throw PatchException("Collections native clipped hit-test fingerprint failed")
        val response = method(OWNER, "accept", listOf("Ljava/lang/Object;"))
        if (!response.hasString("QuickFavoriteManager@2914.showQuickFavoriteDialog\$1")) throw PatchException("Collections response fingerprint failed")
        if (!response.calls("LX/03Tp;", "LLF")) throw PatchException("Collections inlined list-cap fingerprint failed")
        if (response.calls(R, "response") || response.calls(R, "install"))
            throw PatchException("Collections add-on is already applied or selected twice. Use the original APK and select only one copy of the add-on.")
        bindBridge()
        // The first take(3) tests native pin eligibility; only customize the row loop's cap.
        // 47.8.3 merges the response items and pin section into a new list before rendering.
        response.apply {
            val instructions = implementation!!.instructions.toList()
            if (implementation!!.registerCount != 31) throw PatchException("Collections inlined register frame changed")
            val caps = instructions.withIndex().filter { (_, instruction) ->
                (instruction.ref() as? MethodReference)?.let { ref ->
                    ref.definingClass == "LX/03Tp;" && ref.name == "LLF" && ref.returnType == "Ljava/util/List;" &&
                        ref.parameterTypes == listOf("Ljava/lang/Iterable;", "I")
                } == true
            }
            if (caps.size != 2) throw PatchException("Collections initial/row cap fingerprints failed")
            val pinProbe = caps[0].value as FiveRegisterInstruction
            val take = caps[1]
            val takeCall = take.value as FiveRegisterInstruction
            val takeRegister = takeCall.registerD
            if (pinProbe.registerC != 3 || pinProbe.registerD != 6 ||
                (instructions[caps[0].index - 1] as? NarrowLiteralInstruction)?.narrowLiteral != 3 ||
                takeCall.registerC != 1 || takeRegister != 6)
                throw PatchException("Collections native row/pin count registers changed")
            val listRead = instructions[take.index - 2]
            val listReadRef = listRead.ref() as? FieldReference
            if (listRead.opcode != Opcode.IGET_OBJECT || listReadRef?.definingClass != PANEL ||
                listReadRef?.name != "LLJLLL" || (listRead as TwoRegisterInstruction).let { it.registerA != 1 || it.registerB != 7 })
                throw PatchException("Collections native row source fingerprint failed")
            val compare = instructions.withIndex().singleOrNull {
                it.value.opcode == Opcode.IF_LT && (it.value as TwoRegisterInstruction).let { comparison ->
                    comparison.registerA == 2 && comparison.registerB == 1
                }
            } ?: throw PatchException("Collections More threshold fingerprint failed")
            val previous = instructions[compare.index - 1]
            if ((previous as? NarrowLiteralInstruction)?.narrowLiteral != 4 || (previous as? OneRegisterInstruction)?.registerA != 1)
                throw PatchException("Collections native More constant changed")
            val content = instructions.withIndex().singleOrNull {
                (it.value.ref() as? MethodReference)?.let { ref -> ref.definingClass == "LX/02Fv;" &&
                    ref.name == "setContentView" && ref.parameterTypes == listOf("Landroid/view/View;") && ref.returnType == "V" } == true
            } ?: throw PatchException("Collections content installation fingerprint failed")
            val contentCall = content.value as FiveRegisterInstruction
            if (contentCall.registerC != 7 || contentCall.registerD != 4)
                throw PatchException("Collections content registers changed")
            val assign = instructions.withIndex().single { (it.value.ref() as? FieldReference)?.let { ref ->
                ref.definingClass == PANEL && ref.name == "LLJLLL" && it.value.opcode == Opcode.IPUT_OBJECT
            } == true }
            val list = assign.value as TwoRegisterInstruction
            if (list.registerA != 3 || list.registerB != 7 || !(assign.index < content.index && content.index < take.index && take.index < compare.index))
                throw PatchException("Collections native list/render anchors changed")
            replaceInstruction(content.index, "invoke-static {v7, v4}, $R->install(Landroid/app/Dialog;Landroid/view/View;)V")
            // The original response argument remains live and untouched in p1; do not infer
            // hasMore from the new merged list, whose identity differs from response.items.
            val edits = listOf(compare.index to """
                move-object/from16 v1, p1
                invoke-static {v7, v1}, $R->thresholdForResponse(Landroid/app/Dialog;Ljava/lang/Object;)I
                move-result v1
            """, take.index to """
                invoke-static {v7}, $R->count(Landroid/app/Dialog;)I
                move-result v$takeRegister
            """)
            for ((index, text) in edits.sortedByDescending { it.first }) addInstructions(index, text)
        }
        // Preserve argument registers across D8's inlined native frame. Runtime owns a single
        // stock retry if enhanced installation throws before the popup is shown.
        wrapHostMethod(mutableClassDefBy(OWNER), response, "bdcrusherRenderResponse", 0, """
            invoke-static/range {p0 .. p1}, $R->response(Ljava/lang/Object;Ljava/lang/Object;)V
            return-void
        """)
        mutableClassDefBy(OWNER).methods.single { it.name == "bdcrusherRenderResponse" }.apply {
            accessFlags = (accessFlags and 2.inv()) or 1
        }
        method(PANEL, "LJJLL", listOf("Landroid/view/MotionEvent;")).apply {
            val original = implementation!!.instructions.first()
            addInstructionsWithLabels(0, """
                invoke-static/range {p0 .. p1}, $R->touch(Landroid/app/Dialog;Landroid/view/MotionEvent;)Z
                move-result v0
                if-eqz v0, :stock_touch
                const/4 v0, 0x1
                return v0
            """, ExternalLabel("stock_touch", original))
        }
        method(PANEL, "dismiss", emptyList()).addInstructions(0, "invoke-static/range {p0 .. p0}, $R->release(Landroid/app/Dialog;)V")
        method(PANEL, "LJJLJLI", listOf("Landroid/view/View;", "Z")).apply {
            if (implementation!!.registerCount < 4) throw PatchException("Collections pressed-state fingerprint failed")
            val original = implementation!!.instructions.first()
            addInstructionsWithLabels(0, """
                invoke-static/range {p0 .. p2}, $R->pressed(Landroid/app/Dialog;Landroid/view/View;Z)Z
                move-result v0
                if-eqz v0, :stock_pressed
                return-void
            """, ExternalLabel("stock_pressed", original))
        }
    }
    finalize {
        // All bundles' extensions are present now, regardless of selection order.
        val fragment = classDefByOrNull(HUSH) ?: throw PatchException("Collections add-on requires compatible HushFeed Settings selected in the same run. No HushFeed code is bundled by this add-on.")
        val pause = method("Lapp/morphe/extension/shared/settings/HushfeedPause;", "isPaused", emptyList())
        if (pause.returnType != "Z") throw PatchException("Unsupported HushFeed pause integration")
        method("Lapp/morphe/extension/tiktok/settings/preference/SettingsUi;", "styleStandardAlertDialog", listOf("Landroid/app/AlertDialog;"))
        method("Lapp/morphe/extension/tiktok/settings/preference/SettingsUi;", "syncDarkMode", listOf("Landroid/content/Context;"))
        method("Lapp/morphe/extension/tiktok/settings/preference/SettingsMenuPreference;", "<init>", listOf(
            "Landroid/content/Context;", "Ljava/lang/String;", "Ljava/lang/String;",
            "Lapp/morphe/extension/tiktok/settings/preference/SettingsMenuPreference\$Icon;", "Landroid/preference/Preference\$OnPreferenceClickListener;"))
        field("Lapp/morphe/extension/tiktok/settings/preference/SettingsMenuPreference\$Icon;", "LAYOUT", "Lapp/morphe/extension/tiktok/settings/preference/SettingsMenuPreference\$Icon;")
        // Every UI integration is a public compile-only contract. Refuse missing/inaccessible
        // signatures at patch time, rather than leaving an entry that crashes on opening.
        for (type in listOf("Lapp/collectionsaddon/runtime/SettingsEntry;",
                "Lapp/collectionsaddon/runtime/CollectionSettingsFragment;",
                "Lapp/collectionsaddon/runtime/CollectionSettingsFragment\$OptionsDialog;")) {
            for (ownMethod in classDefBy(type).methods) {
                for (instruction in ownMethod.implementation?.instructions ?: emptyList()) {
                    val ref = instruction.ref() as? MethodReference ?: continue
                    if (!ref.definingClass.startsWith("Lapp/morphe/")) continue
                    val target = method(ref.definingClass, ref.name, ref.parameterTypes.map(CharSequence::toString))
                    if (target.returnType != ref.returnType || target.accessFlags and 1 == 0)
                        throw PatchException("Unsupported HushFeed settings UI contract: $ref")
                }
            }
        }
        // Shared unnamed dependency owns the single root entry/lifecycle hooks.
    }
}
