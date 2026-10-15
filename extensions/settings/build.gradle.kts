extension { name = "extensions/settings.mpe" }
android {
    namespace = "app.bdcrusher.runtime"
    defaultConfig { minSdk = 23 }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_11; targetCompatibility = JavaVersion.VERSION_11 }
    testOptions { unitTests.isIncludeAndroidResources = true }
    lint { checkOnly += "NewApi"; error += "NewApi"; abortOnError = true }
}
dependencies {
    compileOnly(project(":integration-stubs"))
    testImplementation(project(":integration-stubs"))
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.17")
}
tasks.register<Jar>("integrationClassesJar") {
    dependsOn("compileReleaseJavaWithJavac")
    archiveFileName.set("settings-integration.jar")
    destinationDirectory.set(layout.buildDirectory.dir("integration"))
    from(layout.buildDirectory.dir("intermediates/javac/release/compileReleaseJavaWithJavac/classes"))
}
