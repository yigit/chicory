import java.io.FileOutputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import org.jetbrains.kotlin.util.capitalizeDecapitalize.capitalizeAsciiOnly

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.android.junit5)
}

val runtimeTestsShards = 50

android {
    namespace = "com.dylibso.runtimeTests"
    compileSdk = 35

    defaultConfig {
        minSdk = 33

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    val chicoryDimension = "chicoryDimension"
    flavorDimensions += chicoryDimension
    productFlavors {
        create("runtime") { dimension = chicoryDimension }
        repeat(runtimeTestsShards) {
            create("runtimeTestsShard$it") { dimension = chicoryDimension }
        }
        // add future modules similar to the runtime configuration above.
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions { jvmTarget = "11" }
}

dependencies {
    // "androidTestRuntimeImplementation" name here comes from Android's product
    // flavor convention. androidTest<productFlavorName>Implementation
    addLibraryTests(libraryName = "runtime", libraryPath = "runtime")
    addLibraryTests(
        libraryName = "runtimeTests",
        libraryPath = "runtime-tests",
        shards = runtimeTestsShards,
    )
    // common dependencies can be added here
    // if you need to add a dependency on a specific module, you can use
    // "androidTest<productFlavorName>Implementation"(<your dependency>)
    // e.g.
    // "androidTestRuntimeImplementation"(libs.chicory.runtime)
    androidTestImplementation(libs.chicory.runtimeTests)
    androidTestImplementation(libs.chicory.runtime)
    androidTestImplementation(libs.chicory.wasm)
    androidTestImplementation(libs.chicory.wasmCorpus)
    androidTestImplementation(libs.junit.jupiter.api)
}

/**
 * Creates a jar of all built test classes from the given library path.
 *
 * The target project ([libraryPath]) requires the maven-test-jar plugin configured for the tests of
 * the target maven project. See the pom file for runtime project for a sample setup.
 *
 * @param configurationName Target Gradle configuration name
 * @param libraryPath Library path relative to the main chicory maven project
 */
fun addLibraryTests(libraryName: String, libraryPath: String, shards: Int = 1) {
    if (shards > 1) {
        val shardDestination = project.layout.buildDirectory.dir("shards-for-$libraryPath")
        val shardTask =
            project.tasks.register<ShardTestsByClassTask>(
                "shardTestsFrom${libraryPath.capitalizeAsciiOnly()}"
            ) {
                testJar.set(
                    project.rootProject
                        .files("../$libraryPath/target")
                        .asFileTree
                        .matching { include("*tests.jar") }
                        .singleFile
                )
                numberOfShards = shards
                outputDirectory.set(shardDestination)
            }
        repeat(shards) { shardIndex ->
            val configurationName =
                "androidTest${libraryName.capitalizeAsciiOnly()}Shard${shardIndex}Implementation"
            project.dependencies.add(
                configurationName,
                project.dependencies.create(
                    project
                        .files({
                            shardDestination.map {
                                it.asFileTree.matching { include("*shard-${shardIndex}.jar") }
                            }
                        })
                        .builtBy(shardTask)
                ),
            )
        }
    } else {
        val configurationName = "androidTest${libraryName.capitalizeAsciiOnly()}Implementation"
        project.dependencies.add(
            configurationName,
            project.dependencies.create(
                project.rootProject.files("../$libraryPath/target").asFileTree.matching {
                    include("*tests.jar")
                }
            ),
        )
    }
}

@CacheableTask
abstract class ShardTestsByClassTask : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val testJar: RegularFileProperty

    @get:Input abstract var numberOfShards: Int

    @get:OutputDirectory abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun shardTests() {
        JarFile(testJar.get().asFile).use { inputJar ->
            val (testClasses, otherClasses) =
                inputJar.entries().asSequence().partition { it.realName.endsWith("Test.class") }
            // Distribute test classes evenly across shards
            val testClassesPerShard =
                testClasses.chunked((testClasses.size + numberOfShards - 1) / numberOfShards)
            testClassesPerShard.forEachIndexed { shardIndex, classes ->
                JarOutputStream(
                        FileOutputStream(outputDirectory.file("shard-$shardIndex.jar").get().asFile)
                    )
                    .use { jos ->
                        // Add test classes for this shard
                        classes.forEach { entry ->
                            jos.putNextEntry(JarEntry(entry.name))
                            inputJar.getInputStream(entry).use { it.copyTo(jos) }
                        }

                        // Add all other files to each shard
                        otherClasses.forEach { entry ->
                            jos.putNextEntry(JarEntry(entry.name))
                            inputJar.getInputStream(entry).use { it.copyTo(jos) }
                        }
                    }
            }
        }
    }
}
