plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.android.junit5)
}

android {
    namespace = "com.dylibso.runtimeTests"
    compileSdk = 35

    defaultConfig {
        minSdk = 24

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
}
dependencies {
    androidTestImplementation(libs.chicory.runtime)
    androidTestImplementation(libs.chicory.wasm)
    androidTestImplementation(libs.chicory.wasmCorpus)
    androidTestImplementation(libs.junit.jupiter.api)
}

chicory {
    addTests("runtime")
    // addTests("machine-tests")
    // addTests("aot-tests")
    // addTests("runtime-tests")
    // addTests("wasm")
    // addTests("wasi-tests")
    // addTests("fuzz")
    // addTests("wabt")
    // addTests("aot")
}