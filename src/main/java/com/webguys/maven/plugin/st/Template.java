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

import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * A template.
 */

public final class Template
{
  /**
   * The name of the template file to render.
   */

  @Parameter(required = true, name = "name")
  public String name;

  /**
   * The path to the input template file.
   */

  @Parameter(required = true, name = "inputFile")
  public File inputFile;

  /**
   * The path to the output file.
   */

  @Parameter(required = true, name = "outputFile")
  public File outputFile;

  /**
   * The template properties.
   */

  @Parameter(required = false, name = "properties")
  public Map<String, String> properties = new HashMap<>();

  /**
   * A template.
   */

  public Template()
  {

  }
}
