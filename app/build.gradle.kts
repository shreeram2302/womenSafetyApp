plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android) // Apply Kotlin plugin
    id("com.google.gms.google-services")


}

android {
    namespace = "com.example.womensafetyapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.womensafetyapp"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
    packagingOptions{
        exclude("META-INF/DEPENDENCIES")
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.play.services.location)
    implementation(libs.androidx.lifecycle.service)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.volley)
    implementation(libs.androidx.gridlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("androidx.camera:camera-core:1.0.0")
    implementation("com.google.guava:guava:30.1-android")
    implementation("androidx.camera:camera-camera2:1.1.0")
    implementation("androidx.camera:camera-lifecycle:1.1.0")
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.activity:activity-ktx:1.3.1")

    implementation(platform("com.google.firebase:firebase-bom:33.9.0"))
    implementation ("com.google.firebase:firebase-auth:23.0.0") // Check if this version is compatible
    implementation ("com.google.firebase:firebase-auth-ktx:23.0.0")

    implementation ("com.google.firebase:firebase-firestore:24.10.3")
    implementation ("com.google.android.gms:play-services-auth:20.7.0")
//    implementation 'com.google.android.gms:play-services-auth:20.7.0'
    implementation ("com.google.android.gms:play-services-safetynet:18.0.1")
    implementation ("com.google.android.gms:play-services-basement:18.2.0")

    implementation ("com.google.firebase:firebase-messaging:23.3.1")


    //location
    implementation ("com.google.android.gms:play-services-location:21.0.1")
    implementation ("com.google.firebase:firebase-firestore-ktx:24.7.0")

    implementation ("com.google.android.gms:play-services-maps:18.1.0")

    implementation ("com.cloudinary:cloudinary-android:2.3.1")
    implementation ("com.squareup.okhttp3:okhttp:4.9.3")

    implementation ("com.github.bumptech.glide:glide:4.16.0")
    implementation ("com.airbnb.android:lottie:5.0.3")

    implementation ("com.google.firebase:firebase-messaging-ktx:23.3.1")
    implementation ("com.google.firebase:firebase-database-ktx:20.3.0")

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.karumi:dexter:6.2.3")
    implementation("com.google.auth:google-auth-library-oauth2-http:1.19.0")

    implementation ("com.google.android.material:material:1.9.0")
    implementation ("com.makeramen:roundedimageview:2.3.0")


}
