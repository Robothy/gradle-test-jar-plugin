package com.robothy.gradle.action;

import static com.robothy.gradle.plugin.TestJarPlugin.TEST_CLASSIFIER;
import static com.robothy.gradle.plugin.TestJarPlugin.TEST_JAR_CONFIGURATION_NAME;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.ResolvedDependency;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.testing.Test;
import org.slf4j.Logger;

/**
 * An action that associate the test classpath with the test task.
 */
public class AssociateTestClasspathAction implements Action<Task> {

  private static final Logger log = Logging.getLogger(AssociateTestClasspathAction.class);

  private final Dependency dependency;

  /**
   * Constructor.
   *
   * @param dependency that resolve the jar with tests.
   */
  public AssociateTestClasspathAction(Dependency dependency) {
    this.dependency = dependency;
  }

  @Override
  public void execute(Task task) {
    Configuration testJar = task.getProject().getConfigurations().getByName(TEST_JAR_CONFIGURATION_NAME);
    Test testTask = (Test) task;
    Optional<ResolvedDependency> resolvedDependency = testJar.getResolvedConfiguration().getFirstLevelModuleDependencies().stream()
        .filter(it -> Objects.equals(it.getModuleGroup(), dependency.getGroup())
            && Objects.equals(it.getModuleName(), dependency.getName())).findFirst();

    if (resolvedDependency.isPresent()) {
      setTestClasses(resolvedDependency.get(), testTask);
    } else {
      log.warn("No tests found for task '{}'", task.getName());
    }
  }

  /**
   * Find test classes from {@code resolvedDependency} and set to the given {@code test}.
   *
   * @param resolvedDependency provides classes.
   * @param test test task.
   */
  private void setTestClasses(ResolvedDependency resolvedDependency, Test test) {
    Set<ResolvedArtifact> jarArtifacts = resolvedDependency.getModuleArtifacts().stream()
        .filter(it -> Objects.equals("jar", it.getType())).collect(Collectors.toSet());
    Project project = test.getProject();

    // Exclude tests under "src/test", these tests are executed via task named 'test'.
    //test.setClasspath(test.getClasspath().minus(test.getClasspath()));
    test.setTestClassesDirs(test.getTestClassesDirs().minus(test.getTestClassesDirs()));

    // Load Jars with TEST_CLASSIFIER.
    boolean withoutTestClassifierJar = true;
    for (ResolvedArtifact jarArtifact : jarArtifacts) {
      if (Objects.equals(TEST_CLASSIFIER, jarArtifact.getClassifier())) {
        withoutTestClassifierJar = false;
        String jarAbsPath = jarArtifact.getFile().getAbsolutePath();
        log.debug("Add {} to test classpath of task '{}'", jarAbsPath, test.getName());
        test.setClasspath(test.getClasspath().plus(project.files(jarAbsPath)));
        test.setTestClassesDirs(test.getTestClassesDirs().plus(project.zipTree(jarAbsPath)));
      }
    }

    // No jars with TEST_CLASSIFIER, load other Jars.
    if (withoutTestClassifierJar) {
      for (ResolvedArtifact jarArtifact : jarArtifacts) {
        String jarAbsPath = jarArtifact.getFile().getAbsolutePath();
        log.debug("Add {} to test classpath of task '{}'", jarAbsPath, test.getName());
        test.setClasspath(test.getClasspath().plus(project.files(jarAbsPath)));
        test.setTestClassesDirs(test.getTestClassesDirs().plus(project.zipTree(jarAbsPath)));
      }
    }

  }
}
