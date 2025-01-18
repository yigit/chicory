plugins {
    alias(libs.plugins.kotlinJvm)
    `java-gradle-plugin`
}
dependencies {
    implementation(gradleApi())
    implementation(gradleKotlinDsl())
    implementation(libs.android.gradle.plugin.api)
}

configure<GradlePluginDevelopmentExtension> {
    plugins {
        create("chicory-android-tester") {
            id = "chicory-android-tester"
            implementationClass = "com.dylibso.chicory.android.BuildPlugin"
        }
    }
}