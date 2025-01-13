package com.dylibso.chicory.android

import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.getByType
import java.io.File
import java.util.Locale

abstract class CopyTestsExtension(
    private val project: Project,
    private val mainProjectDirectory: File,
    private val rootProjectPom: ParsedPom
) {
    @Suppress("unused") // used from the gradle files
    fun addTests(
        path: String,
        name: String = path.sanitize(),
    ) {
        val targetProjectPath = mainProjectDirectory.resolve(path)
        val jarTask = project.tasks.register(
            "jarTestClassesFor${name.capitalizeFirst()}",
            Jar::class.java
        ) { task ->
            task.archiveBaseName.set("${name}Tests")
            task.from(
                targetProjectPath.resolve("target/test-classes")
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
        val productFlavorName = "${name}Module"
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
            val targetPom = PomParser.parse(
                targetProjectPath.resolve("pom.xml"),
                parent = rootProjectPom)
            runtimeConfiguration.addDependencies(project, targetPom, rootProjectPom)
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
private fun Configuration.addDependencies(
    project: Project,
    projectPom: ParsedPom,
    rootProjectPom: ParsedPom
) {
    dependencies.add(
        project.dependencies.create(
            "${projectPom.groupId}:${projectPom.artifactId}:${projectPom.version}"
        )
    )
    projectPom.dependencies.filter {
        it.scope == null || it.scope == "test"
    }.map { dependency ->
        if (dependency.version.takeIf { it.isNotBlank() } == null) {
            // load version from properties
            rootProjectPom.dependencyManagementDependencies.firstOrNull {
                it.artifactId == dependency.artifactId &&
                        it.groupId == dependency.groupId
            } ?: error("missing version but cannot find it in parent pom: $dependency")
        } else {
            dependency
        }
    }.forEach { dependency ->
        this.dependencies.add(
            project.dependencies.create(dependency.toGradleNotation())
        )
    }
}
private fun String.capitalizeFirst() = this.replaceFirstChar {
    if (it.isLowerCase()) it.titlecase(
        Locale.US
    ) else it.toString()
}

/**
 * convert project paths into valid names.
 * e.g. runtime-tests turns into runtimeTests
 */
fun String.sanitize(): String {
    var shouldCapitalize = false
    return this.map { char ->
        when {
            char.isLetterOrDigit() -> {
                if (shouldCapitalize) {
                    shouldCapitalize = false
                    char.uppercase()
                } else {
                    char.toString()
                }
            }
            else -> {
                shouldCapitalize = true
                ""
            }
        }
    }.joinToString("")
}