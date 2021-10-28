package de.darkatra

import de.darkatra.classreading.ParameterAwareMethodMetadata
import de.darkatra.classreading.ParameterMetadata
import org.springframework.core.annotation.AnnotationAttributes
import org.springframework.core.type.AnnotationMetadata
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.ValueConstants
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

		return getRequestMapping(classRequestMapping, null)
	}

	private fun getMethodLevelRequestMappings(annotationMetadata: AnnotationMetadata): List<RequestMapping> {

		val methodsWithRequestMapping = annotationMetadata.getAnnotatedMethods(RequestMappingAnnotation::class.qualifiedName!!)
		return methodsWithRequestMapping.flatMap { methodMetadata ->

			val methodRequestMapping = AnnotationAttributes.fromMap(
				// FIXME: spring's "getAnnotationAttributes" loads enum classes even with "classValuesAsString" set to true
				// https://gitter.im/spring-projects/spring-boot?at=617480fafb8ca0572bdc03ca
				methodMetadata.getAnnotationAttributes(RequestMappingAnnotation::class.qualifiedName!!, true)
			)!!

			getRequestMapping(methodRequestMapping, when (methodMetadata) {
				is ParameterAwareMethodMetadata -> methodMetadata
				else -> null
			})
		}
	}

	private fun getRequestMapping(annotationAttributes: AnnotationAttributes, methodMetadata: ParameterAwareMethodMetadata?): List<RequestMapping> {

		val urlPatterns = annotationAttributes.getStringArray("path")

		@Suppress("UNCHECKED_CAST")
		val httpMethods = annotationAttributes["method"] as Array<RequestMethod>? ?: emptyArray()

		val consumes = annotationAttributes.getStringArray("consumes")
		val produces = annotationAttributes.getStringArray("produces")

		return urlPatterns.map { urlPattern ->
			RequestMapping(
				urlPattern = patternParser.parse(urlPattern),
				httpMethods = httpMethods.mapNotNull { HttpMethod.resolve(it.name) }.toSet(),
				responseStatus = methodMetadata?.getAnnotationAttributes(ResponseStatus::class.qualifiedName!!)?.let {
					it["code"] as HttpStatus
				} ?: HttpStatus.OK,
				requestParameters = getRequestParameters(methodMetadata),
				pathVariables = getPathVariables(methodMetadata),
				consumes = getMediaTypes(consumes),
				produces = getMediaTypes(produces),
				declaringClassName = methodMetadata?.declaringClassName,
				methodName = methodMetadata?.methodName
			)
		}
	}

	private fun getRequestParameters(methodMetadata: ParameterAwareMethodMetadata?): List<RequestMapping.RequestParameter> {

		if (methodMetadata == null) {
			return emptyList()
		}

		return methodMetadata.getParameters()
			.filter { it.isAnnotated(RequestParam::class.qualifiedName!!) }
			.map { parameterMetadata ->
				val parameterAnnotationAttributes =
					AnnotationAttributes.fromMap(parameterMetadata.getAnnotationAttributes(RequestParam::class.qualifiedName!!))!!
				val defaultValue = parameterAnnotationAttributes.getString("defaultValue")

				RequestMapping.RequestParameter(
					name = getParameterName(parameterMetadata, parameterAnnotationAttributes),
					type = parameterMetadata.type,
					required = parameterAnnotationAttributes.getBoolean("required") || ValueConstants.DEFAULT_NONE == defaultValue,
					defaultValue = when (defaultValue) {
						ValueConstants.DEFAULT_NONE -> null
						else -> defaultValue
					}
				)
			}
	}

	private fun getPathVariables(methodMetadata: ParameterAwareMethodMetadata?): List<RequestMapping.PathVariable> {

		if (methodMetadata == null) {
			return emptyList()
		}

		return methodMetadata.getParameters()
			.filter { it.isAnnotated(PathVariable::class.qualifiedName!!) }
			.map { parameterMetadata ->
				val parameterAnnotationAttributes =
					AnnotationAttributes.fromMap(parameterMetadata.getAnnotationAttributes(PathVariable::class.qualifiedName!!))!!
				RequestMapping.PathVariable(
					name = getParameterName(parameterMetadata, parameterAnnotationAttributes),
					type = parameterMetadata.type,
					required = parameterAnnotationAttributes.getBoolean("required")
				)
			}
	}

	private fun getMediaTypes(consumes: Array<String>) = when {
		consumes.isEmpty() -> listOf(MediaType.ALL_VALUE)
		else -> consumes.asList()
	}

	private fun getParameterName(parameterMetadata: ParameterMetadata, parameterAnnotationAttributes: AnnotationAttributes): String {
		return when (val nameFromAnnotation = parameterAnnotationAttributes.getString("name")) {
			"" -> parameterMetadata.name
			else -> nameFromAnnotation
		}

	}
}
