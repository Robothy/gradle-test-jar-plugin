package com.robothy.gradle.plugin;

import java.io.File;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestJarPluginWithJavaLibraryFunctionalTest {

  @DisplayName("Test test-jar with java-library project.")
  @Test
  public void testJar() {
    String projectPath = "src/functionalTest/resources/test-jar-with-java-library-project";
    BuildResult result = GradleRunner.create()
        .withProjectDir(new File(projectPath))
        .withPluginClasspath()
        .withArguments("clean", "jar", "testJar")
        .build();

    Assertions.assertTrue(result.getOutput().contains("BUILD SUCCESSFUL"));

    Assertions.assertTrue(new File(projectPath + "/build/libs/test-jar-with-java-library-project-1.0.jar").exists());
    Assertions.assertTrue(new File(projectPath + "/build/libs/test-jar-with-java-library-project-1.0-test.jar").exists());
  }


}
