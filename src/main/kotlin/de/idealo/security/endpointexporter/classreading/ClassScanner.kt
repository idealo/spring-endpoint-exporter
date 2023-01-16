package de.idealo.security.endpointexporter.classreading

import de.idealo.security.endpointexporter.classreading.type.ApplicationMetadata
import de.idealo.security.endpointexporter.classreading.type.ClassMetadata
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import java.nio.file.Path
import java.util.Properties
import java.util.regex.Pattern

abstract class ClassScanner(
    private val includeFilters: Set<Pattern>,
    private val excludeFilters: Set<Pattern>,
    resourceLoader: ResourceLoader
) {

    protected val resourcePatternResolver = PathMatchingResourcePatternResolver(resourceLoader)

    abstract fun scan(entrypoint: Path): List<ClassMetadata>

    abstract fun scanApplicationData(entrypoint: Path): ApplicationMetadata

    protected fun isCandidate(classMetadata: ClassMetadata): Boolean {

        // filter out all classes that match any exclude filters
        if (this.excludeFilters.any { it.matcher(classMetadata.name).matches() }) {
            return false
        }

        // only include classes that match any include filter
        if (this.includeFilters.any { it.matcher(classMetadata.name).matches() }) {
            return true
        }

        // if no filter matches, exclude per default
        return false
    }

    protected fun loadApplicationDataFromManifest(resourcePattern: String): ApplicationMetadata {

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
