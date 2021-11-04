package de.darkatra.processing

import de.darkatra.classreading.type.AnnotationMetadata
import de.darkatra.classreading.type.MethodMetadata
import de.darkatra.classreading.type.ParameterMetadata
import org.springframework.web.bind.annotation.RequestParam

class RequestParamProcessor : MetadataProcessor<MethodMetadata, RequestMapping.RequestParameter> {

    override fun process(metadata: MethodMetadata): List<RequestMapping.RequestParameter> {

        return metadata.parameters
            .filter { it.isAnnotated(RequestParam::class.qualifiedName!!) }
            .map { parameterMetadata ->
                val parameterAnnotationAttributes = parameterMetadata.getAnnotation(RequestParam::class.qualifiedName!!)!!
                val defaultValue = parameterAnnotationAttributes.getString("defaultValue")

                RequestMapping.RequestParameter(
                    name = getParameterName(parameterMetadata, parameterAnnotationAttributes),
                    type = parameterMetadata.type,
                    required = parameterAnnotationAttributes.getBoolean("required") ?: true || defaultValue == null,
                    defaultValue = defaultValue
                )
            }
    }

    private fun getParameterName(parameterMetadata: ParameterMetadata, annotationMetadata: AnnotationMetadata): String {
        return annotationMetadata.getString("name")
            ?: annotationMetadata.getString("value")
            ?: parameterMetadata.name
    }
}
