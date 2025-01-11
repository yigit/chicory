package com.dylibso.chicory.android

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.FileTree
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.File
import javax.inject.Inject

/**
 * Creates a maven repository from the original project.
 *
 * It can run in 2 ways:
 * * prebuiltRepositoryArg: This parameter points to an environment variable that denotes where
 *   the "already built" repository exists. If provided, the task will simply copy it.
 * * compile main project: If `prebuiltRepositoryArg` is not provided, this task will compile
 *   the main project to build the repository.
 */
@CacheableTask
abstract class PrepareRepositoryTask @Inject constructor(
    private val execOps: ExecOperations,
    private val filesystemOps: FileSystemOperations,
) : DefaultTask() {
    @get:Input
    @get:Optional
    abstract val prebuiltRepositoryArg: Property<String>
    @get:Internal
    abstract val mainProjectDirectory: DirectoryProperty
    @get:Internal
    abstract val androidProjectDirectory: DirectoryProperty

    /**
     * Since our project is in the same directory as the main
     * project, any changes would invalidate the mvn publish task.
     * To prevent this, we declare the [mainProjectDirectory] as
     * an internal input and instead explicitly declare what we
     * depend on in [relevantFiles] to prevent invalidations.
     */
    @Suppress("unused") // used by gradle for invalidation
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val relevantFiles: Provider<FileTree>
        get() = mainProjectDirectory.map {
            it.asFileTree.matching {
                // mvn builds do not seem repeatable so we exclude its outputs.
                // otherwise, just building them invalidates the task.
                it.exclude("**/target/**")
                // exclude our project
                it.exclude(androidProjectDirectory.get().asFile.relativeTo(
                    mainProjectDirectory.get().asFile
                ).path + "/**")
            }
        }
    @get:OutputDirectory
    abstract val repositoryLocation: DirectoryProperty
    @TaskAction
    fun prepareRepository() {
        repositoryLocation.get().asFile.let {
            it.deleteRecursively()
            it.mkdirs()
        }
        if(prebuiltRepositoryArg.isPresent) {
            val inputRepo = File(prebuiltRepositoryArg.get())
            check(inputRepo.exists()) {
                "Cannot find input repository in ${inputRepo.absolutePath}"
            }
            filesystemOps.copy {
                it.from(prebuiltRepositoryArg.get())
                it.into(repositoryLocation.get().asFile)
            }
        } else {
            execOps.exec {
                it.executable = "mvn"
                it.workingDir = mainProjectDirectory.get().asFile
                it.args("deploy",
                    "-DaltDeploymentRepository=local-repo::default::${repositoryLocation.get().asFile.toURI()}",
                    "-DskipTests",
                    "-Dspotless.skip=true",
                    "-DskipCheckStyle=true")
            }
        }
    }
}