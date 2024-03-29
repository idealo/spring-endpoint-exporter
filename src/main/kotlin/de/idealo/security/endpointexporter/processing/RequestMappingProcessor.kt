package de.idealo.security.endpointexporter.processing

import de.idealo.security.endpointexporter.classreading.type.AnnotatedTypeMetadata
import de.idealo.security.endpointexporter.classreading.type.AnnotationMetadata
import de.idealo.security.endpointexporter.classreading.type.ClassMetadata
import de.idealo.security.endpointexporter.classreading.type.MethodMetadata
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.util.pattern.PathPatternParser
import org.springframework.web.bind.annotation.RequestMapping as RequestMappingAnnotation

@Service
class RequestMappingProcessor(
    private val requestParameterProcessor: RequestParameterProcessor,
    private val pathVariableProcessor: PathVariableProcessor,
    private val requestHeaderProcessor: RequestHeaderProcessor,
    private val responseStatusProcessor: ResponseStatusProcessor,
) : MetadataProcessor<ClassMetadata, List<RequestMapping>> {

    private val patternParser: PathPatternParser = PathPatternParser()
    private val requestMappingAnnotations = listOf(
        RequestMappingAnnotation::class.qualifiedName!!,
        GetMapping::class.qualifiedName!!,
        PostMapping::class.qualifiedName!!,
        PutMapping::class.qualifiedName!!,
        PatchMapping::class.qualifiedName!!,
        DeleteMapping::class.qualifiedName!!
    )

    override fun process(metadata: ClassMetadata): List<RequestMapping> {

        val classLevelRequestMappings = getClassLevelRequestMappings(metadata)
        val methodLevelRequestMappings = getMethodLevelRequestMappings(metadata)

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
        val classRequestMapping = getFirstRequestMappingAnnotation(classMetadata) ?: return emptyList()
        return getRequestMapping(classMetadata, classRequestMapping, null)
    }

    private fun getMethodLevelRequestMappings(classMetadata: ClassMetadata): List<RequestMapping> {
        val methodsWithRequestMapping = requestMappingAnnotations.flatMap { classMetadata.getAnnotatedMethods(it) }
        return methodsWithRequestMapping.flatMap { methodMetadata ->
            val methodRequestMapping = getFirstRequestMappingAnnotation(methodMetadata)!!
            getRequestMapping(classMetadata, methodRequestMapping, methodMetadata)
        }
    }

    private fun getRequestMapping(classMetadata: ClassMetadata, annotationMetadata: AnnotationMetadata, methodMetadata: MethodMetadata?): List<RequestMapping> {

        val urlPatterns = getUrlPatterns(annotationMetadata)
        val httpMethods = arrayOf(
            *annotationMetadata.getStringArray("method"),
            getDefaultHttpMethodForRequestMappingAnnotation(annotationMetadata)
        )
        val consumes = annotationMetadata.getStringArray("consumes")
        val produces = annotationMetadata.getStringArray("produces")

        return urlPatterns.map { urlPattern ->
            RequestMapping(
                urlPattern = patternParser.parse(urlPattern),
                httpMethods = httpMethods.filterNotNull().map { HttpMethod.valueOf(it) }.toSet(),
                responseStatus = methodMetadata?.let { responseStatusProcessor.process(it) } ?: HttpStatus.OK,
                requestParameters = methodMetadata?.let { requestParameterProcessor.process(it) } ?: emptyList(),
                pathVariables = methodMetadata?.let { pathVariableProcessor.process(it) } ?: emptyList(),
                requestHeaders = methodMetadata?.let { requestHeaderProcessor.process(it) } ?: emptyList(),
                consumes = consumes.asList(),
                produces = produces.asList(),
                declaringClassName = classMetadata.name,
                methodName = methodMetadata?.name
            )
        }
    }

    private fun getUrlPatterns(annotationMetadata: AnnotationMetadata): Array<String> {

        val urlPatterns = arrayOf(
            *annotationMetadata.getStringArray("path"),
            *annotationMetadata.getStringArray("value")
        )

        return when (urlPatterns.isEmpty()) {
            true -> arrayOf("/")
            false -> urlPatterns
        }
    }

    private fun getDefaultHttpMethodForRequestMappingAnnotation(annotationMetadata: AnnotationMetadata): String? {
        return when (annotationMetadata.name) {
            GetMapping::class.qualifiedName!! -> HttpMethod.GET.name()
            PostMapping::class.qualifiedName!! -> HttpMethod.POST.name()
            PutMapping::class.qualifiedName!! -> HttpMethod.PUT.name()
            PatchMapping::class.qualifiedName!! -> HttpMethod.PATCH.name()
            DeleteMapping::class.qualifiedName!! -> HttpMethod.DELETE.name()
            else -> null
        }
    }

    private fun getFirstRequestMappingAnnotation(annotatedTypeMetadata: AnnotatedTypeMetadata): AnnotationMetadata? {
        return annotatedTypeMetadata.getAnnotations()
            .firstOrNull { requestMappingAnnotations.contains(it.name) }
    }
}
