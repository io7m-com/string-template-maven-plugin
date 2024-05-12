/*
 * Copyright © 2024 Mark Raynsford <code@io7m.com> https://www.io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */


package com.webguys.maven.plugin.st;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.stringtemplate.v4.AutoIndentWriter;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupDir;
import org.stringtemplate.v4.misc.ErrorBuffer;

import java.io.IOException;
import java.nio.file.Files;

/**
 * The main mojo.
 */

@Mojo(
  name = "renderTemplate",
  defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public final class StringTemplateMojo extends AbstractMojo
{
  /**
   * The template to render.
   */

  @Parameter(required = true, name = "template")
  private Template template;

  /**
   * The main mojo.
   */

  public StringTemplateMojo()
  {

  }

  @Override
  public void execute()
    throws MojoExecutionException
  {
    final var logger =
      this.getLog();
    final STGroup group =
      new STGroupDir(this.template.inputFile.getParent());

    final ErrorBuffer errorBuffer = new ErrorBuffer();
    group.setListener(errorBuffer);

    final ST st;
    try {
      st = group.getInstanceOf(this.template.name);
    } catch (final Exception e) {
      errorBuffer.errors.forEach(message -> {
        logger.error(this.template.inputFile + ": " + message.toString());
      });
      throw new MojoExecutionException(e);
    }

    if (null == st || !errorBuffer.errors.isEmpty()) {
      throw new MojoExecutionException(
        String.format("Unable to execute template. %n%s", errorBuffer)
      );
    }

    for (final var entry : this.template.properties.entrySet()) {
      st.add(
        entry.getKey().trim(),
        entry.getValue().trim()
      );
    }

    try {
      Files.createDirectories(
        this.template.outputFile.getParentFile()
          .toPath()
      );
    } catch (final IOException e) {
      throw new MojoExecutionException(e);
    }

    final ErrorBuffer errorListener = new ErrorBuffer();
    try (var fileWriter =
           Files.newBufferedWriter(this.template.outputFile.toPath())) {

      st.write(new AutoIndentWriter(fileWriter), errorListener);
      fileWriter.flush();
    } catch (final IOException e) {
      throw new MojoExecutionException(e);
    }

    if (!errorListener.errors.isEmpty()) {
      throw new MojoExecutionException(errorListener.toString());
    }
  }
}
