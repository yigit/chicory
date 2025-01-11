package com.dylibso.chicory.android

import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import org.gradle.api.Project
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.getByType
import java.io.File
import java.util.Locale

abstract class CopyTestsExtension(
    private val project: Project,
    private val mainProjectDirectory: File
) {
    @Suppress("unused") // used from the gradle files
    fun addTests(
        path: String,
        name: String = path.sanitize(),
    ) {
        val jarTask = project.tasks.register(
            "jarTestClassesFor${name.capitalizeFirst()}",
            Jar::class.java
        ) { task ->
            task.archiveBaseName.set("${name}Tests")
            task.from(
                mainProjectDirectory.resolve(path).resolve("target/test-classes")
            )
            task.destinationDirectory.set(
                project.layout.buildDirectory.dir(
                    "testJars/$name"
                )
            )
        }
        val libraryExtension = project.extensions.getByType<LibraryExtension>()
        val libraryComponentsExtension =
            project.extensions.getByType<LibraryAndroidComponentsExtension>()

        // add the product flavor dimension if it doesn't exist.
        if (!libraryExtension.flavorDimensions.contains(PRODUCT_DIMENSION)) {
            libraryExtension.flavorDimensions.add(PRODUCT_DIMENSION)
        }

        // create the new product flavor to host tests
        val productFlavorName = "${name}TestsOnAndroid"
        libraryExtension.productFlavors {
            this.create(productFlavorName) {
                it.dimension = "originalProject"
            }
        }

        // add the test classes to the runtime configuration for tests
        libraryComponentsExtension.onVariants(
            libraryComponentsExtension.selector().withFlavor(
                dimension = "originalProject",
                flavorName = productFlavorName
            ).withBuildType("debug")
        ) { testVariant ->
            val runtimeConfiguration = testVariant.androidTest?.runtimeConfiguration
            checkNotNull(runtimeConfiguration) {
                "$testVariant must have a compile configuration"
            }
            // add the jar to the runtime classpath of the test configuration
            runtimeConfiguration.dependencies.add(
                project.dependencies.create(
                    project.files({
                        jarTask.get().destinationDirectory.asFileTree.matching {
                            it.include("*.jar")
                        }
                    }).builtBy(jarTask)
                )
            )
        }
    }

    companion object {
        /**
         * The product dimension we add to the android project.
         * Each sub-flavor we create for the maven project will specify itself using this dimension.
         */
        private const val PRODUCT_DIMENSION = "originalProject"
    }
}

private fun String.capitalizeFirst() = this.replaceFirstChar {
    if (it.isLowerCase()) it.titlecase(
        Locale.US
    ) else it.toString()
}

/**
 * convert project paths into valid names.
 */
fun String.sanitize(): String {
    var shouldCapitalize = false
    return this.filter { char ->
        when {
            char.isLetterOrDigit() -> true
            else -> {
                shouldCapitalize = true
                false
            }
        }
    }.mapIndexed { index, char ->
        when {
            index == 0 -> char.lowercase()
            shouldCapitalize -> {
                shouldCapitalize = false
                char.uppercase()
            }
            else -> char.toString()
        }
    }.joinToString("")
}