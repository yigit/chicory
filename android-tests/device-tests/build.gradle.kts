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
    // we need this explicitly for the plugin to discover
    androidTestImplementation(libs.junit.jupiter.api)
    // we need this for the test runner
    androidTestImplementation(libs.androidx.test.runner)
}
configurations.configureEach {
    resolutionStrategy {
        dependencySubstitution {
//            substitute(module("com.google.guava:listenablefuture:1.0"))
//                .using(module("com.google.guava:guava:32.1.1-android"))
            substitute(module("com.google.guava:guava:32.1.1-jre"))
                .using(module("com.google.guava:guava:32.1.1-android"))
        }
    }
}
chicory {
    addTests("runtime")
    // addTests("machine-tests")
    // addTests("aot-tests")
     addTests("runtime-tests")
    // addTests("wasm")
    // addTests("wasi-tests")
    // addTests("fuzz")
    // addTests("wabt")
    // addTests("aot")
}