plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
}

android {
    namespace = "edu.pmdm.gympro"
    compileSdk = 35

    defaultConfig {
        applicationId = "edu.pmdm.gympro"
        minSdk = 26
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
    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
}

dependencies {
    // Firebase BoM (maneja las versiones automáticamente)
    implementation(platform("com.google.firebase:firebase-bom:33.12.0"))

    // Firebase Auth y Firestore
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.github.bumptech.glide:glide:4.15.1")
    implementation(libs.country.code.picker)
    implementation("com.hbb20:ccp:2.7.3")
    implementation ("com.google.firebase:firebase-storage:20.2.1")
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation ("com.applandeo:material-calendar-view:1.9.2")


}