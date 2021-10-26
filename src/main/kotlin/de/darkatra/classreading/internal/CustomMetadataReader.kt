package de.darkatra.classreading.internal

import org.springframework.asm.ClassReader
import org.springframework.core.NestedIOException
import org.springframework.core.io.Resource
import org.springframework.core.type.AnnotationMetadata
import org.springframework.core.type.ClassMetadata
import org.springframework.core.type.classreading.MetadataReader

/**
 * Facade for accessing class metadata, as read by an ASM [ClassReader].
 * Heavily inspired by [SimpleMetadataReader][org.springframework.core.type.classreading.SimpleMetadataReader].
 */
class CustomMetadataReader(
	private val resource: Resource
) : MetadataReader {

	private val annotationMetadata: AnnotationMetadata

	init {
		val visitor = CustomAnnotationMetadataReadingClassVisitor()
		getClassReader(resource).accept(visitor, ClassReader.SKIP_CODE or ClassReader.SKIP_FRAMES)
		annotationMetadata = visitor.getMetadata()
	}

	override fun getResource(): Resource = resource

	override fun getClassMetadata(): ClassMetadata = annotationMetadata

	override fun getAnnotationMetadata(): AnnotationMetadata = annotationMetadata

	private fun getClassReader(resource: Resource): ClassReader {
		resource.inputStream.use {
			return try {
				ClassReader(it)
			} catch (e: IllegalArgumentException) {
				throw NestedIOException("ASM ClassReader failed to parse class file - probably due to a new Java class file version that isn't supported yet: $resource", e)
			}
		}
	}
}
