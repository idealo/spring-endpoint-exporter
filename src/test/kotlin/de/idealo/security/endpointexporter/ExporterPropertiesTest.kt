package de.idealo.security.endpointexporter

import jakarta.validation.Validation
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.file.Path
import java.util.regex.Pattern

internal class ExporterPropertiesTest {

    private val validator = Validation.buildDefaultValidatorFactory().validator

    @Test
    internal fun shouldBeValid() {

        val exporterProperties = ExporterProperties().apply {
            jarPath = Path.of("./app.jar")
            outputPath = Path.of("out.json")
            includeFilters = setOf(
                Pattern.compile("de.idealo.*")
            )
        }

        val constraintViolations = validator.validate(exporterProperties)

        assertThat(constraintViolations).isEmpty()
    }

    @Test
    internal fun shouldBeValidWhenExcludeFiltersArePresent() {

        val exporterProperties = ExporterProperties().apply {
            jarPath = Path.of("./app.jar")
            outputPath = Path.of("out.json")
            includeFilters = setOf(
                Pattern.compile("de.idealo.*")
            )
            excludeFilters = setOf(
                Pattern.compile("org.springframework.*")
            )
        }

        val constraintViolations = validator.validate(exporterProperties)

        assertThat(constraintViolations).isEmpty()
    }

    @Test
    internal fun shouldBeInvalidWhenIncludeFiltersIsEmpty() {

        val exporterProperties = ExporterProperties().apply {
            jarPath = Path.of("./app.jar")
            outputPath = Path.of("out.json")
            includeFilters = emptySet()
        }

        val constraintViolations = validator.validate(exporterProperties)

        assertThat(constraintViolations).hasSize(1)
    }
}
