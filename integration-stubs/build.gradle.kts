plugins { `java-library` }
java { sourceCompatibility = JavaVersion.VERSION_11; targetCompatibility = JavaVersion.VERSION_11 }
val sdk = providers.gradleProperty("androidSdkDir").orNull ?: System.getenv("ANDROID_HOME")
    ?: "${System.getProperty("user.home")}/AppData/Local/Android/Sdk"
dependencies { compileOnly(files("$sdk/platforms/android-36/android.jar")) }
