package de.idealo.security.endpointexporter

import de.idealo.security.endpointexporter.classreading.JarClassScanner
import de.idealo.security.endpointexporter.classreading.type.ClassMetadata
import de.idealo.security.endpointexporter.export.ExportService
import de.idealo.security.endpointexporter.processing.RequestMappingProcessor
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

// test
fun main(args: Array<String>) {
    runApplication<SpringBootConsoleApplication>(*args)
}

@SpringBootApplication
@EnableConfigurationProperties(ExporterProperties::class)
class SpringBootConsoleApplication(
    private val requestMappingProcessor: RequestMappingProcessor,
    private val exportService: ExportService,
    private val exporterProperties: ExporterProperties
) : ApplicationRunner {

    override fun run(args: ApplicationArguments) {

        val scanner = JarClassScanner(
            includeFilters = exporterProperties.includeFilters,
            excludeFilters = exporterProperties.excludeFilters
        )

        val applicationData = scanner.scanApplicationData(exporterProperties.jarPath)

        val scanResult = scanner.scan(exporterProperties.jarPath)
        val classToRequestMappings = scanResult.associateBy(
            ClassMetadata::name,
            requestMappingProcessor::process
        )

        exportService.writeAsOpenAPIDefinitions(applicationData, classToRequestMappings, exporterProperties.outputPath)
    }
}
