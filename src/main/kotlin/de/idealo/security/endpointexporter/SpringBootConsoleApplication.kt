package de.idealo.security.endpointexporter

import de.idealo.security.endpointexporter.classreading.JarClassScanner
import de.idealo.security.endpointexporter.classreading.type.ClassMetadata
import de.idealo.security.endpointexporter.export.ExportService
import de.idealo.security.endpointexporter.processing.RequestMappingProcessor
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

        val jarPath = (args.getOptionValues("jarPath") ?: emptyList()).firstOrNull()
            ?: throw IllegalArgumentException("Required parameter 'jarPath' is not set.")

        val packagesToInclude = args.getOptionValues("include")
            ?: throw IllegalArgumentException("Required parameter 'include' is not set.")

        val packagesToExclude: List<String> = args.getOptionValues("exclude")
            ?: emptyList()

        val scanner = JarClassScanner(
            includeFilters = packagesToInclude.map(Pattern::compile),
            excludeFilters = packagesToExclude.map(Pattern::compile)
        )
        val scanResult = scanner.scan(Path.of(jarPath))

        val classToRequestMappings = scanResult.associateBy(
            ClassMetadata::name,
            requestMappingProcessor::process
        )

        exportService.writeAsOpenAPIDefinitions(classToRequestMappings)
    }
}
