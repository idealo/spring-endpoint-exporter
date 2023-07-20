package de.idealo.security.endpointexporter.classreading

import de.idealo.security.endpointexporter.classreading.type.ApplicationMetadata
import de.idealo.security.endpointexporter.classreading.type.ClassMetadata
import org.springframework.core.io.DefaultResourceLoader
import java.nio.file.Path
import java.util.regex.Pattern

class JarClassScanner(
    includeFilters: Set<Pattern> = emptySet(),
    excludeFilters: Set<Pattern> = emptySet()
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

    override fun scanApplicationData(entrypoint: Path): ApplicationMetadata {
        return loadApplicationDataFromManifest("jar:${entrypoint.normalize().toUri().toURL().toExternalForm()}!/META-INF/MANIFEST.MF")
    }
}
