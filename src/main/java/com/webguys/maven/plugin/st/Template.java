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
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.stringtemplate.v4.AutoIndentWriter;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.misc.ErrorBuffer;
import org.twdata.maven.mojoexecutor.MojoExecutor.ExecutionEnvironment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * A string template.
 */

public final class Template
{
  /**
   * A string template.
   */

  public Template()
  {

  }

  /**
   * The path to the template file's parent directory.
   */

  @Parameter(required = true)
  private File directory;

  /**
   * The name of the template file to render.
   */

  @Parameter(required = true)
  private String name;

  /**
   * The path to the output file.
   */

  @Parameter(required = true)
  private File target;

  /**
   * The class to invoke to provide data for the template.
   */

  @Parameter
  private Controller controller;

  /**
   * The static properties to be provided to the template.
   */

  @Parameter
  private Map<String, String> properties;

  /**
   * @return The base directory
   */

  public File getDirectory()
  {
    return this.directory;
  }

  /**
   * @return The template name
   */

  public String getName()
  {
    return this.name;
  }

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

  public void invokeController(
    final ST st,
    final ExecutionEnvironment executionEnvironment,
    final ProjectDependenciesResolver dependenciesResolver,
    final Log log)
    throws MojoExecutionException
  {
    if (null != this.controller) {
      this.controller.invoke(
        st,
        executionEnvironment,
        dependenciesResolver,
        log);
    }
  }

  /**
   * Install template properties.
   *
   * @param st The template
   */

  public void installProperties(final ST st)
  {
    if (null != this.properties) {
      for (final var entry : this.properties.entrySet()) {
        st.add(entry.getKey(), entry.getValue());
      }
    }
  }

  /**
   * Render the template.
   *
   * @param st      The template
   * @param project The project
   * @param log     A logger
   *
   * @throws MojoExecutionException On errors
   */

  public void render(
    final ST st,
    final MavenProject project,
    final Log log)
    throws MojoExecutionException
  {
    try {
      final var buildEncoding =
        project.getProperties().getProperty("project.build.sourceEncoding");

      if (buildEncoding == null) {
        throw new MojoExecutionException(
          "The property 'project.build.sourceEncoding' is required to be set");
      }

      final var outputFile = this.prepareOutputFile(project.getBasedir());
      this.prepareCompilerSourceRoot(outputFile, project, log);
      final var fileWriter =
        new FileWriter(outputFile, Charset.forName(buildEncoding));
      final var listener =
        new ErrorBuffer();
      st.write(new AutoIndentWriter(fileWriter), listener);
      fileWriter.flush();
      fileWriter.close();

      if (!listener.errors.isEmpty()) {
        throw new MojoExecutionException(listener.toString());
      }
    } catch (final IOException e) {
      throw new MojoExecutionException(String.format(
        "Unable to write output file: %s. (%s)",
        this.target.getAbsolutePath(),
        e.getMessage()), e);
    }
  }

  private File prepareOutputFile(final File baseDirectory)
    throws MojoExecutionException, IOException
  {
    var outputFile = this.target;
    if (!outputFile.isAbsolute()) {
      outputFile = new File(baseDirectory, outputFile.getPath());
    }

    if (!outputFile.exists()) {
      if (!outputFile.getParentFile().exists() && !outputFile.getParentFile().mkdirs()) {
        throw new MojoExecutionException(String.format(
          "Unable to fully create the output directory: %s",
          this.target.getParentFile()));
      }
      if (!outputFile.createNewFile()) {
        throw new MojoExecutionException(String.format(
          "Unable to create the output file: %s",
          this.target));
      }
    }

    return outputFile;
  }

  private void prepareCompilerSourceRoot(
    final File file,
    final MavenProject project,
    final Log log)
  {
    final var path = file.getPath();
    if (file.getName().endsWith("java") && path.contains("generated-sources")) {
      var index = path.indexOf("generated-sources") + 18;
      index = path.indexOf(File.separator, index);
      final var sourceRoot = path.substring(0, index);
      log.info("Adding compile source root: " + sourceRoot);
      project.addCompileSourceRoot(sourceRoot);
    }
  }
}
