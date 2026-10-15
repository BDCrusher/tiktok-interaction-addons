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
private const val TITLE = "Lcom/ss/android/ugc/aweme/feed/assem/music/VideoMusicTitleAssem;"
private const val PARAMS = "Lcom/ss/android/ugc/aweme/feed/model/VideoItemParams;"
private const val MUSIC = "Lcom/ss/android/ugc/aweme/music/model/Music;"
private const val PLATFORM = "Lcom/ss/android/ugc/aweme/music/model/DspPlatform;"
private const val INFO = "Lcom/ss/android/ugc/aweme/music/model/TT2DSPSongInfo;"
private const val CONTEXT = "Lcom/ss/android/ugc/aweme/music/model/AddSongContextData;"
private const val SERVICE = "LX/0Af8;"
private const val HUSH = "Lapp/morphe/extension/tiktok/settings/preference/TikTokPreferenceFragment;"
private const val EDGE_ABILITY = "Lcom/ss/android/ugc/aweme/feed/longvideo/edgespeedup/EdgeSpeedupAbility;"
private const val DISPATCH_ACTIVITY = "LX/03uJ;"
private const val FAVORITES = "LX/0yck;"
private fun BytecodePatchContext.find(type: String, name: String, params: List<String>): MutableMethod =
    mutableClassDefByOrNull(type)?.methods?.singleOrNull { it.name == name && it.parameterTypes.map(CharSequence::toString) == params }
        ?: throw PatchException("Sound actions add-on: unsupported $type->$name; requires original TikTok 47.1.4 and compatible HushFeed Settings (targeted against 0.74.0).")
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
    description = "Hold the music disc for native sound saving and eligible music-app destinations. Configure under BDCrusher's Settings. Requires compatible HushFeed Settings. Experimental; device validation pending.",
    default = false,
) {
    category("Interaction")
    compatibleWith(Compatibility(name = "TikTok", packageName = "com.zhiliaoapp.musically", appIconColor = 0xFE2C55,
        signatures = setOf("9041803e91bcb814b4b4399fb5c85a91640b755e5e8ba76813814bf4cf2ab5ba"),
        targets = listOf(AppTarget(version = "47.1.4", versionCodes = mapOf(SupportedAbi.ARM64_V8A to 2024701040,
            SupportedAbi.ARMEABI_V7A to 2024701040), isExperimental = true, minSdk = 23))))
    extendWith("extensions/sound.mpe")
    dependsOn(settingsHubPatch)
    execute {
        if (packageMetadata.versionName != "47.1.4" || packageMetadata.versionCode != "2024701040")
            throw PatchException("Sound button actions supports original TikTok 47.1.4 (2024701040) only.")
        val cover = find(COVER, "z4", listOf("Ljava/lang/Object;"))
        val title = find(TITLE, "z4", listOf("Ljava/lang/Object;"))
        if (cover.calls(R, "bindDisc") || title.calls(R, "bindTitle"))
            throw PatchException("Sound actions add-on is already applied or selected twice. Use the original APK.")
        if (listOf("LLLLLLL","LLLLLLLLL").any { field -> classDefBy(COVER).fields.none {
                it.name == field && it.type == "Landroid/widget/FrameLayout;" } })
            throw PatchException("Music disc binding fingerprint failed")
        if (!cover.calls("Lcom/ss/android/ugc/aweme/feed/assem/music/BaseMusicCoverAssem;", "Pr"))
            throw PatchException("Music disc lifecycle fingerprint failed")
        val created = find(COVER, "onViewCreated", listOf("Landroid/view/View;"))
        if (created.implementation!!.instructions.none {
            ((it as? ReferenceInstruction)?.reference as? TypeReference)?.type == "LX/07pb;"
        }) throw PatchException("Music disc touch listener fingerprint failed")
        body("disc", 1, """
            check-cast p0, $COVER
            iget-object p0, p0, $COVER->LLLLLLL:Landroid/widget/FrameLayout;
            return-object p0
        """)
        body("alternateDisc", 1, """
            check-cast p0, $COVER
            iget-object p0, p0, $COVER->LLLLLLLLL:Landroid/widget/FrameLayout;
            return-object p0
        """)
        val bound=find("Lcom/bytedance/assem/arch/reused/ReusedAssem;","rq",emptyList())
        if(bound.returnType!="Ljava/lang/Object;" || bound.implementation!!.instructions.none {
                ((it as? ReferenceInstruction)?.reference as? FieldReference)?.name=="LLJJL"
            }) throw PatchException("Music current bound-data fingerprint failed")
        body("boundParams", 1, """
            check-cast p0, Lcom/bytedance/assem/arch/reused/ReusedAssem;
            invoke-virtual {p0}, Lcom/bytedance/assem/arch/reused/ReusedAssem;->rq()Ljava/lang/Object;
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
        body("titleActivity",1,"""
            check-cast p0, $TITLE
            invoke-static {p0}, LX/05GG;->LIZ(Landroidx/lifecycle/LifecycleOwner;)LX/02HQ;
            move-result-object p0
            return-object p0
        """)
        val detailApi="Lcom/ss/android/ugc/aweme/music/api/MusicDetailApi;"
        val detailType="Lcom/ss/android/ugc/aweme/music/api/MusicDetailApi${'$'}DetailApi;"
        val detail="Lcom/ss/android/ugc/aweme/music/model/MusicDetail;"
        if(classDefBy(detailApi).fields.none {it.name=="LIZ" && it.type==detailType}
            || find(detailType,"queryMusic",listOf("Ljava/lang/String;","I")).returnType!="Lcom/google/common/util/concurrent/ListenableFuture;"
            || listOf("music","matchedPgcMusicInfo").any {name->classDefBy(detail).fields.none {it.name==name && it.type==MUSIC}})
            throw PatchException("Native song detail client fingerprint failed")
        body("queryMusic",3,"""
            sget-object v0, $detailApi->LIZ:$detailType
            const/4 v1, 0x0
            invoke-interface {v0, p0, v1}, $detailType->queryMusic(Ljava/lang/String;I)Lcom/google/common/util/concurrent/ListenableFuture;
            move-result-object v0
            return-object v0
        """)
        for((method,field) in listOf("detailMusic" to "music","detailMatchedMusic" to "matchedPgcMusicInfo")) {
            body(method,2,"""
                check-cast p0, $detail
                iget v0, p0, Lcom/ss/android/ugc/aweme/base/api/BaseResponse;->status_code:I
                if-nez v0, :no_music
                iget-object p0, p0, $detail->$field:$MUSIC
                return-object p0
                :no_music
                const/4 p0, 0x0
                return-object p0
            """)
        }
        body("matchedId",1,"""
            check-cast p0, $MUSIC
            invoke-virtual {p0}, $MUSIC->getMatchedSongInfo()Lcom/ss/android/ugc/aweme/music/model/MatchedSoundInfo;
            move-result-object p0
            if-eqz p0, :no_match
            invoke-virtual {p0}, Lcom/ss/android/ugc/aweme/music/model/MatchedSoundInfo;->getId()Ljava/lang/String;
            move-result-object p0
            return-object p0
            :no_match
            const/4 p0, 0x0
            return-object p0
        """)
        if(classDefBy(FAVORITES).fields.none {it.name=="LLJLL" && it.type=="Landroid/view/View;"})
            throw PatchException("Native Favorites popup width fingerprint failed")
        body("favoritesRoot",1,"""
            check-cast p0, $FAVORITES
            iget-object p0, p0, $FAVORITES->LLJLL:Landroid/view/View;
            return-object p0
        """)
        body("destinations", 2, """
            check-cast p0, $MUSIC
            sget-object v0, $SERVICE->LIZIZ:$SERVICE
            invoke-virtual {v0, p0}, $SERVICE->LJJJJI($MUSIC)Ljava/util/List;
            move-result-object v0
            return-object v0
        """)
        body("provider", 1, """
            check-cast p0, $INFO
            iget-object p0, p0, $INFO->dspPlatform:Ljava/lang/Integer;
            return-object p0
        """)
        body("platform", 2, """
            sget-object v0, $PLATFORM->Companion:Lcom/ss/android/ugc/aweme/music/model/DspPlatform${'$'}Companion;
            invoke-virtual {v0, p0}, Lcom/ss/android/ugc/aweme/music/model/DspPlatform${'$'}Companion;->from(Ljava/lang/Integer;)$PLATFORM
            move-result-object v0
            return-object v0
        """)
        body("label", 1, """
            check-cast p0, $PLATFORM
            invoke-static {p0}, LX/0lAV;->LIZIZ($PLATFORM)Ljava/lang/String;
            move-result-object p0
            return-object p0
        """)
        body("add", 20, """
            move-object/from16 v0, p0
            check-cast v0, $TITLE
            move-object/from16 v1, p1
            check-cast v1, $PARAMS
            move-object/from16 v2, p2
            check-cast v2, $MUSIC
            invoke-static {v1}, LX/09JO;->LIZ($PARAMS)LX/09JU;
            move-result-object v3
            sget-object v6, $SERVICE->LIZIZ:$SERVICE
            invoke-virtual {v6, v2}, $SERVICE->LJIIJ($MUSIC)$PLATFORM
            move-result-object v4
            const/4 v5, 0x0
            invoke-virtual/range {v0 .. v5}, $TITLE->Qr(${PARAMS}${MUSIC}LX/09JU;${PLATFORM}Z)$CONTEXT
            move-result-object v3
            invoke-static {v0}, LX/05GG;->LIZ(Landroidx/lifecycle/LifecycleOwner;)LX/02HQ;
            move-result-object v1
            invoke-static {v0}, LX/05GG;->LJI(Landroidx/lifecycle/LifecycleOwner;)Landroidx/fragment/app/Fragment;
            move-result-object v2
            move-object/from16 v4, p3
            check-cast v4, $PLATFORM
            move-object/from16 v5, p4
            const/4 v7, 0x0
            move-object v0, v6
            move v6, v7
            invoke-virtual/range {v0 .. v6}, $SERVICE->LJJIIZ(Landroid/app/Activity;Landroidx/fragment/app/Fragment;${CONTEXT}${PLATFORM}Ljava/util/List;Z)V
            return-void
        """)
        body("collect", 2, """
            check-cast p0, $MUSIC
            invoke-virtual {p0}, $MUSIC->getIdStr()Ljava/lang/String;
            move-result-object p0
            new-instance v0, Lcom/ss/android/ugc/aweme/music/v2/viewmodel/MusicCollectViewModel;
            invoke-direct {v0}, Lcom/ss/android/ugc/aweme/music/v2/viewmodel/MusicCollectViewModel;-><init>()V
            iput-object p0, v0, Lcom/ss/android/ugc/aweme/music/v2/viewmodel/MusicCollectViewModel;->LL:Ljava/lang/String;
            invoke-virtual {v0}, Lcom/ss/android/ugc/aweme/music/v2/viewmodel/MusicCollectViewModel;->xU2()V
            return-void
        """)
        fun bindWrapper(type:String,method:MutableMethod,callback:String) {
            wrapHostMethod(mutableClassDefBy(type),method,"bdcrusherNativeBind",0,"""
                invoke-direct/range {p0 .. p1}, $type->bdcrusherNativeBind(Ljava/lang/Object;)V
                invoke-static/range {p0 .. p1}, $R->$callback(Ljava/lang/Object;Ljava/lang/Object;)V
                return-void
            """)
        }
        bindWrapper(COVER,cover,"bindDisc")
        bindWrapper(TITLE,title,"bindTitle")
        val coverClass=mutableClassDefBy(COVER)
        val unbind=coverClass.methods.singleOrNull {it.name=="unBind" && it.parameterTypes.isEmpty()}
        if(unbind!=null) {
            unbind.addInstructions(0,"invoke-static/range {p0 .. p0}, $R->unbindDisc(Ljava/lang/Object;)V")
        } else {
            // Cover inherits this lifecycle callback; title supplies its own override.
            val parent=coverClass.superclass ?: throw PatchException("Missing music cover superclass")
            val inherited=generateSequence(classDefByOrNull(parent)) {it.superclass?.let(::classDefByOrNull)}
                .flatMap {it.methods.asSequence()}.firstOrNull {it.name=="unBind" && it.parameterTypes.isEmpty() && it.returnType=="V"}
                ?: throw PatchException("Missing inherited music cover unBind lifecycle")
            if(inherited.accessFlags and 16 != 0) throw PatchException("Music cover unBind unexpectedly final")
            val override=MutableMethod(ImmutableMethod(COVER,"unBind",emptyList(),"V",1,
                emptySet(),emptySet(),MutableMethodImplementation(1)))
            override.addInstructions(0,"""
                invoke-static/range {p0 .. p0}, $R->unbindDisc(Ljava/lang/Object;)V
                invoke-super/range {p0 .. p0}, $parent->unBind()V
                return-void
            """.trimIndent())
            coverClass.methods.add(override)
        }
        find(TITLE, "unBind", emptyList()).addInstructions(0, "invoke-static/range {p0 .. p0}, $R->unbindTitle(Ljava/lang/Object;)V")
        val discTouch=find("LX/07pb;", "onTouch", listOf("Landroid/view/View;", "Landroid/view/MotionEvent;"))
        wrapHostMethod(mutableClassDefBy("LX/07pb;"),discTouch,"bdcrusherNativeDiscTouch",2,"""
            invoke-direct/range {p0 .. p2}, LX/07pb;->bdcrusherNativeDiscTouch(Landroid/view/View;Landroid/view/MotionEvent;)Z
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
            || !dispatch.calls("LX/04UK;","dispatchTouchEvent") || dispatch.calls(R,"dispatchDiscTouch"))
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
        val favorites=find(FAVORITES,"Qk",listOf("Ljava/util/List;"))
        wrapHostMethod(mutableClassDefBy(FAVORITES),favorites,"bdcrusherNativeFavoritesWidth",0,"""
            invoke-direct/range {p0 .. p1}, $FAVORITES->bdcrusherNativeFavoritesWidth(Ljava/util/List;)V
            invoke-static/range {p0 .. p0}, $R->observeFavorites(Ljava/lang/Object;)V
            return-void
        """)
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
