package app.collectionsaddon.patches
import app.morphe.patcher.patch.loadPatchesFromJar
import com.google.gson.GsonBuilder
import java.io.File
import java.security.MessageDigest
import java.util.zip.ZipFile
import java.nio.ByteBuffer
import com.android.tools.smali.dexlib2.Opcodes
import com.android.tools.smali.dexlib2.dexbacked.DexBackedDexFile

fun main(args: Array<String>) {
    val bundle = File(args[0]); val root = File(args[1]); val dist = root.resolve("dist").apply { mkdirs() }
    val patches = loadPatchesFromJar(setOf(bundle))
    check(patches.size == 2 && patches.map { it.name }.toSet() == setOf("Enhanced collection popup", "Sound button actions"))
    val payloadClasses = ZipFile(bundle).use { zip ->
        listOf("collections", "sound", "settings").flatMap { module ->
            val bytes = zip.getInputStream(zip.getEntry("extensions/$module.mpe")).use { it.readBytes() }
            val classes = DexBackedDexFile(Opcodes.getDefault(), ByteBuffer.wrap(bytes)).classes.map { it.type }.sorted()
            val prefix=when(module) { "collections" -> "Lapp/collectionsaddon/"; "sound" -> "Lapp/soundaddon/"; else -> "Lapp/bdcrusher/" }
            check(classes.isNotEmpty() && classes.all { it.startsWith(prefix) })
            classes
        }
    }
    check(payloadClasses.size == payloadClasses.distinct().size) { "Duplicate classes across add-on payloads" }
    val version = args[2]
    val destination = dist.resolve("TikTok-Interaction-Addons-$version.mpp")
    bundle.copyTo(destination, overwrite = true)
    val gson = GsonBuilder().setPrettyPrinting().create()
    val list = mapOf("version" to version, "patches" to patches.map { p -> mapOf(
        "name" to p.name, "description" to p.description, "default" to p.default,
        "compatiblePackages" to p.compatibility?.map { c -> mapOf(
            "packageName" to c.packageName, "name" to c.name, "signatures" to c.signatures,
            "targets" to c.targets.map { t -> mapOf("version" to t.version,
                "versionCodes" to t.versionCodes?.mapKeys { it.key.name }, "isExperimental" to t.isExperimental,
                "minSdk" to t.minSdk) }) },
        "options" to emptyList<Any>(), "dependencies" to emptyList<Any>()) })
    dist.resolve("patches-list.json").writeText(gson.toJson(list))
    dist.resolve("patches-bundle.json").writeText(gson.toJson(mapOf("name" to "TikTok Interaction Add-ons", "version" to version, "file" to destination.name, "patcherVersion" to "1.15.0", "source" to "local; not publicly published")))
    val checksum = MessageDigest.getInstance("SHA-256").digest(destination.readBytes()).joinToString("") { "%02x".format(it) }
    dist.resolve("SHA256SUMS.txt").writeText("$checksum  ${destination.name}\n")
    dist.resolve("payload-audit.json").writeText(gson.toJson(mapOf("classCount" to payloadClasses.size,
        "classes" to payloadClasses, "compileOnlyStubsIncluded" to false, "foreignClassesIncluded" to false)))
    println("TWO INDEPENDENT PATCHES: ${destination.absolutePath}\nSHA256: $checksum")
}
