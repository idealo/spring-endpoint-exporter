package de.idealo.security.endpointexporter

import de.idealo.security.endpointexporter.ExporterProperties.ScanMode
import de.idealo.security.endpointexporter.classreading.FileSystemClassScanner
import de.idealo.security.endpointexporter.classreading.JarClassScanner
import de.idealo.security.endpointexporter.classreading.type.ClassMetadata
import de.idealo.security.endpointexporter.export.ExportService
import de.idealo.security.endpointexporter.processing.RequestMappingProcessor
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

fun main(args: Array<String>) {
    runApplication<SpringBootConsoleApplication>(*args)
}

@SpringBootApplication
@EnableConfigurationProperties(ExporterProperties::class)
@RegisterReflectionForBinding(ExporterProperties::class)
class SpringBootConsoleApplication(
    private val requestMappingProcessor: RequestMappingProcessor,
    private val exportService: ExportService,
    private val exporterProperties: ExporterProperties
) : ApplicationRunner {

    companion object : LoggingAware()

    override fun run(args: ApplicationArguments) {

        val scanner = when (exporterProperties.scanMode) {
            ScanMode.JAR -> JarClassScanner(
                includeFilters = exporterProperties.includeFilters,
                excludeFilters = exporterProperties.excludeFilters
            )

            ScanMode.FILE_SYSTEM -> FileSystemClassScanner(
                includeFilters = exporterProperties.includeFilters,
                excludeFilters = exporterProperties.excludeFilters
            )
        }

        val applicationData = scanner.scanApplicationData(exporterProperties.inputPath)

        val scanResult = scanner.scan(exporterProperties.inputPath)

        log.info("Processing ${scanResult.size} classes using ${scanner.javaClass.simpleName}...")

        val classToRequestMappings = scanResult.associateBy(
            ClassMetadata::name,
            requestMappingProcessor::process
        )

        log.info("Found ${classToRequestMappings.values.flatten().count()} RequestMappings.")

        exportService.writeAsOpenAPIDefinitions(applicationData, classToRequestMappings, exporterProperties.outputPath)
    }
}
