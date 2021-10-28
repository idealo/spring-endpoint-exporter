package de.darkatra.classreading.internal

import org.springframework.core.annotation.MergedAnnotations
import org.springframework.core.type.AnnotationMetadata
import org.springframework.core.type.MethodMetadata

class SimpleAnnotationMetadataFactory {

	companion object {

		private val clazz = Class.forName("org.springframework.core.type.classreading.SimpleAnnotationMetadata")

		fun get(
			className: String,
			access: Int,
			enclosingClassName: String?,
			superClassName: String?,
			independentInnerClass: Boolean,
			interfaceNames: Array<String>,
			memberClassNames: Array<String>,
			annotatedMethods: Array<MethodMetadata>,
			annotations: MergedAnnotations
		): AnnotationMetadata {

			return clazz.declaredConstructors.first()
				.also { it.trySetAccessible() }
				.newInstance(className,
					access,
					enclosingClassName,
					superClassName,
					independentInnerClass,
					interfaceNames,
					memberClassNames,
					annotatedMethods,
					annotations)
				as AnnotationMetadata
		}
	}
}
