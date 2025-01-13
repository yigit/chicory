package com.dylibso.chicory.android

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register

@Suppress("UnstableApiUsage")
class BuildPlugin : Plugin<Settings> {
    override fun apply(target: Settings) {
        val mavenRepoDir = target.settingsDir.resolve("build/chicory_repo")
        target.dependencyResolutionManagement {
            it.repositories.maven {
                it.url = mavenRepoDir.toURI()
            }
        }
        target.gradle.rootProject { rootProject ->
            val buildRepoTask: TaskProvider<PrepareRepositoryTask> = rootProject.tasks.register(
                "prepareRepository",
                PrepareRepositoryTask::class
            ) {
                it.prebuiltRepositoryArg.set(
                    rootProject.providers.environmentVariable("CHICORY_REPO")
                )
                it.androidProjectDirectory.set(
                    target.rootDir
                )
                it.mainProjectDirectory.set(
                    rootProject.layout.projectDirectory.dir(MAIN_PROJECT_RELATIVE_DIR)
                )
                it.repositoryLocation.set(
                    mavenRepoDir
                )
            }

            val rootProjectPom = PomParser.parse(
                target.rootDir.resolve(MAIN_PROJECT_RELATIVE_DIR).resolve("pom.xml"),
                parent = null
            )

            rootProject.subprojects { project ->
                project.extensions.create("chicory", CopyTestsExtension::class.java,
                    project, target.rootDir.resolve(MAIN_PROJECT_RELATIVE_DIR), rootProjectPom)
                project.tasks.configureEach { task ->
                    // make sure local repository is built before running any tasks
                    task.dependsOn(buildRepoTask)
                }
            }
        }
    }

    companion object {
        private val MAIN_PROJECT_RELATIVE_DIR = "../."
    }
}