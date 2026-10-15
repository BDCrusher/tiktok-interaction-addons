import org.gradle.jvm.tasks.Jar
import java.security.MessageDigest
group = "app.collectionsaddon"
patches {
    about {
        name = "TikTok Interaction Add-ons"
        description = "Independent collection popup and sound-button patches for use alongside HushFeed."
        source = "local"
        author = "Personal project"
        contact = "not published"
        website = "not published"
        license = "GPLv3"
    }
}
val generatorClasspath = configurations.create("generatorClasspath")
dependencies {
    implementation(kotlin("stdlib"))
    compileOnly(libs.gson)
    generatorClasspath(libs.gson)
    testImplementation("junit:junit:4.13.2")
    testImplementation(libs.gson)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
}
tasks {
    withType<Jar>().configureEach {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
        manifest.attributes["Timestamp"] = "1790985600000"
        from(rootProject.files("LICENSE", "NOTICE")) { into("META-INF/collections-addon") }
    }
    register<JavaExec>("generateMetadata") {
        dependsOn("buildAndroid")
        classpath = sourceSets["main"].runtimeClasspath + generatorClasspath
        mainClass.set("app.collectionsaddon.patches.MetadataKt")
        args(project.tasks.named<Jar>("jar").get().archiveFile.get().asFile.absolutePath, rootProject.projectDir.absolutePath, project.version.toString())
    }
    register<JavaExec>("verifyCombination") {
        dependsOn(testClasses, "buildAndroid")
        classpath = sourceSets["test"].runtimeClasspath + generatorClasspath
        mainClass.set("app.collectionsaddon.patches.VerifyCombinationKt")
        maxHeapSize = "6g"
        doFirst {
            args(requireNotNull(project.findProperty("apk")), requireNotNull(project.findProperty("hushfeed")),
                project.tasks.named<Jar>("jar").get().archiveFile.get().asFile.absolutePath,
                rootProject.layout.buildDirectory.dir("verification").get().asFile.absolutePath,
                project.findProperty("selection") ?: "core", project.findProperty("order") ?: "hush-first")
        }
    }
    val compileNativeHookAudit = register<JavaCompile>("compileNativeHookAudit") {
        source(rootProject.file("verification/NativeHookAudit.java"))
        classpath = sourceSets["test"].compileClasspath
        destinationDirectory.set(rootProject.layout.buildDirectory.dir("native-hook-audit"))
        options.release.set(17)
    }
    val compileDexProbe = register<JavaCompile>("compileDexProbe") {
        source(rootProject.file("verification/DexProbe.java"))
        classpath = sourceSets["test"].compileClasspath
        destinationDirectory.set(rootProject.layout.buildDirectory.dir("dex-probe"))
        options.release.set(17)
    }
    register<JavaExec>("probeDex") {
        dependsOn(compileDexProbe)
        classpath = files(compileDexProbe.flatMap { it.destinationDirectory }) + sourceSets["test"].runtimeClasspath
        mainClass.set("DexProbe")
        maxHeapSize = "1g"
        doFirst {
            args(requireNotNull(project.findProperty("probeFile")), requireNotNull(project.findProperty("probeClass")),
                project.findProperty("probeMethod") ?: ".*")
        }
    }
    register<JavaExec>("verifyNativeHooks") {
        dependsOn(compileNativeHookAudit)
        classpath = files(compileNativeHookAudit.flatMap { it.destinationDirectory }) + sourceSets["test"].runtimeClasspath
        mainClass.set("NativeHookAudit")
        maxHeapSize = "1g"
        doFirst {
            args(requireNotNull(project.findProperty("compiledDex")) { "Supply -PcompiledDex=<diagnostic scratch/patched/dex directory>" })
        }
    }
    register<Zip>("packageSource") {
        dependsOn("generateMetadata")
        archiveFileName.set("TikTok-Interaction-Addons-${project.version}-source.zip")
        destinationDirectory.set(rootProject.layout.projectDirectory.dir("dist"))
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
        from(rootProject.projectDir) {
            exclude(".git/**", ".gradle/**", ".kotlin/**", "**/build/**", "build-tools/**",
                "dist/**", "verification/inputs/**", "local.properties", "**/__pycache__/**", "**/*.pyc",
                ".env", ".env.*", "**/*.apk", "**/*.apkm", "**/*.keystore", "**/*.jks")
        }
        doLast {
            val dist = destinationDirectory.get().asFile
            val sums = dist.listFiles()!!.filter { it.extension in setOf("mpp", "json", "zip") }
                .sortedBy { it.name }.joinToString("\n", postfix = "\n") { file ->
                    val hash = MessageDigest.getInstance("SHA-256").digest(file.readBytes())
                        .joinToString("") { "%02x".format(it) }
                    "$hash  ${file.name}"
                }
            dist.resolve("SHA256SUMS.txt").writeText(sums)
        }
    }
}
