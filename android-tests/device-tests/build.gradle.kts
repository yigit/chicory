import com.android.build.api.variant.VariantSelector
import com.dylibso.chicory.testgen.TestGen

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.android.junit5)
}

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
        // add future modules similar to the runtime configuration above.
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions { jvmTarget = "11" }
    androidComponents.onVariants { variant ->
        if (variant.flavorName == "runtime" && variant.buildType == "debug") {
            val sourceGenTask = generateTestSources()
            variant.androidTest!!.sources.java!!.addGeneratedSourceDirectory(
                sourceGenTask,
                GenerateTests::sourceDestinationFolder
            )
            variant.androidTest!!.sources.resources!!.addGeneratedSourceDirectory(
                sourceGenTask,
                GenerateTests::compiledWastTargetFolder
            )
        }
    }
//    androidComponents.onVariants(
//        selector = selector()
//    ) {
//
//    }
}

dependencies {
    // "androidTestRuntimeImplementation" name here comes from Android's product
    // flavor convention. androidTest<productFlavorName>Implementation
    addLibraryTests(configurationName = "androidTestRuntimeImplementation", libraryPath = "runtime")
    // common dependencies can be added here
    // if you need to add a dependency on a specific module, you can use
    // "androidTest<productFlavorName>Implementation"(<your dependency>)
    // e.g.
    // "androidTestRuntimeImplementation"(libs.chicory.runtime)
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
fun addLibraryTests(configurationName: String, libraryPath: String) {
    // Add the jar task's output as a dependency.
    // Gradle will figure out that it needs to run the task before compiling the
    // project.
    project.dependencies.add(
        configurationName,
        project.dependencies.create(
            project.rootProject.files("../$libraryPath/target").asFileTree.matching {
                include("*tests.jar")
            }
        ),
    )
}

fun generateTestSources(): TaskProvider<GenerateTests> {
    return tasks.register<GenerateTests>("generateTestSuite") {
        //    val root = project.rootProject.rootDir.resolve("..")
        //    testSuiteRepo.set(project.rootProject.)
        val taskOutputRoot = project.layout.buildDirectory.dir("testgen")
        testsuiteFolder.set(taskOutputRoot.map { it.dir("wasm-test-suite") })
        sourceDestinationFolder.set(taskOutputRoot.map { it.dir("generated-test-sources/test-gen") })
        compiledWastTargetFolder.set(taskOutputRoot.map { it.dir("generated-resources/compiled-wast") })
        includedWasts.set(listOf("address.wast"))
        excludedWasts.set(listOf(
            "f32_cmp.wast",
            "simd_f64x2_pmin_pmax.wast",
            "table_grow.wast",
            "comments.wast",
            "simd_load16_lane.wast",
            "simd_load32_lane.wast",
            "return.wast",
            "inline-module.wast",
            "f32.wast",
            "simd_f32x4.wast",
            "simd_address.wast",
            "nop.wast",
            "i32.wast",
            "simd_i8x16_arith2.wast",
            "simd_i32x4_dot_i16x8.wast",
            "conversions.wast",
            "simd_conversions.wast",
            "switch.wast",
            "simd_i8x16_sat_arith.wast",
            "global.wast",
            "i64.wast",
            "memory_grow.wast",
            "simd_i64x2_extmul_i32x4.wast",
            "simd_f64x2_cmp.wast",
            "func_ptrs.wast",
            "simd_i64x2_arith.wast",
            "simd_f32x4_arith.wast",
            "start.wast",
            "elem.wast",
            "table-sub.wast",
            "simd_linking.wast",
            "ref_null.wast",
            "table_copy.wast",
            "labels.wast",
            "simd_bitwise.wast",
            "simd_f64x2.wast",
            "memory_fill.wast",
            "custom.wast",
            "unreached-valid.wast",
            "simd_load64_lane.wast",
            "exports.wast",
            "memory_init.wast",
            "simd_i32x4_cmp.wast",
            "binary.wast",
            "func.wast",
            "endianness.wast",
            "table_init.wast",
            "simd_i32x4_arith2.wast",
            "table.wast",
            "simd_i16x8_extadd_pairwise_i8x16.wast",
            "memory_redundancy.wast",
            "f64.wast",
            "fac.wast",
            "utf8-custom-section-id.wast",
            "simd_store32_lane.wast",
            "ref_func.wast",
            "simd_i32x4_trunc_sat_f64x2.wast",
            "binary-leb128.wast",
            "simd_i16x8_q15mulr_sat_s.wast",
            "simd_splat.wast",
            "simd_boolean.wast",
            "traps.wast",
            "select.wast",
            "obsolete-keywords.wast",
            "simd_store.wast",
            "int_exprs.wast",
            "utf8-import-module.wast",
            "linking.wast",
            "simd_store8_lane.wast",
            "forward.wast",
            "loop.wast",
            "local_tee.wast",
            "imports.wast",
            "f32_bitwise.wast",
            "br_if.wast",
            "block.wast",
            "load.wast",
            "simd_i8x16_cmp.wast",
            "bulk.wast",
            "utf8-import-field.wast",
            "memory.wast",
            "float_memory.wast",
            "skip-stack-guard-page.wast",
            "type.wast",
            "simd_i8x16_arith.wast",
            "const.wast",
            "unreached-invalid.wast",
            "simd_f64x2_rounding.wast",
            "table_set.wast",
            "local_get.wast",
            "memory_copy.wast",
            "simd_bit_shift.wast",
            "float_literals.wast",
            "simd_load_zero.wast",
            "simd_load_splat.wast",
            "table_size.wast",
            "table_get.wast",
            "simd_i16x8_cmp.wast",
            "simd_i64x2_cmp.wast",
            "names.wast",
            "simd_align.wast",
            "left-to-right.wast",
            "local_set.wast",
            "f64_cmp.wast",
            "simd_i32x4_extmul_i16x8.wast",
            "unwind.wast",
            "data.wast",
            "simd_lane.wast",
            "simd_f32x4_pmin_pmax.wast",
            "memory_trap.wast",
            "call.wast",
            "float_exprs.wast",
            "simd_i32x4_trunc_sat_f32x4.wast",
            "align.wast",
            "simd_f32x4_rounding.wast",
            "table_fill.wast",
            "float_misc.wast",
            "ref_is_null.wast",
            "simd_i16x8_arith2.wast",
            "store.wast",
            "f64_bitwise.wast",
            "simd_load.wast",
            "token.wast",
            "unreachable.wast",
            "simd_load8_lane.wast",
            "simd_int_to_int_extend.wast",
            "int_literals.wast",
            "br_table.wast",
            "if.wast",
            "simd_f32x4_cmp.wast",
            "stack.wast",
            "simd_i16x8_extmul_i8x16.wast",
            "br.wast",
            "call_indirect.wast",
            "simd_f64x2_arith.wast",
            "simd_i32x4_arith.wast",
            "simd_i16x8_sat_arith.wast",
            "simd_i16x8_arith.wast",
            "simd_const.wast",
            "memory_size.wast",
            "simd_load_extend.wast",
            "simd_store64_lane.wast",
            "simd_i64x2_arith2.wast",
            "simd_i32x4_extadd_pairwise_i16x8.wast",
            "utf8-invalid-encoding.wast",
            "simd_store16_lane.wast"
        ))
    }
}

@CacheableTask
abstract class GenerateTests : DefaultTask() {
    /** Repository of the testsuite. */
    @get:Input abstract val testSuiteRepo: Property<String>
    /** Repository of the testsuite. */
    @get:Input abstract val testSuiteRepoRef: Property<String>

    @get:OutputDirectory abstract val testsuiteFolder: DirectoryProperty

    @get:OutputDirectory abstract val sourceDestinationFolder: DirectoryProperty

    @get:OutputDirectory abstract val compiledWastTargetFolder: DirectoryProperty

    @get:Input abstract val includedWasts: ListProperty<String>

    @get:Input abstract val excludedWasts: ListProperty<String>

    init {
        testSuiteRepo.convention("https://github.com/WebAssembly/testsuite")
        testSuiteRepoRef.convention("main")
    }

    @TaskAction
    fun generateTests() {
        val testSuiteRepo = testSuiteRepo.get()
        val testSuiteRepoRef = testSuiteRepoRef.get()
        println("$testSuiteRepo / $testSuiteRepoRef")
        TestGen.execute(
            testSuiteRepo,
            testSuiteRepoRef,
            testsuiteFolder.get().asFile.also { it.deleteRecursively() },
            sourceDestinationFolder.get().asFile.also { it.deleteRecursively() },
            compiledWastTargetFolder.get().asFile.also { it.deleteRecursively() },
            includedWasts.get().sorted(),
            emptyList(), // excludedTests
            emptyList(), // excludedMalformedWasts
            emptyList(), // excludedInvalidWasts
            emptyList(), // excludedUninstantiableWasts
            emptyList(), // excludedUnlinkableWasts
            excludedWasts.get().sorted(), // excludedWasts
        )
    }
}
