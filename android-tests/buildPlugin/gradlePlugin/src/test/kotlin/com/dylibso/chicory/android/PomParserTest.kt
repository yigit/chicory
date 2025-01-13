package com.dylibso.chicory.android

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.File

class PomParserTest {
    @Test
    fun parsePom() {
        val parsedPom = PomParser.parse(RUNTIME_POM, parent = null)

        assertThat(
            parsedPom.dependencies
                .groupBy { it.scope }
                .mapValues { it.value.map { it.toGradleNotation() } }
        ).containsExactly(
            null, listOf(
                "com.dylibso.chicory:wasm",

            ),
            "test", listOf(
                "com.dylibso.chicory:wasm-corpus",
                "org.junit.jupiter:junit-jupiter-api",
                "org.junit.jupiter:junit-jupiter-engine"
            )
        )
        assertThat(parsedPom.properties).containsExactly(
            "project.artifactId", "runtime"
        )
    }

    @Test
    fun parseProjectPom() {
        val parsedPom = PomParser.parse(PROJECT_POM_STRIPPED, parent = null)
        assertThat(
            parsedPom.properties
        ).containsExactly(
            "maven.compiler.release", "11",
            "commons-io.version", "2.18.0"
        )
        assertThat(
            parsedPom.dependencyManagementDependencies.map { it.toGradleNotation() }
        ).containsExactly(
            "commons-io:commons-io:2.18.0"
        )
    }

    @Test
    fun parse() {
        val pom = PomParser.parse(
            File("/Users/yigit/src/chicory/pom.xml"),
            parent = null
        )
        assertThat(
            pom.dependencyManagementDependencies.map {
                it.toGradleNotation()
            }
        ).contains(
            "com.dylibso.chicory:wasm-corpus:999-SNAPSHOT"
        )
    }
}

internal val RUNTIME_POM = """
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <parent>
    <groupId>com.dylibso.chicory</groupId>
    <artifactId>chicory</artifactId>
    <version>999-SNAPSHOT</version>
    <relativePath>../pom.xml</relativePath>
  </parent>
  <artifactId>runtime</artifactId>
  <packaging>jar</packaging>
  <name>Chicory - Runtime</name>
  <description>Native JVM WebAssembly runtime</description>
  
  <dependencies>
    <dependency>
      <groupId>com.dylibso.chicory</groupId>
      <artifactId>wasm</artifactId>
    </dependency>
    <dependency>
      <groupId>com.dylibso.chicory</groupId>
      <artifactId>wasm-corpus</artifactId>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>org.junit.jupiter</groupId>
      <artifactId>junit-jupiter-api</artifactId>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>org.junit.jupiter</groupId>
      <artifactId>junit-jupiter-engine</artifactId>
      <scope>test</scope>
    </dependency>
  </dependencies>
</project>
""".trimIndent()

internal  val PROJECT_POM_STRIPPED = """
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <properties>
    <maven.compiler.release>11</maven.compiler.release>
    <commons-io.version>2.18.0</commons-io.version>
  </properties>
  <dependencyManagement>
      <dependencies>
          <dependency>
            <groupId>commons-io</groupId>
            <artifactId>commons-io</artifactId>
            <version>${'$'}{commons-io.version}</version>
          </dependency>
      </dependencies>
  </dependencyManagement>
  <profiles>
    <profile>
      <!-- Disable strict checks during development -->
      <id>dev</id>
      <activation>
        <property>
          <name>dev</name>
        </property>
      </activation>
      <properties>
        <spotless.check.skip>true</spotless.check.skip>
        <checkstyle.skip>true</checkstyle.skip>
        <enforcer.skip>true</enforcer.skip>
        <maven.compiler.failOnWarning>false</maven.compiler.failOnWarning>
        <maven.dependency.failOnWarning>false</maven.dependency.failOnWarning>
        <maven.javadoc.skip>true</maven.javadoc.skip>
      </properties>
    </profile>
  </profiles>
</project>

""".trimIndent()