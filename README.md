string-template-maven-plugin
===

[![Maven Central](https://img.shields.io/maven-central/v/com.io7m.stmp/string-template-maven-plugin.svg?style=flat-square)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22string-template-maven-plugin%22)
[![Maven Central (snapshot)](https://img.shields.io/nexus/s/com.io7m.stmp/string-template-maven-plugin?server=https%3A%2F%2Fs01.oss.sonatype.org&style=flat-square)](https://s01.oss.sonatype.org/content/repositories/snapshots/com/io7m/stmp/)
[![Codecov](https://img.shields.io/codecov/c/github/io7m-com/string-template-maven-plugin.svg?style=flat-square)](https://codecov.io/gh/io7m-com/string-template-maven-plugin)
![Java Version](https://img.shields.io/badge/21-java?label=java&color=e6c35c)

![string-template-maven-plugin](./src/site/resources/string-template-maven-plugin.jpg?raw=true)

| JVM | Platform | Status |
|-----|----------|--------|
| OpenJDK (Temurin) Current | Linux | [![Build (OpenJDK (Temurin) Current, Linux)](https://img.shields.io/github/actions/workflow/status/io7m-com/string-template-maven-plugin/main.linux.temurin.current.yml)](https://www.github.com/io7m-com/string-template-maven-plugin/actions?query=workflow%3Amain.linux.temurin.current)|
| OpenJDK (Temurin) LTS | Linux | [![Build (OpenJDK (Temurin) LTS, Linux)](https://img.shields.io/github/actions/workflow/status/io7m-com/string-template-maven-plugin/main.linux.temurin.lts.yml)](https://www.github.com/io7m-com/string-template-maven-plugin/actions?query=workflow%3Amain.linux.temurin.lts)|
| OpenJDK (Temurin) Current | Windows | [![Build (OpenJDK (Temurin) Current, Windows)](https://img.shields.io/github/actions/workflow/status/io7m-com/string-template-maven-plugin/main.windows.temurin.current.yml)](https://www.github.com/io7m-com/string-template-maven-plugin/actions?query=workflow%3Amain.windows.temurin.current)|
| OpenJDK (Temurin) LTS | Windows | [![Build (OpenJDK (Temurin) LTS, Windows)](https://img.shields.io/github/actions/workflow/status/io7m-com/string-template-maven-plugin/main.windows.temurin.lts.yml)](https://www.github.com/io7m-com/string-template-maven-plugin/actions?query=workflow%3Amain.windows.temurin.lts)|
## string-template-maven-plugin

The `string-template-maven-plugin` package provides a plugin to
execute StringTemplate templates during a build.

## Usage

An example of generating a `PAreasBDTest.java` file from a template named
`PAreasTest` in `src/main/string-template/PAreasTest.st`:

```
<build>
  <plugins>
...

<plugin>
  <groupId>com.io7m.stmp</groupId>
  <artifactId>string-template-maven-plugin</artifactId>
  <executions>
    <execution>
      <id>generate-area-P-BD</id>
      <phase>generate-sources</phase>
      <goals>
        <goal>renderTemplate</goal>
      </goals>
      <configuration>
        <template>
          <name>PAreasTest</name>
          <inputFile>
            ${project.basedir}/src/main/string-template/PAreasTest.st
          </inputFile>
          <outputFile>
            ${project.build.directory}/generated-sources/string-template/com/io7m/jregions/tests/core/parameterized/PAreasBDTest.java
          </outputFile>
          <properties>
            <scalarType>java.math.BigDecimal</scalarType>
            <scalarGeneratorType>Generator&lt;java.math.BigDecimal&gt;</scalarGeneratorType>
            <areaType>com.io7m.jregions.core.parameterized.areas.PAreaBD</areaType>
            <areaSizeType>com.io7m.jregions.core.parameterized.sizes.PAreaSizeBD</areaSizeType>
            <areaOpsType>com.io7m.jregions.core.parameterized.areas.PAreasBD</areaOpsType>
            <className>PAreasBDTest</className>
            <splitXType>com.io7m.jregions.core.parameterized.areas.PAreaXSplitBD</splitXType>
            <splitYType>com.io7m.jregions.core.parameterized.areas.PAreaYSplitBD</splitYType>
            <splitXYType>com.io7m.jregions.core.parameterized.areas.PAreaXYSplitBD</splitXYType>
            <opClass>PAreasBDTestOps</opClass>
          </properties>
        </template>
      </configuration>
    </execution>
  </executions>
</plugin>

...
  </plugins>
</build>
```

