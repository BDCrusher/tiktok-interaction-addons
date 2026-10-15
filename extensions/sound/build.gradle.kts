extension { name = "extensions/sound.mpe" }
android {
    namespace = "app.soundaddon.runtime"
    defaultConfig { minSdk = 23 }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_11; targetCompatibility = JavaVersion.VERSION_11 }
    testOptions { unitTests.isIncludeAndroidResources = true }
    lint { checkOnly += "NewApi"; error += "NewApi"; abortOnError = true }
}
dependencies {
    val settingsClasses = rootProject.project(":extensions:settings").layout.buildDirectory.file("integration/settings-integration.jar")
    compileOnly(files(settingsClasses))
    testImplementation(files(settingsClasses))
    compileOnly(project(":integration-stubs"))
    testImplementation(project(":integration-stubs"))
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.17")
}
tasks.withType<JavaCompile>().configureEach { dependsOn(":extensions:settings:integrationClassesJar") }
