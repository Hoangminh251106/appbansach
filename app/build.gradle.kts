plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
}

android {
    // Namespace đồng nhất với package Java
    namespace = "com.example.appbansach"
    compileSdk = 35

    defaultConfig {
        // Sửa lại applicationId cho khớp với google-services.json và package chính
        applicationId = "com.example.appbansach"
        minSdk = 26
        targetSdk = 34
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
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.glide)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)
    
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)
    implementation(libs.room.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
