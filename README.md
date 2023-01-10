[![Build](https://github.com/idealo/spring-endpoint-exporter/actions/workflows/build.yml/badge.svg)](https://github.com/idealo/spring-endpoint-exporter/actions/workflows/build.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=spring-endpoint-exporter&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=spring-endpoint-exporter)

# Spring Endpoint Exporter

Spring Endpoint Exporter aims to increase the value of your dynamic security scans by exporting all endpoints from
a [Spring Boot](https://github.com/spring-projects/spring-boot) application in [OpenAPI 3 format](https://swagger.io/docs/specification/about/), so that
scanners like [ZAP](https://github.com/zaproxy/zaproxy) can use this information to yield better results.

## How does it work?

First, it extracts metadata from class files using [ASM](https://asm.ow2.io/). It then processes the metadata and
applies [Spring Boot](https://github.com/spring-projects/spring-boot) specific rules. Finally, the collected information is converted
into [OpenAPI 3 format](https://swagger.io/docs/specification/about/) and written to a file. This file can now be used, for example
with [ZAP](https://github.com/zaproxy/zaproxy), to dynamically scan an application for security issues.

## Configuration Properties

| Property                   | Type           | Description                                                                                                                                                                                                                     | Default value       |
|----------------------------|----------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------|
| `exporter.scan-mode`       | `ScanMode`     | The mode the exporter will operate in. Either `JAR` or `FILE_SYSTEM`.<br/><br/> `JAR` mode expects input-path to point to a valid jar. `FILE_SYSTEM` expects input-path to point the a directory that contains `*.class` files. | `"JAR"`             |
| `exporter.input-path`      | `Path`         | The jar or directory with class files to scan and export all request mappings from.                                                                                                                                             | `null`              |
| `exporter.output-path`     | `Path`         | Where to output the result of the exporter.                                                                                                                                                                                     | `"./open-api.json"` |
| `exporter.include-filters` | `Set<Pattern>` | A set of packages to include when scanning for request mappings.                                                                                                                                                                | `null`              |
| `exporter.exclude-filters` | `Set<Pattern>` | A set of packages to exclude when scanning for request mappings.                                                                                                                                                                | `null`              |

You can pass properties to the application using environment variables or command line arguments. E.g.:

```
export EXPORTER_INPUT_PATH=/data/app.jar
java -jar ./spring-endpoint-exporter-1.0.0.jar
```

or

```
java -jar ./spring-endpoint-exporter-1.0.0.jar --exporter.input-path="/data/app.jar" --exporter.include-filters="de.idealo.*"
```

## Building from source

### Jar File

Simply run the following command:

```
mvn clean package
```

You can now run the application using:

```
java -jar ./target/spring-endpoint-exporter-1.0.0.jar
```

### Docker Image

Make sure your docker daemon is running and run the following command:

```
mvn clean spring-boot:build-image
```

The resulting image is named `ghcr.io/idealo/spring-endpoint-exporter:1.0.0`.

### Native Docker Image (Beta)

Make sure your docker daemon is running and run the following command:

```
mvn -Pnative clean spring-boot:build-image
```

The resulting image is named `ghcr.io/idealo/spring-endpoint-exporter:1.0.0-native`.

| :warning: WARNING                                                                                                                               |
|:------------------------------------------------------------------------------------------------------------------------------------------------|
| Native images are still in beta and are expected to have some bugs. For example, only the `FILE_SYSTEM` scan mode is supported as of right now. |

## Known limitations

Since this tool only accesses information from the bytecode, and thus does not load classes, it does not pick up custom `@RequestMapping` annotations, e.g.:

```java

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@RequestMapping(method = RequestMethod.GET)
public @interface CustomRequestMapping {

    @AliasFor(annotation = RequestMapping.class)
    String name() default "";

    @AliasFor(annotation = RequestMapping.class)
    String[] value() default {};

    @AliasFor(annotation = RequestMapping.class)
    String[] path() default {};

    @AliasFor(annotation = RequestMapping.class)
    String[] params() default {};

    @AliasFor(annotation = RequestMapping.class)
    String[] headers() default {};

    @AliasFor(annotation = RequestMapping.class)
    String[] consumes() default {};

    @AliasFor(annotation = RequestMapping.class)
    String[] produces() default {};
}
```

However, it correctly handles the builtin variants of `@RequestMapping`, e.g.: `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`
and `@PatchMapping`.

## License

This project is licensed under the Apache-2.0 License. See the [LICENSE](LICENSE) file for the full license text.
