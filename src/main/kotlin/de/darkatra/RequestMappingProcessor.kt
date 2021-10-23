package de.darkatra

import org.springframework.core.annotation.AnnotationAttributes
import org.springframework.core.type.AnnotationMetadata
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.util.pattern.PathPatternParser
import org.springframework.web.bind.annotation.RequestMapping as RequestMappingAnnotation

@Service
class RequestMappingProcessor(
	private val patternParser: PathPatternParser = PathPatternParser()
) {

	fun process(annotationMetadata: AnnotationMetadata): List<RequestMapping> {

		val classLevelRequestMappings = getClassLevelRequestMappings(annotationMetadata)
		val methodLevelRequestMappings = getMethodLevelRequestMappings(annotationMetadata)

		return when {
			classLevelRequestMappings.isEmpty() -> methodLevelRequestMappings
			else -> methodLevelRequestMappings.flatMap { methodLevelRequestInformation ->
				classLevelRequestMappings.map { classLevelRequestInformation ->
					classLevelRequestInformation.combine(methodLevelRequestInformation)
				}
			}
		}.map(RequestMapping::normalize)
	}

	private fun getClassLevelRequestMappings(annotationMetadata: AnnotationMetadata): List<RequestMapping> {

		val classRequestMapping = AnnotationAttributes.fromMap(
			annotationMetadata.getAnnotationAttributes(RequestMappingAnnotation::class.qualifiedName!!, true)
		) ?: return emptyList()

		return getRequestMapping(classRequestMapping)
	}

	private fun getMethodLevelRequestMappings(annotationMetadata: AnnotationMetadata): List<RequestMapping> {

		val methodsWithRequestMapping = annotationMetadata.getAnnotatedMethods(RequestMappingAnnotation::class.qualifiedName!!)
		return methodsWithRequestMapping.flatMap { methodMetadata ->

			val methodRequestMapping = AnnotationAttributes.fromMap(
				// FIXME: spring's "getAnnotationAttributes" loads enum classes even with "classValuesAsString" set to true
				// https://gitter.im/spring-projects/spring-boot?at=617480fafb8ca0572bdc03ca
				methodMetadata.getAnnotationAttributes(RequestMappingAnnotation::class.qualifiedName!!, true)
			)!!

			getRequestMapping(methodRequestMapping)
		}
	}

	// TODO: parse @PathVariables and @RequestParameter annotations
	private fun getRequestMapping(annotationAttributes: AnnotationAttributes): List<RequestMapping> {

		val urlPatterns = annotationAttributes.getStringArray("path")

		@Suppress("UNCHECKED_CAST")
		val httpMethods = annotationAttributes["method"] as Array<RequestMethod>? ?: emptyArray()

		return urlPatterns.map { urlPattern ->
			RequestMapping(
				urlPattern = patternParser.parse(urlPattern),
				httpMethods = httpMethods.mapNotNull { HttpMethod.resolve(it.name) }.toSet()
			)
		}
	}
}
