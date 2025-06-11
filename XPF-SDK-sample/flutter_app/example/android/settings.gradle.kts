pluginManagement {
    val flutterSdkPath = run {
        val properties = java.util.Properties()
        file("local.properties").inputStream().use { properties.load(it) }
        val flutterSdkPath = properties.getProperty("flutter.sdk")
        require(flutterSdkPath != null) { "flutter.sdk not set in local.properties" }
        flutterSdkPath
    }

    includeBuild("$flutterSdkPath/packages/flutter_tools/gradle")

    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("dev.flutter.flutter-plugin-loader") version "1.0.0"
    id("com.android.application") version "8.8.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.10" apply false
    id("com.android.library") version "8.8.0" apply false
    id("androidx.privacysandbox.library") version "1.0.0-alpha02" apply false
    id("com.google.devtools.ksp") version "1.9.10-1.0.13" apply false
    id("org.jetbrains.kotlin.jvm") version "1.9.10" apply false
}


include(":app")
include(":runtime-aware-sdk")
project(":runtime-aware-sdk").projectDir = File("../../android/runtime-aware-sdk")
include(":runtime-enabled-sdk")
project(":runtime-enabled-sdk").projectDir = File("../../android/runtime-enabled-sdk")
include(":runtime-enabled-sdk-bundle")
project(":runtime-enabled-sdk-bundle").projectDir = File("../../android/runtime-enabled-sdk-bundle")
include(":mediatee-sdk")
project(":mediatee-sdk").projectDir = File("../../android/mediatee-sdk")
include(":mediatee-sdk-bundle")
project(":mediatee-sdk-bundle").projectDir = File("../../android/mediatee-sdk-bundle")
include(":mediatee-sdk-adapter-bundle")
project(":mediatee-sdk-adapter-bundle").projectDir = File("../../android/mediatee-sdk-adapter-bundle")
include(":mediatee-sdk-adapter")
project(":mediatee-sdk-adapter").projectDir = File("../../android/mediatee-sdk-adapter")
include(":inapp-mediatee-sdk-adapter")
project(":inapp-mediatee-sdk-adapter").projectDir = File("../../android/inapp-mediatee-sdk-adapter")
include(":inapp-mediatee-sdk")
project(":inapp-mediatee-sdk").projectDir = File("../../android/inapp-mediatee-sdk")