/*
 * Copyright © 2011 Kevin Birch <kmb@pobox.com>. All rights reserved.
 */

/*
 * The MIT License
 *
 * Copyright (c) 2011 Kevin Birch <kmb@pobox.com>. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.webguys.maven.plugin.st;

import org.apache.maven.ProjectDependenciesResolver;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.sonatype.aether.util.artifact.JavaScopes;
import org.stringtemplate.v4.ST;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import static org.twdata.maven.mojoexecutor.MojoExecutor.ExecutionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.name;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
import static org.twdata.maven.mojoexecutor.MojoExecutor.version;

/**
 * The main controller.
 */

public final class Controller
{
  /**
   * The main controller.
   */

  public Controller()
  {

  }

  /**
   * The name of the class to instantiate.
   */

  @Parameter(required = true)
  private String className;

  /**
   * The name of the method to invoke.
   */

  @Parameter(required = true)
  private String method;

  /**
   * The static properties to be provided to the controller.
   */

  @Parameter
  private Map<String, String> properties;

  /**
   * Should the this controller attempt to be compiled?
   */

  @Parameter(defaultValue = "true")
  private boolean compile = true;

  @Parameter(defaultValue = "1.6")
  private String sourceVersion = "1.6";

  @Parameter(defaultValue = "1.6")
  private String targetVersion = "1.6";

  @Parameter(defaultValue = "3.0")
  private String compilerVersion = "3.0";

  private Object controllerInstance;

  /**
   * Execute the controller.
   *
   * @param st                   The string template
   * @param executionEnvironment The execution environment
   * @param dependenciesResolver The dependency resolver
   * @param log                  A logger
   *
   * @throws MojoExecutionException On errors
   */

  public void invoke(
    final ST st,
    final ExecutionEnvironment executionEnvironment,
    final ProjectDependenciesResolver dependenciesResolver,
    final Log log)
    throws MojoExecutionException
  {
    try {
      final var controllerClass = this.findControllerClass(
        dependenciesResolver,
        executionEnvironment,
        log);
      final var controllerMethod = this.getMethod(controllerClass);

      this.applyProperties(controllerClass, this.properties, log);
      final var results = this.invoke(controllerClass, controllerMethod, log);

      this.applyResults(st, results);
    } catch (final Exception e) {
      throw new MojoExecutionException(String.format(
        "Unable to invoke controller: %s (%s)",
        this.className,
        e.getMessage()), e);
    }
  }

  private Class<?> findControllerClass(
    final ProjectDependenciesResolver dependenciesResolver,
    final ExecutionEnvironment executionEnvironment,
    final Log log)
    throws
    MojoExecutionException,
    ClassNotFoundException,
    MalformedURLException,
    ArtifactResolutionException,
    ArtifactNotFoundException
  {
    try {
      return this.loadController(
        executionEnvironment.getMavenProject(),
        executionEnvironment.getMavenSession(),
        dependenciesResolver);
    } catch (final ClassNotFoundException e) {
      if (this.compile) {
        log.info(String.format(
          "Unable to find the class: %s.  Attempting to compile it...",
          this.className));
        return this.compileAndLoadController(
          log,
          dependenciesResolver,
          executionEnvironment);
      } else {
        throw new MojoExecutionException(String.format(
          "The class %s is not in the classpath, and compilation is not enabled.",
          this.className), e);
      }
    }
  }

  private Class<?> compileAndLoadController(
    final Log log,
    final ProjectDependenciesResolver dependenciesResolver,
    final ExecutionEnvironment executionEnvironment)
    throws
    MojoExecutionException,
    ClassNotFoundException,
    MalformedURLException,
    ArtifactResolutionException,
    ArtifactNotFoundException
  {
    final var project = executionEnvironment.getMavenProject();

    final var originalArtifacts = this.configureArtifacts(project);
    this.executeCompilerPlugin(executionEnvironment, log);
    final var result = this.loadController(
      project,
      executionEnvironment.getMavenSession(),
      dependenciesResolver);
    project.setArtifacts(originalArtifacts);
    return result;
  }

