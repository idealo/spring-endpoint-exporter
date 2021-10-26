package de.darkatra.classreading

import org.springframework.core.io.FileSystemResourceLoader
import org.springframework.core.type.classreading.MetadataReader
import org.springframework.core.type.filter.TypeFilter
import java.nio.file.Path
import kotlin.io.path.pathString

class FileSystemClassScanner(
	includeFilters: List<TypeFilter> = emptyList(),
	excludeFilters: List<TypeFilter> = emptyList()
) : ClassScanner(
	includeFilters = includeFilters,
	excludeFilters = excludeFilters,
	resourceLoader = FileSystemResourceLoader()
) {

	/**
	 * @param entrypoint the directory to scan
	 */
	override fun scan(entrypoint: Path): List<MetadataReader> {

		// pattern to find all class files in a directory
		// note: Path.resolve with wildcards (aka. *) does not work on windows
		val resourcePattern = entrypoint.normalize().pathString + "/**/*.class"

		// get the resources
		return resourcePatternResolver.getResources(resourcePattern)
			// obtain the metadata reader
			.map { resource -> metadataReaderFactory.getMetadataReader(resource) }
			// apply all include and exclude filters
			.filter(this::isCandidate)
	}
}
