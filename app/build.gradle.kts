plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.unicalculator.app"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.unicalculator.app"
        minSdk = 26
        targetSdk = 37
        versionCode = 2
        versionName = "1.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters.addAll(listOf("arm64-v8a", "armeabi-v7a", "x86_64"))
        }
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
        compose = true
    }
    packaging {
        jniLibs.pickFirsts.addAll(
            listOf(
                "**/libc++_shared.so",
                "**/libqalculate.so",
                "**/libqalculate_swig.so",
                "**/libgmp.so",
                "**/libmpfr.so",
                "**/libxml2.so",
                "**/libiconv.so"
            )
        )
    }
}

dependencies {
    // AndroidX Compose BOM
    implementation(platform(libs.androidx.compose.bom))

    // Core & lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose UI
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // Additional Compose dependencies
    //noinspection UseTomlInstead
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    //noinspection UseTomlInstead
    implementation("androidx.compose.material3:material3-window-size-class:1.4.0")

    // JSON serialization
    //noinspection UseTomlInstead
    implementation("com.google.code.gson:gson:2.14.0")

    // Coroutines
    //noinspection UseTomlInstead
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.11.0")

    // DataStore Preferences
    //noinspection UseTomlInstead
    implementation("androidx.datastore:datastore-preferences:1.2.1")

    // Qalculate! engine (latest stable)
    //noinspection UseTomlInstead,Aligned16KB
    implementation("com.jherkenhoff:libqalculate:5.8.2-2")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}