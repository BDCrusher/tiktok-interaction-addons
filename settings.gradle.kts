rootProject.name = "tiktok-collections-addon"
pluginManagement {
    includeBuild(providers.gradleProperty("morphePluginDir").orNull ?: "build-tools/morphe-patches-gradle-plugin")
    repositories {
        gradlePluginPortal()
        google()
        maven {
            url = uri("https://maven.pkg.github.com/MorpheApp/registry")
            credentials {
                username = providers.gradleProperty("gpr.user").orNull ?: System.getenv("GITHUB_ACTOR")
                password = providers.gradleProperty("gpr.key").orNull ?: System.getenv("GITHUB_TOKEN")
            }
        }
        maven { url = uri("https://jitpack.io") }
    }
}
val patcherDir = providers.gradleProperty("morphePatcherDir").orNull ?: "build-tools/morphe-patcher-2.0.0"
if (file(patcherDir).exists()) {
    includeBuild(patcherDir) {
        dependencySubstitution { substitute(module("app.morphe:morphe-patcher")).using(project(":")) }
    }
}
plugins { id("app.morphe.patches") version "1.3.4" }
include(":integration-stubs")
