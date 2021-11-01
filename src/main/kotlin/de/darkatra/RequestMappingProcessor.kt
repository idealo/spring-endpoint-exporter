package de.darkatra

import de.darkatra.classreading.type.AnnotationMetadata
import de.darkatra.classreading.type.ClassMetadata
import de.darkatra.classreading.type.MethodMetadata
import de.darkatra.classreading.type.ParameterMetadata
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.ValueConstants
import org.springframework.web.util.pattern.PathPatternParser
import org.springframework.web.bind.annotation.RequestMapping as RequestMappingAnnotation

@Service
class RequestMappingProcessor(
    private val patternParser: PathPatternParser = PathPatternParser()
) {

    fun process(classMetadata: ClassMetadata): List<RequestMapping> {

        val classLevelRequestMappings = getClassLevelRequestMappings(classMetadata)
        val methodLevelRequestMappings = getMethodLevelRequestMappings(classMetadata)

        return when {
            classLevelRequestMappings.isEmpty() -> methodLevelRequestMappings
            else -> methodLevelRequestMappings.flatMap { methodLevelRequestInformation ->
                classLevelRequestMappings.map { classLevelRequestInformation ->
                    classLevelRequestInformation.combine(methodLevelRequestInformation)
                }
            }
        }.map(RequestMapping::normalize)
    }

    private fun getClassLevelRequestMappings(classMetadata: ClassMetadata): List<RequestMapping> {
        val classRequestMapping = classMetadata.getAnnotation(RequestMappingAnnotation::class.qualifiedName!!) ?: return emptyList()
        return getRequestMapping(classRequestMapping, null)
    }

    private fun getMethodLevelRequestMappings(classMetadata: ClassMetadata): List<RequestMapping> {
        val methodsWithRequestMapping = classMetadata.getAnnotatedMethods(RequestMappingAnnotation::class.qualifiedName!!)
        return methodsWithRequestMapping.flatMap { methodMetadata ->
            val methodRequestMapping = methodMetadata.getAnnotation(RequestMappingAnnotation::class.qualifiedName!!)!!
            getRequestMapping(methodRequestMapping, methodMetadata)
        }
    }

    private fun getRequestMapping(annotationMetadata: AnnotationMetadata, methodMetadata: MethodMetadata?): List<RequestMapping> {

        val urlPatterns = annotationMetadata.getStringArray("path")
        val httpMethods = annotationMetadata.getStringArray("method")
        val consumes = annotationMetadata.getStringArray("consumes")
        val produces = annotationMetadata.getStringArray("produces")

        return urlPatterns.map { urlPattern ->
            RequestMapping(
                urlPattern = patternParser.parse(urlPattern),
                httpMethods = httpMethods.mapNotNull { HttpMethod.resolve(it) }.toSet(),
                responseStatus = methodMetadata?.getAnnotation(ResponseStatus::class.qualifiedName!!)
                    ?.getString("code")
                    ?.let { HttpStatus.valueOf(it) }
                    ?: HttpStatus.OK,
                requestParameters = getRequestParameters(methodMetadata),
                pathVariables = getPathVariables(methodMetadata),
                consumes = getMediaTypes(consumes),
                produces = getMediaTypes(produces)
            )
        }
    }

    private fun getRequestParameters(methodMetadata: MethodMetadata?): List<RequestMapping.RequestParameter> {

        if (methodMetadata == null) {
            return emptyList()
        }

        return methodMetadata.parameters
            .filter { it.isAnnotated(RequestParam::class.qualifiedName!!) }
            .map { parameterMetadata ->
                val parameterAnnotationAttributes = parameterMetadata.getAnnotation(RequestParam::class.qualifiedName!!)!!
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

    private fun getPathVariables(methodMetadata: MethodMetadata?): List<RequestMapping.PathVariable> {

        if (methodMetadata == null) {
            return emptyList()
        }

        return methodMetadata.parameters
            .filter { it.isAnnotated(PathVariable::class.qualifiedName!!) }
            .map { parameterMetadata ->
                val parameterAnnotationAttributes = parameterMetadata.getAnnotation(PathVariable::class.qualifiedName!!)!!

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

    private fun getParameterName(parameterMetadata: ParameterMetadata, annotationMetadata: AnnotationMetadata): String {
        return when (val nameFromAnnotation = annotationMetadata.getString("name")) {
            "" -> parameterMetadata.name
            else -> nameFromAnnotation
        }
    }
}
