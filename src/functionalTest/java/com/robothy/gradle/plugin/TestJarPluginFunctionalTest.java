package com.robothy.gradle.plugin;

import java.io.File;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestJarPluginFunctionalTest {

  @DisplayName("Test package test classes.")
  @Test
  public void testJar() {
    String projectPath = "src/functionalTest/resources/test-jar-project";
    BuildResult result = GradleRunner.create()
        .withProjectDir(new File(projectPath))
        .withPluginClasspath()
        .withArguments("jar", "testJar")
        .build();

    Assertions.assertTrue(result.getOutput().contains("BUILD SUCCESSFUL"));
  }


}
