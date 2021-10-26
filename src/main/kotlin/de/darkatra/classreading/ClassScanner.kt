package de.darkatra.classreading

import de.darkatra.classreading.internal.CustomMetadataReaderFactory
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.core.type.classreading.MetadataReader
import org.springframework.core.type.filter.TypeFilter
import java.nio.file.Path

abstract class ClassScanner(
	private val includeFilters: List<TypeFilter>,
	private val excludeFilters: List<TypeFilter>,
	resourceLoader: ResourceLoader
) {

	protected val resourcePatternResolver = PathMatchingResourcePatternResolver(resourceLoader)
	protected val metadataReaderFactory = CustomMetadataReaderFactory(resourceLoader)

	abstract fun scan(entrypoint: Path): List<MetadataReader>

	protected fun isCandidate(metadataReader: MetadataReader): Boolean {

		// filter out all classes that match any exclude filters
		if (this.excludeFilters.any { it.match(metadataReader, metadataReaderFactory) }) {
			return false
		}

		// only include classes that match any include filter
		if (this.includeFilters.any { it.match(metadataReader, metadataReaderFactory) }) {
			return true
		}

		// if no filter matches, exclude per default
		return false
	}
}
