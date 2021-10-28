package de.darkatra.classreading.internal

import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.core.type.classreading.MetadataReader
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory

class CustomMetadataReaderFactory(
	resourceLoader: ResourceLoader
) : SimpleMetadataReaderFactory(resourceLoader) {

	override fun getMetadataReader(resource: Resource): MetadataReader {
		return CustomMetadataReader(resource)
	}
}
