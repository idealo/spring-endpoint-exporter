package de.idealo.security.endpointexporter

import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated
import java.nio.file.Path
import java.util.regex.Pattern

@Validated
@ConfigurationProperties("exporter")
class ExporterProperties {

    @field:NotNull
    lateinit var scanMode: ScanMode

    @field:NotNull
    lateinit var inputPath: Path

    @field:NotNull
    lateinit var outputPath: Path

    @field:NotEmpty
    lateinit var includeFilters: Set<@NotNull Pattern>

    lateinit var excludeFilters: Set<@NotNull Pattern>

    enum class ScanMode {
        JAR,
        FILE_SYSTEM
    }
}
