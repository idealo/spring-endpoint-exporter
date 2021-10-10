package de.darkatra

import org.springframework.core.io.DefaultResourceLoader
import org.springframework.core.type.classreading.MetadataReader
import org.springframework.core.type.filter.TypeFilter
import java.nio.file.Path

class JarClassScanner(
	includeFilters: List<TypeFilter> = emptyList(),
	excludeFilters: List<TypeFilter> = emptyList()
) : ClassScanner(
	includeFilters = includeFilters,
	excludeFilters = excludeFilters,
	resourceLoader = DefaultResourceLoader()
) {

	/**
	 * @param entrypoint the jar file to scan
	 */
	override fun scan(entrypoint: Path): List<MetadataReader> {

		// pattern to find all class files in a jar file
		val resourcePattern = "jar:${entrypoint.normalize().toUri().toURL().toExternalForm()}!/**/*.class"

		// get the resources
		return resourcePatternResolver.getResources(resourcePattern)
			// obtain the metadata reader
			.map { resource -> metadataReaderFactory.getMetadataReader(resource) }
			// apply all include and exclude filters
			.filter(this::isCandidate)
	}
}
