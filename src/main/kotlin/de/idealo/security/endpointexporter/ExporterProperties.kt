package de.idealo.security.endpointexporter

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.validation.annotation.Validated
import java.nio.file.Path
import java.util.regex.Pattern
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

@Validated
@ConstructorBinding
@ConfigurationProperties("exporter")
class ExporterProperties {

    @field:NotNull
    lateinit var jarPath: Path

    @field:NotNull
    lateinit var outputPath: Path

    @field:NotEmpty
    lateinit var includeFilters: Set<@NotNull Pattern>

    lateinit var excludeFilters: Set<@NotNull Pattern>
}
