package app.soundaddon.patches

import app.morphe.patcher.patch.*
import app.bdcrusher.patches.settingsHubPatch
import app.bdcrusher.patches.wrapHostMethod
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.util.smali.ExternalLabel
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.iface.instruction.*
import com.android.tools.smali.dexlib2.iface.reference.*
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod

private const val B = "Lapp/soundaddon/runtime/NativeBridge;"
private const val R = "Lapp/soundaddon/runtime/SoundRuntime;"
private const val ENTRY = "Lapp/soundaddon/runtime/SoundSettings;"
private const val COVER = "Lcom/ss/android/ugc/aweme/feed/assem/music/VideoMusicCoverAssem;"
private const val PARAMS = "Lcom/ss/android/ugc/aweme/feed/model/VideoItemParams;"
private const val MUSIC = "Lcom/ss/android/ugc/aweme/music/model/Music;"
private const val HUSH = "Lapp/morphe/extension/tiktok/settings/preference/TikTokPreferenceFragment;"
private const val EDGE_ABILITY = "Lcom/ss/android/ugc/aweme/feed/longvideo/edgespeedup/EdgeSpeedupAbility;"
private const val DISPATCH_ACTIVITY = "LX/04LH;"
private const val FAVORITES = "LX/0wHV;"
private fun BytecodePatchContext.find(type: String, name: String, params: List<String>): MutableMethod =
    mutableClassDefByOrNull(type)?.methods?.singleOrNull { it.name == name && it.parameterTypes.map(CharSequence::toString) == params }
        ?: throw PatchException("Sound actions add-on: unsupported $type->$name; requires original TikTok 47.8.3 and compatible HushFeed Settings (targeted against 0.83.0).")
private fun MutableMethod.calls(type: String, name: String) = implementation!!.instructions.any {
    ((it as? ReferenceInstruction)?.reference as? MethodReference)?.let { ref -> ref.definingClass == type && ref.name == name } == true
}
private fun BytecodePatchContext.body(name: String, registers: Int, code: String) {
    val clazz = mutableClassDefBy(B)
    val old = clazz.methods.single { it.name == name }
    val next = MutableMethod(ImmutableMethod(B, old.name, old.parameters, old.returnType, old.accessFlags,
        old.annotations, old.hiddenApiRestrictions, MutableMethodImplementation(registers)))
    next.addInstructions(0, code.trimIndent()); clazz.methods.remove(old); clazz.methods.add(next)
}

