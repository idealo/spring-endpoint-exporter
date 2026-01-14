package de.idealo.security.endpointexporter

import de.idealo.security.endpointexporter.ExporterProperties.ScanMode
import jakarta.validation.Validation
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.file.Path
import java.util.regex.Pattern

internal class ExporterPropertiesTest {

    private val validator = Validation.buildDefaultValidatorFactory().validator

    @Test
    internal fun shouldBeValid() {

        val exporterProperties = getValidExportProperties()

        val constraintViolations = validator.validate(exporterProperties)

        assertThat(constraintViolations).isEmpty()
    }

    @Test
    internal fun shouldBeValidWhenExcludeFiltersArePresent() {

        val exporterProperties = getValidExportProperties(
            excludeFilters = setOf(
                Pattern.compile("org.springframework.*")
            )
        )

        val constraintViolations = validator.validate(exporterProperties)

        assertThat(constraintViolations).isEmpty()
    }

    @Test
    internal fun shouldBeInvalidWhenIncludeFiltersIsEmpty() {

        val exporterProperties = getValidExportProperties(
            includeFilters = emptySet()
        )

        val constraintViolations = validator.validate(exporterProperties)

        assertThat(constraintViolations).hasSize(1)
    }

    private fun getValidExportProperties(
        scanMode: ScanMode = ScanMode.JAR,
        inputPath: Path = Path.of("./app.jar"),
        outputPath: Path = Path.of("./out.json"),
        includeFilters: Set<Pattern> = setOf(Pattern.compile("de.idealo.*")),
        excludeFilters: Set<Pattern> = emptySet()
    ): ExporterProperties {
        return ExporterProperties(
            scanMode = scanMode,
            inputPath = inputPath,
            outputPath = outputPath,
            includeFilters = includeFilters,
            excludeFilters = excludeFilters
        )
    }
}
