import org.jetbrains.kotlin.ir.backend.js.compile

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.msdk_coords"
    compileSdk = 35
    useLibrary("org.apache.http.legacy")

    packagingOptions{
        doNotStrip("*/*/libdjivideo.so")
        doNotStrip("*/*/libSDKRelativeJNI.so")
        doNotStrip("*/*/libFlyForbid.so")
        doNotStrip("*/*/libduml_vision_bokeh.so")
        doNotStrip("*/*/libyuv2.so")
        doNotStrip("*/*/libGroudStation.so")
        doNotStrip("*/*/libFRCorkscrew.so")
        doNotStrip("*/*/libUpgradeVerify.so")
        doNotStrip("*/*/libFR.so")
        doNotStrip("*/*/libDJIFlySafeCore.so")
        doNotStrip("*/*/libdjifs_jni.so")
        doNotStrip("*/*/libsfjni.so")
        doNotStrip("*/*/libc++_shared.so")
        doNotStrip("*/*/libmrtc_core_jni.so")
        doNotStrip("*/*/libDJIRegister.so")
        doNotStrip("*/*/libDJIUpgradeJNI.so")
        exclude("META-INF/rxjava.properties")
        jniLibs {
            excludes += setOf("META-INF/**")
        }

    }

    defaultConfig {
        applicationId = "com.example.msdk_coords"
        minSdk = 24
        targetSdk = 31
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        ndk {
            // On x86 devices that run Android API 23 or above, if the application is targeted with API 23 or
            // above, FFmpeg lib might lead to runtime crashes or warnings.
            abiFilters += setOf("armeabi-v7a", "arm64-v8a")
        }
    }

    buildFeatures {
        viewBinding = true
    }

    buildTypes {
        release {
            isShrinkResources = false
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isShrinkResources = false
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro"
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
}

dependencies {
    implementation("com.yandex.android:maps.mobile:4.5.0-full")
    implementation("androidx.multidex:multidex:2.0.0")
    implementation("com.dji:dji-sdk:4.16.4")
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    compileOnly("com.dji:dji-sdk-provided:4.16.4")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
