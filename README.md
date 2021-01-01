StringTemplate Maven Plugin
===

[![Maven Central](https://img.shields.io/maven-central/v/com.io7m.stmp/string-template-maven-plugin.svg?style=flat-square)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22string-template-maven-plugin%22)
[![Maven Central (snapshot)](https://img.shields.io/nexus/s/https/oss.sonatype.org/com.io7m.stmp/string-template-maven-plugin.svg?style=flat-square)](https://oss.sonatype.org/content/repositories/snapshots/com/io7m/stmp/string-template-maven-plugin/)

This plugin allows you to execute [StringTemplate](http://www.stringtemplate.org/) template files during your
build. The values for templates can come from static declarations or from a Java class specified to be executed.

For detailed instructions on how to use this plugin, please refer to the
[plugin site](http://kevinbirch.github.com/string-template-maven-plugin/).

This fork uses a different `groupId`:

```
<plugin>
  <groupId>com.io7m.stmp</groupId>
  <artifactId>string-template-maven-plugin</artifactId>
  <version>1.2.0</version>
</plugin>
```

| JVM             | Platform | Status |
|-----------------|----------|--------|
| OpenJDK LTS     | Linux    | [![Build (OpenJDK LTS, Linux)](https://img.shields.io/github/workflow/status/io7m/string-template-maven-plugin/main-openjdk_lts-linux)](https://github.com/io7m/string-template-maven-plugin/actions?query=workflow%3Amain-openjdk_lts-linux) |
| OpenJDK Current | Linux    | [![Build (OpenJDK Current, Linux)](https://img.shields.io/github/workflow/status/io7m/string-template-maven-plugin/main-openjdk_current-linux)](https://github.com/io7m/string-template-maven-plugin/actions?query=workflow%3Amain-openjdk_current-linux)
| OpenJDK Current | Windows  | [![Build (OpenJDK Current, Windows)](https://img.shields.io/github/workflow/status/io7m/string-template-maven-plugin/main-openjdk_current-windows)](https://github.com/io7m/string-template-maven-plugin/actions?query=workflow%3Amain-openjdk_current-windows)

## License

```
Copyright (c) 2011-2013 Kevin Birch <kmb@pobox.com>. All rights reserved.

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
of the Software, and to permit persons to whom the Software is furnished to do
so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
