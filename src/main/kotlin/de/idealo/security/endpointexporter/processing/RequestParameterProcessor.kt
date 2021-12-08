package de.idealo.security.endpointexporter.processing

import de.idealo.security.endpointexporter.classreading.type.AnnotationMetadata
import de.idealo.security.endpointexporter.classreading.type.MethodMetadata
import de.idealo.security.endpointexporter.classreading.type.ParameterMetadata
import org.springframework.web.bind.annotation.RequestParam

class RequestParameterProcessor : MetadataProcessor<MethodMetadata, RequestMapping.RequestParameter> {

    override fun process(metadata: MethodMetadata): List<RequestMapping.RequestParameter> {

        return metadata.parameters
            .filter { it.isAnnotated(RequestParam::class.qualifiedName!!) }
            .map { parameterMetadata ->
                val parameterAnnotationAttributes = parameterMetadata.getAnnotation(RequestParam::class.qualifiedName!!)!!
                val defaultValue = parameterAnnotationAttributes.getString("defaultValue")
                val required = parameterAnnotationAttributes.getBoolean("required")

                RequestMapping.RequestParameter(
                    name = getParameterName(parameterMetadata, parameterAnnotationAttributes),
                    type = parameterMetadata.type,
                    required = required ?: (defaultValue == null),
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
