package de.idealo.security.endpointexporter

import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated
import java.nio.file.Path
import java.util.regex.Pattern

@Validated
@ConfigurationProperties("exporter")
class ExporterProperties(
    @field:NotNull
    val scanMode: ScanMode? = ScanMode.JAR,

    @field:NotNull
    val inputPath: Path? = null,

    @field:NotNull
    val outputPath: Path? = Path.of("./open-api.json"),

    @field:NotEmpty
    val includeFilters: Set<@NotNull Pattern> = emptySet(),

    val excludeFilters: Set<@NotNull Pattern> = emptySet()
) {

    enum class ScanMode {
        JAR,
        FILE_SYSTEM
    }
}
