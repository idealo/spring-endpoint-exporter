package de.darkatra.classreading

import org.springframework.core.annotation.MergedAnnotation
import org.springframework.core.annotation.MergedAnnotations
import org.springframework.core.type.AnnotatedTypeMetadata

data class ParameterMetadata(
	val name: String,
	val access: Int,
	val type: String,
	private val annotations: MergedAnnotations
) : AnnotatedTypeMetadata {

	class Builder(
		var name: String? = null,
		var access: Int? = null,
		var type: String? = null,
		private val annotations: MutableList<MergedAnnotation<*>> = mutableListOf()
	) {

		fun addAnnotation(annotation: MergedAnnotation<*>) {
			annotations.add(annotation)
		}

		fun build(): ParameterMetadata {
			return ParameterMetadata(
				name = name!!,
				access = access!!,
				type = type!!,
				annotations = MergedAnnotations.of(annotations)
			)
		}
	}

	override fun getAnnotations(): MergedAnnotations = annotations
}
