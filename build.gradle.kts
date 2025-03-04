//// Top-level build file where you can add configuration options common to all sub-projects/modules.
//plugins {
//    alias(libs.plugins.android.application) apply false
//    alias(libs.plugins.kotlin.android) apply false
//
//    id("com.google.gms.google-services") version "4.4.2" apply false
//}
// Project-level build.gradle.kts
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false  // No need to remove this line

    id("com.google.gms.google-services") version "4.3.15" apply false
}

buildscript {
    val kotlin_version by extra("2.1.0")  // Update Kotlin version to 2.1.0

    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version")  // Use Kotlin 2.1.0
        classpath("com.google.gms:google-services:4.3.15")
//        classpath 'com.google.gms:google-services:4.3.15' // or latest version
//        classpath ("com.google.gms:google-services:4.3.15")
    }
}
