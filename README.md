# Gradle test JAR Plugin

This Gradle plugin is able to package tests and run tests from JARs. You can find this
plugin in [Gradle Plugin Portal](https://plugins.gradle.org/plugin/com.robothy.test-jar).

## Usage

Applying this plugin will automatically package the classes and resources of the test source set. i.e.
The sources under `${projectDir}/src/test` directory. The packaged test JAR with the 'test' classifier
will be added to all declared Maven publications.

For example, there is a Java library called `lib-a`, whose version is `1.0`. The JAR is named `lib-a-1.0.jar`
while its test JAR is named `lib-a-1.0-test.jar`. If you want to add a such test JAR for testing,
the dependency declaration should specify the 'test' classifier.

```groovy
plugins {
    id "com.robothy.test-jar" version "2022.1"
}

dependencies {
    // Declares a test JAR with 'test' classifier. Will resolve lib-a-1.0-test.jar
    testJar 'com.robothy:lib-a:1.0:test'
    
    // Declares a test JAR without classifier. Will resolve lib-b-1.0.jar
    testJar 'com.robothy:lib-b:1.0'
}
```

You can execute `./gradlew testReport` to execute all test jars and the merged test report is in `${projectDir}/build/reports/tests/testReport`.
Different test JARs are running with isolated classpaths. Therefore, classes in different test JARs
won't affect each other at runtime.

You also could run test classes in a single JAR. For example, `./gradlew testComRobothyLibA`.
The plugin registers a test task in the 'verification' group for each test JAR. Execute `./gradlew test`
only running tests in `${proejctDir}/src/test`.