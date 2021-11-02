package de.darkatra

import de.darkatra.classreading.JarClassScanner
import de.darkatra.classreading.type.ClassMetadata
import de.darkatra.export.ExportService
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.nio.file.Path
import java.util.regex.Pattern

fun main(args: Array<String>) {
    runApplication<SpringBootConsoleApplication>(*args)
}

@SpringBootApplication
class SpringBootConsoleApplication(
    private val requestMappingProcessor: RequestMappingProcessor,
    private val exportService: ExportService
) : ApplicationRunner {

    override fun run(args: ApplicationArguments) {

        // eg. "./target/spring-security-demo-1.0.0.jar"
        val jarPath = (args.getOptionValues("jarPath") ?: emptyList()).firstOrNull()
            ?: throw IllegalArgumentException("Required parameter 'jarPath' is not set.")
        // eg. "de.darkatra.*"
        val basePackage = (args.getOptionValues("basePackage") ?: emptyList()).firstOrNull()
            ?: throw IllegalArgumentException("Required parameter 'basePackage' is not set.")

        val scanner = JarClassScanner(includeFilters = listOf(
            Pattern.compile(basePackage)
        ))
        val scanResult = scanner.scan(Path.of(jarPath))

        val classToRequestMappings = scanResult.associateBy(
            ClassMetadata::name,
            requestMappingProcessor::process
        )

        exportService.writeAsOpenAPIDefinitions(classToRequestMappings)
    }
}
