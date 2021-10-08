package de.darkatra

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
		val resourcePattern = entrypoint.resolve("./**/*.class").normalize().pathString

		// get the resources
		return resourcePatternResolver.getResources(resourcePattern)
			// obtain the metadata reader
			.map { resource -> metadataReaderFactory.getMetadataReader(resource) }
			// apply all include and exclude filters
			.filter(this::isCandidateComponent)
	}
}
