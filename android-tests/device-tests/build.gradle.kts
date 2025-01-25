import java.io.FileOutputStream
import java.util.PriorityQueue
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import org.jetbrains.kotlin.util.capitalizeDecapitalize.capitalizeAsciiOnly
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.ASM9

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

    class Shard(val id: Int) : Comparable<Shard> {
        private val entries: MutableList<JarEntry> = mutableListOf()
        private var totalTests: Int = 0

        fun addClass(entry: JarEntry, numberOfTests: Int) {
            totalTests += numberOfTests
            entries.add(entry)
        }

        fun entries(): List<JarEntry> = entries

        override fun compareTo(other: Shard): Int {
            val totalTestsCmp = totalTests.compareTo(other.totalTests)
            if (totalTestsCmp != 0) return totalTestsCmp
            return id.compareTo(other.id)
        }
    }

    @TaskAction
    fun shardTests() {
        val jarFile = JarFile(testJar.get().asFile)
        val entries = jarFile.entries().asSequence().toList()

        // Distribute by weight
        val priorityQueue = PriorityQueue<Shard>()
        val shards = Array(numberOfShards) { Shard(id = it).also { priorityQueue.add(it) } }

        // Sort classes by their test count and we'll then fill shards based on the next
        // smallest shard. Not the most optimal solution wrt distribution but shall be good
        // enough.
        entries
            .filter { it.name.endsWith("Test.class") }
            .map { entry ->
                val bytes = jarFile.getInputStream(entry).use { it.readBytes() }
                val numberOfTests = countTestAnnotations(bytes)
                entry to numberOfTests
            }
            .sortedByDescending { it.second }
            .forEach { (entry, numberOfTests) ->
                val shard = priorityQueue.poll()
                shard.addClass(entry, numberOfTests)
                priorityQueue.add(shard)
            }

        // Other files
        val otherFiles = entries.filter { !it.name.endsWith("Test.class") }

        // Create output JARs
        shards.forEach { shard ->
            JarOutputStream(
                    FileOutputStream(outputDirectory.file("shard-${shard.id}.jar").get().asFile)
                )
                .use { jos ->
                    shard.entries().forEach { entry ->
                        jos.putNextEntry(JarEntry(entry.name))
                        jarFile.getInputStream(entry).use { it.copyTo(jos) }
                    }

                    otherFiles.forEach { entry ->
                        jos.putNextEntry(JarEntry(entry.name))
                        jarFile.getInputStream(entry).use { it.copyTo(jos) }
                    }
                }
        }

        jarFile.close()
    }

    private fun countTestAnnotations(bytes: ByteArray): Int {
        val cr = ClassReader(bytes)
        var testCount = 0
        cr.accept(
            object : ClassVisitor(ASM9) {
                override fun visitMethod(
                    access: Int,
                    name: String?,
                    descriptor: String?,
                    signature: String?,
                    exceptions: Array<out String>?,
                ): MethodVisitor {
                    return object : MethodVisitor(ASM9) {
                        override fun visitAnnotation(
                            descriptor: String?,
                            visible: Boolean,
                        ): AnnotationVisitor? {
                            if (descriptor == "Lorg/junit/jupiter/api/Test;") testCount++
                            return null
                        }
                    }
                }
            },
            ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES,
        )
        return testCount
    }
}