@Suppress("unused")
val soundButtonActionsPatch = bytecodePatch(
    name = "Sound button actions",
    description = "Hold the music disc to open Save sound to TikTok, using the Favorites hold delay. Configure under BDCrusher's Settings. Requires compatible HushFeed Settings. Experimental; device validation pending.",
    default = false,
) {
    category("Interaction")
    compatibleWith(Compatibility(name = "TikTok", packageName = "com.zhiliaoapp.musically", appIconColor = 0xFE2C55,
        signatures = setOf("9041803e91bcb814b4b4399fb5c85a91640b755e5e8ba76813814bf4cf2ab5ba"),
        targets = listOf(AppTarget(version = "47.8.3", versionCodes = mapOf(SupportedAbi.ARM64_V8A to 2024708030,
            SupportedAbi.ARMEABI_V7A to 2024708030), isExperimental = true, minSdk = 23))))
    extendWith("extensions/sound.mpe")
    dependsOn(settingsHubPatch)
    execute {
        if (packageMetadata.versionName != "47.8.3" || packageMetadata.versionCode != "2024708030")
            throw PatchException("Sound button actions supports original TikTok 47.8.3 (2024708030) only.")
        val cover = find(COVER, "n4", listOf("Ljava/lang/Object;"))
        if (cover.calls(R, "bindDisc"))
            throw PatchException("Sound actions add-on is already applied or selected twice. Use the original APK.")
        if (listOf("LLLJIL","LLLJL").any { field -> classDefBy(COVER).fields.none {
                it.name == field && it.type == "Landroid/widget/FrameLayout;" } })
            throw PatchException("Music disc binding fingerprint failed")
        if (!cover.calls("Lcom/ss/android/ugc/aweme/feed/assem/music/BaseMusicCoverAssem;", "Sq"))
            throw PatchException("Music disc lifecycle fingerprint failed")
        val created = find(COVER, "onViewCreated", listOf("Landroid/view/View;"))
        if (created.implementation!!.instructions.none {
            ((it as? ReferenceInstruction)?.reference as? TypeReference)?.type == "LX/09f1;"
        }) throw PatchException("Music disc touch listener fingerprint failed")
        body("disc", 1, """
            check-cast p0, $COVER
            iget-object p0, p0, $COVER->LLLJIL:Landroid/widget/FrameLayout;
            return-object p0
        """)
        body("alternateDisc", 1, """
            check-cast p0, $COVER
            iget-object p0, p0, $COVER->LLLJL:Landroid/widget/FrameLayout;
            return-object p0
        """)
        val bound=find("Lcom/bytedance/assem/arch/reused/ReusedAssem;","getItem",emptyList())
        if(bound.returnType!="Ljava/lang/Object;" || bound.implementation!!.instructions.none {
                ((it as? ReferenceInstruction)?.reference as? FieldReference)?.name=="cellProxy"
            }) throw PatchException("Music current bound-data fingerprint failed")
        body("boundParams", 1, """
            check-cast p0, Lcom/bytedance/assem/arch/reused/ReusedAssem;
            invoke-virtual {p0}, Lcom/bytedance/assem/arch/reused/ReusedAssem;->getItem()Ljava/lang/Object;
            move-result-object p0
            return-object p0
        """)
        body("track", 1, """
            check-cast p0, $PARAMS
            invoke-virtual {p0}, $PARAMS->getAweme()Lcom/ss/android/ugc/aweme/feed/model/Aweme;
            move-result-object p0
            invoke-virtual {p0}, Lcom/ss/android/ugc/aweme/feed/model/Aweme;->getAid()Ljava/lang/String;
            move-result-object p0
            return-object p0
        """)
        body("music", 1, """
            check-cast p0, $PARAMS
            invoke-virtual {p0}, $PARAMS->getAweme()Lcom/ss/android/ugc/aweme/feed/model/Aweme;
            move-result-object p0
            invoke-virtual {p0}, Lcom/ss/android/ugc/aweme/feed/model/Aweme;->getMusic()$MUSIC
            move-result-object p0
            return-object p0
        """)
        body("musicId", 1, """
            check-cast p0, $MUSIC
            invoke-virtual {p0}, $MUSIC->getIdStr()Ljava/lang/String;
            move-result-object p0
            return-object p0
        """)
        if(classDefBy(FAVORITES).fields.none {it.name=="LLJZ" && it.type=="Landroid/view/View;"})
            throw PatchException("Native Favorites popup width fingerprint failed")
        body("favoritesRoot",1,"""
            check-cast p0, $FAVORITES
            iget-object p0, p0, $FAVORITES->LLJZ:Landroid/view/View;
            return-object p0
        """)
        val delayFlag="LX/1W01;"
        val quickFavoriteTouch=classDefBy("LX/0CMh;").methods.filter { it.name=="LIZ" }
        if (quickFavoriteTouch.none { method -> method.implementation?.instructions?.any {
                ((it as? ReferenceInstruction)?.reference as? MethodReference)?.let { ref ->
                    ref.definingClass==delayFlag && ref.name=="LJ" && ref.parameterTypes.isEmpty() && ref.returnType=="Z"
                }==true
            }==true }) throw PatchException("Native Favorites hold-delay fingerprint failed")
        body("holdDelayMillis", 2, """
            invoke-static {}, $delayFlag->LJ()Z
            move-result v0
            if-eqz v0, :standard_hold
            const-wide/16 v0, 500
            return-wide v0
            :standard_hold
            const-wide/16 v0, 200
            return-wide v0
        """)
        val collectVm="Lcom/ss/android/ugc/aweme/music/v2/viewmodel/MusicCollectViewModel;"
        val account="Lcom/ss/android/ugc/aweme/IAccountUserService;"
        val login="LX/057u;"
        val nativeCollect=classDefBy(collectVm)
        if (nativeCollect.methods.none { it.name=="PQ2" && it.parameterTypes.isEmpty() && it.returnType=="V"
                && it.accessFlags and 1!=0 && it.implementation?.instructions?.any { insn ->
                    ((insn as? ReferenceInstruction)?.reference as? MethodReference)?.name=="withState"
                }==true }
            || nativeCollect.fields.none { it.name=="LL" && it.type=="Ljava/lang/String;" && it.accessFlags and 1!=0 }
            || nativeCollect.fields.none { it.name=="LLJJIJI" && it.type==MUSIC && it.accessFlags and 1!=0 })
            throw PatchException("Native TikTok sound-save ViewModel fingerprint failed")
        if (classDefBy("LX/041Z;").methods.none { it.name=="LJFF" && it.parameterTypes.isEmpty()
                && it.returnType==account && it.accessFlags and 9==9 }
            || classDefBy(account).methods.none { it.name=="isLogin" && it.parameterTypes.isEmpty() && it.returnType=="Z" }
            || classDefBy(login).methods.none { it.name=="LIZIZ" && it.parameterTypes==listOf(
                "Landroid/app/Activity;","Ljava/lang/String;","Ljava/lang/String;","Landroid/os/Bundle;","LX/07Gd;")
                && it.returnType=="V" && it.accessFlags and 9==9 })
            throw PatchException("Native TikTok sound-save sign-in fingerprint failed")
        // Start from the ViewModel's native default uncollected state rather than toggling an
        // already-collected item off. Prompt through TikTok's normal sign-in route if needed.
        body("collect", 6, """
            invoke-static {}, LX/041Z;->LJFF()$account
            move-result-object v0
            invoke-interface {v0}, $account->isLogin()Z
            move-result v0
            if-nez v0, :save
            const-string v0, "music_page"
            const-string v1, "click_favorite_music"
            const/4 v2, 0x0
            invoke-static {p0, v0, v1, v2, v2}, $login->LIZIZ(Landroid/app/Activity;Ljava/lang/String;Ljava/lang/String;Landroid/os/Bundle;LX/07Gd;)V
            return-void
            :save
            check-cast p1, $MUSIC
            invoke-virtual {p1}, $MUSIC->getIdStr()Ljava/lang/String;
            move-result-object v0
            new-instance v1, $collectVm
            invoke-direct {v1}, $collectVm-><init>()V
            iput-object v0, v1, $collectVm->LL:Ljava/lang/String;
            iput-object p1, v1, $collectVm->LLJJIJI:$MUSIC
            invoke-virtual {v1}, $collectVm->PQ2()V
            return-void
        """)
        val coverClass=mutableClassDefBy(COVER)
        wrapHostMethod(coverClass,cover,"bdcrusherNativeBind",0,"""
            invoke-direct/range {p0 .. p1}, $COVER->bdcrusherNativeBind(Ljava/lang/Object;)V
            invoke-static/range {p0 .. p1}, $R->bindDisc(Ljava/lang/Object;Ljava/lang/Object;)V
            return-void
        """)
        val unbind=find(COVER,"unBind",emptyList())
        if (unbind.returnType!="V" || !unbind.calls(COVER,"Br"))
            throw PatchException("Music disc unbind lifecycle fingerprint failed")
        unbind.addInstructions(0,"invoke-static/range {p0 .. p0}, $R->unbindDisc(Ljava/lang/Object;)V")
        val discTouch=find("LX/09f1;", "onTouch", listOf("Landroid/view/View;", "Landroid/view/MotionEvent;"))
        wrapHostMethod(mutableClassDefBy("LX/09f1;"),discTouch,"bdcrusherNativeDiscTouch",2,"""
            invoke-direct/range {p0 .. p2}, LX/09f1;->bdcrusherNativeDiscTouch(Landroid/view/View;Landroid/view/MotionEvent;)Z
            move-result v0
            invoke-static/range {p1 .. p2}, $R->touch(Landroid/view/View;Landroid/view/MotionEvent;)Z
            move-result v1
            or-int/2addr v0, v1
            return v0
        """)
        // Find the real native listener by its ability, event signature and 300 ms timer, not a guessed name.
        val candidates=mutableListOf<Pair<String,String>>()
        classDefForEach { clazz ->
            if(clazz.fields.none { it.type==EDGE_ABILITY }) return@classDefForEach
            for(method in clazz.methods) {
                val code=method.implementation?.instructions?.toList() ?: continue
                if(method.name!="onTouch" || method.parameterTypes!=listOf("Landroid/view/View;","Landroid/view/MotionEvent;")
                    || method.returnType!="Z") continue
                val hasEligibility=code.any { ((it as? ReferenceInstruction)?.reference as? MethodReference)?.let { ref ->
                    ref.definingClass==EDGE_ABILITY && ref.parameterTypes==listOf("F","F") && ref.returnType=="Z"
                }==true }
                val hasTimer=code.any { (it as? WideLiteralInstruction)?.wideLiteral==300L } && code.any {
                    ((it as? ReferenceInstruction)?.reference as? MethodReference)?.let { ref ->
                        ref.definingClass=="Landroid/os/Handler;" && ref.name=="postDelayed"
                    }==true
                }
                if(hasEligibility && hasTimer) candidates+=clazz.type to method.name
            }
        }
        if(candidates.size!=1) throw PatchException("Sound disc priority: expected one native edge-speed timer listener, found ${candidates.size}")
        val edge=find(candidates.single().first,"onTouch",listOf("Landroid/view/View;","Landroid/view/MotionEvent;"))
        if(edge.implementation!!.registerCount<4 || edge.calls(R,"skipEdgeTouch"))
            throw PatchException("Unsupported or already-patched sound disc priority hook")
        edge.addInstructionsWithLabels(0,"""
            invoke-static/range {p1 .. p2}, $R->skipEdgeTouch(Landroid/view/View;Landroid/view/MotionEvent;)Z
            move-result v0
            if-eqz v0, :stock_edge_touch
            const/4 v0, 0x0
            return v0
        """,ExternalLabel("stock_edge_touch",edge.getInstruction(0)))
        // MainActivity and DetailActivity share this native window dispatcher. Route only a
        // bound disc hit before feed parents/custom dispatch can schedule any long-press timer.
        val dispatchClass=classDefBy(DISPATCH_ACTIVITY)
        if(dispatchClass.superclass!="Lcom/bytedance/ies/foundation/activity/BaseActivity;"
            || classDefBy("Lcom/ss/android/ugc/aweme/detail/ui/DetailActivity;").superclass!=DISPATCH_ACTIVITY)
            throw PatchException("Sound window dispatch ancestry fingerprint failed")
        val dispatch=find(DISPATCH_ACTIVITY,"dispatchTouchEvent",listOf("Landroid/view/MotionEvent;"))
        if(dispatch.returnType!="Z" || dispatch.implementation!!.registerCount<3
            || !dispatch.calls("Landroid/app/Activity;","dispatchTouchEvent")
            || !dispatch.calls("LX/048V;","dispatchTouchEvent") || dispatch.calls(R,"dispatchDiscTouch"))
            throw PatchException("Sound window dispatch fingerprint failed or add-on already applied")
        dispatch.addInstructionsWithLabels(0,"""
            invoke-static/range {p0 .. p1}, $R->dispatchDiscTouch(Landroid/app/Activity;Landroid/view/MotionEvent;)Z
            move-result v0
            if-eqz v0, :stock_window_dispatch
            const/4 v0, 0x1
            return v0
        """,ExternalLabel("stock_window_dispatch",dispatch.getInstruction(0)))
    }
    finalize {
        if (classDefByOrNull(HUSH) == null) throw PatchException("Sound actions add-on requires compatible HushFeed Settings selected in the same run.")
        // Wrap only after execute-time upstream fingerprints have inspected their original body.
        val created=find(COVER,"onViewCreated",listOf("Landroid/view/View;"))
        wrapHostMethod(mutableClassDefBy(COVER),created,"bdcrusherNativeViewsCreated",0,"""
            invoke-direct/range {p0 .. p1}, $COVER->bdcrusherNativeViewsCreated(Landroid/view/View;)V
            invoke-static/range {p0 .. p0}, $R->viewsCreated(Ljava/lang/Object;)V
            return-void
        """)
        // 47.8.3 inlines popup construction into this consumer. Observe immediately after
        // content installation, whether stock or Collections-enhanced, in either source order.
        val owner = mutableClassDefBy("LX/0wHU;")
        val favorites = owner.methods.singleOrNull { it.name == "bdcrusherRenderResponse" }
            ?: find(owner.type,"accept",listOf("Ljava/lang/Object;"))
        val content = favorites.implementation!!.instructions.withIndex().singleOrNull {
            val ref = ((it.value as? ReferenceInstruction)?.reference as? MethodReference)
            ref?.name == "setContentView" || (ref?.definingClass == "Lapp/collectionsaddon/runtime/CollectionsRuntime;" && ref.name == "install")
        } ?: throw PatchException("Sound native Favorites inlined content fingerprint failed")
        val call = content.value as? FiveRegisterInstruction
            ?: throw PatchException("Sound native Favorites content calling convention changed")
        if (call.registerC != 7 || call.registerD != 4) throw PatchException("Sound native Favorites content registers changed")
        favorites.addInstructions(content.index + 1,"invoke-static {v7}, $R->observeFavorites(Ljava/lang/Object;)V")
        // Resolve all compile-only public UI contracts now, after every source has merged.
        for (type in listOf(ENTRY, "Lapp/soundaddon/runtime/SoundSettings${'$'}Page;", R, "Lapp/soundaddon/runtime/SoundRuntime${'$'}Binding;")) {
            val clazz = classDefByOrNull(type) ?: continue
            for (own in clazz.methods) for (insn in own.implementation?.instructions ?: emptyList()) {
                val ref = ((insn as? ReferenceInstruction)?.reference as? MethodReference) ?: continue
                if (!ref.definingClass.startsWith("Lapp/morphe/")) continue
                val target = find(ref.definingClass, ref.name, ref.parameterTypes.map(CharSequence::toString))
                if (target.returnType != ref.returnType || target.accessFlags and 1 == 0)
                    throw PatchException("Unsupported public HushFeed UI contract: $ref")
            }
        }
        // Shared unnamed dependency owns the root menu; no Feed screen section gating.
    }
}
