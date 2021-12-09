package de.idealo.security.endpointexporter.classreading

import de.idealo.security.endpointexporter.classreading.type.ApplicationMetadata
import de.idealo.security.endpointexporter.classreading.type.ClassMetadata
import org.springframework.core.io.DefaultResourceLoader
import java.nio.file.Path
import java.util.Properties
import java.util.regex.Pattern

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

    fun scanApplicationData(entrypoint: Path): ApplicationMetadata {

        val resourcePattern = "jar:${entrypoint.normalize().toUri().toURL().toExternalForm()}!/META-INF/MANIFEST.MF"
        val resource = resourcePatternResolver.getResource(resourcePattern)

        return when {
            resource.exists() -> {
                val properties = resource.inputStream.use { stream ->
                    Properties().also { it.load(stream) }
                }
                ApplicationMetadata(
                    title = properties.getProperty("Implementation-Title") ?: "n/a",
                    version = properties.getProperty("Implementation-Version") ?: "n/a",
                )
            }
            else -> ApplicationMetadata(
                title = "n/a",
                version = "n/a"
            )
        }
    }
}