  private Set<Artifact> configureArtifacts(final MavenProject project)
  {
    final var originalArtifacts = project.getArtifacts();
    project.setArtifacts(project.getDependencyArtifacts());
    return originalArtifacts;
  }

  private void executeCompilerPlugin(
    final ExecutionEnvironment executionEnvironment,
    final Log log)
    throws MojoExecutionException
  {
    final var path = this.className.replace(".", File.separator) + ".java";
    log.info(String.format("Compiling %s...", path));

    executeMojo(
      plugin(
        groupId("org.apache.maven.plugins"),
        artifactId("maven-compiler-plugin"),
        version(this.compilerVersion)
      ),
      goal("compile"),
      configuration(
        element(name("source"), this.sourceVersion),
        element(name("target"), this.targetVersion),
        element(name("includes"), element("include", path))
      ),
      executionEnvironment
    );
  }

  private Class<?> loadController(
    final MavenProject project,
    final MavenSession session,
    final ProjectDependenciesResolver dependenciesResolver)
    throws
    MalformedURLException,
    ClassNotFoundException,
    ArtifactResolutionException,
    ArtifactNotFoundException
  {
    final var scopes = new ArrayList<String>(1);
    scopes.add(JavaScopes.RUNTIME);
    final var artifacts = dependenciesResolver.resolve(
      project,
      scopes,
      session);

    final var urls = new ArrayList<URL>();
    urls.add(new File(project.getBuild().getOutputDirectory()).getAbsoluteFile().toURI().toURL());
    for (final var artifact : artifacts) {
      urls.add(artifact.getFile().getAbsoluteFile().toURI().toURL());
    }

    final ClassLoader loader = URLClassLoader.newInstance(
      urls.toArray(new URL[urls.size()]),
      this.getClass().getClassLoader());
    return loader.loadClass(this.className);
  }

  private Method getMethod(final Class<?> controllerClass)
    throws NoSuchMethodException, MojoExecutionException
  {
    final var targetMethod = controllerClass.getMethod(this.method);

    if (!targetMethod.getReturnType().isAssignableFrom(Map.class)) {
      throw new MojoExecutionException(String.format(
        "The return type of the method %s was not of type Map<String, Object>",
        this.method));
    }

    return targetMethod;
  }

  private void applyProperties(
    final Class<?> controllerClass,
    final Map<String, String> appliedProperties,
    final Log log)
    throws
    IllegalAccessException,
    InvocationTargetException,
    InstantiationException
  {
    if (null == appliedProperties || appliedProperties.isEmpty()) {
      return;
    }

    Method setProperties = null;
    try {
      setProperties = controllerClass.getMethod("setProperties", Map.class);
    } catch (final NoSuchMethodException ignored) {
      // ignore
    }
    if (null != setProperties) {
      this.invoke(controllerClass, setProperties, log, appliedProperties);
    }
  }

  private Object invoke(
    final Class<?> controllerClass,
    final Method targetMethod,
    final Log log,
    final Object... args)
    throws
    InstantiationException,
    IllegalAccessException,
    InvocationTargetException
  {
    Object controller = null;
    if (!Modifier.isStatic(targetMethod.getModifiers())) {
      if (null == this.controllerInstance) {
        this.controllerInstance = controllerClass.newInstance();
      }
      controller = this.controllerInstance;
    }

    log.info(String.format(
      "Invoking controller method: %s.%s()",
      controllerClass.getName(),
      targetMethod.getName()));
    return targetMethod.invoke(controller, args);
  }

  private void applyResults(
    final ST st,
    final Object result)
    throws MojoExecutionException
  {
    if (null == result) {
      throw new MojoExecutionException(String.format(
        "The result invoking %s.%s was null.",
        this.className,
        this.method));
    }
    final var attributes = (Map<Object, Object>) result;

    for (final var entry : attributes.entrySet()) {
      final var key = entry.getKey();
      if (!(key instanceof String)) {
        final var msg = String.format(
          "A non-String key of type %s was found in the %s.%s results.",
          key.getClass().getName(),
          this.className,
          this.method);
        throw new MojoExecutionException(msg);
      }
      st.add((String) key, entry.getValue());
    }
  }
}
