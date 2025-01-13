This is a Android project designed to run Chicory tests on device.

Since Chicory uses maven and Android doesn't have official maven
support, we cannot make Android testing part of the main build.

Instead, we use this project where it uses the outputs of the
main maven build to run those tests as Android instrumentation tests.

Inside the `device-tests` folder, there is an Android library project
setup. It doesn't have any code, except for declaring dependencies
on Chicory runtime artifacts.

Inside the `buildPlugin/gradlePlugin`, there is a Gradle settings plugin
which provides the ability to compile the main project and set it as
a maven repository for all sub projects (in this case, device-tests).

It also provides a `chicory` extension to the sub projects, where they
can add tests from other projects.

```kotlin
chicory {
    addTests("runtime")
}
```
When `addTests` is called, the build plugin will create a new product
flavor to run the tests inside the `runtime` folder (of the main project).

Tests in this project can be run via:

```
// this will require a connected emulator
cd android-tests && ./gradlew device-tests:connectedCheck
```

This project includes a device test per maven project. To list them, you can run:

```
./gradlew device-tests:tasks --group verification|grep connected
xconnectedCheck - Runs all device checks on currently connected devices.
connectedRuntimeModuleDebugAndroidTest - Tests for the runtime maven module
connectedRuntimeTestsModuleDebugAndroidTest - Tests for the runtime-test maven module
```

The dependencies between this project and the maven project are setup properly
such that, if the code in the main maven project changes, this android project
will recompile the repository and run up-to-date tests.

To avoid re-building the main project (e.g. in CI), you can also pass
`CHICORY_REPO` environment variable, in which case, this android project will
re-use its output instead of recompile (but it won't make any attempt to
compile their tests either)

```
mvn deploy -DaltDeploymentRepository=local-repo::default::file:./local-repo -DskipTests
cd android-tests && CHICORY_REPO=../local-repo ./gradlew device-tests:connectedCheck
```
