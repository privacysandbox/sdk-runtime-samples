buildscript {
    repositories {
        mavenCentral()
        google()
    }
    dependencies {
        classpath(libs.kotlin.gradlePlugin)
        classpath(libs.ksp.gradlePlugin)
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

val newBuildDir: Directory = rootProject.layout.buildDirectory.dir("../../build").get()
rootProject.layout.buildDirectory.value(newBuildDir)

subprojects {
    val newSubprojectBuildDir: Directory = newBuildDir.dir(project.name)
    project.layout.buildDirectory.value(newSubprojectBuildDir)
}
subprojects {
    project.evaluationDependsOn(":app")
}

plugins {
    id("com.android.application").version("8.8.0").apply(false)
    id("com.android.library").version("8.8.0").apply(false)
    id("androidx.privacysandbox.library").version("1.0.0-alpha02").apply(false)
    id("org.jetbrains.kotlin.jvm").version("1.9.10").apply(false)
    id("com.google.devtools.ksp").version("1.9.10-1.0.13").apply(false)
    id("org.jetbrains.kotlin.plugin.compose").version("2.1.0").apply(false)
    id("org.jetbrains.kotlin.android").version("1.9.10").apply(false)
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}
