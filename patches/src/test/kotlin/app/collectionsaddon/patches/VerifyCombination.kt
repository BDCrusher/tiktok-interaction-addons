package app.collectionsaddon.patches
import app.morphe.patcher.Patcher
import app.morphe.patcher.PatcherConfig
import app.morphe.patcher.patch.loadPatchesFromJar
import app.morphe.patcher.patch.Patch
import java.net.URLClassLoader
import com.android.tools.smali.dexlib2.Opcodes
import com.android.tools.smali.dexlib2.dexbacked.DexBackedDexFile
import com.android.tools.smali.dexlib2.iface.ClassDef
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.instruction.RegisterRangeInstruction
import com.google.gson.GsonBuilder
import java.security.MessageDigest
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import java.io.File
import java.nio.ByteBuffer
import java.nio.file.Files

/** Local diagnostic: compile output only, never install or distribute the host APK. */
fun main(args: Array<String>) = runBlocking {
    val input = File(args[0]); val hush = File(args[1]); val addon = File(args[2]); val root = File(args[3]).apply { mkdirs() }
    val mode = args[4]; val order = args[5]
    val hushPatches = loadPatchesFromJar(setOf(hush))
    fun loadAddon(): Set<Patch<*>> {
        // Do not accidentally test the main-source classes on this diagnostic's own classpath.
        val loader = object : URLClassLoader(arrayOf(addon.toURI().toURL()), Patch::class.java.classLoader) {
            override fun loadClass(name: String, resolve: Boolean): Class<*> {
                if (!name.startsWith("app.collectionsaddon.patches.") && !name.startsWith("app.soundaddon.patches.") && !name.startsWith("app.bdcrusher.patches.")) return super.loadClass(name, resolve)
                synchronized(getClassLoadingLock(name)) {
                    val clazz = findLoadedClass(name) ?: findClass(name)
                    if (resolve) resolveClass(clazz)
                    return clazz
                }
            }
            override fun getResource(name: String) =
                if (name in setOf("extensions/collections.mpe", "extensions/sound.mpe", "extensions/settings.mpe")) findResource(name) else super.getResource(name)
        }
        val result = linkedSetOf<Patch<*>>()
        if (!mode.startsWith("sound")) {
            val clazz = loader.loadClass("app.collectionsaddon.patches.CollectionsPatchKt")
            result.add(clazz.getMethod("getEnhancedCollectionsPatch").invoke(null) as Patch<*>)
        }
        if (mode.startsWith("sound") || mode.startsWith("both")) {
            val clazz = loader.loadClass("app.soundaddon.patches.SoundPatchKt")
            result.add(clazz.getMethod("getSoundButtonActionsPatch").invoke(null) as Patch<*>)
        }
        return result
    }
    val addonPatches = loadAddon()
    check(addonPatches.size == if(mode.startsWith("both")) 2 else 1)
    val coreNames = setOf("Settings", "Long-press controls", "Keep the Favorites tab", "Hide feed save button")
    val extendedNames = coreNames + setOf("AMOLED dark theme", "Remember clear display", "Change app name", "Look like the store app")
    val selectedHush = when (mode) {
        "missing", "sound-missing" -> emptySet()
        "sound-minimal", "collections-minimal" -> hushPatches.filter { it.name == "Settings" }.toSet()
        "defaults", "sound-defaults", "both-defaults" -> hushPatches.filter { it.default }.toSet()
        "both-all" -> hushPatches.filter { it.name != null }.toSet()
        "amoled" -> hushPatches.filter { it.name in coreNames || it.name == "AMOLED dark theme" }.toSet()
        "extended", "sound-extended", "both-extended" -> hushPatches.filter { it.name in extendedNames }.toSet()
        else -> hushPatches.filter { it.name in coreNames }.toSet()
    }
    if (mode !in setOf("missing", "defaults", "sound-missing", "sound-minimal", "collections-minimal", "sound-defaults", "both-defaults")) check(selectedHush.mapNotNull { it.name }.containsAll(coreNames))
    if (mode in setOf("extended", "sound-extended", "both-extended")) check(selectedHush.mapNotNull { it.name }.containsAll(extendedNames))
    check(mode in setOf("missing", "sound-missing") || selectedHush.isNotEmpty()) { "No external patches selected" }
    val selected = if (order == "addon-first") addonPatches + selectedHush else selectedHush + addonPatches
    val ordered = if (mode.endsWith("duplicate")) selected + loadAddon() else selected
    val run = Files.createTempDirectory(root.toPath(), "$mode-$order-").toFile().canonicalFile
    val scratch = run.resolve("scratch")
    check(scratch.toPath().startsWith(root.canonicalFile.toPath()) && !scratch.exists())
    println("Mode $mode / $order. Hush patches: ${selectedHush.map { it.name }.sortedBy { it }}")
    fun hash(file: File) = file.inputStream().use { stream ->
        val digest = MessageDigest.getInstance("SHA-256"); val buffer = ByteArray(65536)
        var count = stream.read(buffer)
        while (count >= 0) { if (count > 0) digest.update(buffer, 0, count); count = stream.read(buffer) }
        digest.digest().joinToString("") { "%02x".format(it) }
    }
    val report = linkedMapOf<String, Any>("selection" to mode, "order" to order,
        "hushPatches" to selectedHush.mapNotNull { it.name }.sorted(), "apkSha256" to hash(input),
        "hushBundleSha256" to hash(hush), "addonSha256" to hash(addon), "androidDeviceTested" to false)
    fun save() {
        val output = root.parentFile.parentFile.resolve("verification/reports").apply { mkdirs() }
        output.resolve("flat7-${hush.nameWithoutExtension}-$mode-$order.json").writeText(GsonBuilder().setPrettyPrinting().create().toJson(report))
    }
    Patcher(PatcherConfig(input.canonicalFile, temporaryFilesPath = scratch)).use { patcher ->
        patcher += ordered
        val results = patcher().toList()
        results.forEach { println("${it.patch.name ?: "internal"}: ${it.exception?.stackTraceToString() ?: "PASS"}") }
        if (mode == "missing" || mode == "sound-missing") {
            check(results.any { result -> generateSequence(result.exception as Throwable?) { it.cause }
                .any { it.message?.contains("requires compatible HushFeed Settings") == true } })
            println("PASS: missing HushFeed settings refused explicitly")
            report["result"] = "PASS: missing dependency rejected"; save()
            return@use
        }
        if (mode.endsWith("duplicate")) {
            check(results.any { result -> generateSequence(result.exception as Throwable?) { it.cause }
                .any { it.message?.contains("already applied or selected twice") == true } })
            report["result"] = "PASS: duplicate add-on rejected"; save()
            println("PASS: duplicate add-on selection refused explicitly")
            return@use
        }
        if (results.any { it.exception != null }) {
            report["result"] = "FAIL: combined patch execution"
            report["failures"] = results.filter { it.exception != null }.map {
                mapOf("patch" to (it.patch.name ?: "internal"), "error" to it.exception!!.stackTraceToString())
            }
            save()
            error("Combined patch execution failed; see verification report")
        }
        val result = patcher.get()
        val classes = HashMap<String, ClassDef>()
        for (dex in result.dexFiles) {
            val bytes = dex.stream.use { it.readBytes() }
            val parsed = DexBackedDexFile(Opcodes.getDefault(), ByteBuffer.wrap(bytes))
            for (clazz in parsed.classes) check(classes.put(clazz.type, clazz) == null) { "Duplicate class ${clazz.type}" }
            println("Compiled ${dex.name}: ${parsed.classes.size} classes")
        }
        fun resolves(ref: MethodReference, type: String = ref.definingClass): Boolean {
            val clazz = classes[type] ?: return type.startsWith("Ljava/") || type.startsWith("Landroid/")
            if (clazz.methods.any { it.name == ref.name && it.parameterTypes == ref.parameterTypes && it.returnType == ref.returnType }) return true
            if (clazz.interfaces.any { resolves(ref, it) }) return true
            return clazz.superclass?.let { resolves(ref, it) } ?: false
        }
        fun resolvesField(ref: FieldReference, type: String = ref.definingClass): Boolean {
            val clazz = classes[type] ?: return false
            if (clazz.fields.any { it.name == ref.name && it.type == ref.type }) return true
            return clazz.superclass?.let { resolvesField(ref, it) } ?: false
        }
        val own = classes.values.filter { it.type.startsWith("Lapp/collectionsaddon/") || it.type.startsWith("Lapp/soundaddon/") || it.type.startsWith("Lapp/bdcrusher/") }
        check(own.isNotEmpty())
        for (clazz in own) for (method in clazz.methods) for (insn in (method.implementation?.instructions ?: emptyList())) {
            val reference = (insn as? ReferenceInstruction)?.reference
            val field = reference as? FieldReference
            if (field != null && (field.definingClass.startsWith("LX/") || field.definingClass.startsWith("Lcom/ss/") || field.definingClass.startsWith("Lapp/morphe/") || field.definingClass.startsWith("Lapp/bdcrusher/"))) {
                check(resolvesField(field)) { "Unresolved injected field: $field" }
            }
            val ref = reference as? MethodReference ?: continue
            if (ref.definingClass.startsWith("LX/") || ref.definingClass.startsWith("Lapp/morphe/") || ref.definingClass.startsWith("Lapp/bdcrusher/") || ref.definingClass.startsWith("Lapp/collectionsaddon/") || ref.definingClass.startsWith("Lapp/soundaddon/") || ref.definingClass.startsWith("Lcom/bytedance/") || ref.definingClass.startsWith("Lcom/ss/")) {
                check(resolves(ref)) { "Unresolved injected reference: $ref in ${method.definingClass}->${method.name}" }
            }
        }
        if (mode.startsWith("sound")) check(classes.keys.none { it.startsWith("Lapp/collectionsaddon/") })
        if (!mode.startsWith("sound") && !mode.startsWith("both")) check(classes.keys.none { it.startsWith("Lapp/soundaddon/") })
        if (!mode.startsWith("sound")) check(classes["Lapp/collectionsaddon/runtime/NativeCallback;"]!!.interfaces.contains("LX/02uH;"))
        val hostMenu = classes["Lapp/morphe/extension/tiktok/settings/preference/TikTokPreferenceFragment;"]!!.methods.single { it.name == "createMasterMenu" }
        val entryCalls = hostMenu.implementation!!.instructions.count {
            ((it as? ReferenceInstruction)?.reference as? MethodReference)?.definingClass == "Lapp/bdcrusher/runtime/SettingsHub;"
        }
        check(entryCalls == hostMenu.implementation!!.instructions.count { it.opcode == com.android.tools.smali.dexlib2.Opcode.RETURN_VOID })
        val hostResume = classes["Lapp/morphe/extension/tiktok/settings/preference/TikTokPreferenceFragment;"]!!.methods.single { it.name == "onResume" }
        check(hostResume.implementation!!.instructions.count {
            ((it as? ReferenceInstruction)?.reference as? MethodReference)?.let { ref ->
                ref.definingClass == "Lapp/bdcrusher/runtime/SettingsHub;" && ref.name == "refresh"
            } == true
        } == hostResume.implementation!!.instructions.count { it.opcode == com.android.tools.smali.dexlib2.Opcode.RETURN_VOID })
        if (mode.startsWith("sound") || mode.startsWith("both")) {
            val section = classes["Lapp/morphe/extension/tiktok/settings/preference/TikTokPreferenceFragment;"]!!.methods.single { it.name == "createSectionMenu" }
            check(section.implementation!!.instructions.count {
                ((it as? ReferenceInstruction)?.reference as? MethodReference)?.let { ref -> ref.definingClass == "Lapp/soundaddon/runtime/SoundSettings;" && ref.name == "install" } == true
            } == 0)
        }
        check(classes.containsKey("Lapp/bdcrusher/runtime/SettingsHub;"))
        fun wrapper(type:String,name:String,native:String,count:Int) {
            val clazz=classes[type] ?: error("Missing wrapped host $type")
            val method=clazz.methods.single {it.name==name}
            val code=method.implementation!!.instructions.toList()
            check(code.map {it.opcode}==listOf(com.android.tools.smali.dexlib2.Opcode.INVOKE_DIRECT_RANGE,
                com.android.tools.smali.dexlib2.Opcode.INVOKE_STATIC_RANGE,com.android.tools.smali.dexlib2.Opcode.RETURN_VOID))
                { "Unsafe argument-preserving wrapper: $type->$name" }
            check(method.implementation!!.registerCount==count)
            check((code[0] as RegisterRangeInstruction).startRegister==0 && (code[0] as RegisterRangeInstruction).registerCount==count)
            check((code[1] as RegisterRangeInstruction).startRegister==0 && (code[1] as RegisterRangeInstruction).registerCount==count)
            check(((code[0] as ReferenceInstruction).reference as MethodReference).name==native)
            check(clazz.methods.single {it.name==native}.accessFlags and 2 != 0)
        }
        val settingsType="Lapp/morphe/extension/tiktok/settings/preference/TikTokPreferenceFragment;"
        wrapper(settingsType,"createMasterMenu","bdcrusherNative_createMasterMenu",3)
        wrapper(settingsType,"onResume","bdcrusherNative_onResume",1)
        report["settingsArgumentsPreserved"]=true
        val menuType="Lapp/morphe/extension/tiktok/settings/preference/SettingsMenuPreference;"
        val menuCtor=classes[menuType]!!.methods.filter {it.name=="<init>"}
        check(menuCtor.any {it.parameterTypes.size==5})
        val hubRefs=classes["Lapp/bdcrusher/runtime/SettingsHub;"]!!.methods.flatMap {method->
            method.implementation?.instructions?.mapNotNull {(it as? ReferenceInstruction)?.reference as? MethodReference}?.toList() ?: emptyList()
        }
        check(hubRefs.any {it.definingClass==menuType && it.name=="<init>" && it.parameterTypes.size==5})
        check(hubRefs.none {it.definingClass==menuType && it.name=="setActiveCount"})
        report["fiveArgumentMenuConstructorBound"]=true
        report["removedBadgeApiNotReferenced"]=true
        fun references(type:String,name:String):List<Any> = classes[type]!!.methods.single {it.name==name}
            .implementation!!.instructions.mapNotNull {(it as? ReferenceInstruction)?.reference}.toList()
        val haptic=references("Lapp/bdcrusher/runtime/NativeUiBridge;","haptic").filterIsInstance<MethodReference>()
        check(haptic.any {it.definingClass=="LX/0d6x;" && it.name=="<init>" && it.parameterTypes==listOf("Landroid/content/Context;")})
        check(haptic.any {it.definingClass=="LX/0d6x;" && it.name=="LIZ" && it.parameterTypes==listOf("I")})
        report["nativeFavoritesHapticBound"]=true
        if(mode.startsWith("sound") || mode.startsWith("both")) {
            wrapper("Lcom/ss/android/ugc/aweme/feed/assem/music/VideoMusicCoverAssem;","z4","bdcrusherNativeBind",2)
            wrapper("Lcom/ss/android/ugc/aweme/feed/assem/music/VideoMusicTitleAssem;","z4","bdcrusherNativeBind",2)
            val guarded=classes.values.flatMap {clazz->clazz.methods.toList()}.filter {method->
                method.implementation?.instructions?.any {
                    ((it as? ReferenceInstruction)?.reference as? MethodReference)?.let {ref->
                        ref.definingClass=="Lapp/soundaddon/runtime/SoundRuntime;" && ref.name=="skipEdgeTouch"
                    }==true
                }==true && !method.definingClass.startsWith("Lapp/")
            }
            check(guarded.size==1) {"Missing/duplicate native sound-disc priority guard"}
            val code=guarded.single().implementation!!.instructions.toList()
            val call=((code.first() as ReferenceInstruction).reference as MethodReference)
            check(call.name=="skipEdgeTouch" && (code.first() as RegisterRangeInstruction).registerCount==2)
            check(code.take(5).map {it.opcode}==listOf(com.android.tools.smali.dexlib2.Opcode.INVOKE_STATIC_RANGE,
                com.android.tools.smali.dexlib2.Opcode.MOVE_RESULT,com.android.tools.smali.dexlib2.Opcode.IF_EQZ,
                com.android.tools.smali.dexlib2.Opcode.CONST_4,com.android.tools.smali.dexlib2.Opcode.RETURN))
            report["soundBindingsArgumentsPreserved"]=true
            report["soundDiscPriorityBeforeNativeTimer"]=true
            report["soundEdgeListener"]=guarded.single().definingClass
            val window=classes["LX/03uJ;"]!!.methods.single {it.name=="dispatchTouchEvent"}
            val windowCode=window.implementation!!.instructions.toList()
            check(windowCode.take(5).map {it.opcode}==listOf(com.android.tools.smali.dexlib2.Opcode.INVOKE_STATIC_RANGE,
                com.android.tools.smali.dexlib2.Opcode.MOVE_RESULT,com.android.tools.smali.dexlib2.Opcode.IF_EQZ,
                com.android.tools.smali.dexlib2.Opcode.CONST_4,com.android.tools.smali.dexlib2.Opcode.RETURN))
            check(((windowCode.first() as ReferenceInstruction).reference as MethodReference).let {
                it.definingClass=="Lapp/soundaddon/runtime/SoundRuntime;" && it.name=="dispatchDiscTouch"
            })
            check((windowCode.first() as RegisterRangeInstruction).let {
                it.registerCount==2 && it.startRegister==window.implementation!!.registerCount-2
            })
            val alternate=classes["Lapp/soundaddon/runtime/NativeBridge;"]!!.methods.single {it.name=="alternateDisc"}
            check(alternate.implementation!!.instructions.any {
                ((it as? ReferenceInstruction)?.reference as? FieldReference)?.name=="LLLLLLLLL"
            })
            report["soundWindowDispatchBeforeFeedParents"]=true
            report["soundAlternateDiscBound"]=true
            val created=classes["Lcom/ss/android/ugc/aweme/feed/assem/music/VideoMusicCoverAssem;"]!!.methods.single {it.name=="onViewCreated"}
            val creationCode=created.implementation!!.instructions.toList()
            check(creationCode.map{it.opcode}==listOf(com.android.tools.smali.dexlib2.Opcode.INVOKE_DIRECT_RANGE,
                com.android.tools.smali.dexlib2.Opcode.INVOKE_STATIC_RANGE,com.android.tools.smali.dexlib2.Opcode.RETURN_VOID))
            check(((creationCode[0] as ReferenceInstruction).reference as MethodReference).name=="bdcrusherNativeViewsCreated")
            check(((creationCode[1] as ReferenceInstruction).reference as MethodReference).name=="viewsCreated")
            check((creationCode[1] as RegisterRangeInstruction).registerCount==1)
            report["soundBindBeforeInflationCovered"]=true
            val favorites=classes["LX/0yck;"]!!.methods.single {it.name=="Qk"}
            val favoriteCode=favorites.implementation!!.instructions.toList()
            check(favoriteCode.map {it.opcode}==listOf(com.android.tools.smali.dexlib2.Opcode.INVOKE_DIRECT_RANGE,
                com.android.tools.smali.dexlib2.Opcode.INVOKE_STATIC_RANGE,com.android.tools.smali.dexlib2.Opcode.RETURN_VOID))
            check(((favoriteCode[0] as ReferenceInstruction).reference as MethodReference).name=="bdcrusherNativeFavoritesWidth")
            check(((favoriteCode[1] as ReferenceInstruction).reference as MethodReference).let {
                it.definingClass=="Lapp/soundaddon/runtime/SoundRuntime;" && it.name=="observeFavorites"
            })
            check((favoriteCode[0] as RegisterRangeInstruction).registerCount==2)
            check((favoriteCode[1] as RegisterRangeInstruction).registerCount==1)
            val bridge="Lapp/soundaddon/runtime/NativeBridge;"
            val query=references(bridge,"queryMusic").filterIsInstance<MethodReference>().single {it.name=="queryMusic"}
            check(query.definingClass=="Lcom/ss/android/ugc/aweme/music/api/MusicDetailApi${'$'}DetailApi;"
                && query.parameterTypes==listOf("Ljava/lang/String;","I")
                && query.returnType=="Lcom/google/common/util/concurrent/ListenableFuture;")
            for((name,field) in listOf("detailMusic" to "music","detailMatchedMusic" to "matchedPgcMusicInfo")) {
                val refs=references(bridge,name).filterIsInstance<FieldReference>()
                check(refs.any {it.name=="status_code"} && refs.any {it.name==field})
            }
            check(references(bridge,"matchedId").filterIsInstance<MethodReference>().any {it.name=="getMatchedSongInfo"})
            report["nativeMusicDetailFutureBound"]=true
            report["nativeDetailStatusAndMatchedIdChecked"]=true
            report["nativeFavoritesWidthObservedWithoutReplacingMenu"]=true
        }
        report["settingsHub"]="BDCrusher's Settings"
        report["result"] = "PASS: patch execution and compiled DEX/resources"
        report["classCount"] = classes.size; report["ownClassCount"] = own.size
        report["duplicateClasses"] = false; report["unresolvedHostReferences"] = false
        report["settingsEntryHooks"] = entryCalls; save()
        println("PASS: compiled combined DEX, no duplicate classes, injected host references resolve. NOT an Android runtime test.")
    }
}
