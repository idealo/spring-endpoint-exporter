package de.idealo.security.endpointexporter.classreading

import de.idealo.security.endpointexporter.classreading.type.ClassMetadata
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import java.nio.file.Path
import java.util.regex.Pattern

abstract class ClassScanner(
    private val includeFilters: Set<Pattern>,
    private val excludeFilters: Set<Pattern>,
    resourceLoader: ResourceLoader
) {

    protected val resourcePatternResolver = PathMatchingResourcePatternResolver(resourceLoader)

    abstract fun scan(entrypoint: Path): List<ClassMetadata>

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
}
