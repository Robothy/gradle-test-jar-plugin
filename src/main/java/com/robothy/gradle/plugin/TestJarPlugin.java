package com.robothy.gradle.plugin;

import com.robothy.gradle.action.AssociateTestClasspathAction;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.logging.Logging;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.testing.Test;
import org.gradle.api.tasks.testing.TestReport;
import org.gradle.jvm.tasks.Jar;
import org.slf4j.Logger;

/**
 * Plugin to test classes from jars.
 */
public class TestJarPlugin implements Plugin<Project> {

  private static final Logger log = Logging.getLogger(TestJarPlugin.class);

  /**
   * Test classifier of an artifact.
   */
  public static final String TEST_CLASSIFIER = "test";

  /**
   * configuration testJar
   */
  public static final String TEST_JAR_CONFIGURATION_NAME = "testJar";

  private static final String VERIFICATION_GROUP_NAME = "verification";

  private static final String TEST_REPORT_DESTINATION_DIR = "build/reports/tests/testReport";

  private static final String TEST_JAR_TASK_NAME = "testJar";

  private static final String TEST_REPORT_TASK_NAME = "testReport";

  @Override
  public void apply(Project project) {
    if (Objects.isNull(project.getPlugins().findPlugin(JavaPlugin.class))) {
      throw new IllegalArgumentException("The plugin 'java' must be applied before plugin 'com.robothy.test-jar'.");
    }
    project.getConfigurations().create(TEST_JAR_CONFIGURATION_NAME);
    project.afterEvaluate(this::configureTestJar);
    project.afterEvaluate(this::registerTestTasks);
  }

  /**
   * Configure the test.
   */
  private void configureTestJar(Project project) {
    JavaPluginExtension javaPluginExtension = project.getExtensions().getByType(JavaPluginExtension.class);
    SourceSet testSourceSet = javaPluginExtension.getSourceSets().getByName("test");
    Jar testJarTask = project.getTasks().create(TEST_JAR_TASK_NAME, Jar.class);
    testJarTask.getArchiveClassifier().set(TEST_CLASSIFIER);
    testJarTask.doFirst(jar -> {
      testJarTask.from(testSourceSet.getResources().getSourceDirectories());
      testJarTask.from(testSourceSet.getOutput());
    });

    PublishArtifact testJarArtifact = project.getArtifacts().add("testJar", testJarTask);
    PublishingExtension extension = project.getExtensions().findByType(PublishingExtension.class);
    if (Objects.nonNull(extension)) {
      extension.getPublications().withType(MavenPublication.class)
          .configureEach(mavenPublication -> mavenPublication.artifact(testJarArtifact));
    }
  }

  private void registerTestTasks(Project project) {

    TestReport testReport = project.getTasks().create(TEST_REPORT_TASK_NAME, TestReport.class);
    testReport.setGroup(VERIFICATION_GROUP_NAME);
    Test test = (Test) project.getTasks().getByName("test");
    testReport.reportOn(test);
    testReport.setDestinationDir(project.file(TEST_REPORT_DESTINATION_DIR));

    Configuration testJar = project.getConfigurations().getByName(TEST_JAR_CONFIGURATION_NAME);
    for (Dependency dependency : testJar.getDependencies()) {
      // First create test tasks for each testJar dependency.
      String taskName = toTestTaskName(dependency.getGroup(), dependency.getName());
      Test testTask = project.getTasks().create(taskName, Test.class);
      testTask.setGroup(VERIFICATION_GROUP_NAME);
      testReport.reportOn(testTask);
      testTask.doFirst(new AssociateTestClasspathAction(dependency));
    }
  }

  private String toTestTaskName(String group, String name) {
    String result = "test";
    if (Objects.nonNull(group)) {
      result += upperCaseFirstChar(group);
    }

    return result + upperCaseFirstChar(name);
  }

  private String upperCaseFirstChar(String input) {
    String[] slices = input.split("\\.|\\-");
    return Stream.of(slices).map(s -> Character.toUpperCase(s.charAt(0)) + s.substring(1))
        .collect(Collectors.joining());
  }

}
