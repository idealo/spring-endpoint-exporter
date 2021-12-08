package de.idealo.security.endpointexporter.classreading

import java.nio.file.Path
import java.util.Properties
import java.util.regex.Pattern
import de.idealo.security.endpointexporter.classreading.type.ApplicationMetadata
import de.idealo.security.endpointexporter.classreading.type.ClassMetadata
import org.springframework.core.io.DefaultResourceLoader


class JarClassScanner(
    includeFilters: List<Pattern> = emptyList(),
    excludeFilters: List<Pattern> = emptyList()
) : ClassScanner(
    includeFilters = includeFilters,
    excludeFilters = excludeFilters,
    resourceLoader = DefaultResourceLoader()
) {

    /**
     * @param entrypoint the jar file to scan
     */
    override fun scan(entrypoint: Path): List<ClassMetadata> {

        // pattern to find all class files in a jar file
        val resourcePattern = "jar:${entrypoint.normalize().toUri().toURL().toExternalForm()}!/**/*.class"

        // get the resources
        return resourcePatternResolver.getResources(resourcePattern)
            // obtain the class metadata
            .map { resource -> MetadataReader(resource).classMetadata }
            // apply all include and exclude filters
            .filter(this::isCandidate)
    }

    fun scanApplicationData(entrypoint: Path) =
        Properties().let { properties ->
            resourcePatternResolver.getResources("jar:${entrypoint.normalize().toUri().toURL().toExternalForm()}!/**/MANIFEST.MF")
                .map { resource ->
                    resource.inputStream.use { stream -> properties.load(stream) }
                }
                ApplicationMetadata(properties)
        }
}
